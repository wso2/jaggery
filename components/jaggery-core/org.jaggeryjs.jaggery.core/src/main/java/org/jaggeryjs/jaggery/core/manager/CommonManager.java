package org.jaggeryjs.jaggery.core.manager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.stream.StreamHostObject;
import org.jaggeryjs.hostobjects.web.Constants;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.scriptengine.EngineConstants;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.engine.*;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.security.RhinoSecurityController;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Shares the common functionality, of initialization of functions, and Host Objects
 */
public class CommonManager {

    private static final int BYTE_BUFFER_SIZE = 1024;

    private static final Log log = LogFactory.getLog(CommonManager.class);

    public static final String JAGGERY_URLS_MAP = "jaggery.urls.map";
    public static final String JAGGERY_OUTPUT_STREAM = "jaggery.output.stream";

    public static final String HOST_OBJECT_NAME = "RhinoTopLevel";

    private static CommonManager manager;

    private RhinoEngine engine = null;
    private ModuleManager moduleManager = null;

    private CommonManager() throws ScriptException {
    }

    public static CommonManager getInstance() throws ScriptException {
        if (manager == null) {
            manager = new CommonManager();
        }
        return manager;
    }

    public RhinoEngine getEngine() {
        return this.engine;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public void initialize(String modulesDir, RhinoSecurityController securityController) throws ScriptException {
        this.engine = new RhinoEngine(new CacheManager(null), new RhinoContextFactory(securityController));
        this.moduleManager = new ModuleManager(modulesDir);
        exposeDefaultModules(this.engine, this.moduleManager.getModules());
    }

    public static void initContext(JaggeryContext context) throws ScriptException {
        context.setEngine(manager.engine);
        context.setScope(manager.engine.getRuntimeScope());
        if (WebAppManager.isCarbonServer()) {
            context.setTenantDomain(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true));
        }

        context.addProperty(Constants.JAGGERY_CORE_MANAGER, manager);
        context.addProperty(Constants.JAGGERY_INCLUDED_SCRIPTS, new HashMap<String, Boolean>());
        context.addProperty(Constants.JAGGERY_INCLUDES_CALLSTACK, new Stack<String>());
    }

    private static void exposeDefaultModules(RhinoEngine engine, Map<String, JavaScriptModule> modules)
            throws ScriptException {
        for (JavaScriptModule module : modules.values()) {
            if (module.isExpose()) {
                String namespace = module.getNamespace();
                if (namespace == null || namespace.equals("")) {
                    //expose globally
                    exposeModule(engine, module);
                } else {
                    engine.defineModule(module);
                }
            }
        }

    }

    public static void include(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "include";
        int argsCount = args.length;
        if (argsCount != 1 && argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        if (argsCount == 2 && !(args[1] instanceof ScriptableObject)) {
            HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME, functionName, "2", "Object", args[1], false);
        }
        JaggeryContext jaggeryContext = getJaggeryContext();
        RhinoEngine engine = jaggeryContext.getEngine();
        if (engine == null) {
            log.error("Rhino Engine in Jaggery context is null");
            throw new ScriptException("Rhino Engine in Jaggery context is null");
        }
        Stack<String> includesCallstack = getCallstack(jaggeryContext);
        Map<String, Boolean> includedScripts = getIncludes(jaggeryContext);
        String parent = includesCallstack.lastElement();
        String fileURL = (String) args[0];
        if (isHTTP(fileURL) || isHTTP(parent)) {
            if (!isHTTP(fileURL)) {
                fileURL = parent + fileURL;
            }
            if (includesCallstack.search(fileURL) != -1) {
                return;
            }
            ScriptReader source;
            ScriptableObject scope;
            if (argsCount == 2) {
                scope = (ScriptableObject) args[1];
            } else {
                scope = jaggeryContext.getScope();
            }
            //this is a remote file url
            try {
                URL url = new URL(fileURL);
                url.openConnection();
                source = new ScriptReader(url.openStream());
                includedScripts.put(fileURL, true);
                includesCallstack.push(fileURL);
                engine.exec(source, scope, null);
                includesCallstack.pop();
            } catch (MalformedURLException e) {
                String msg = "Malformed URL. function : import, url : " + fileURL;
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            } catch (IOException e) {
                String msg = "IO exception while importing content from url : " + fileURL;
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            }
        } else {
            String msg = "Unsupported file include : " + fileURL;
            throw new ScriptException(msg);
        }
    }

    public static void include_once(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "include_once";
        int argsCount = args.length;
        if (argsCount != 1 && argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        if (argsCount == 2 && !(args[1] instanceof ScriptableObject)) {
            HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME, functionName, "2", "Object", args[1], false);
        }
        JaggeryContext jaggeryContext = getJaggeryContext();
        RhinoEngine engine = jaggeryContext.getEngine();
        if (engine == null) {
            log.error("Rhino Engine in Jaggery context is null");
            throw new ScriptException("Rhino Engine in Jaggery context is null");
        }

        Stack<String> includesCallstack = getCallstack(jaggeryContext);
        String parent = includesCallstack.lastElement();
        String fileURL = (String) args[0];
        if (isHTTP(fileURL) || isHTTP(parent)) {
            if (!isHTTP(fileURL)) {
                fileURL = parent + fileURL;
            }
            if (includesCallstack.search(fileURL) != -1) {
                return;
            }
            Map<String, Boolean> includedScripts = getIncludes(jaggeryContext);
            if (includedScripts.get(fileURL)) {
                return;
            }

            ScriptReader source;
            ScriptableObject scope;
            if (argsCount == 2) {
                scope = (ScriptableObject) args[1];
            } else {
                scope = jaggeryContext.getScope();
            }
            //this is a remote file url
            try {
                URL url = new URL(fileURL);
                url.openConnection();
                source = new ScriptReader(url.openStream());
                includedScripts.put(fileURL, true);
                includesCallstack.push(fileURL);
                engine.exec(source, scope, null);
                includesCallstack.pop();
            } catch (MalformedURLException e) {
                String msg = "Malformed URL. function : import, url : " + fileURL;
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            } catch (IOException e) {
                String msg = "IO exception while importing content from url : " + fileURL;
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            }
        } else {
            String msg = "Unsupported file include : " + fileURL;
            throw new ScriptException(msg);
        }
    }

    public static boolean isHTTP(String url) {
        return url.matches("^[hH][tT][tT][pP][sS]?.*");
    }

    public static ScriptableObject require(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, IOException {
        String functionName = "require";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        String moduleName = (String) args[0];
        JaggeryContext context = getJaggeryContext();
        //RhinoEngine engine = context.getEngine();
        //ScriptableObject scope = context.getScope();
        CommonManager manager = (CommonManager) context.getProperty(Constants.JAGGERY_CORE_MANAGER);
        ModuleManager moduleManager = manager.getModuleManager();
        JavaScriptModule module = moduleManager.getModule(moduleName);

        if (module == null) {
            String msg = "A module cannot be found with the specified name : " + moduleName;
            log.error(msg);
            throw new ScriptException(msg);
        }
        ScriptableObject object = (ScriptableObject) cx.newObject(thisObj);
        object.setPrototype(thisObj);
        object.setParentScope(thisObj);
        exposeModule(cx, object, module);
        return object;
    }

    private static void exposeModule(Context cx, ScriptableObject object, JavaScriptModule module)
            throws ScriptException {
        for (JavaScriptHostObject hostObject : module.getHostObjects()) {
            RhinoEngine.defineHostObject(object, hostObject);
        }

        for (JavaScriptMethod method : module.getMethods()) {
            RhinoEngine.defineMethod(object, method);
        }

        for (JavaScriptScript script : module.getScripts()) {
            script.getScript().exec(cx, object);
        }
    }

    private static void exposeModule(RhinoEngine engine, JavaScriptModule module) {
        for (JavaScriptHostObject hostObject : module.getHostObjects()) {
            engine.defineHostObject(hostObject);
        }

        for (JavaScriptMethod method : module.getMethods()) {
            engine.defineMethod(method);
        }

        for (JavaScriptScript script : module.getScripts()) {
            engine.defineScript(script);
        }
    }

    /**
     * JaggeryMethod responsible of writing to the output stream
     */
    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "print";
        JaggeryContext jaggeryContext = getJaggeryContext();

        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs("RhinoTopLevel", functionName, argsCount, false);
        }
        OutputStream out = (OutputStream) jaggeryContext.getProperty(CommonManager.JAGGERY_OUTPUT_STREAM);
        if (args[0] instanceof StreamHostObject) {
            InputStream in = ((StreamHostObject) args[0]).getStream();
            if (in instanceof FileInputStream) {  //if form file we will use channel since it's faster

                ReadableByteChannel inputChannel = null;
                WritableByteChannel outputChannel = null;
                FileChannel fc = null;
                try {
                    inputChannel = Channels.newChannel(in);
                    outputChannel = Channels.newChannel(out);
                    fc = (FileChannel) inputChannel;
                    fc.transferTo(0, fc.size(), outputChannel);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    throw new ScriptException(e);
                } finally {
                    clearResources(fc, inputChannel, outputChannel, in, out);
                }
            } else {
                try {
                    IOUtils.copy(in, out);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    throw new ScriptException(e);
                } finally {
                    clearResources(in, out);
                }
            }
        } else {
            try {
                out.write(HostObjectUtil.serializeObject(args[0]).getBytes());
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage(), e);
                }
                throw new ScriptException(e);
            }
        }
    }

    private static void clearResources(Closeable... resources) {
        for (Closeable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (IOException e) {
                    if (log.isDebugEnabled()) {
                        log.debug(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public static JaggeryContext getJaggeryContext() {
        return (JaggeryContext) RhinoEngine.getContextProperty(EngineConstants.JAGGERY_CONTEXT);
    }

    public static void setJaggeryContext(JaggeryContext jaggeryContext) {
        RhinoEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, jaggeryContext);
    }

    public static Map<String, Boolean> getIncludes(JaggeryContext jaggeryContext) {
        return (Map<String, Boolean>) jaggeryContext.getProperty(Constants.JAGGERY_INCLUDED_SCRIPTS);
    }

    public static Stack<String> getCallstack(JaggeryContext jaggeryContext) {
        return (Stack<String>) jaggeryContext.getProperty(Constants.JAGGERY_INCLUDES_CALLSTACK);
    }
}
