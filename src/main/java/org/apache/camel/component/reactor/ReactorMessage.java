package org.apache.camel.component.reactor;

import org.apache.camel.RuntimeExchangeException;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.Event;

import java.util.Map;

/**
 * Created by Matteo on 13/03/2015.
 */
public class ReactorMessage extends DefaultMessage {

  private static final Logger LOG = LoggerFactory.getLogger(ReactorMessage.class);
  private Event<?> event;

  private ReactorBinding binding;

  public ReactorMessage(Event<?> event) {
    this.event = event;
    this.binding = null;
  }

  public ReactorMessage(Event<?> event, ReactorBinding binding) {
    this.event = event;
    this.binding = binding;
  }

  private ReactorEndpoint getEndpoint() {
    return (ReactorEndpoint) getExchange().getFromEndpoint();
  }

  public Event<?> getEvent() {
    return event;
  }

  public void setEvent(Event<?> event) {
    if (event != null) {
      setMessageId(event.getId().toString());
    }
    this.event = event;
  }

  public ReactorBinding getBinding() {
    if (binding == null) {
      binding = ExchangeHelper.getBinding(getExchange(), ReactorBinding.class);
    }
    return binding;
  }

  public void setBinding(ReactorBinding binding) {
    this.binding = binding;
  }

  @Override
  public void setBody(Object body) {
    super.setBody(body);
    if (body == null) {
      ensureInitialHeaders();
      event = null;
    }
  }

  @Override
  public Object getHeader(String name) {
    Object answer = null;
    if (event != null && !hasPopulatedHeaders()) {
      try {
        answer = getBinding().getObjectProperty(event, name);
      } catch (Exception e) {
        throw new RuntimeExchangeException("Unable to retrieve header from Reactor Message: "
            + name, getExchange(), e);
      }
    }
    if (answer == null && (hasPopulatedHeaders())) {
      answer = super.getHeader(name);
    }
    return answer;
  }

  @Override
  public String toString() {
    if (event != null) {
      try {
        return "ReactorMessage[ReactorMessageID: " + event.getId() + ", ReactorMessageBody: "
            + event.getData() + "]";
      } catch (Throwable e) {
        // ignore
      }
    }
    return "ReactorMessage@" + ObjectHelper.getIdentityHashCode(this);
  }

  @Override
  protected Object createBody() {
    if (event != null) {
      return getBinding().getBodyFromEvent(getExchange(), event);
    }
    return null;
  }

  @Override
  public Map<String, Object> getHeaders() {
    ensureInitialHeaders();
    return super.getHeaders();
  }

  @Override
  public void setHeaders(Map<String, Object> headers) {
    ensureInitialHeaders();
    super.setHeaders(headers);
  }

  @Override
  public Object removeHeader(String name) {
    ensureInitialHeaders();
    return super.removeHeader(name);
  }

  @Override
  public void setHeader(String name, Object value) {
    ensureInitialHeaders();
    super.setHeader(name, value);
  }

  @Override
  public ReactorMessage newInstance() {
    return new ReactorMessage(null, binding);
  }

  protected void ensureInitialHeaders() {
    if (event != null && !hasPopulatedHeaders()) {
      super.setHeaders(createHeaders());
    }
  }

  @Override
  protected void populateInitialHeaders(Map<String, Object> map) {
    if (event != null && map != null) {
      map.putAll(getBinding().getHeadersFromEvent(event, getExchange()));
    }
  }

  @Override
  protected String createMessageId() {
    if (event == null) {
      LOG.trace("No {} set so generating a new message id", Event.class.getName());
      return super.createMessageId();
    }
    return event.getId().toString();
  }
}
