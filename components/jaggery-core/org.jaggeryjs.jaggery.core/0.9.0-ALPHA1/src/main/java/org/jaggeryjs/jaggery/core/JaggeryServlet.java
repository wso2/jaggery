package org.jaggeryjs.jaggery.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.manager.WebAppManager;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class JaggeryServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(JaggeryServlet.class);
    public static final String JAGGERY_MODULES_DIR = "modules";

    private static WebAppManager manager = null;

    static {
        try {

            String jaggeryDir = System.getProperty("jaggery.home");
            if (jaggeryDir == null) {
                jaggeryDir = System.getProperty("carbon.home");
            }

            if(jaggeryDir == null) {
                log.error("Unable to find jaggery.home or carbon.home system properties");
            }
            manager = new WebAppManager(jaggeryDir + File.separator + JAGGERY_MODULES_DIR);
        } catch (ScriptException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }

    protected void doTrace(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        manager.execute(request, response);
    }
}
