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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import reactor.event.Event;

import java.util.Map;

/**
 * @author Matteo
 * @version $$Revision$$ Created: 12/03/2015 10:15 Last change: $$Date$$ Last changed by: $$Author$$
 */
public abstract class ReactorMessageHelper {

  public static Event<?> getReactorEvent(Exchange exchange) {
    Message msg = exchange.hasOut() ? exchange.getOut() : exchange.getIn();
    if(msg instanceof ReactorMessage && ((ReactorMessage) msg).getEvent() != null) {
      return ((ReactorMessage) msg).getEvent();
    }
    Object body = msg.getBody();
    Event<?> event = Event.wrap(body);
    for (Map.Entry<String, Object> entry : msg.getHeaders().entrySet()) {
      String key = entry.getKey();
      if (key.startsWith("REACTOR_")) {
        switch (key) {
          case "REACTOR_key":
            event.setKey(entry.getValue());
            break;
          case "REACTOR_replyTo":
            event.setReplyTo(entry.getValue());
            break;
          default:
            event.getHeaders().set(key.substring("REACTOR_".length()), entry.getValue());
        }
      }
    }
    return event;
  }

  public static void fillMessage(Event<?> src, Message dst) {
    dst.setBody(src.getData());
    dst.setMessageId(src.getId().toString());
    for (Map.Entry<String, Object> entry : src.getHeaders().asMap().entrySet()) {
      dst.setHeader("REACTOR_"+entry.getKey(), entry.getValue());
    }
    if (src.getKey() != null) {
      dst.setHeader("REACTOR_key", src.getKey());
    }
    if (src.getReplyTo() != null) {
      dst.setHeader("REACTOR_replyTo", src.getReplyTo());
    }
  }
}
