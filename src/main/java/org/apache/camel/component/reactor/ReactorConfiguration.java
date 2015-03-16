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
  protected boolean mapReactorEvent;
  @UriParam
  private ErrorHandler errorHandler;
  @UriParam(defaultValue = "false")
  private boolean transferExchange;
  @UriParam(defaultValue = "true")
  private boolean includeAllProperties;

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public boolean isMapReactorEvent() {
    return false;
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
}
