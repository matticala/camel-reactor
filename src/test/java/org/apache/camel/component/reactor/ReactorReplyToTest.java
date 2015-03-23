/*
 * Copyright 2014 Matteo Massimo Calabro'
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.camel.component.reactor;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.Synchronization;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.Event;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author mmcalabro
 */
@RunWith(JUnit4.class)
public class ReactorReplyToTest extends ReactorBaseTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ReactorReplyToTest.class);

    private final CountDownLatch latch = new CountDownLatch(1);
    private final int MAX_WAIT = 10;

    protected String input = "reactor:uri:/out/activemq";
    protected String firstUri = "reactor:uri:/out/{destination}";
    protected String finalUri = "direct:test";

    protected String requestBody = "REQUEST";
    protected String requestRelpyTo = "/reply/here";
    protected String responseBody = "RESPONSE";
    protected Event<String> inputEvent = Event.wrap(requestBody, requestRelpyTo);

    @Override
    public CamelContext context() {
        return super.context();
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext ctx = super.createCamelContext();
        ctx.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                this.from(firstUri).log(LoggingLevel.DEBUG, logger, String.format(LOG_STRING, ++step))
                        .to(finalUri);
                this.from(finalUri).log(LoggingLevel.DEBUG, logger, String.format(LOG_STRING, ++step))
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                exchange.getOut().setBody(responseBody);
                                latch.countDown();
                            }
                        });
            }
        });
        return ctx;
    }

    @Test
    public void testInOutEvent() throws Exception {
        ReactorEndpoint in = context().getEndpoint(input, ReactorEndpoint.class);
        Producer prod = in.createProducer();
        prod.start();
        Exchange ex = in.createExchange(inputEvent);
        prod.process(ex);
        final AtomicBoolean complete = new AtomicBoolean(false);
        ex.addOnCompletion(new Synchronization() {

            @Override
            public void onComplete(Exchange exchange) {
                LOG.info("*** RECEIVED: {} ***", exchange.getOut());
                latch.countDown();
            }

            @Override
            public void onFailure(Exchange exchange) {
                latch.countDown();
            }
        });
        prod.process(ex);
        latch.await(MAX_WAIT, TimeUnit.SECONDS);
        assertTrue(ex.hasOut());
        assertEquals(responseBody, ex.getOut().getBody());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        stopCamelContext();
    }
}
