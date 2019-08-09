/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.jaggeryjs.jaggery.core.websocket;

import org.jaggeryjs.hostobjects.web.WebSocketHostObject;

import java.nio.CharBuffer;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 * Message Handler for the incoming String messages to be processed by the jaggery websocket host.
 */
public class JaggeryStringMessageHandler implements MessageHandler.Whole<String> {

    private WebSocketHostObject webSockHostObject;

    public JaggeryStringMessageHandler(WebSocketHostObject webSockHostObject, Session session) {

        this.webSockHostObject = webSockHostObject;
    }

    @Override
    public void onMessage(String msg) {

        webSockHostObject.processText(msg);
    }
}
