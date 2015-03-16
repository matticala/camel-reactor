/*
 * Copyright 2015 Matteo Massimo Calabro'
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
import org.apache.camel.impl.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;
import reactor.function.Predicate;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author matticala
 *         <p/>
 * @version $$Revision$$
 *          <p/>
 *          Last change: $$Date$$ Last changed by: $$Author$$
 * @since 21-nov-2014
 */
public class ReactorConsumer extends DefaultConsumer implements Consumer<Event<?>> {

  private static final Logger LOG = LoggerFactory.getLogger(ReactorConsumer.class);

  private final ReactorEndpoint endpoint;

  private final ArrayList<Registration<?>> registrations = new ArrayList<>(5);

  public ReactorConsumer(ReactorEndpoint endpoint, Processor processor) {
    super(endpoint, processor);
    this.endpoint = endpoint;
  }

  @Override public ReactorEndpoint getEndpoint() {
    return (ReactorEndpoint) super.getEndpoint();
  }

  @Override @SuppressWarnings("unchecked") protected void doStart() throws Exception {
    super.doStart();

    Reactor reactor = getEndpoint().getReactor();
    switch (endpoint.getSelectorType()) {
      case type:
        if (!(endpoint.getSelectorObject() instanceof Class)) {
          throw new RuntimeCamelException("Endpoint selector object is not a " + Class.class);
        }
        registrations
          .add(reactor.on(Selectors.type((Class<?>) endpoint.getSelectorObject()), this));
        break;
      case uri:
        registrations.add(reactor.on(Selectors.uri((String) endpoint.getSelectorObject()), this));
        break;
      case regex:
        registrations.add(reactor.on(Selectors.regex((String) endpoint.getSelectorObject()), this));
        break;
      case predicate:
        if (!(endpoint.getSelectorObject() instanceof reactor.function.Predicate)) {
          throw new RuntimeCamelException("Endpoint selector object is not a " + Predicate.class);
        }
        registrations.add(reactor.on(
          Selectors.predicate((reactor.function.Predicate<Object>) endpoint.getSelectorObject()),
          this));
        break;
      case set:
        if (!(endpoint.getSelectorObject() instanceof Set)) {
          throw new RuntimeCamelException("Endpoint selector object is not a " + Set.class);
        }
        registrations
          .add(reactor.on(Selectors.setMembership((Set) endpoint.getSelectorObject()), this));
        break;
      default:
        registrations.add(reactor.on(Selectors.object(endpoint.getSelectorObject()), this));
    }
  }

  @Override protected void doStop() throws Exception {
    for (Registration r : registrations) {
      r.cancel();
    }
    super.doStop();
  }

  @Override public void accept(final Event<?> event) {
    final boolean inOut = event.getReplyTo() != null;

    final Exchange exchange = endpoint.createExchange(event);
    exchange.setPattern(inOut ? ExchangePattern.InOut : ExchangePattern.InOnly);
//    Message in = exchange.getIn();
//    ReactorMessageHelper.fillMessage(event, in);
    try {
      getAsyncProcessor().process(exchange, new AsyncCallback() {
        @Override public void done(boolean done) {
          if (inOut) {
            Reactor reactor = getEndpoint().getReactor();
            Event<?> response = getEndpoint().getBinding().createReactorEvent(exchange,
              exchange.hasOut() ? exchange.getOut() : exchange.getIn());
            reactor.notify(event.getReplyTo(), response);
            LOG.debug("Sent reply to: {} with body: {}", event.getReplyTo(), response);
          }
        }
      });
    } catch (Exception e) {
      getExceptionHandler()
        .handleException("Error processing Reactor event: " + event, exchange, e);
    }
  }
}
