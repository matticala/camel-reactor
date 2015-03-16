package org.apache.camel.component.reactor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.NoTypeConversionAvailableException;
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
        event.getData();
      }
    } catch (Exception e) {
      throw new RuntimeCamelException(
          "Failed to extract body due to: " + e + ". Message: " + event, e);
    }
    // Unreachable code.
    return null;
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

  public Event<?> createReactorEvent(Exchange exchange, Object body, Map<String, Object> headers,
      CamelContext context) {
    if (endpoint != null && endpoint.isTransferExchange()) {
      Serializable holder = DefaultExchangeHolder.marshal(exchange);
      return Event.wrap(holder);
    }

    if (body != null) {
      try {
        Serializable payload =
            context.getTypeConverter().mandatoryConvertTo(Serializable.class, exchange, body);
        Event<?> event = Event.wrap(payload);
        if (headers != null && !headers.isEmpty()) {
          event.getHeaders().setAll(headers);
        }
        return event;
      } catch (NoTypeConversionAvailableException e) {
        throw new RuntimeCamelException(e);
      }
    }
    return null;
  }


}
