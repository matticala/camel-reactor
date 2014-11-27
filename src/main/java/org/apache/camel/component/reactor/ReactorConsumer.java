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

import java.util.ArrayList;
import java.util.concurrent.*;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.util.AsyncProcessorConverterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;

/**
 *
 * TODO:
 *
 * @author matticala
 * <p>
 * @since 21-nov-2014
 * @version $$Revision$$
 *
 * Last change: $$Date$$ Last changed by: $$Author$$
 */
public class ReactorConsumer extends DefaultConsumer implements Consumer<Event<?>> {

  private static final Logger LOG = LoggerFactory.getLogger(ReactorConsumer.class);

  private final ExecutorService pool = this.getEndpoint().getCamelContext().getExecutorServiceManager().newDefaultThreadPool(
      this, "ReactorInOutThreads");

  protected final Reactor reactor;

  protected final AsyncProcessor processor;

  private final ArrayList<Registration<?>> registrations = new ArrayList<>(5);

  public ReactorConsumer(ReactorEndpoint endpoint, Processor processor) {
    super(endpoint, processor);
    this.reactor = endpoint.getReactor();
    this.processor = AsyncProcessorConverterHelper.convert(processor);
  }

  @Override
  public ReactorEndpoint getEndpoint() {
    return (ReactorEndpoint) super.getEndpoint();
  }

  @Override
  protected void doStart()
      throws Exception {
    super.doStart();

    ReactorEndpoint endpoint = getEndpoint();

    if (endpoint.isMatchAll()) {
      registrations.add(reactor.on(Selectors.matchAll(), this));
    } else {
      if (endpoint.hasUri()) {
        registrations.add(reactor.on(Selectors.uri(endpoint.getUri()), this));
      }
      if (endpoint.hasType()) {
        registrations.add(reactor.on(Selectors.type(endpoint.getType()), this));
      }
      if (endpoint.hasRegex()) {
        registrations.add(reactor.on(Selectors.regex(endpoint.getRegex()), this));
      }

      boolean noOtherOption = !(endpoint.hasUri() && endpoint.hasType() && endpoint.hasRegex());

      if (endpoint.isObject() || noOtherOption) {
        registrations.add(reactor.on(Selectors.object(endpoint.getSelector()), this));
      }
    }
  }

  @Override
  protected void doStop()
      throws Exception {
    for (Registration r : registrations) {
      r.cancel();
    }
    super.doStop();
  }

  @Override
  public void accept(final Event<?> t) {
    final Exchange exchange = getEndpoint().createExchange();
    if (t.getReplyTo() != null) {
      exchange.setPattern(ExchangePattern.InOut);
      Future<Event<?>> future = pool.submit(new ReactorReplyConsumer(exchange));
      LOG.debug("##### Notifying response: " + future + " on selector " + t.getReplyTo().toString());
      reactor.notify(t.getReplyTo(), Event.wrap(future));
    }
    exchange.getIn().getHeaders().putAll(t.getHeaders().asMap());
    exchange.getIn().setBody(t);
    this.processor.process(exchange, EmptyAsyncCallback.DUMMY);
  }

  protected static class EmptyAsyncCallback implements AsyncCallback {

    protected static final EmptyAsyncCallback DUMMY = new EmptyAsyncCallback();

    @Override
    public void done(boolean doneSync) {
      //
    }

  }

  private static class ReactorReplyConsumer implements Callable<Event<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(ReactorReplyConsumer.class);

    private final Exchange exchange;

    ReactorReplyConsumer(Exchange exchange) {
      this.exchange = exchange;
    }

    @Override
    public Event<?> call()
        throws Exception {
      CountDownLatch timeout = new CountDownLatch(22);
      while (!exchange.hasOut() && !exchange.isFailed() && !timeout.await(1000, TimeUnit.MILLISECONDS)) {
        timeout.countDown();
      }
      LOG.debug(". Exchange: " + exchange);
      Object reply = exchange.getOut().getBody();
      Event<?> e;
      if (reply == null) {
        /*
         * FIXME: Out is filled with In almost immediately. Why?
         */
        LOG.debug("",exchange.getException());
        e = Event.wrap(exchange.getException());
      } else {
        if (reply instanceof Event) {
          e = (Event) reply;
        } else {
          e = Event.wrap(reply);
        }
      }
      e.getHeaders().setAll(exchange.getOut().getHeaders());
      e.setKey(exchange.getIn().getBody(Event.class).getId());
      return e;
    }
  }

}
