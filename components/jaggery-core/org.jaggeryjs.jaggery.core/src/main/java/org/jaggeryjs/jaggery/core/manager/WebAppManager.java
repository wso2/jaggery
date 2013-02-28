package org.jaggeryjs.jaggery.core.manager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.jaggeryjs.hostobjects.log.LogHostObject;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.jaggery.core.plugins.WebAppFileManager;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.JavaScriptProperty;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.security.RhinoSecurityController;
import org.jaggeryjs.scriptengine.security.RhinoSecurityDomain;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.context.CarbonContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.PermissionCollection;
import java.util.*;

public class WebAppManager {

    private static final Log log = LogFactory.getLog(WebAppManager.class);

    public static final String CORE_MODULE_NAME = "core";

    public static final String SERVLET_RESPONSE = "webappmanager.servlet.response";

    public static final String SERVLET_REQUEST = "webappmanager.servlet.request";

    public static final String SERVLET_CONTEXT = "webappmanager.servlet.context";

    private static final String DEFAULT_CONTENT_TYPE = "text/html";

    private static final String DEFAULT_CHAR_ENCODING = "UTF-8";

    public static final String JAGGERY_MODULES_DIR = "modules";

    public static final String WS_REQUEST_PATH = "requestURI";

    public static final String WS_SERVLET_CONTEXT = "/websocket";

    private static boolean isWebSocket = false;

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

    public static RhinoEngine getEngine() throws ScriptException {
        return CommonManager.getInstance().getEngine();
    }

    public static void include(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "include";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(CommonManager.HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }

        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        Stack<String> includesCallstack = CommonManager.getCallstack(jaggeryContext);
        String parent = includesCallstack.lastElement();
        String fileURL = (String) args[0];

        if (CommonManager.isHTTP(fileURL) || CommonManager.isHTTP(parent)) {
            CommonManager.include(cx, thisObj, args, funObj);
            return;
        }
        executeScript(jaggeryContext, jaggeryContext.getScope(), fileURL, false, false, false);
    }

    public static void include_once(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "include_once";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(CommonManager.HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }

        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        Stack<String> includesCallstack = CommonManager.getCallstack(jaggeryContext);
        String parent = includesCallstack.lastElement();
        String fileURL = (String) args[0];

        if (CommonManager.isHTTP(fileURL) || CommonManager.isHTTP(parent)) {
            CommonManager.include_once(cx, thisObj, args, funObj);
            return;
        }
        executeScript(jaggeryContext, jaggeryContext.getScope(), fileURL, false, false, true);
    }

    /**
     * JaggeryMethod responsible of writing to the output stream
     */
    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        if (!isWebSocket) {
            JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();

            //If the script itself havent set the content type we set the default content type to be text/html
            HttpServletResponse servletResponse = (HttpServletResponse) jaggeryContext.getProperty(SERVLET_RESPONSE);
            if (servletResponse.getContentType() == null) {
                servletResponse.setContentType(DEFAULT_CONTENT_TYPE);
            }

            if (servletResponse.getCharacterEncoding() == null) {
                servletResponse.setCharacterEncoding(DEFAULT_CHAR_ENCODING);
            }

            CommonManager.print(cx, thisObj, args, funObj);
        }
    }

    private static ScriptableObject executeScript(JaggeryContext jaggeryContext, ScriptableObject scope,
                                                  String fileURL, final boolean isJSON, boolean isBuilt,
                                                  boolean isIncludeOnce) throws ScriptException {
        Stack<String> includesCallstack = CommonManager.getCallstack(jaggeryContext);
        Map<String, Boolean> includedScripts = CommonManager.getIncludes(jaggeryContext);
        ServletContext context = (ServletContext) jaggeryContext.getProperty(SERVLET_CONTEXT);
        String parent = includesCallstack.lastElement();

        String keys[] = WebAppManager.getKeys(context.getContextPath(), parent, fileURL);
        fileURL = getNormalizedScriptPath(keys);
        if (includesCallstack.search(fileURL) != -1) {
            return scope;
        }
        if (isIncludeOnce && includedScripts.get(fileURL) != null) {
            return scope;
        }

        ScriptReader source;
        RhinoEngine engine = jaggeryContext.getEngine();
        if (isBuilt) {
            source = new ScriptReader(context.getResourceAsStream(fileURL)) {
                @Override
                protected void build() throws IOException {
                    try {
                        if (isJSON) {
                            sourceReader = new StringReader("(" + HostObjectUtil.streamToString(sourceIn) + ")");
                        } else {
                            sourceReader = new StringReader(HostObjectUtil.streamToString(sourceIn));
                        }
                    } catch (ScriptException e) {
                        throw new IOException(e);
                    }
                }
            };
        } else {
            source = new ScriptReader(context.getResourceAsStream(fileURL));
        }

        ScriptCachingContext sctx = new ScriptCachingContext(jaggeryContext.getTenantId(), keys[0], keys[1], keys[2]);
        sctx.setSecurityDomain(new JaggerySecurityDomain(fileURL, context));
        long lastModified = WebAppManager.getScriptLastModified(context, fileURL);
        sctx.setSourceModifiedTime(lastModified);

        includedScripts.put(fileURL, true);
        includesCallstack.push(fileURL);
        if (isJSON) {
            scope = (ScriptableObject) engine.eval(source, scope, sctx);
        } else {
            engine.exec(source, scope, sctx);
        }
        includesCallstack.pop();
        return scope;
    }

    private static String getNormalizedScriptPath(String[] keys) {
        return "/".equals(keys[1]) ? keys[2] : keys[1] + keys[2];
    }

    public static ScriptableObject require(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, IOException {
        String functionName = "require";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(CommonManager.HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }

        String param = (String) args[0];
        int dotIndex = param.lastIndexOf(".");
        if (param.length() == dotIndex + 1) {
            String msg = "Invalid file path for require method : " + param;
            log.error(msg);
            throw new ScriptException(msg);
        }

        if (dotIndex == -1) {
            ScriptableObject object = CommonManager.require(cx, thisObj, args, funObj);
            initModule(cx, CommonManager.getJaggeryContext(), param, object);
            return object;
        }

        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        ScriptableObject object = (ScriptableObject) cx.newObject(thisObj);
        object.setPrototype(thisObj);
        object.setParentScope(null);
        String ext = param.substring(dotIndex + 1);
        if (ext.equalsIgnoreCase("json")) {
            return executeScript(jaggeryContext, object, param, true, true, false);
        } else if (ext.equalsIgnoreCase("js")) {
            return executeScript(jaggeryContext, object, param, false, true, false);
        } else if (ext.equalsIgnoreCase("jag")) {
            return executeScript(jaggeryContext, object, param, false, false, false);
        } else {
            String msg = "Unsupported file type for require() method : ." + ext;
            log.error(msg);
            throw new ScriptException(msg);
        }
    }

    public static void initContext(Context cx, JaggeryContext context) throws ScriptException {
        CommonManager.initContext(context);
        defineProperties(cx, context, context.getScope());
    }

    public static void initModule(Context cx, JaggeryContext context, String module, ScriptableObject object) {
        if (CORE_MODULE_NAME.equals(module)) {
            defineProperties(cx, context, object);
        }
    }

    public static void execute(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String scriptPath = getScriptPath(request);
        InputStream sourceIn = request.getServletContext().getResourceAsStream(scriptPath);
        if (sourceIn == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
            return;
        }
        RhinoEngine engine = null;
        try {
            engine = CommonManager.getInstance().getEngine();
            Context cx = engine.enterContext();
            //Creating an OutputStreamWritter to write content to the servletResponse
            OutputStream out = response.getOutputStream();
            JaggeryContext context = createJaggeryContext(out, scriptPath, request, response);
            initContext(cx, context);
            context.addProperty(FileHostObject.JAVASCRIPT_FILE_MANAGER,
                    new WebAppFileManager(request.getServletContext()));
            CommonManager.getInstance().getEngine().exec(new ScriptReader(sourceIn), context.getScope(),
                    getScriptCachingContext(request, scriptPath));
            //out.flush();
        } catch (ScriptException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        } finally {
            //Exiting from the context
            if (engine != null) {
                RhinoEngine.exitContext();
            }
        }
    }

    public static String getScriptPath(HttpServletRequest request) {
        String url = request.getServletPath();
        Map<String, Object> urlMappings = (Map<String, Object>) request.getServletContext()
                .getAttribute(CommonManager.JAGGERY_URLS_MAP);
        if (urlMappings == null) {
            return url;
        }
        String path;
        if (url.equals("/")) {
            path = getPath(urlMappings, url);
        } else {
            path = resolveScriptPath(new ArrayList<String>(Arrays.asList(url.substring(1).split("/"))), urlMappings);
        }
        return path == null ? url : path;
    }

    private static String resolveScriptPath(List<String> parts, Map<String, Object> map) {
        String part = parts.remove(0);
        if (parts.isEmpty()) {
            return getPath(map, part);
        }
        Object obj = map.get(part);
        if (obj == null) {
            return getPath(map, "/");
        }
        if (obj instanceof Map) {
            return resolveScriptPath(parts, (Map<String, Object>) obj);
        }
        return null;
    }

    private static String getPath(Map<String, Object> map, String part) {
        Object obj = "/".equals(part) ? null : map.get(part);
        if (obj == null) {
            obj = map.get("/");
            if (obj != null) {
                return (String) obj;
            }
            obj = map.get("*");
            return (String) obj;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        map = (Map<String, Object>) obj;
        obj = map.get("/");
        if (obj != null) {
            return (String) obj;
        }
        obj = map.get("*");
        return (String) obj;
    }

    private static void defineProperties(Context cx, JaggeryContext context, ScriptableObject scope) {

        JavaScriptProperty request = new JavaScriptProperty("request");
        HttpServletRequest servletRequest = (HttpServletRequest) context.getProperty(SERVLET_REQUEST);
        request.setValue(cx.newObject(scope, "Request", new Object[]{servletRequest}));
        request.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(scope, request);

        JavaScriptProperty response = new JavaScriptProperty("response");
        HttpServletResponse servletResponse = (HttpServletResponse) context.getProperty(SERVLET_RESPONSE);
        response.setValue(cx.newObject(scope, "Response", new Object[]{servletResponse}));
        response.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(scope, response);

        JavaScriptProperty session = new JavaScriptProperty("session");
        session.setValue(cx.newObject(scope, "Session", new Object[]{servletRequest.getSession()}));
        session.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(scope, session);

        JavaScriptProperty application = new JavaScriptProperty("application");
        ServletContext servletConext = (ServletContext) context.getProperty(SERVLET_CONTEXT);
        application.setValue(cx.newObject(scope, "Application", new Object[]{servletConext}));
        application.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(scope, application);

        if (isWebSocket(servletRequest)) {
            JavaScriptProperty websocket = new JavaScriptProperty("websocket");
            websocket.setValue(cx.newObject(scope, "WebSocket", new Object[0]));
            websocket.setAttribute(ScriptableObject.READONLY);
            RhinoEngine.defineProperty(scope, websocket);
        }

    }

    private static JaggeryContext createJaggeryContext(OutputStream out, String scriptPath,
                                                       HttpServletRequest request, HttpServletResponse response) {
        JaggeryContext context = new JaggeryContext();
        context.setTenantId(Integer.toString(CarbonContext.getCurrentContext().getTenantId()));
        context.addProperty(SERVLET_REQUEST, request);
        context.addProperty(SERVLET_RESPONSE, response);
        context.addProperty(SERVLET_CONTEXT, request.getServletContext());
        CommonManager.getCallstack(context).push(scriptPath);
        CommonManager.getIncludes(context).put(scriptPath, true);
        context.addProperty(CommonManager.JAGGERY_OUTPUT_STREAM, out);

        String logLevel = (String) request.getServletContext().getAttribute(LogHostObject.LOG_LEVEL);
        if (logLevel != null) {
            context.addProperty(LogHostObject.LOG_LEVEL, logLevel);
        }

        return context;
    }

    protected static ScriptCachingContext getScriptCachingContext(HttpServletRequest request, String scriptPath)
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

    public static boolean isWebSocket(ServletRequest request) {
        isWebSocket = "websocket".equals(((HttpServletRequest) request).getHeader("Upgrade"));
        return "websocket".equals(((HttpServletRequest) request).getHeader("Upgrade"));
    }
}
