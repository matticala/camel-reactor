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

import org.apache.camel.impl.DefaultHeaderFilterStrategy;

/**
 * @author mmcalabro
 */
public class ReactorHeaderFilterStrategy extends DefaultHeaderFilterStrategy {

  public ReactorHeaderFilterStrategy() {
    init();
  }

  protected void init() {
    // setOutFilterPattern(String.format("%s(?!%s|%s)[\\.|a-z|A-Z|0-9]+",
    // ReactorConstants.HEADER_PREFIX, ReactorConstants.KEY, ReactorConstants.REPLY_TO));
    getOutFilter().add(ReactorConstants.KEY);
    setOutFilterPattern("(?i)(Camel|org\\.apache\\.camel|JMS)[\\.|a-z|A-Z|0-9]*");
    setCaseInsensitive(true);
  }
}
