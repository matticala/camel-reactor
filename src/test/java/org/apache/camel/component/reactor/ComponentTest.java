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

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Producer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;

/**
 *
 * @author CalabroM
 * @since 26-nov-2014
 * @version $$Revision$$
 *
 * Last change: $$Date$$
 * Last changed by: $$Author$$
 */
@RunWith(JUnit4.class)
public class ComponentTest extends CamelTestSupport {

    private final Reactor reactor = Reactors.reactor(new Environment());

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext ctx = super.createCamelContext();
        ctx.addComponent("reactor", new ReactorComponent(reactor));
        ctx.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                this.from("vm:input").to("reactor:mybus?uri=/input/test");

                this.from("reactor:mybus?matchAll=true").log("###### LOGGER ${body} ######");

                this.from("reactor:mybus?uri=/input/{destination}").to("vm:output");
            }
        });
        return ctx;
    }

    @Test
    public void test() throws Exception {
        Endpoint e = context.getEndpoint("vm:input");

        Exchange exchange = e.createExchange(ExchangePattern.InOnly);
        exchange.getIn().setBody("PUPPA!");

        Producer p = e.createProducer();
        p.start();
        p.process(exchange);
        p.stop();

        Endpoint e1 = context.getEndpoint("vm:output");
        PollingConsumer c = e1.createPollingConsumer();
        Exchange received = c.receive();
        log.info("***** Received {} from {}", received, e1);

        reactor.notify("/input/myself", received.getIn().getBody(Event.class));
        received = c.receive();
        log.info("***** Received again {} from {}", received, e1);
        assertTrue(received != null);
    }

}
