/*
 * Copyright 2014 Matteo Massimo Calabro'.
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

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import reactor.core.Reactor;
import reactor.event.Event;

/**
 *
 * @author matticala
 * @since 21-nov-2014
 * @version $$Revision$$
 *
 * Last change: $$Date$$ Last changed by: $$Author$$
 */
public class ReactorProducer extends DefaultProducer {

    private final Reactor reactor;

    public ReactorProducer(ReactorEndpoint endpoint) {
        super(endpoint);
        this.reactor = endpoint.getReactor();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Object payload = exchange.getIn().getBody();
        Event<?> event;

        if (payload instanceof Event) {
            event = (Event<?>) payload;
        } else {
            event = Event.wrap(payload);
        }

        ReactorEndpoint endpoint = getEndpoint();

        if (endpoint.hasUri()) {
            reactor.notify(endpoint.getUri(), event);
        }
        if (endpoint.hasType()) {
            reactor.notify(endpoint.getType(), event);
        }
        if (endpoint.hasRegex()) {
            reactor.notify(endpoint.getRegex(), event);
        }

        boolean noOtherOption = !(endpoint.hasUri() && endpoint.hasType() && endpoint.hasRegex());

        if (endpoint.isObject() || noOtherOption) {
            reactor.notify(endpoint.getSelector(), event);
        }
    }

    @Override
    public ReactorEndpoint getEndpoint() {
        return (ReactorEndpoint) super.getEndpoint();
    }

}
