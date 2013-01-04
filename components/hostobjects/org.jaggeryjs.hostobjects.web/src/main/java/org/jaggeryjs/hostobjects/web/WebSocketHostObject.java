package org.jaggeryjs.hostobjects.web;


import org.apache.catalina.websocket.WsOutbound;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class WebSocketHostObject extends ScriptableObject {

    private static final String hostObjectName = "WebSocket";
    private Context context;
    private static WsOutbound wsOutbound;

    private Function onTextMessageFunction;
    private Function onBinMessageFunction;


    public WebSocketHostObject() {
    }

    public static WsOutbound getWsOutbound() {
        return wsOutbound;
    }

    public static void setWsOutbound(WsOutbound wsOut) {
        wsOutbound = wsOut;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws Exception {
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }

        WebSocketHostObject who = new WebSocketHostObject();

        who.context = cx;

        return who;

    }

    public Context getContext() {
        return context;
    }


    public void jsSet_onTextMessage(Object outPutMessage) {

        if (outPutMessage instanceof Function) {
            this.setOnTextMessageFunction((Function) outPutMessage);
        }

    }


    public Scriptable jsGet_onTextMessage() {
        return getOnTextMessageFunction();
    }


    public Function getOnTextMessageFunction() {
        return onTextMessageFunction;
    }

    private void setOnTextMessageFunction(Function onMessageFunction) {
        this.onTextMessageFunction = onMessageFunction;
    }



    public void jsSet_onBinaryMessage(Object outPutMessage) {

        if (outPutMessage instanceof Function) {
            this.setOnBinMessageFunction((Function) outPutMessage);
        }

    }

    public Scriptable jsGet_onBinaryMessage() {
        return getOnBinMessageFunction();
    }

    public Function getOnBinMessageFunction() {

        return onBinMessageFunction;
    }


    private void setOnBinMessageFunction(Function onMessageFunction) {

        this.onBinMessageFunction = onMessageFunction;

    }


    public static void jsFunction_send(Context cx, Scriptable thisObj, Object[] args, Function funObj) {


        if (args[0] instanceof String) {

            CharBuffer buffer = CharBuffer.wrap((String) args[0]);

            try {
                getWsOutbound().writeTextMessage(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        else if (args[0] instanceof ByteBuffer){

            try {
                getWsOutbound().writeBinaryMessage((ByteBuffer)args[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}

