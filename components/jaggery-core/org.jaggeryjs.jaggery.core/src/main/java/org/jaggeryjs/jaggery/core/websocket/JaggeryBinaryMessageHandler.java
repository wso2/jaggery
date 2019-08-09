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

import java.nio.ByteBuffer;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 * Message Handler for the incoming ByteBuffer messages to be processed by the jaggery websocket host object.
 */
public class JaggeryBinaryMessageHandler implements MessageHandler.Whole<ByteBuffer> {

    private WebSocketHostObject webSockHostObject;

    public JaggeryBinaryMessageHandler(WebSocketHostObject webSockHostObject, Session session) {

        this.webSockHostObject = webSockHostObject;
    }

    @Override
    public void onMessage(ByteBuffer byteBuffer) {

        webSockHostObject.processBinary(byteBuffer);
    }
}
