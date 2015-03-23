package org.apache.camel.component.reactor;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.camel.component.ActiveMQConfiguration;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import reactor.event.Event;

/**
 * @author mmcalabro
 */
@RunWith(JUnit4.class)
public class ReactorToJmsToFileRouteTest extends ReactorBaseTestSupport {

    @Override
    public boolean isUseRouteBuilder() {
        return true;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                this.from("reactor:uri:/{entity}/{subsystem}/{app}")
                        .log(LoggingLevel.DEBUG, logger, String.format(LOG_STRING, ++step))
                        .to("activemq:queue:X.Y.Z");
                this.from("activemq:queue:X.Y.Z")
                        .log(LoggingLevel.DEBUG, logger, String.format(LOG_STRING, ++step))
                        .to("seda:SEDANINI");
                this.from("seda:SEDANINI")
                        .log(LoggingLevel.DEBUG, logger, String.format(LOG_STRING, ++step))
                        .to("reactor:uri:/received");
            }
        };
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        System.setProperty("javax.jms.broker.host", "localhost");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL("vm:(broker:(tcp://localhost:61616)?persistent=false&useJmx=true)?marshal=false");
        final PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(connectionFactory);

        pooledConnectionFactory.setMaxConnections(10);
        pooledConnectionFactory.setMaximumActiveSessionPerConnection(500);
        pooledConnectionFactory.start();
        final ActiveMQConfiguration configuration = new ActiveMQConfiguration();
        configuration.setConnectionFactory(pooledConnectionFactory);
        configuration.setUsePooledConnection(true);
        configuration.setConcurrentConsumers(12);
        configuration.setTransacted(false);
        camelContext = new DefaultCamelContext();
        camelContext.addComponent("activemq", new ActiveMQComponent(configuration));
        return camelContext;
    }

    @Test
    public void multiComponentRoutingTest() throws Exception {
        CamelContext ctx = context();
        ReactorEndpoint s = ctx.getEndpoint("reactor:uri:/M/M/test", ReactorEndpoint.class);
        Exchange tx = s.createExchange(ExchangePattern.InOnly);
        tx.setIn(new ReactorMessage(Event.wrap("FOO.BAR")));
        s.createExchange(Event.wrap("FOO.BAR"));
        Producer p = s.createProducer();
        p.start();
        p.process(tx);

        Endpoint d = ctx.getEndpoint("reactor:uri:/received");
        PollingConsumer pc = d.createPollingConsumer();
        pc.start();
        Exchange rx = pc.receive();
        Assert.assertEquals("FOO.BAR", rx.getIn().getBody());
        Assert.assertEquals("M", rx.getIn().getHeader("entity"));
    }
}
