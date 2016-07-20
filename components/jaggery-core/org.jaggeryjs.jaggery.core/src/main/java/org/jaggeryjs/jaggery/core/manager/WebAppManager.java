package org.jaggeryjs.jaggery.core.manager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.jaggeryjs.hostobjects.log.LogHostObject;
import org.jaggeryjs.hostobjects.web.Constants;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.jaggery.core.plugins.WebAppFileManager;
import org.jaggeryjs.scriptengine.EngineConstants;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.JavaScriptProperty;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.engine.RhinoTopLevel;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.security.RhinoSecurityController;
import org.jaggeryjs.scriptengine.security.RhinoSecurityDomain;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mozilla.javascript.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.PermissionCollection;
import java.util.*;

public class WebAppManager {

    private static final Log log = LogFactory.getLog(WebAppManager.class);

    public static final String CORE_MODULE_NAME = "core";

    public static final String SERVLET_RESPONSE = "webappmanager.servlet.response";

    public static final String SERVLET_REQUEST = "webappmanager.servlet.request";

    private static final String DEFAULT_CONTENT_TYPE = "text/html";

    private static final String DEFAULT_CHAR_ENCODING = "UTF-8";

    public static final String JAGGERY_MODULES_DIR = "modules";

    public static final String WS_REQUEST_PATH = "requestURI";

    public static final String WS_SERVLET_CONTEXT = "/websocket";

    private static final String SHARED_JAGGERY_CONTEXT = "shared.jaggery.context";

    private static final String SERVE_FUNCTION_JAGGERY = "org.jaggeryjs.serveFunction";

    private static final Map<String, List<String>> timeouts = new HashMap<String, List<String>>();

    private static final Map<String, List<String>> intervals = new HashMap<String, List<String>>();

    private static final Map<String, Object> locks = new HashMap<String, Object>();

    private static boolean isWebSocket = false;

    private static final Object modRefreshLock = new Object();

    private static final String ATTRIBUTE_PREFIX = "org.jaggeryjs.";

    private static final String CARBON_HOME = "carbon.home";

    private static final String JAGGERY_HOME = "jaggery.home";

    private static final String CATALINA_HOME = "catalina.base";

    private static final String JAGGERY = "jaggery";

    static {
        try {

            String jaggeryDir = System.getProperty(JAGGERY_HOME);
            if (System.getProperty(CATALINA_HOME) != null && jaggeryDir == null) {
                jaggeryDir = System.getProperty(CATALINA_HOME) + File.pathSeparator + JAGGERY;
            }

            if (jaggeryDir == null) {
                jaggeryDir = System.getProperty(CARBON_HOME);
            }

            if (jaggeryDir == null) {
                log.error("Unable to find jaggery.home or carbon.home system properties");
                throw new RuntimeException("Unable to find jaggery.home or carbon.home system properties");
            }

            String modulesDir = jaggeryDir + File.separator + JAGGERY_MODULES_DIR;

            CommonManager.getInstance().initialize(modulesDir, new RhinoSecurityController() {
                @Override protected void updatePermissions(PermissionCollection permissions,
                        RhinoSecurityDomain securityDomain) throws ScriptException {
                    JaggeryContext context = CommonManager.getJaggeryContext();
                    ServletContext servletContext = (ServletContext) context.getProperty(Constants.SERVLET_CONTEXT);
                    String docBase = servletContext.getRealPath("/");
                    // Create a file read permission for web app context directory
                    if(docBase == null){
                        try {
                            docBase = servletContext.getResource("").getPath();
                        } catch (MalformedURLException e) {
                            log.error("Error in getting the real path of the webapp", e);
                        }
                    }
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

    public static void include(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "include";
        int argsCount = args.length;
        if (argsCount != 1 && argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(CommonManager.HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil
                    .invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }

        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        Stack<String> includesCallstack = CommonManager.getCallstack(jaggeryContext);
        String parent = includesCallstack.lastElement();
        String fileURL = (String) args[0];

        if (CommonManager.isHTTP(fileURL) || CommonManager.isHTTP(parent)) {
            CommonManager.include(cx, thisObj, args, funObj);
            return;
        }
        if (argsCount == 2 && !(args[1] instanceof ScriptableObject)) {
            HostObjectUtil
                    .invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "2", "Object", args[1], false);
        }
        ScriptableObject scope;
        if (argsCount == 2) {
            scope = (ScriptableObject) args[1];
        } else {
            scope = jaggeryContext.getScope();
        }
        executeScript(jaggeryContext, scope, fileURL, false, false, false);
    }

    public static void include_once(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "include_once";
        int argsCount = args.length;
        if (argsCount != 1 && argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(CommonManager.HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil
                    .invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }

        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        Stack<String> includesCallstack = CommonManager.getCallstack(jaggeryContext);
        String parent = includesCallstack.lastElement();
        String fileURL = (String) args[0];

        if (CommonManager.isHTTP(fileURL) || CommonManager.isHTTP(parent)) {
            CommonManager.include_once(cx, thisObj, args, funObj);
            return;
        }
        if (argsCount == 2 && !(args[1] instanceof ScriptableObject)) {
            HostObjectUtil
                    .invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "2", "Object", args[1], false);
        }
        ScriptableObject scope;
        if (argsCount == 2) {
            scope = (ScriptableObject) args[1];
        } else {
            scope = jaggeryContext.getScope();
        }
        executeScript(jaggeryContext, scope, fileURL, false, false, true);
    }

    /**
     * JaggeryMethod responsible of writing to the output stream
     */
    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        if (!isWebSocket) {
            JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();

            //If the script itself havent set the content type we set the default content type to be text/html
            HttpServletResponse servletResponse = (HttpServletResponse) jaggeryContext.getProperty(SERVLET_RESPONSE);

            //servletResponse can be null if the script is executed as a initScripts, destroyScripts or
            //a sessionDestroyedListeners
            if (servletResponse != null) {
                if (servletResponse.getContentType() == null) {
                    servletResponse.setContentType(DEFAULT_CONTENT_TYPE);
                }

                if (servletResponse.getCharacterEncoding() == null) {
                    servletResponse.setCharacterEncoding(DEFAULT_CHAR_ENCODING);
                }

                CommonManager.print(cx, thisObj, args, funObj);
            }
        }
    }

    public static String setTimeout(final Context cx, final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        JaggeryContext context = CommonManager.getJaggeryContext();
        ServletContext servletContext = (ServletContext) context.getProperty(Constants.SERVLET_CONTEXT);
        String contextPath = servletContext.getContextPath();
        String taskId = RhinoTopLevel.setTimeout(cx, thisObj, args, funObj);
        List<String> taskIds = timeouts.get(contextPath);
        if (taskIds == null) {
            taskIds = new ArrayList<String>();
            timeouts.put(contextPath, taskIds);
        }
        taskIds.add(taskId);
        return taskId;
    }

    public static String setInterval(final Context cx, final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        JaggeryContext context = CommonManager.getJaggeryContext();
        ServletContext servletContext = (ServletContext) context.getProperty(Constants.SERVLET_CONTEXT);
        String contextPath = servletContext.getContextPath();
        String taskId = RhinoTopLevel.setInterval(cx, thisObj, args, funObj);
        List<String> taskIds = intervals.get(contextPath);
        if (taskIds == null) {
            taskIds = new ArrayList<String>();
            intervals.put(contextPath, taskIds);
        }
        taskIds.add(taskId);
        return taskId;
    }

    public static void clearTimeout(final Context cx, final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        JaggeryContext context = CommonManager.getJaggeryContext();
        ServletContext servletContext = (ServletContext) context.getProperty(Constants.SERVLET_CONTEXT);
        String contextPath = servletContext.getContextPath();
        RhinoTopLevel.clearTimeout(cx, thisObj, args, funObj);
        List<String> taskIds = timeouts.get(contextPath);
        taskIds.remove(String.valueOf(args[0]));
    }

    public static void clearInterval(final Context cx, final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        JaggeryContext context = CommonManager.getJaggeryContext();
        ServletContext servletContext = (ServletContext) context.getProperty(Constants.SERVLET_CONTEXT);
        String contextPath = servletContext.getContextPath();
        RhinoTopLevel.clearTimeout(cx, thisObj, args, funObj);
        List<String> taskIds = intervals.get(contextPath);
        taskIds.remove(String.valueOf(args[0]));
    }

    private static ScriptableObject executeScript(JaggeryContext jaggeryContext, ScriptableObject scope, String fileURL,
            final boolean isJSON, boolean isBuilt, boolean isIncludeOnce) throws ScriptException {
        Stack<String> includesCallstack = CommonManager.getCallstack(jaggeryContext);
        Map<String, Boolean> includedScripts = CommonManager.getIncludes(jaggeryContext);
        ServletContext context = (ServletContext) jaggeryContext.getProperty(Constants.SERVLET_CONTEXT);
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
                @Override protected void build() throws IOException {
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

        ScriptCachingContext sctx = new ScriptCachingContext(jaggeryContext.getTenantDomain(), keys[0], keys[1],
                keys[2]);
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
            HostObjectUtil
                    .invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }

        String moduleId = (String) args[0];
        int dotIndex = moduleId.lastIndexOf(".");
        if (moduleId.length() == dotIndex + 1) {
            String msg = "Invalid file path for require method : " + moduleId;
            log.error(msg);
            throw new ScriptException(msg);
        }

        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        Map<String, ScriptableObject> requiredModules = (Map<String, ScriptableObject>) jaggeryContext
                .getProperty(Constants.JAGGERY_REQUIRED_MODULES);
        ScriptableObject object = requiredModules.get(moduleId);
        if (object != null) {
            return object;
        }

        if (dotIndex == -1) {
            object = CommonManager.require(cx, thisObj, args, funObj);
            initModule(cx, jaggeryContext, moduleId, object);
        } else {
            object = (ScriptableObject) cx.newObject(thisObj);
            object.setPrototype(thisObj);
            object.setParentScope(thisObj);
            //sharedJaggeryContext((ServletContext) jaggeryContext.getProperty(Constants.SERVLET_CONTEXT)).getScope()
            //object.setParentScope(sharedJaggeryContext((ServletContext) jaggeryContext.getProperty(Constants.SERVLET_CONTEXT)).getScope());
            String ext = moduleId.substring(dotIndex + 1);
            if (ext.equalsIgnoreCase("json")) {
                object = executeScript(jaggeryContext, object, moduleId, true, true, false);
            } else if (ext.equalsIgnoreCase("js")) {
                object = executeScript(jaggeryContext, object, moduleId, false, true, false);
            } else if (ext.equalsIgnoreCase("jag")) {
                object = executeScript(jaggeryContext, object, moduleId, false, false, false);
            } else {
                String msg = "Unsupported file type for require() method : ." + ext;
                log.error(msg);
                throw new ScriptException(msg);
            }
        }
        requiredModules.put(moduleId, object);
        return object;
    }

    /**
     * The sync function creates a synchronized function (in the sense
     * of a Java synchronized method) from an existing function. The
     * new function synchronizes on the the second argument if it is
     * defined, or otherwise the <code>this</code>
     */
    public static Object sync(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "sync";
        int argsCount = args.length;
        if (argsCount <= 1 && argsCount >= 2) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof Function)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "1",
                    "function", args[0], false);
        }
        Object lock = null;
        if (argsCount == 2) {
            if (args[1] == Undefined.instance) {
                HostObjectUtil
                        .invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "1",
                                "object", args[0], false);
            }
            if (args[0] instanceof String) {
                String key = (String) args[0];
                lock = locks.get(key);
                if (lock == null) {
                    lock = new Object();
                    locks.put(key, lock);
                }
            } else {
                lock = args[1];
            }
        }
        return new Synchronizer((Function) args[0], lock);
    }

    public static void initModule(Context cx, JaggeryContext context, String module, ScriptableObject object) {
        if (CORE_MODULE_NAME.equals(module)) {
            defineProperties(cx, context, object);
        }
    }

    public static JaggeryContext sharedJaggeryContext(ServletContext ctx) {
        return (JaggeryContext) ctx.getAttribute(SHARED_JAGGERY_CONTEXT);
    }

    public static JaggeryContext clonedJaggeryContext(ServletContext context) {
        JaggeryContext shared = sharedJaggeryContext(context);
        RhinoEngine engine = shared.getEngine();
        Scriptable sharedScope = shared.getScope();
        Context cx = Context.getCurrentContext();
        ScriptableObject instanceScope = (ScriptableObject) cx.newObject(sharedScope);
        instanceScope.setPrototype(sharedScope);
        instanceScope.setParentScope(null);

        JaggeryContext clone = new JaggeryContext();
        clone.setEngine(engine);
        clone.setTenantDomain(shared.getTenantDomain());
        clone.setScope(instanceScope);

        clone.addProperty(Constants.SERVLET_CONTEXT, shared.getProperty(Constants.SERVLET_CONTEXT));
        clone.addProperty(LogHostObject.LOG_LEVEL, shared.getProperty(LogHostObject.LOG_LEVEL));
        clone.addProperty(FileHostObject.JAVASCRIPT_FILE_MANAGER,
                shared.getProperty(FileHostObject.JAVASCRIPT_FILE_MANAGER));
        clone.addProperty(Constants.JAGGERY_CORE_MANAGER, shared.getProperty(Constants.JAGGERY_CORE_MANAGER));
        clone.addProperty(Constants.JAGGERY_INCLUDED_SCRIPTS, new HashMap<String, Boolean>());
        clone.addProperty(Constants.JAGGERY_INCLUDES_CALLSTACK, new Stack<String>());
        clone.addProperty(Constants.JAGGERY_REQUIRED_MODULES, new HashMap<String, ScriptableObject>());

        return clone;
    }

    public static void deploy(org.apache.catalina.Context context) throws ScriptException {
        ServletContext ctx = context.getServletContext();
        JaggeryContext sharedContext = new JaggeryContext();
        Context cx = Context.getCurrentContext();
        CommonManager.initContext(sharedContext);

        sharedContext.addProperty(Constants.SERVLET_CONTEXT, ctx);
        sharedContext.addProperty(FileHostObject.JAVASCRIPT_FILE_MANAGER, new WebAppFileManager(ctx));
        sharedContext.addProperty(Constants.JAGGERY_REQUIRED_MODULES, new HashMap<String, ScriptableObject>());
        String logLevel = (String) ctx.getAttribute(LogHostObject.LOG_LEVEL);
        if (logLevel != null) {
            sharedContext.addProperty(LogHostObject.LOG_LEVEL, logLevel);
        }
        ScriptableObject sharedScope = sharedContext.getScope();
        JavaScriptProperty application = new JavaScriptProperty("application");
        application.setValue(cx.newObject(sharedScope, "Application", new Object[] { ctx }));
        application.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(sharedScope, application);
        ctx.setAttribute(SHARED_JAGGERY_CONTEXT, sharedContext);
    }

    public static void undeploy(org.apache.catalina.Context context) {
        String contextPath = context.getServletContext().getContextPath();
        List<String> taskIds = timeouts.get(contextPath);
        if (taskIds != null) {
            for (String taskId : taskIds) {
                try {
                    log.debug("clearTimeout : " + taskId);
                    RhinoTopLevel.clearTimeout(taskId);
                } catch (ScriptException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        taskIds = intervals.get(contextPath);
        if (taskIds != null) {
            for (String taskId : taskIds) {
                try {
                    log.debug("clearInterval : " + taskId);
                    RhinoTopLevel.clearInterval(taskId);
                } catch (ScriptException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        log.debug("Releasing resources of : " + context.getServletContext().getContextPath());
    }

    private static void refreshServletContext(ServletContext context) {
        Enumeration<String> names = context.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!name.startsWith(ATTRIBUTE_PREFIX)) {
                continue;
            }
            context.removeAttribute(name);
        }
    }

    public static void execute(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (!ModuleManager.isModuleRefreshEnabled()) {
            exec(request, response);
            return;
        }
        synchronized (modRefreshLock) {
            exec(request, response);
        }
    }

    private static void exec(HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream sourceIn;
        Context cx;
        JaggeryContext context;
        RhinoEngine engine = null;
        ServletContext servletContext = request.getServletContext();

        try {
            engine = CommonManager.getInstance().getEngine();
            cx = engine.enterContext();
            String scriptPath = getScriptPath(request);
            OutputStream out = response.getOutputStream();
            context = createJaggeryContext(cx, out, scriptPath, request, response);
            context.addProperty(FileHostObject.JAVASCRIPT_FILE_MANAGER,
                    new WebAppFileManager(request.getServletContext()));

            if (ModuleManager.isModuleRefreshEnabled()) {
                //reload init scripts
                refreshServletContext(servletContext);
                InputStream jaggeryConf = servletContext.getResourceAsStream(JaggeryCoreConstants.JAGGERY_CONF_FILE);
                if (jaggeryConf != null) {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(jaggeryConf, writer, null);
                    String jsonString = writer.toString();
                    JSONObject conf = (JSONObject) JSONValue.parse(jsonString);
                    JSONArray initScripts = (JSONArray) conf.get(JaggeryCoreConstants.JaggeryConfigParams.INIT_SCRIPTS);
                    if (initScripts != null) {
                        JaggeryContext sharedContext = WebAppManager.sharedJaggeryContext(servletContext);
                        ScriptableObject sharedScope = sharedContext.getScope();

                        Object[] scripts = initScripts.toArray();
                        for (Object script : scripts) {
                            if (!(script instanceof String)) {
                                log.error("Invalid value for initScripts in jaggery.conf : " + script);
                                continue;
                            }
                            String path = (String) script;
                            path = path.startsWith("/") ? path : "/" + path;
                            Stack<String> callstack = CommonManager.getCallstack(sharedContext);
                            callstack.push(path);
                            engine.exec(new ScriptReader(servletContext.getResourceAsStream(path)) {
                                @Override protected void build() throws IOException {
                                    try {
                                        sourceReader = new StringReader(HostObjectUtil.streamToString(sourceIn));
                                    } catch (ScriptException e) {
                                        throw new IOException(e);
                                    }
                                }
                            }, sharedScope, null);
                            callstack.pop();
                        }
                    }
                }
            }

            Object serveFunction = request.getServletContext().getAttribute(SERVE_FUNCTION_JAGGERY);
            if (serveFunction != null) {
                Function function = (Function) serveFunction;
                ScriptableObject scope = context.getScope();
                function.call(cx, scope, scope,
                        new Object[] { scope.get("request", scope), scope.get("response", scope),
                                scope.get("session", scope) });
            } else {
                //resource rendering model proceeding
                sourceIn = request.getServletContext().getResourceAsStream(scriptPath);
                if (sourceIn == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
                    return;
                }
                CommonManager.getInstance().getEngine().exec(new ScriptReader(sourceIn), context.getScope(),
                        getScriptCachingContext(request, scriptPath));
            }
        } catch (ScriptException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        } catch (Error e) {
            //Rhino doesn't catch Error instances and it is been used to stop the script execution
            //from any specific place. Hence, Error exception propagates up to Java and we silently
            //ignore it, assuming it was initiated by an exit() method call.
            log.debug("Script has called exit() method", e);
        } finally {
            // Exiting from the context
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
            path = resolveScriptPath(new ArrayList<String>(Arrays.asList(url.substring(1).split("/", -1))),
                    urlMappings);
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
            String path = (String) obj;
            if (path != null && path.endsWith("*")) {
                return path.substring(0, path.length() - 1) + part;
            } else {
                return path;
            }
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
        request.setValue(cx.newObject(scope, "Request", new Object[] { servletRequest }));
        request.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(scope, request);

        JavaScriptProperty response = new JavaScriptProperty("response");
        HttpServletResponse servletResponse = (HttpServletResponse) context.getProperty(SERVLET_RESPONSE);
        response.setValue(cx.newObject(scope, "Response", new Object[] { servletResponse }));
        response.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(scope, response);

        JavaScriptProperty session = new JavaScriptProperty("session");
        session.setValue(cx.newObject(scope, "Session", new Object[] { servletRequest }));
        RhinoEngine.defineProperty(scope, session);

        JavaScriptProperty application = new JavaScriptProperty("application");
        ServletContext servletConext = (ServletContext) context.getProperty(Constants.SERVLET_CONTEXT);
        application.setValue(cx.newObject(scope, "Application", new Object[] { servletConext }));
        application.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(scope, application);

        if (isWebSocket(servletRequest)) {
            JavaScriptProperty websocket = new JavaScriptProperty("webSocket");
            websocket.setValue(cx.newObject(scope, "WebSocket", new Object[0]));
            websocket.setAttribute(ScriptableObject.READONLY);
            RhinoEngine.defineProperty(scope, websocket);
        }

    }

    private static JaggeryContext createJaggeryContext(Context cx, OutputStream out, String scriptPath,
            HttpServletRequest request, HttpServletResponse response) {
        ServletContext servletContext = request.getServletContext();
        JaggeryContext context = clonedJaggeryContext(servletContext);
        CommonManager.setJaggeryContext(context);
        context.addProperty(SERVLET_REQUEST, request);
        context.addProperty(SERVLET_RESPONSE, response);
        CommonManager.getCallstack(context).push(scriptPath);
        CommonManager.getIncludes(context).put(scriptPath, true);
        context.addProperty(CommonManager.JAGGERY_OUTPUT_STREAM, out);
        defineProperties(cx, context, context.getScope());
        return context;
    }

    protected static ScriptCachingContext getScriptCachingContext(HttpServletRequest request, String scriptPath)
            throws ScriptException {
        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        String tenantId = jaggeryContext.getTenantDomain();
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
        return new String[] { context, path, normalizedScriptPath };
    }

    @SuppressFBWarnings({"CRLF_INJECTION_LOGS", "CRLF_INJECTION_LOGS", "CRLF_INJECTION_LOGS"})
    private static long getScriptLastModified(ServletContext context, String scriptPath) throws ScriptException {
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
                            while (separatorPos >= 0 && !isPathSeparator(result.charAt(separatorPos))) {
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
        isWebSocket = "websocket".equalsIgnoreCase(((HttpServletRequest) request).getHeader("Upgrade"));
        return isWebSocket;
    }

    public static boolean isCarbonServer() {
        if (System.getProperty(CARBON_HOME) != null) {
            return true;
        } else {
            return false;
        }
    }
}
