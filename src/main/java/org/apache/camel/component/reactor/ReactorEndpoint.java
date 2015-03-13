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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Reactor;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  private HeaderFilterStrategy headerFilterStrategy = new ReactorHeaderFilterStrategy();

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

  static enum TYPE {
    CLASS, URI, OBJECT, REGEX
  }

  private final Reactor reactor;

  @UriPath
  @Metadata(required = "true")
  private final Object selector;

  private TYPE type;

  public ReactorEndpoint(String uri, ReactorComponent component, String selector) throws Exception {
    super(uri, component);
    this.reactor = component.getReactor();
    Pattern p = Pattern.compile("(uri|class|type|regex|object):(.+)");
    Matcher m = p.matcher(selector);
    if (m.matches()) {
      String prefix = m.group(1);
      String object = m.group(2);
      switch (prefix) {
        case "class":
        case "type":
          this.type = TYPE.CLASS;
          if (object.startsWith("class ")) {
            object = object.substring("class ".length());
          }
          this.selector = Class.forName(object);
          break;
        case "uri":
          this.type = TYPE.URI;
          this.selector = object;
          break;
        case "regex":
          this.type = TYPE.REGEX;
          this.selector = object;
          break;
        case "object":
          this.type = TYPE.OBJECT;
          this.selector = object;
          break;
        default:
          throw new URISyntaxException(uri, selector);
      }
    } else {
      throw new URISyntaxException(uri, selector);
    }
  }

  @Override
  public Producer createProducer() throws Exception {
    return new ReactorProducer(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Consumer createConsumer(Processor processor) throws Exception {
    return new ReactorConsumer(this, processor);
  }

  public Reactor getReactor() {
    return reactor;
  }

  public Object getSelector() {
    return selector;
  }

  public TYPE getType() {
    return type;
  }

  public void setType(TYPE type) {
    this.type = type;
  }

  public void setType(String type) {
    this.type = TYPE.valueOf(type);
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
