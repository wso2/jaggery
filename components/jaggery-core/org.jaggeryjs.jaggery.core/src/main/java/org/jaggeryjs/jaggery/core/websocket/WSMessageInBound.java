package org.jaggeryjs.jaggery.core.websocket;


import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.jaggeryjs.hostobjects.web.WebSocketHostObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class WSMessageInBound extends MessageInbound {

    private WebSocketHostObject webSockHostObject = null;

    public WSMessageInBound(WebSocketHostObject webSockHostObject) {
        this.webSockHostObject = webSockHostObject;
        this.webSockHostObject.setInbound(this);
    }

    /**
     * @param byteBuffer
     * @throws IOException
     */
    @Override
    protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {
        webSockHostObject.processBinary(byteBuffer);
    }

    /**
     * @param charBuffer
     * @throws IOException
     */

    @Override
    protected void onTextMessage(CharBuffer charBuffer) throws IOException {
        webSockHostObject.processText(charBuffer);
    }
    
    protected void onOpen(WsOutbound outbound){
        webSockHostObject.setOutbound(outbound);
		webSockHostObject.processOnOpen(outbound);
    }
    
    protected void onClose(int status){
		webSockHostObject.processOnClose(status);
    }
}

