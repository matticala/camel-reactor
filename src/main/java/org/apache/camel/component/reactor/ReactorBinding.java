package org.apache.camel.component.reactor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultExchangeHolder;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.Event;
import reactor.tuple.Tuple2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.component.reactor.ReactorConstants.*;

/**
 * A Strategy used to convert between a Camel {@link Exchange} and {@link ReactorMessage} to and
 * from a Reactor {@link reactor.event.Event}
 *
 * @author Matteo Calabrò
 */
public class ReactorBinding {

  private static final Logger LOG = LoggerFactory.getLogger(ReactorBinding.class);
  private final ReactorEndpoint endpoint;
  private final HeaderFilterStrategy filter;

  public ReactorBinding() {
    this.endpoint = null;
    filter = new ReactorHeaderFilterStrategy();
  }

  public ReactorBinding(ReactorEndpoint endpoint) {
    this.endpoint = endpoint;
    if (endpoint.getHeaderFilterStrategy() != null) {
      filter = endpoint.getHeaderFilterStrategy();
    } else {
      filter = new ReactorHeaderFilterStrategy();
    }
  }

  public Object getBodyFromEvent(Exchange exchange, Event<?> event) {
    try {
      // if we are configured to not map the jms message then return it as body
      if (endpoint != null && !endpoint.getConfiguration().isMapReactorEvent()) {
        LOG.trace("Option map Reactor event is false so using Event as body: {}", event);
        return event;
      }
      Object data = event.getData();
      if (data instanceof DefaultExchangeHolder) {
        DefaultExchangeHolder holder = (DefaultExchangeHolder) data;
        DefaultExchangeHolder.unmarshal(exchange, holder);
        return exchange.getIn().getBody();
      } else {
        return event.getData();
      }
    } catch (Exception e) {
      throw new RuntimeCamelException("Failed to extract body due to: " + e + ". Message: " + event,
        e);
    }
  }

  public Map<? extends String, ?> getHeadersFromEvent(Event<?> event, Exchange exchange) {
    Map<String, Object> map = new HashMap<>();
    if (event != null) {
      try {
        map.put("key", event.getKey());
        map.put("replyTo", event.getReplyTo());
      } catch (Exception e) {
        throw new RuntimeCamelException(e);
      }
      for (Tuple2<String, Object> tuple : event.getHeaders()) {
        String prop = tuple.getT1();
        Object value = tuple.getT2();
        if (filter.applyFilterToExternalHeaders(prop, value, exchange)) {
          continue;
        }
        map.put(prop, value);
      }
    }
    return map;
  }

  public Object getObjectProperty(Event<?> event, String name) {
    return event.getHeaders().get(name);
  }

  public Event<Exception> createReactorEvent(Exception cause) {
    return Event.wrap(cause);
  }

  public Event<?> createReactorEvent(Exchange exchange, Message message) {
    Event<?> ret = null;
    if (endpoint != null && endpoint.isTransferExchange()) {
      Serializable holder = DefaultExchangeHolder.marshal(exchange);
      ret = Event.wrap(holder);
    }

    boolean alwaysCopy = endpoint != null && endpoint.getConfiguration().isAlwaysCopyMessage();
    if (ret == null && message instanceof ReactorMessage
      && ((ReactorMessage) message).getEvent() != null) {
      ret = (alwaysCopy) ?
        ((ReactorMessage) message).getEvent().copy() :
        ((ReactorMessage) message).getEvent();
    }

    if (ret == null && message.getBody() != null) {
      ret = createReactorEvent(exchange, message.getBody(), message.getHeaders());
    }
    return ret;
  }

  public Event<?> createReactorEvent(Exchange exchange, Object body, Map<String, Object> headers) {
    if (endpoint != null && endpoint.isTransferExchange()) {
      Serializable holder = DefaultExchangeHolder.marshal(exchange);
      return Event.wrap(holder);
    }

    if (body != null) {
      Event<?> event = Event.wrap(body);
      if (headers != null && !headers.isEmpty()) {
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
          String key = entry.getKey();
          if (key.startsWith(HEADER_PREFIX)) {
            switch (key) {
              case KEY:
                event.setKey(entry.getValue());
                break;
              case REPLY_TO:
                event.setReplyTo(entry.getValue());
                break;
              default:
                event.getHeaders().set(key.substring(HEADER_PREFIX.length()), entry.getValue());
            }
          }
        }
      }
      return event;
    }
    return null;
  }


}
