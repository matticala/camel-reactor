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

import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;

/**
 *
 * @author matticala
 * @since 21-nov-2014
 * @version $$Revision$$
 * <p>
 * Last change: $$Date$$
 * Last changed by: $$Author$$
 */
public class ReactorComponent extends UriEndpointComponent {

    private Reactor reactor;

    private ReactorConfiguration configuration;

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

    public ReactorComponent(ReactorConfiguration configuration) {
        super(ReactorEndpoint.class);
        this.configuration = configuration;
    }

    public ReactorComponent(CamelContext context, Class<? extends Endpoint> endpointClass) {
        super(context, endpointClass);
    }

    public ReactorComponent(Reactor reactor, ReactorConfiguration configuration, CamelContext context, Class<? extends Endpoint> endpointClass) {
        super(context, endpointClass);
        this.reactor = reactor;
        this.configuration = configuration;
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters)
            throws Exception {

        ReactorConfiguration config;
        if (configuration != null) {
            config = configuration.copy();
        } else {
            config = new ReactorConfiguration();
        }
        config.validateConfiguration();

        if (reactor == null) {
            reactor = Reactors.reactor(config.getEnvironment(), config.getDispatcher());
        }

        ReactorEndpoint endpoint = new ReactorEndpoint(uri, remaining, this);
        setProperties(endpoint.getEndpointConfiguration(), parameters);
        return endpoint;
    }

    public Reactor getReactor() {
        return reactor;
    }

    public void setReactor(Reactor reactor) {
        this.reactor = reactor;
    }

    public ReactorConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ReactorConfiguration configuration) {
        this.configuration = configuration;
    }

}
