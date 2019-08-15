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

import java.util.Map;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 * The websocket endpoint to process the onOpen and onClose from the websocket host object.
 */
public class JaggeryWSEndpoint extends Endpoint {

    private WebSocketHostObject webSockHostObject = null;

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

        Map<String, Object> userProperties = endpointConfig.getUserProperties();
        webSockHostObject = (WebSocketHostObject)userProperties.get("webSocket");
        webSockHostObject.setSession(session);
        MessageHandler stringMsgHandler = new JaggeryStringMessageHandler(webSockHostObject, session);
        MessageHandler binaryMsgHandler = new JaggeryBinaryMessageHandler(webSockHostObject, session);
        session.addMessageHandler(stringMsgHandler);
        session.addMessageHandler(binaryMsgHandler);
        webSockHostObject.processOnOpen();
    }

    @Override
    public final void onClose(Session session, CloseReason closeReason) {

        int status = closeReason.getCloseCode().getCode();
        webSockHostObject.processOnClose(status);

    }
}
