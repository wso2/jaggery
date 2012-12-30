package org.jaggeryjs.jaggery.core.websocket;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.jaggeryjs.hostobjects.web.WebSocketHostObject;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.jaggery.core.manager.*;
import org.jaggeryjs.jaggery.core.plugins.WebAppFileManager;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.engine.JavaScriptProperty;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.security.RhinoSecurityController;
import org.jaggeryjs.scriptengine.security.RhinoSecurityDomain;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.context.CarbonContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.PermissionCollection;


public class JaggeryWebSocketServlet extends WebSocketServlet {


    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

         executeWebSocket(request,response);

    }

    @Override
    protected StreamInbound createWebSocketInbound(String s) {

        return getWsMessageInBound();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebAppManager.execute(request, response);
    }

    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebAppManager.execute(request, response);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebAppManager.execute(request, response);
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebAppManager.execute(request, response);
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebAppManager.execute(request, response);
    }

    protected void doTrace(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebAppManager.execute(request, response);
    }

    private static final Log log = LogFactory.getLog(WebAppManager.class);

    public static final String CORE_MODULE_NAME = "core";

    private static final String DEFAULT_CONTENT_TYPE = "text/html";

    private static final String DEFAULT_CHAR_ENCODING = "UTF-8";

    public static final String JAGGERY_MODULES_DIR = "modules";
    private static WsMessageInBound wsMessageInBound = null;

    static {
        try {

            String jaggeryDir = System.getProperty("jaggery.home");
            if (jaggeryDir == null) {
                jaggeryDir = System.getProperty("carbon.home");
            }

            if (jaggeryDir == null) {
                log.error("Unable to find jaggery.home or carbon.home system properties");
            }

            String modulesDir = jaggeryDir + File.separator + JAGGERY_MODULES_DIR;

            CommonManager.getInstance().initialize(modulesDir, new RhinoSecurityController() {
                @Override
                protected void updatePermissions(PermissionCollection permissions, RhinoSecurityDomain securityDomain) {
                    JaggerySecurityDomain domain = (JaggerySecurityDomain) securityDomain;
                    ServletContext context = domain.getServletContext();
                    String docBase = context.getRealPath("/");
                    // Create a file read permission for web app context directory
                    if (!docBase.endsWith(File.separator)) {
                        permissions.add(new FilePermission(docBase, "read"));
                        docBase = docBase + File.separator;
                    } else {
                        permissions.add(new FilePermission(docBase.substring(0, docBase.length() - 1), "read"));
                    }
                    docBase = docBase + "-";
                    permissions.add(new FilePermission(docBase, "read"));
                }
            });
        } catch (ScriptException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String getNormalizedScriptPath(String[] keys) {
        return "/".equals(keys[1]) ? keys[2] : keys[1] + keys[2];
    }


    public  void executeWebSocket(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {



        HttpServletRequest request = (HttpServletRequest) ((HttpServletRequestWrapper)req).getRequest();

        String scriptPath = (String) request.getAttribute("reqURI");
        InputStream sourceIn = request.getServletContext().getResourceAsStream(scriptPath);
        if (sourceIn == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());

            return;
        }
        RhinoEngine engine = null;
        OutputStream out = null;
        try {
            engine = CommonManager.getInstance().getEngine();
            Context cx = engine.enterContext();
            //Creating an OutputStreamWritter to write content to the servletResponse
             out = resp.getOutputStream();
            JaggeryContext webAppContext = createJaggeryContext(out, scriptPath, request, resp);


            CommonManager.initContext(webAppContext);


            WsMessageInBound wsMsgInBnd = new WsMessageInBound();

            Scriptable scriptableWsHostObject = cx.newObject(webAppContext.getScope(), "WebSocket", new Object[]{});

            wsMsgInBnd.setWebSockHostObject((WebSocketHostObject) scriptableWsHostObject);

            setWsMessageInBound(wsMsgInBnd);

            JavaScriptProperty websocket = new JavaScriptProperty("websocket");
            websocket.setValue(scriptableWsHostObject);
            websocket.setAttribute(ScriptableObject.PERMANENT);
            RhinoEngine.defineProperty(webAppContext.getScope(), websocket);



            RhinoEngine.putContextProperty(FileHostObject.JAVASCRIPT_FILE_MANAGER,
                    new WebAppFileManager(request.getServletContext()));
            CommonManager.getInstance().getEngine().exec(new ScriptReader(sourceIn), webAppContext.getScope(),
                    getScriptCachingContext(request, scriptPath));


            // Go to super and establish the connection via handshaking

            super.doGet(request,resp);

        } catch (ScriptException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        } finally {
            //Exiting from the context
            if (engine != null) {
                engine.exitContext();
            }

            if(out !=null){
                 out.close();
            }
        }



    }


    private static JaggeryContext createJaggeryContext(OutputStream out, String scriptPath,
                                                       HttpServletRequest request, HttpServletResponse response) {
        WebAppContext context = new WebAppContext();
        context.setTenantId(Integer.toString(CarbonContext.getCurrentContext().getTenantId()));
        context.setOutputStream(out);
        context.setServletRequest(request);
        context.setServletResponse(response);
        context.setServletConext(request.getServletContext());
        context.setScriptPath(scriptPath);
        context.getIncludesCallstack().push(scriptPath);
        context.getIncludedScripts().put(scriptPath, true);
        return context;
    }

    public static ScriptCachingContext getScriptCachingContext(HttpServletRequest request, String scriptPath)
            throws ScriptException {
        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        String tenantId = jaggeryContext.getTenantId();
        String[] parts = getKeys(request.getContextPath(), scriptPath, scriptPath);
        /**
         * tenantId = tenantId
         * context = webapp context
         * path = relative path to the directory of *.js file
         * cacheKey = name of the *.js file being cached
         */
        ScriptCachingContext sctx = new ScriptCachingContext(tenantId, parts[0], parts[1], parts[2]);
        ServletContext servletContext = request.getServletContext();
        sctx.setSecurityDomain(new JaggerySecurityDomain(getNormalizedScriptPath(parts), servletContext));
        long lastModified = getScriptLastModified(servletContext, scriptPath);
        sctx.setSourceModifiedTime(lastModified);
        return sctx;
    }

    /**
     * @param context    in the form of /foo
     * @param parent     in the form of /foo/bar/ or /foo/bar/dar.jss
     * @param scriptPath in the form of /foo/bar/mar.jss or bar/mar.jss
     * @return String[] with keys
     */
    public static String[] getKeys(String context, String parent, String scriptPath) {
        String path;
        String normalizedScriptPath;
        context = context.equals("") ? "/" : context;
        normalizedScriptPath = scriptPath.startsWith("/") ?
                FilenameUtils.normalize(scriptPath, true) :
                FilenameUtils.normalize(FilenameUtils.getFullPath(parent) + scriptPath, true);
        path = FilenameUtils.getFullPath(normalizedScriptPath);
        //remove trailing "/"
        path = path.equals("/") ? path : path.substring(0, path.length() - 1);
        normalizedScriptPath = "/" + FilenameUtils.getName(normalizedScriptPath);
        return new String[]{
                context,
                path,
                normalizedScriptPath
        };
    }

    public static long getScriptLastModified(ServletContext context, String scriptPath) throws ScriptException {
        long result = -1;
        URLConnection uc = null;
        try {
            URL scriptUrl = context.getResource(canonicalURI(scriptPath));
            if (scriptUrl == null) {
                String msg = "Requested resource " + scriptPath + " cannot be found";
                log.error(msg);
                throw new ScriptException(msg);
            }
            uc = scriptUrl.openConnection();
            if (uc instanceof JarURLConnection) {
                result = ((JarURLConnection) uc).getJarEntry().getTime();
            } else {
                result = uc.getLastModified();
            }
        } catch (IOException e) {
            log.warn("Error getting last modified time for " + scriptPath, e);
            result = -1;
        } finally {
            if (uc != null) {
                try {
                    uc.getInputStream().close();
                } catch (IOException e) {
                    log.error("Error closing input stream for script " + scriptPath, e);
                }
            }
        }
        return result;
    }

    private static String canonicalURI(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        final int len = s.length();
        int pos = 0;
        while (pos < len) {
            char c = s.charAt(pos);
            if (isPathSeparator(c)) {
                /*
                * multiple path separators.
                * 'foo///bar' -> 'foo/bar'
                */
                while (pos + 1 < len && isPathSeparator(s.charAt(pos + 1))) {
                    ++pos;
                }

                if (pos + 1 < len && s.charAt(pos + 1) == '.') {
                    /*
                    * a single dot at the end of the path - we are done.
                    */
                    if (pos + 2 >= len) {
                        break;
                    }

                    switch (s.charAt(pos + 2)) {
                        /*
                        * self directory in path
                        * foo/./bar -> foo/bar
                        */
                        case '/':
                        case '\\':
                            pos += 2;
                            continue;

                            /*
                            * two dots in a path: go back one hierarchy.
                            * foo/bar/../baz -> foo/baz
                            */
                        case '.':
                            // only if we have exactly _two_ dots.
                            if (pos + 3 < len && isPathSeparator(s.charAt(pos + 3))) {
                                pos += 3;
                                int separatorPos = result.length() - 1;
                                while (separatorPos >= 0 &&
                                        !isPathSeparator(result
                                                .charAt(separatorPos))) {
                                    --separatorPos;
                                }
                                if (separatorPos >= 0) {
                                    result.setLength(separatorPos);
                                }
                                continue;
                            }
                    }
                }
            }
            result.append(c);
            ++pos;
        }
        return result.toString();
    }

    private static boolean isPathSeparator(char c) {
        return (c == '/' || c == '\\');
    }

    public static WsMessageInBound getWsMessageInBound() {
        return wsMessageInBound;
    }

    private static void setWsMessageInBound(WsMessageInBound wsMsgInBnd) {
        wsMessageInBound = wsMsgInBnd;
    }



}
