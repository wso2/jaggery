package org.jaggeryjs.jaggery.core.websocket;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.web.WebSocketHostObject;
import org.jaggeryjs.jaggery.core.manager.CommonManager;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.jaggery.core.manager.WebAppManager;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.Scriptable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JaggeryWebSocketServlet extends WebSocketServlet {

    private static final Log log = LogFactory.getLog(JaggeryWebSocketServlet.class);

    private WSMessageInBound wsMessageInBound = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            CommonManager.getInstance().getEngine().enterContext();
        } catch (ScriptException e) {
            log.error(e.getMessage(), e);
            throw new ServletException(e);
        }
        WebAppManager.execute(request, response);
        JaggeryContext context = CommonManager.getJaggeryContext();
        Scriptable scope = context.getScope();
        wsMessageInBound = new WSMessageInBound((WebSocketHostObject) scope.get("webSocket", scope));
        RhinoEngine.exitContext();
        super.doGet(request, response);
    }

    @Override
    protected StreamInbound createWebSocketInbound(String s, HttpServletRequest request) {
        return wsMessageInBound;
    }

}
