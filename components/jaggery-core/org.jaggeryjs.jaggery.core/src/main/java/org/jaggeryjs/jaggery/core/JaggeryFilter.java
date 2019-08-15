package org.jaggeryjs.jaggery.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.websocket.server.UpgradeUtil;
import org.apache.tomcat.websocket.server.WsServerContainer;
import org.jaggeryjs.hostobjects.web.WebSocketHostObject;
import org.jaggeryjs.jaggery.core.manager.CommonManager;
import org.jaggeryjs.jaggery.core.manager.WebAppManager;
import org.jaggeryjs.jaggery.core.websocket.JaggeryWSEndpoint;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerEndpointConfig;

public class JaggeryFilter implements Filter {

    private static final Log log = LogFactory.getLog(JaggeryFilter.class);

    private transient WsServerContainer sc;
    private static final String SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE =
            "javax.websocket.server.ServerContainer";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        sc = (WsServerContainer) filterConfig.getServletContext().getAttribute(SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (WebAppManager.isWebSocket(servletRequest)) {
            try {
                CommonManager.getInstance().getEngine().enterContext();
            } catch (ScriptException e) {
                log.error(e.getMessage(), e);
                throw new ServletException(e);
            }

            // HTTP request with an upgrade header for WebSocket present
            HttpServletRequest req = (HttpServletRequest) servletRequest;
            HttpServletResponse resp = (HttpServletResponse) servletResponse;

            WebSocketHostObject webSocketHostObject;
            try {
                WebAppManager.execute(req, resp);
                JaggeryContext context = CommonManager.getJaggeryContext();
                Scriptable scope = context.getScope();
                webSocketHostObject = (WebSocketHostObject) scope.get("webSocket", scope);
            } finally {
                RhinoEngine.exitContext();
            }
            if (webSocketHostObject != null) {
                Endpoint jaggeryWSEndpoint = new JaggeryWSEndpoint();
                ServerEndpointConfig sec = ServerEndpointConfig.Builder.create(jaggeryWSEndpoint.getClass(), "").build();
                sec.getUserProperties().put("webSocket", webSocketHostObject);
                UpgradeUtil.doUpgrade(sc, req, resp, sec, new HashMap<String, String>());
            }
        } else {
            servletRequest.getServletContext().getNamedDispatcher(
                    JaggeryCoreConstants.JAGGERY_SERVLET_NAME).forward(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
