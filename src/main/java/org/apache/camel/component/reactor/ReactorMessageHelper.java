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

import org.apache.camel.Message;
import reactor.event.Event;

import java.util.Map;

import static org.apache.camel.component.reactor.ReactorConstants.*;

/**
 * @author matticala
 */
public abstract class ReactorMessageHelper {

  public static void fillMessage(Event<?> src, Message dst) {
    dst.setBody(src.getData());
    dst.setMessageId(src.getId().toString());
    for (Map.Entry<String, Object> entry : src.getHeaders().asMap().entrySet()) {
      dst.setHeader(HEADER_PREFIX + entry.getKey(), entry.getValue());
    }
    if (src.getKey() != null) {
      dst.setHeader(KEY, src.getKey());
    }
    if (src.getReplyTo() != null) {
      dst.setHeader(REPLY_TO, src.getReplyTo());
    }
  }
}
