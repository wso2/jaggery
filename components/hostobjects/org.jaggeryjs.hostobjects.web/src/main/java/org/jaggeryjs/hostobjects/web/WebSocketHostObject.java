package org.jaggeryjs.hostobjects.web;


import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.jaggeryjs.hostobjects.log.LogHostObject;
import org.jaggeryjs.hostobjects.stream.StreamHostObject;
import org.jaggeryjs.scriptengine.EngineConstants;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Stack;

public class WebSocketHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(WebSocketHostObject.class);

    private static final String hostObjectName = "WebSocket";

    private ContextFactory contextFactory;
    private MessageInbound inbound;
	private WsOutbound outbound;
	private Function textCallback = null;
    private Function onOpencallback = null;
    private Function onCloseCallback = null;
    private Function binaryCallback = null;
    private JaggeryContext asyncContext;

    public WebSocketHostObject() {

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
        who.contextFactory = cx.getFactory();
        JaggeryContext currentContext = (JaggeryContext) RhinoEngine.getContextProperty(
                EngineConstants.JAGGERY_CONTEXT);
        JaggeryContext asyncContext = new JaggeryContext();
        asyncContext.setEngine(currentContext.getEngine());
        asyncContext.setScope(currentContext.getScope());
        asyncContext.setTenantDomain(currentContext.getTenantDomain());

        asyncContext.addProperty(Constants.SERVLET_CONTEXT, currentContext.getProperty(Constants.SERVLET_CONTEXT));
        asyncContext.addProperty(LogHostObject.LOG_LEVEL, currentContext.getProperty(LogHostObject.LOG_LEVEL));
        asyncContext.addProperty(FileHostObject.JAVASCRIPT_FILE_MANAGER, currentContext.getProperty(
                FileHostObject.JAVASCRIPT_FILE_MANAGER));
        asyncContext.addProperty(Constants.JAGGERY_CORE_MANAGER, currentContext.getProperty(Constants.JAGGERY_CORE_MANAGER));
        asyncContext.addProperty(Constants.JAGGERY_INCLUDED_SCRIPTS, new HashMap<String, Boolean>());
        asyncContext.addProperty(Constants.JAGGERY_INCLUDES_CALLSTACK, new Stack<String>());
        asyncContext.addProperty(Constants.JAGGERY_REQUIRED_MODULES, new HashMap<String, ScriptableObject>());
        who.asyncContext = asyncContext;
        return who;
    }

    public void jsSet_ontext(Object outPutMessage) throws ScriptException {
        if (!(outPutMessage instanceof Function)) {
            HostObjectUtil.invalidProperty(hostObjectName, "ontext", "function", outPutMessage);
        }
        textCallback = (Function) outPutMessage;
    }

    public Scriptable jsGet_ontext() {
        return textCallback;
    }
    
    public void jsSet_onopen(Object openFunction) throws ScriptException {
        if (!(openFunction instanceof Function)) {
            HostObjectUtil.invalidProperty(hostObjectName, "onopen", "function", openFunction);
        }
        onOpencallback = (Function) openFunction;
    }

    public Scriptable jsGet_onopen() {
        return onOpencallback;
    }
    
    public void jsSet_onclose(Object closeFunction) throws ScriptException {
        if (!(closeFunction instanceof Function)) {
            HostObjectUtil.invalidProperty(hostObjectName, "onclose", "function", closeFunction);
        }
        onCloseCallback = (Function) closeFunction;
    }

    public Scriptable jsGet_onclose() {
        return onCloseCallback;
    }


    public void jsSet_onbinary(Object outPutMessage) throws ScriptException {
        if (!(outPutMessage instanceof Function)) {
            HostObjectUtil.invalidProperty(hostObjectName, "onbinary", "function", outPutMessage);
        }
        binaryCallback = (Function) outPutMessage;
    }

    public Scriptable jsGet_onbinary() {
        return binaryCallback;
    }

    public static void jsFunction_send(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "send";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String) && !(args[1] instanceof StreamHostObject)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string | Stream", args[0], false);
        }

        WebSocketHostObject who = (WebSocketHostObject) thisObj;
        if (args[0] instanceof String) {
            try {
                who.inbound.getWsOutbound().writeTextMessage(CharBuffer.wrap((String) args[0]));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        } else {
            StreamHostObject sho = (StreamHostObject) args[0];
            InputStream is = sho.getStream();
            try {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    who.inbound.getWsOutbound().writeBinaryMessage(ByteBuffer.wrap(buffer, 0, length));
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        }
    }

    public void setInbound(MessageInbound inbound) {
        this.inbound = inbound;
    }
    
    public WsOutbound getOutbound() {
		return outbound;
	}

	public void setOutbound(WsOutbound outbound) {
		this.outbound = outbound;
	}

    public void processText(CharBuffer charBuffer) {
        if (textCallback == null) {
            return;
        }
        Context cx = RhinoEngine.enterContext(this.contextFactory);
        RhinoEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, this.asyncContext);
        textCallback.call(cx, this, this, new Object[]{charBuffer.toString()});
        RhinoEngine.exitContext();
    }
    
    public void processOnOpen(WsOutbound wsOutbound) {
        if (onOpencallback == null) {
            return;
        }
        Context cx = RhinoEngine.enterContext(this.contextFactory);
        RhinoEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, this.asyncContext);
        onOpencallback.call(cx, this, this, new Object[]{wsOutbound});
        RhinoEngine.exitContext();
    }
    
    public void processOnClose(int status) {
        if (onCloseCallback == null) {
            return;
        }
        Context cx = RhinoEngine.enterContext(this.contextFactory);
        RhinoEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, this.asyncContext);
        onCloseCallback.call(cx, this, this, new Object[]{status});
        RhinoEngine.exitContext();
    }

    public void processBinary(ByteBuffer byteBuffer) {
        if (binaryCallback == null) {
            return;
        }
        Context cx = RhinoEngine.enterContext(this.contextFactory);
        RhinoEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, this.asyncContext);
        ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer.array());
        StreamHostObject sho = (StreamHostObject) cx.newObject(this, "Stream", new Object[]{bis});
        binaryCallback.call(cx, this, this, new Object[]{sho});
        RhinoEngine.exitContext();
    }
}

