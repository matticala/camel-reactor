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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author CalabroM
 * @version $$Revision$$
 *          <p/>
 *          Last change: $$Date$$ Last changed by: $$Author$$
 * @since 26-nov-2014
 */
@RunWith(JUnit4.class)
public class ReactorRouteTest extends ReactorBaseTestSupport {

    protected String input = "vm:input";
    protected String firstUriProd = "reactor:uri:/input/test";
    protected String firstUriCons = "reactor:uri:/input/{destination}";
    protected String secondUri = "reactor:type:" + String.class;
    protected String output = "vm:output";

    protected String body = "PUPPA!";


  @Override
  protected CamelContext createCamelContext() throws Exception {
    CamelContext ctx = super.createCamelContext();
    ctx.addRoutes(new RouteBuilder() {

      @Override
      public void configure() throws Exception {
          this.from(input)
                  .log(LoggingLevel.DEBUG, logger, String.format(LOG_STRING, ++step))
                  .to(firstUriProd);
          this.from(firstUriCons)
                  .log(LoggingLevel.DEBUG, logger, String.format(LOG_STRING, ++step))
                  .to(secondUri);
          this.from(secondUri)
                  .log(LoggingLevel.DEBUG, logger, String.format(LOG_STRING, ++step))
                  .to(output);
      }
    });
    return ctx;
  }

  @Test
  public void test() throws Exception {
      Endpoint in = context().getEndpoint(input);
      Exchange exchange = in.createExchange(ExchangePattern.InOnly);
      exchange.getIn().setBody(body);

      Producer p = in.createProducer();
    p.start();
    p.process(exchange);
    p.stop();

      Endpoint out = context().getEndpoint(output);
      PollingConsumer c = out.createPollingConsumer();
    Exchange received = c.receive();
      log.info("***** Received {} from {}", received, out);

    assertTrue(received != null);
      assertEquals(body, received.getIn().getBody());
  }

}
