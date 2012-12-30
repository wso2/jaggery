package org.jaggeryjs.jaggery.core.websocket;


import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.jaggeryjs.hostobjects.web.WebSocketHostObject;
import org.mozilla.javascript.Function;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class WsMessageInBound extends MessageInbound {

    private WsOutbound out = null;
    private static String msg = null;
    private static ByteBuffer byteBuffer = null;
    private WebSocketHostObject webSockHostObject = null;


    private static String getMsg() {
        return msg;
    }

    private static void setMsg(String msg) {
        WsMessageInBound.msg = msg;
    }

    private static ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    private static void setByteBuffer(ByteBuffer byteBuffer) {
        WsMessageInBound.byteBuffer = byteBuffer;
    }


    public  void setWebSockHostObject(WebSocketHostObject webSockHostObject) {
        this.webSockHostObject = webSockHostObject;
    }

    /**
     *
     * @param byteBuffer
     * @throws IOException
     */
    @Override
    protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {
        setByteBuffer(byteBuffer);
        if(out == null){

            out = getWsOutbound();
        }

        WebSocketHostObject.setWsOutbound(out);

        Function onBinMessageFunction = webSockHostObject.getOnBinMessageFunction();

        onBinMessageFunction.call(webSockHostObject.getContext(), webSockHostObject, webSockHostObject, new Object[]{getByteBuffer()}) ;

    }

    /**
     *
     * @param charBuffer
     * @throws IOException
     */

    @Override
    protected void onTextMessage(CharBuffer charBuffer) throws IOException {

        setMsg(charBuffer.toString());

        if(out == null){

            out = getWsOutbound();
        }
        WebSocketHostObject.setWsOutbound(out);

        Function onMessageFunction = webSockHostObject.getOnMessageFunction();

        onMessageFunction.call(webSockHostObject.getContext(), webSockHostObject, webSockHostObject, new Object[]{getMsg()});


    }

    @Override
    protected void onOpen(WsOutbound outbound) {

        super.onOpen(outbound);
        System.out.println("Opening the websocket connection");
    }

    @Override
    protected void onClose(int status) {

        super.onClose(status);
        System.out.println("Closing the websocket connection with status :" + status);
    }


    public WsMessageInBound() {
    }

    @Override
    public int getReadTimeout() {
        return super.getReadTimeout();
    }

    @Override
    public void setOutboundCharBufferSize(int outboundCharBufferSize) {
        super.setOutboundCharBufferSize(outboundCharBufferSize);
    }

    @Override
    public int getOutboundCharBufferSize() {
        return super.getOutboundCharBufferSize();
    }

    @Override
    public void setOutboundByteBufferSize(int outboundByteBufferSize) {
        super.setOutboundByteBufferSize(outboundByteBufferSize);
    }

    @Override
    public int getOutboundByteBufferSize() {
        return super.getOutboundByteBufferSize();
    }
}

