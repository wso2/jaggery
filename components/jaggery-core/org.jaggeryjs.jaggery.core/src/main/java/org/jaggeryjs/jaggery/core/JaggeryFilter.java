package org.jaggeryjs.jaggery.core;

import org.jaggeryjs.jaggery.core.manager.WebAppManager;

import javax.servlet.*;
import java.io.IOException;

public class JaggeryFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (WebAppManager.isWebSocket(servletRequest)) {
            servletRequest.getServletContext().getNamedDispatcher(
                    JaggeryCoreConstants.JAGGERY_WEBSOCKET_SERVLET_NAME).forward(servletRequest, servletResponse);
        } else {
            servletRequest.getServletContext().getNamedDispatcher(
                    JaggeryCoreConstants.JAGGERY_SERVLET_NAME).forward(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
