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

import java.util.Map;

import org.apache.camel.BeanInject;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.dispatch.Dispatcher;

/**
 *
 * A Camel Component for <a href="http://projectreactor.io/">vert.x</a>
 *
 * @author matticala
 * @since 21-nov-2014
 * @version $$Revision$$
 * <p>
 * Last change: $$Date$$
 * Last changed by: $$Author$$
 */
public class ReactorComponent extends UriEndpointComponent {

    @BeanInject
    private Reactor reactor;

    public ReactorComponent() {
        this(ReactorEndpoint.class);
    }

    public ReactorComponent(Class<? extends Endpoint> endpointClass) {
        super(endpointClass);
    }

    public ReactorComponent(Reactor reactor) {
        super(ReactorEndpoint.class);
        this.reactor = reactor;
    }

    public ReactorComponent(Environment environment, Dispatcher dispatcher) {
        super(ReactorEndpoint.class);
        this.reactor = Reactors.reactor(environment, dispatcher);
    }

    public ReactorComponent(Environment environment, String dispatcher) {
        super(ReactorEndpoint.class);
        this.reactor = Reactors.reactor(environment, dispatcher);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters)
            throws Exception {

        ReactorEndpoint endpoint = new ReactorEndpoint(uri, this, remaining);
        setProperties(endpoint.getEndpointConfiguration(), parameters);
        return endpoint;
    }

    public Reactor getReactor() {
        return reactor;
    }

}
