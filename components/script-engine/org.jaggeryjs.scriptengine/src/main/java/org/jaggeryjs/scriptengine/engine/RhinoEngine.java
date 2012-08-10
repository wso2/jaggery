package org.jaggeryjs.scriptengine.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.security.RhinoSecurityController;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;

import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The <code>RhinoEngine</code> class acts as a global engine for executing JavaScript codes using Mozilla Rhino. Each engine instance
 * associates a scope and a caching manager.
 * <p>During the class initialization time, it creates a static global scope, which will be cloned upon request. This also has a constructor which
 * accepts class object itself as a parameter. So, it allows you to keep customised versions of RhinoEngine instances.
 * <p>It also has several util methods to register hostobjects, methods, properties with the engine's scope.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class RhinoEngine {

    private static final Log log = LogFactory.getLog(RhinoEngine.class);

    private static ContextFactory globalContextFactory;

    private CacheManager cacheManager;
    private ContextFactory contextFactory;
    //private SecurityController securityController;
    private List<JavaScriptModule> modules = new ArrayList<JavaScriptModule>();
    private JavaScriptModule globalModule = new JavaScriptModule("global");

    static {
        globalContextFactory = new RhinoContextFactory(
                RhinoSecurityController.isSecurityEnabled() ? new RhinoSecurityController() : null);
        ContextFactory.initGlobal(globalContextFactory);
    }

    /**
     * This constructor gets an existing <code>CacheManager</code> instance and returns a new engine instance with the new cache manager.
     * <p>Scope of the engine will be a clone of the static global scope associates with the <code>RhinoEngine</code> class.
     * @param cacheManager  A {@code CacheManager} instance to be used as the cache manager of the engine
     */
    public RhinoEngine(CacheManager cacheManager, SecurityController securityController) {
        this.cacheManager = cacheManager;
        if(securityController != null) {
            this.contextFactory = new RhinoContextFactory(securityController);
        } else {
            this.contextFactory = globalContextFactory;
        }
    }

    /**
     * This method registers a hostobject in the engine scope.
     * @param scope The scope where hostobject will be defined
     * @param hostObject HostObject to be defined
     */
    public static void defineHostObject(ScriptableObject scope, JavaScriptHostObject hostObject)
            throws ScriptException {
        String msg = "Error while registering the hostobject : ";
        Class clazz = hostObject.getClazz();
        String className = clazz.getName();
        try {
            ScriptableObject.defineClass(scope, clazz);
        } catch (InvocationTargetException e) {
            log.error(msg + className, e);
        } catch (InstantiationException e) {
            log.error(msg + className, e);
        } catch (IllegalAccessException e) {
            log.error(msg + className, e);
        }
    }

    public void defineHostObject(JavaScriptHostObject hostObject) {
        globalModule.addHostObject(hostObject);
    }

    /**
     * This method registers the specified object in the specified scope.
     * @param scope The scope to register the bean object
     * @param property Property to be defined
     */
    public static void defineProperty(ScriptableObject scope, JavaScriptProperty property) {
        String name = property.getName();
        Object object = property.getValue();
        if ((object instanceof Number) ||
                (object instanceof String) ||
                (object instanceof Boolean)) {
            scope.defineProperty(name, object, property.getAttribute());
        } else {
            // Must wrap non-scriptable objects before presenting to Rhino
            Scriptable wrapped = Context.toObject(object, scope);
            scope.defineProperty(name, wrapped, property.getAttribute());
        }
    }

    public void defineProperty(JavaScriptProperty property) {
        globalModule.addProperty(property);
    }

    public static void defineScript(ScriptableObject scope, JavaScriptScript script) {
        Context cx = enterGlobalContext();
        script.getScript().exec(cx, scope);
        exitContext();
    }

    public void defineScript(JavaScriptScript script) {
        globalModule.addScript(script);
    }

    public static void defineMethod(ScriptableObject scope, JavaScriptMethod method) throws ScriptException {
        String name = method.getName();
        FunctionObject f = new FunctionObject(name, method.getMethod(), scope);
        scope.defineProperty(name, f, method.getAttribute());
    }

    public void defineMethod(JavaScriptMethod method) {
        globalModule.addMethod(method);
    }

    public void defineModule(JavaScriptModule module) {
        modules.add(module);
    }

    /**
     * Evaluates the specified script and the result is returned. If the <code>sctx</code> is provided and cache is upto date, cached script
     * will be evaluated instead of the original one. Otherwise, a either cache will be updated or evaluated the script directly without caching.
     * <p>A clone of the engine scope will be used as the scope during the evaluation.
     * @param scriptReader Reader object to read the script when ever needed
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return Returns the resulting object after evaluating script
     * @throws ScriptException If error occurred while evaluating
     */
    public Object eval(Reader scriptReader, ScriptCachingContext sctx) throws ScriptException {
        return evalScript(scriptReader, getRuntimeScope(), sctx);
    }

    /**
     * Evaluates the specified script and the result is returned. If the <code>sctx</code> is provided and cache is upto date, cached script
     * will be evaluated instead of the original one. Otherwise, either the cache will be updated or evaluated the script directly without caching.
     * <p>The specified scope will be used as the scope during the evaluation.
     * @param scriptReader Reader object to read the script when ever needed
     * @param scope Scope to be used during the evaluation
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return Returns the resulting object after evaluating script
     * @throws ScriptException If error occurred while evaluating
     */
    public Object eval(Reader scriptReader, ScriptableObject scope, ScriptCachingContext sctx)
            throws ScriptException {
        if (scope == null) {
            String msg = "ScriptableObject value for scope, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        return evalScript(scriptReader, scope, sctx);
    }

    /**
     * Executes the script on a clone of the engine scope and the scope is returned.
     * <p>If the <code>sctx</code> is provided and cache is upto date, cached script
     * will be evaluated instead of the original one. Otherwise, either the cache will be updated or evaluated the script directly without caching.
     * @param scriptReader Reader object to read the script when ever needed
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return Modified clone of the engine scope
     * @throws ScriptException If error occurred while evaluating
     */
    public ScriptableObject exec(Reader scriptReader, ScriptCachingContext sctx) throws ScriptException {
        return execScript(scriptReader, getRuntimeScope(), sctx);
    }

    /**
     * Executes the script on the specified scope.
     * <p>If the <code>sctx</code> is provided and cache is upto date, cached script
     * will be evaluated instead of the original one. Otherwise, either the cache will be updated or evaluated the script directly without caching.
     * @param scriptReader Reader object to read the script when ever needed
     * @param scope Scope to be used during the execution
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @throws ScriptException If error occurred while evaluating
     */
    public void exec(Reader scriptReader, ScriptableObject scope, ScriptCachingContext sctx) throws ScriptException {
        if (scope == null) {
            String msg = "ScriptableObject value for scope, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        execScript(scriptReader, scope, sctx);
    }

    /**
     * Executes a particular JavaScript function from a script on a clone of engine's scope and returns the result.
     * @param scriptReader Reader object to read the script when ever needed
     * @param funcName Name of the function to be invoked
     * @param args Arguments for the functions as an array of objects
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return Returns the resulting object after invoking the function
     * @throws ScriptException If error occurred while invoking the function
     */
    public Object call(Reader scriptReader, String funcName, Object[] args, ScriptCachingContext sctx)
            throws ScriptException {
        return execFunc(scriptReader, funcName, args, getRuntimeScope(), getRuntimeScope(), sctx);
    }

    /**
     * Executes a particular JavaScript function from a script on a clone of engine's scope and returns the result.
     * @param scriptReader Reader object to read the script when ever needed
     * @param funcName Name of the function to be invoked
     * @param args Arguments for the functions as an array of objects
     * @param thiz {@code this} object for the function
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return Returns the resulting object after invoking the function
     * @throws ScriptException If error occurred while invoking the function
     */
    public Object call(Reader scriptReader, String funcName, Object[] args, ScriptableObject thiz,
                       ScriptCachingContext sctx) throws ScriptException {
        if (thiz == null) {
            String msg = "ScriptableObject value for thiz, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        return execFunc(scriptReader, funcName, args, thiz, getRuntimeScope(), sctx);
    }

    /**
     * Executes a particular JavaScript function from a script on the specified scope and returns the result.
     * @param scriptReader Reader object to read the script when ever needed
     * @param funcName Name of the function to be invoked
     * @param args Arguments for the functions as an array of objects
     * @param thiz {@code this} object for the function
     * @param scope The scope where function will be executed
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return Returns the resulting object after invoking the function
     * @throws ScriptException If error occurred while invoking the function
     */
    public Object call(Reader scriptReader, String funcName, Object[] args, ScriptableObject thiz,
                       ScriptableObject scope, ScriptCachingContext sctx) throws ScriptException {
        if (scope == null) {
            String msg = "ScriptableObject value for scope, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        if (thiz == null) {
            String msg = "ScriptableObject value for thiz, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        return execFunc(scriptReader, funcName, args, thiz, scope, sctx);
    }

    /**
     * This clones the engine scope and returns.
     * @return Cloned scope
     */
    public ScriptableObject getRuntimeScope() throws ScriptException {
        Context cx = enterContext();
        ScriptableObject scope = removeUnsafeObjects(new RhinoTopLevel(cx, false));
        exposeModule(scope, globalModule);
        for (JavaScriptModule module : modules) {
            String name = module.getName();
            ScriptableObject object = (ScriptableObject) newObject(scope);
            exposeModule(object, module);
            JavaScriptProperty property = new JavaScriptProperty(name);
            property.setValue(object);
            property.setAttribute(ScriptableObject.PERMANENT);
            defineProperty(scope, property);
        }
        exitContext();
        return scope;
    }

    public void unloadTenant(String tenantId) {
        this.cacheManager.unloadTenant(tenantId);
    }

    public Context enterContext() {
        return contextFactory.enterContext();
    }

    public static Scriptable newObject(String constructor, ScriptableObject scope, Object[] args) {
        Context cx = enterGlobalContext();
        Scriptable obj = cx.newObject(scope, constructor, args);
        exitContext();
        return obj;
    }

    public static Scriptable newObject(ScriptableObject scope) {
        Context cx = enterGlobalContext();
        Scriptable obj = cx.newObject(scope);
        exitContext();
        return obj;
    }

    public static void putContextProperty(Object key, Object value) {
        Context cx = Context.getCurrentContext();
        cx.putThreadLocal(key, value);
    }

    public static Object getContextProperty(Object key) {
        Context cx = Context.getCurrentContext();
        Object value = cx.getThreadLocal(key);
        return value;
    }

    public static ScriptableObject cloneScope(ScriptableObject scope) {
        Context cx = enterGlobalContext();
        ScriptableObject clone = (ScriptableObject) cx.newObject(scope);
        clone.setPrototype(scope.getPrototype());
        clone.setParentScope(scope.getParentScope());
        exitContext();
        return clone;
    }

    public static Context enterContext(ContextFactory factory) {
        return globalContextFactory.enterContext();
    }

    public static Context enterGlobalContext() {
        return globalContextFactory.enterContext();
    }

    public static void exitContext() {
        Context.exit();
    }

    private void defineClass(ScriptableObject scope, Class clazz) {
        try {
            ScriptableObject.defineClass(scope, clazz);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } catch (InstantiationException e) {
            log.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void defineMethod(ScriptableObject scope, String name, Method method, int attribute) {
        FunctionObject f = new FunctionObject(name, method, scope);
        scope.defineProperty(name, f, attribute);
    }

    private void exposeModule(ScriptableObject scope, JavaScriptModule module) throws ScriptException {
        Context cx = enterContext();
        for (JavaScriptHostObject hostObject : module.getHostObjects()) {
            defineClass(scope, hostObject.getClazz());
        }

        for (JavaScriptMethod method : module.getMethods()) {
            defineMethod(scope, method.getName(), method.getMethod(), method.getAttribute());
        }

        for (JavaScriptScript script : module.getScripts()) {
            script.getScript().exec(cx, scope);
        }
        exitContext();
    }

    private CacheManager getCacheManager() {
        return cacheManager;
    }

    private Object execFunc(Reader scriptReader, String funcName, Object[] args, ScriptableObject thiz,
                            ScriptableObject scope, ScriptCachingContext sctx) throws ScriptException {
        Context cx = enterContext();
        try {
            if (sctx == null) {
                cx.evaluateString(scope, HostObjectUtil.readerToString(scriptReader), "wso2js", 1, null);
            } else {
                Script script = cacheManager.getScriptObject(scriptReader, sctx);
                if (script == null) {
                    cacheManager.cacheScript(scriptReader, sctx);
                    script = cacheManager.getScriptObject(scriptReader, sctx);
                }
                script.exec(cx, scope);
            }
            return execFunc(funcName, args, thiz, scope, cx);
        } catch (Exception e) {
            log.error(e);
            throw new ScriptException(e);
        } finally {
            exitContext();
        }
    }

    private static Object execFunc(String funcName, Object[] args, ScriptableObject thiz,
                                   ScriptableObject scope, Context cx) throws ScriptException {
        Object object = scope.get(funcName, scope);
        if (!(object instanceof Function)) {
            String msg = "Function cannot be found with the name '" + funcName + "', but a " + object.toString();
            log.error(msg);
            throw new ScriptException(msg);
        }
        try {
            return ((Function) object).call(cx, scope, thiz, args);
        } catch (Exception e) {
            log.error(e);
            throw new ScriptException(e);
        }
    }

    private Object evalScript(Reader scriptReader, ScriptableObject scope, ScriptCachingContext sctx)
            throws ScriptException {
        Context cx = enterContext();
        Object result;
        try {
            if (sctx == null) {
                result = cx.evaluateString(scope, HostObjectUtil.readerToString(scriptReader), "wso2js", 1, null);
            } else {
                Script script = cacheManager.getScriptObject(scriptReader, sctx);
                if (script == null) {
                    cacheManager.cacheScript(scriptReader, sctx);
                    script = cacheManager.getScriptObject(scriptReader, sctx);
                }
                result = script.exec(cx, scope);
            }
            return result;
        } catch (Exception e) {
            log.error(e);
            throw new ScriptException(e);
        } finally {
            exitContext();
        }
    }

    private ScriptableObject execScript(Reader scriptReader, ScriptableObject scope, ScriptCachingContext sctx)
            throws ScriptException {
        Context cx = enterContext();
        try {
            if (sctx == null) {
                cx.evaluateString(scope, HostObjectUtil.readerToString(scriptReader), "wso2js", 1, null);
            } else {
                Script script = cacheManager.getScriptObject(scriptReader, sctx);
                if (script == null) {
                    cacheManager.cacheScript(scriptReader, sctx);
                    script = cacheManager.getScriptObject(scriptReader, sctx);
                }
                script.exec(cx, scope);
            }
            return scope;
        } catch (Exception e) {
            log.error(e);
            throw new ScriptException(e);
        } finally {
            exitContext();
        }
    }

    private static ScriptableObject removeUnsafeObjects(ScriptableObject scope) {
        /**
         * TODO : go through ECMAScript and E4X specs and remove unwanted objects from following values
         * QName, TypeError, isNaN, isFinite, ConversionError, EvalError, encodeURI, Boolean, Call, Iterator, Array,
         * XML, unescape, URIError, decodeURI, Infinity, SyntaxError, Date, String, encodeURIComponent, RangeError,
         * ReferenceError, RegExp, With, Function, InternalError, NaN, Number, escape, XMLList, Math, JavaException,
         * parseFloat, Error, undefined, parseInt, Object, Continuation, decodeURIComponent, StopIteration, log,
         * Namespace, isXMLName, global, eval
         */
        scope.delete("JavaAdapter");
        scope.delete("org");
        scope.delete("java");
        scope.delete("JavaImporter");
        scope.delete("Script");
        scope.delete("edu");
        scope.delete("uneval");
        scope.delete("javax");
        scope.delete("getClass");
        scope.delete("com");
        scope.delete("net");
        scope.delete("Packages");
        scope.delete("importClass");
        scope.delete("importPackage");
        return scope;
    }

    private static void copyEngineScope(ScriptableObject engineScope, ScriptableObject scope) {
        Object[] objs = engineScope.getAllIds();
        for (Object obj : objs) {
            String id = (String) obj;
            scope.put(id, scope, engineScope.get(id, engineScope));
        }
    }
}
