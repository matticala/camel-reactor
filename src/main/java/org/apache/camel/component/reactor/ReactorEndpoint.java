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
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Reactor;

/**
 *
 * @author matticala
 * @since 21-nov-2014
 * @version $$Revision$$
 *
 * Last change: $$Date$$ Last changed by: $$Author$$
 */
@ManagedResource(description = "Managed Reactor Endpoint")
@UriEndpoint(scheme = "reactor", consumerClass = ReactorConsumer.class)
public class ReactorEndpoint extends DefaultEndpoint {

    public static final String TYPE = "class";
    public static final String URI = "uri";
    public static final String REGEX = "regex";
    public static final String OBJECT = "object";
    public static final String MATCH_ALL = "matchAll";

    private static final Logger LOG = LoggerFactory.getLogger(ReactorEndpoint.class);

    private final Reactor reactor;

    private final String selector;

    @UriParam(name = ReactorEndpoint.TYPE)
    private Class<?> type;

    @UriParam(name = ReactorEndpoint.REGEX)
    private String regex;

    @UriParam(name = ReactorEndpoint.URI)
    private String uri;

    @UriParam(name = ReactorEndpoint.OBJECT)
    private boolean object = false;

    @UriParam(name = ReactorEndpoint.MATCH_ALL)
    private boolean matchAll = false;

    public ReactorEndpoint(String uri, String selector, ReactorComponent component) {
        super(UnsafeUriCharactersEncoder.encode(uri), component);
        this.selector = selector;
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

    public boolean isMatchAll() {
        return matchAll;
    }

    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }

    public boolean isObject() {
        return object;
    }

    public void setObject(boolean object) {
        this.object = object;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getSelector() {
        return selector;
    }

    public Reactor getReactor() {
        return reactor;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean hasUri() {
        return this.uri != null && !this.uri.isEmpty();
    }

    public boolean hasRegex() {
        return this.regex != null && !this.regex.isEmpty();
    }

    public boolean hasType() {
        return this.type != null;
    }

}
