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

import static org.apache.camel.component.reactor.ReactorConfiguration.*;

/**
 * A Camel Component for <a href="http://projectreactor.io/">Reactor</a>
 *
 * @author matticala
 */
public class ReactorComponent extends HeaderFilterStrategyComponent {

  private final Reactor reactor;

  private ReactorConfiguration configuration;

  public ReactorComponent() {
    this(ReactorEndpoint.class);
  }

  public ReactorComponent(Class<? extends Endpoint> endpointClass) {
    super(endpointClass);
    this.reactor = Reactors.reactor(new Environment(), Environment.WORK_QUEUE);
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
    SelectorType type;
    Object selector;
    if (remaining.startsWith(URI_PREFIX)) {
      type = SelectorType.uri;
      selector = remaining.substring(URI_PREFIX.length());
    } else if (remaining.startsWith(TYPE_PREFIX)) {
      type = SelectorType.type;
      remaining = remaining.substring(TYPE_PREFIX.length());
      if (remaining.startsWith("class")) {
        remaining = remaining.substring("class".length() + 1);
      }
      selector = Class.forName(remaining);
    } else if (remaining.startsWith(REGEX_PREFIX)) {
      type = SelectorType.regex;
      selector = remaining.substring(REGEX_PREFIX.length());
    } else {
      type = SelectorType.object;
      selector = remaining;
    }

    ReactorConfiguration conf = getConfiguration().copy();

    ReactorEndpoint endpoint = new ReactorEndpoint(uri, this, type, selector, conf);
    setProperties(endpoint.getEndpointConfiguration(), parameters);
    return endpoint;
  }

  public Reactor getReactor() {
    return reactor;
  }

  public ReactorConfiguration getConfiguration() {
    if (configuration == null) {
      configuration = new ReactorConfiguration();
    }
    return configuration;
  }

  public void setConfiguration(ReactorConfiguration configuration) {
    this.configuration = configuration;
  }
}
