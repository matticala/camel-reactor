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

import org.apache.camel.*;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.processor.ErrorHandler;
import org.apache.camel.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.event.selector.Selectors;

/**
 * @author matticala
 * @version $$Revision$$
 *          <p/>
 *          Last change: $$Date$$ Last changed by: $$Author$$
 * @since 21-nov-2014
 */
@ManagedResource(description = "Managed Reactor Endpoint")
@UriEndpoint(scheme = "reactor", syntax = "reactor:type|uri|regex|object:selector",
    consumerClass = ReactorConsumer.class, label = "reactor")
public class ReactorEndpoint extends DefaultEndpoint implements HeaderFilterStrategyAware {

  private static final Logger LOG = LoggerFactory.getLogger(ReactorEndpoint.class);
  private ReactorConfiguration configuration;
  private Reactor reactor;
  private SelectorType selectorType;
  @UriPath
  @Metadata(required = "true")
  private Object selectorObject;
  private HeaderFilterStrategy headerFilterStrategy = new ReactorHeaderFilterStrategy();
  private ReactorBinding binding;

  public ReactorEndpoint() {
    super();
  }

  public ReactorEndpoint(SelectorType selectorType, Object selectorObject) {
    this(null, null, selectorType, selectorObject, new ReactorConfiguration());
  }

  public ReactorEndpoint(String uri, ReactorComponent component, SelectorType selectorType, Object selectorObject,
      ReactorConfiguration configuration) {
    super(uri, component);
    this.reactor = component.getReactor();
    this.selectorType = selectorType;
    this.selectorObject = selectorObject;
    this.configuration = configuration;
  }

  /**
   * Gets the header filter strategy used
   * 
   * @return the strategy
   */
  @Override
  public HeaderFilterStrategy getHeaderFilterStrategy() {
    return headerFilterStrategy;
  }

  /**
   * Sets the header filter strategy to use
   * 
   * @param strategy the strategy
   */
  @Override
  public void setHeaderFilterStrategy(HeaderFilterStrategy strategy) {
    this.headerFilterStrategy = strategy;
  }

  public ReactorConfiguration getConfiguration() {
    if(configuration == null) {
      configuration = new ReactorConfiguration();
    }
    return configuration;
  }

  @ManagedAttribute
  public boolean isTransferExchange() {
    return getConfiguration().isTransferExchange();
  }

  @ManagedAttribute
  public void setTransferExchange(boolean transferExchange) {
    getConfiguration().setTransferExchange(transferExchange);
  }

  @ManagedAttribute
  public boolean isIncludeAllProperties() {
    return getConfiguration().isIncludeAllProperties();
  }

  @ManagedAttribute
  public void setIncludeAllProperties(boolean includeAllProperties) {
    getConfiguration().setIncludeAllProperties(includeAllProperties);
  }

  @Override
  public Producer createProducer() throws Exception {
    return new ReactorProducer(this);
  }

  @Override
  public Exchange createExchange(ExchangePattern pattern) {
    Exchange exchange = new DefaultExchange(this, pattern);
    exchange.setProperty(Exchange.BINDING, getBinding());
    return exchange;
  }

  @Override
  public Exchange createExchange() {
    return createExchange(getExchangePattern());
  }

  public Exchange createExchange(Event<?> event) {
    Exchange exchange = createExchange(getExchangePattern());
    exchange.setIn(new ReactorMessage(event, getBinding()));
    return exchange;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Consumer createConsumer(Processor processor) throws Exception {
    return new ReactorConsumer(this, processor);
  }

  public Reactor getReactor() {
    return reactor;
  }

  public Object getSelectorObject() {
    return selectorObject;
  }

  public SelectorType getSelectorType() {
    return selectorType;
  }

  @ManagedAttribute
  @Override
  public boolean isSingleton() {
    return true;
  }

  public ReactorBinding getBinding() {
    if (binding == null) {
      binding = new ReactorBinding(this);
    }
    return binding;
  }

  public void setBinding(ReactorBinding binding) {
    this.binding = binding;
  }

  @Override
  @ManagedAttribute(description = "Endpoint Uri", mask = true)
  public String getEndpointUri() {
    return super.getEndpointUri();
  }

  @ManagedAttribute(description = "Service State")
  public String getState() {
    ServiceStatus status = this.getStatus();
    if (status == null) {
      status = ServiceStatus.Stopped;
    }
    return status.name();
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    getConfiguration().setErrorHandler(errorHandler);
  }

  public void setConfiguration(ReactorConfiguration configuration) {
    this.configuration = configuration;
  }

  public void setReactor(Reactor reactor) {
    this.reactor = reactor;
  }

  public void setSelectorType(SelectorType selectorType) {
    this.selectorType = selectorType;
  }

  public void setSelectorObject(Object selectorObject) {
    this.selectorObject = selectorObject;
  }


}
