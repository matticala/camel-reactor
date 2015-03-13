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

import org.apache.camel.BeanInject;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.HeaderFilterStrategyComponent;
import org.apache.camel.spi.HeaderFilterStrategy;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.dispatch.Dispatcher;

import java.util.Map;

/**
 * 
 * A Camel Component for <a href="http://projectreactor.io/">Reactor</a>
 * 
 * @author matticala
 * @since 21-nov-2014
 * @version $$Revision$$
 *          <p>
 *          Last change: $$Date$$ Last changed by: $$Author$$
 */
public class ReactorComponent extends HeaderFilterStrategyComponent {

  private final Reactor reactor;

  public ReactorComponent() {
    this(ReactorEndpoint.class);
  }

  public ReactorComponent(Class<? extends Endpoint> endpointClass) {
    super(endpointClass);
    this.reactor = Reactors.reactor(new Environment(), Environment.RING_BUFFER);
  }

  @BeanInject
  public ReactorComponent(Reactor reactor) {
    super(ReactorEndpoint.class);
    this.reactor = reactor;
  }

  @BeanInject
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

    HeaderFilterStrategy headerFilterStrategy =
        resolveAndRemoveReferenceParameter(parameters, "headerFilterStrategy",
            HeaderFilterStrategy.class);
    ReactorEndpoint endpoint = new ReactorEndpoint(uri, this, remaining);
    setProperties(endpoint.getEndpointConfiguration(), parameters);
    return endpoint;
  }

  public Reactor getReactor() {
    return reactor;
  }

}
