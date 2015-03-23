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

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadRuntimeException;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.function.Consumer;

import java.util.concurrent.RejectedExecutionException;

/**
 * @author matticala
 */
public class ReactorProducer extends DefaultAsyncProducer {

  private static final Logger LOG = LoggerFactory.getLogger(ReactorProducer.class);

  public ReactorProducer(ReactorEndpoint endpoint) {
    super(endpoint);
  }

  @Override
  public ReactorEndpoint getEndpoint() {
    return (ReactorEndpoint) super.getEndpoint();
  }

  @Override
  public boolean process(Exchange exchange, AsyncCallback callback) {
    if (!isRunAllowed()) {
      if (exchange.getException() == null) {
        exchange.setException(new RejectedExecutionException());
      }
      // we cannot process so invoke callback
      callback.done(true);
      return true;
    }
    Reactor reactor = getEndpoint().getReactor();
    final Object key = getEndpoint().getSelectorObject();
    boolean reply = ExchangeHelper.isOutCapable(exchange);
    // Event<?> event = ReactorMessageHelper.getReactorEvent(exchange);
    Event<?> event =
        getEndpoint().getBinding().createReactorEvent(exchange,
            exchange.hasOut() ? exchange.getOut() : exchange.getIn());
    if (event != null) {
      if (reply) {
        LOG.debug("Sending to: {} the event: {}", key, event);
        reactor.sendAndReceive(key, event, new ReplyHandler(exchange, callback));
        return false;
      } else {
        LOG.debug("Sending to: {} the event: {}", key, event);
        reactor.notify(key, event);
        callback.done(true);
        return true;
      }

    }
    exchange.setException(new InvalidPayloadRuntimeException(exchange, String.class));
    callback.done(true);
    return true;
  }

  private static final class ReplyHandler implements Consumer<Event<?>> {

    private final Exchange exchange;
    private final AsyncCallback callback;

    public ReplyHandler(Exchange exchange, AsyncCallback callback) {
      this.exchange = exchange;
      this.callback = callback;
    }

    @Override
    public void accept(Event<?> event) {
      try {
        // preserve headers
        MessageHelper.copyHeaders(exchange.getIn(), exchange.getOut(), false);
        exchange.setOut(new ReactorMessage(event, null));
        // Map<String, Object> headers = out.getHeaders();
        // for (String s : headers.keySet()) {
        // if (s.startsWith("reactor.")) {
        // headers.remove(s);
        // }
        // }
        // ReactorMessageHelper.fillMessage(event, out);
      } finally {
        callback.done(false);
      }
    }
  }
}
