/*
 * Copyright 2015 Matteo Massimo Calabrò
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

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.processor.ErrorHandler;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;

/**
 * Created by Matteo on 16/03/2015.
 */
@UriParams
public class ReactorConfiguration implements Cloneable {

  public static final String TYPE_PREFIX = "type:";
  public static final String REGEX_PREFIX = "regex:";
  public static final String URI_PREFIX = "uri:";

  @UriParam(defaultValue = "true")
  protected boolean mapReactorEvent = true;
  @UriParam
  private ErrorHandler errorHandler;
  @UriParam(defaultValue = "false")
  private boolean transferExchange = false;
  @UriParam(defaultValue = "true")
  private boolean includeAllProperties = true;
  @UriParam(defaultValue = "true")
  private boolean alwaysCopyMessage = true;
  @UriParam(
      defaultValue = "false",
      description = "Sets whether synchronous processing should be strictly used, or Camel is allowed to use asynchronous processing (if supported).")
  private boolean synchronous = true;

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public boolean isMapReactorEvent() {
    return mapReactorEvent;
  }

  public void setMapReactorEvent(boolean mapReactorEvent) {
    this.mapReactorEvent = mapReactorEvent;
  }

  public ReactorConfiguration copy() {
    try {
      return (ReactorConfiguration) this.clone();
    } catch (CloneNotSupportedException var2) {
      throw new RuntimeCamelException(var2);
    }
  }

  public boolean isSynchronous() {
    return synchronous;
  }

  public void setSynchronous(boolean synchronous) {
    this.synchronous = synchronous;
  }

  public boolean isTransferExchange() {
    return transferExchange;
  }

  public void setTransferExchange(boolean transferExchange) {
    this.transferExchange = transferExchange;
  }

  public boolean isIncludeAllProperties() {
    return includeAllProperties;
  }

  public void setIncludeAllProperties(boolean includeAllProperties) {
    this.includeAllProperties = includeAllProperties;
  }

  public boolean isAlwaysCopyMessage() {
    return alwaysCopyMessage;
  }

  public void setAlwaysCopyMessage(boolean alwaysCopyMessage) {
    this.alwaysCopyMessage = alwaysCopyMessage;
  }
}
