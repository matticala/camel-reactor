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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;

/**
 *
 * @author matticala
 * @since 21-nov-2014
 * @version $$Revision$$
 *
 * Last change: $$Date$$ Last changed by: $$Author$$
 */
public class ReactorConfiguration implements Cloneable {

    private static final Logger LOG = LoggerFactory.getLogger(ReactorConfiguration.class);

    private final Environment environment;

    private final String dispatcher;

    public ReactorConfiguration() {
        this.environment = new Environment();
        this.dispatcher = Environment.EVENT_LOOP;
    }

    public ReactorConfiguration(Environment environment, String dispatcher) {
        this.environment = environment;
        this.dispatcher = dispatcher;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getDispatcher() {
        return dispatcher;
    }

    public ReactorConfiguration copy() {
        try {
            ReactorConfiguration config = (ReactorConfiguration) clone();
            return config;
        }
        catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void validateConfiguration() {
        switch (dispatcher) {
            case Environment.EVENT_LOOP:
            case Environment.RING_BUFFER:
            case Environment.THREAD_POOL:
            case Environment.WORK_QUEUE:
                break;
            default:
                LOG.warn("The dispatcher {} is not valid.", dispatcher);
        }
    }

    public static Configurer configure() {
        return new Configurer();
    }

    protected static class Configurer {

        private Environment environment;

        private String dispatcher;

        public Configurer environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public Configurer dispatcher(String dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public ReactorConfiguration get() {
            return new ReactorConfiguration(this.environment, this.dispatcher);
        }
    }
}
