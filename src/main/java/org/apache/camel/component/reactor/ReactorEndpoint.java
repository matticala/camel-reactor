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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Reactor;

/**
 * @author matticala
 * @version $$Revision$$
 *          <p/>
 *          Last change: $$Date$$ Last changed by: $$Author$$
 * @since 21-nov-2014
 */
@ManagedResource(description = "Managed Reactor Endpoint")
@UriEndpoint(scheme = "reactor", consumerClass = ReactorConsumer.class)
//@UriEndpoint(scheme = "reactor", syntax = "reactor:selector", consumerClass = ReactorConsumer.class, label = "reactor")
public class ReactorEndpoint extends DefaultEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ReactorEndpoint.class);

    static enum TYPE {
        CLASS, URI, OBJECT, REGEX
    }

    private final Reactor reactor;

    @UriPath
//  TODO: Camel-2.15.x  @Metadata(required = true)
    private final Object selector;

    @UriParam(name = "type")
    private TYPE type;

    public ReactorEndpoint(String uri, ReactorComponent component, String selector) throws Exception {
        super(uri, component);
        char prefix = selector.charAt(0);
        String object = selector.substring(2,selector.length()-1);
        switch (prefix) {
            case 'T':
                this.type = TYPE.CLASS;
                if(object.startsWith("class ")) {
                    object = object.substring("class ".length());
                }
                this.selector = Class.forName(object);
                break;
            case 'U':
                this.type = TYPE.URI;
                this.selector = object;
                break;
            case 'R':
                this.type = TYPE.REGEX;
                this.selector = object;
                break;
            case '$':
                this.type = TYPE.OBJECT;
                this.selector = object;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported selector prefix: " + prefix);
        }
        this.reactor = component.getReactor();
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
