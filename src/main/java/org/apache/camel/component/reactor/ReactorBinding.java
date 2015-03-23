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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.component.reactor.ReactorConstants.*;

/**
 * A Strategy used to convert between a Camel {@link Exchange} and {@link ReactorMessage} to and
 * from a Reactor {@link reactor.event.Event}
 *
 * @author matticala
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

    private static <T> Method findGetter(Class<T> klass, String name) {
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (Method method : klass.getDeclaredMethods()) {
            if (isGetter(method) && (
                    method.getName().matches("^get" + name) ||
                            method.getName().matches("^is" + name))
                    ) {
                return method;
            }
        }
        return null;
    }

    private static boolean isGetter(Method method) {
        String name = method.getName();
        Class<?> returnType = method.getReturnType();
        return Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0 &&
                (
                        (name.matches("^get[A-Z].*") && !returnType.equals(void.class)) ||
                    (name.matches("^is[A-Z].*") && !returnType.equals(boolean.class))
            );
    }

    private static boolean isSetter(Method method) {
        String name = method.getName();
        Class<?> returnType = method.getReturnType();
        return Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0 &&
            name.matches("^set[A-Z].*") && returnType.equals(void.class);
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
            throw new RuntimeCamelException(
                "Failed to extract body due to: " + e + ". Message: " + event, e);
        }
    }

    public Map<? extends String, ?> getHeadersFromEvent(Event<?> event, Exchange exchange) {
        Map<String, Object> map = new HashMap<>();
        if (event != null) {
            try {
                map.put(KEY, event.getKey());
                map.put(REPLY_TO, event.getReplyTo());
                map.put(EVENT_ID, event.getId());
            } catch (Exception e) {
                throw new RuntimeCamelException(e);
            }
            for (Tuple2<String, Object> tuple : event.getHeaders()) {
                String prop = tuple.getT1();
                Object value = tuple.getT2();
                map.put(prop, value);
            }
        }
        return map;
    }

    public Object getObjectProperty(Event<?> event, String name) {
        Method getter = findGetter(event.getClass(), name);
        Object value = null;
        try {
            value = getter.invoke(event);
        } catch (Exception e) {
            //noop
        }
        if (value == null) {
            value = event.getHeaders().get(name);
        }
        return value;
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
            ret =
                    (alwaysCopy) ?
                            ((ReactorMessage) message).getEvent().copy() :
                            ((ReactorMessage) message)
                                    .getEvent();
        }

        if (ret == null && message.getBody() != null) {
            ret = createReactorEvent(exchange, message.getBody(), message.getHeaders());
        }
        return ret;
    }

    public Event<?> createReactorEvent(Exchange exchange, Object body,
                                       Map<String, Object> headers) {
        if (endpoint != null && endpoint.isTransferExchange()) {
            Serializable holder = DefaultExchangeHolder.marshal(exchange);
            return Event.wrap(holder);
        }

        if (body != null) {
            Event<?> event = Event.wrap(body);
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    String key = entry.getKey();
                    if (filter.applyFilterToCamelHeaders(key, entry.getValue(), exchange)) {
                        continue;
                    }

                    if (key.equals(REPLY_TO)) {
                        event.setReplyTo(entry.getValue());
                    } else {
                        event.getHeaders().set(key, entry.getValue());
                    }
                }
            }
            return event;
        }
        return null;
    }
}
