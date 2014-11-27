/*
 * Copyright 2014 Matteo Massimo Calabro'
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.reactor;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.camel.component.ActiveMQConfiguration;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static reactor.event.selector.Selectors.$;
import static reactor.event.selector.Selectors.U;

/**
 * Created by CalabroM on 27/11/2014.
 */
@RunWith(JUnit4.class)
public class InOutExchangeTest extends CamelTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(InOutExchangeTest.class);

    private static final String BROKER = "tcp://localhost:61616";

    private final Reactor reactor = Reactors.reactor(new Environment());

    private final CountDownLatch latch = new CountDownLatch(1);

    private final UUID uuid = UUID.randomUUID();

    private ActiveMQComponent setupBroker() {
        final ActiveMQConfiguration configuration = new ActiveMQConfiguration();
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(
            "vm:(broker:(" + BROKER + ")?persistent=false)?marshal=false");
        final PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(connectionFactory);
        pooledConnectionFactory.setMaxConnections(10);
        pooledConnectionFactory.setMaximumActiveSessionPerConnection(500);
        pooledConnectionFactory.start();
        configuration.setConnectionFactory(connectionFactory);
        configuration.setUsePooledConnection(true);
        configuration.setConcurrentConsumers(12);
        configuration.setTransacted(false);
        return new ActiveMQComponent(configuration);
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext ctx = super.createCamelContext();
        ctx.addComponent("reactor", new ReactorComponent(reactor));
//        ctx.addComponent("activemq", setupBroker());
        ctx.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                this.from("reactor:mybus?uri=/out/{destination}").to("vm:events");
                this.from("vm:events").process(new Processor() {
                    @Override public void process(Exchange exchange) throws Exception {
                        Random random = new Random();
                        LOG.info("##### JMS received " + exchange.getIn().getBody(Event.class));
//                        wait(Math.abs(random.nextInt(30000)));
//                        exchange.getOut().setBody("RESPONSE FOR " + exchange.getIn().getBody(Event.class).getId());
                    }
                });
            }
        });
        return ctx;
    }

    public void doPostSetup() {
        Registration r = reactor.on($(uuid), new Consumer<Event<?>>() {
            @Override public void accept(Event<?> event) {
                try {
                    LOG.info("##### Received " + ((Future<Event<?>>) event.getData()).get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        });
//        r.cancelAfterUse();
        LOG.info("##### {}", r);
    }



    @Test
    public void testInOutEvent() throws Exception  {
        Event e = Event.wrap("REQUEST");
        e.setReplyTo(uuid);

//        Registration r = reactor.on(Selectors.object(e.getReplyTo()), new Consumer<Event<?>>() {
//            @Override public void accept(Event<?> event) {
//                LOG.info("##### Received " + event);
//                latch.countDown();
//            }
//        });
//        r.cancelAfterUse();

        reactor.notify("/out/activemq", e);
        latch.await(30, TimeUnit.SECONDS);
    }
}
