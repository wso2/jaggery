package org.jaggeryjs.scriptengine.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.EngineConstants;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.*;

public class RhinoTopLevel extends ImporterTopLevel {

    private static final String JS_TIMER_THREADS = "jsTimerThreads";

    private static final Log log = LogFactory.getLog(RhinoTopLevel.class);

    private static final ScheduledExecutorService timerExecutor;

    private static final Map<String, Map<String, ScheduledFuture>> timeouts = new HashMap<String, Map<String, ScheduledFuture>>();
    private static final Map<String, Map<String, ScheduledFuture>> intervals = new HashMap<String, Map<String, ScheduledFuture>>();

    static {
        String threadCount = System.getProperty(JS_TIMER_THREADS);
        timerExecutor = Executors.newScheduledThreadPool(threadCount != null ? Integer.parseInt(threadCount) : 5);
    }

    public RhinoTopLevel(Context context, boolean sealed) {
        super(context, sealed);
    }

    public static Object parse(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "parse";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "1",
                    "string", args[0], false);
        }
        return HostObjectUtil.parseJSON(cx, thisObj, (String) args[0]);
    }

    public static String stringify(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "stringify";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME, functionName, argsCount, false);
        }
        return HostObjectUtil.serializeJSON(args[0]);
    }

    /**
     * The sync function creates a synchronized function (in the sense
     * of a Java synchronized method) from an existing function. The
     * new function synchronizes on the the second argument if it is
     * defined, or otherwise the <code>this</code>
     */
    public static Object sync(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        Object syncObject = null;
        String functionName = "sync";
        int argsCount = args.length;
        if (argsCount <= 1 && argsCount >= 2) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME, functionName, argsCount, false);
        } else {
            if (!(args[0] instanceof Function)) {
                HostObjectUtil
                        .invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "1",
                                "function", args[0], false);
            }
            if (argsCount == 2) {
                if (args[1] == Undefined.instance) {
                    HostObjectUtil
                            .invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME,
                                    "1", "object", args[0], false);
                } else {
                    syncObject = args[1];
                }
            }
        }
        return new Synchronizer((Function) args[0], syncObject);
    }

    public static String setTimeout(Context cx, final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "setTimeout";
        int argsCount = args.length;
        if (argsCount < 2) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME, functionName, argsCount, false);
        }
        Function function = null;
        long timeout;
        if (args[0] instanceof Function) {
            function = (Function) args[0];
        } else if (args[0] instanceof String) {
            function = getFunction(cx, thisObj, (String) args[0], functionName);
        } else {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "1",
                    "string|function", args[0], false);
        }
        if (!(args[1] instanceof Number)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "2",
                    "number", args[1], false);
        }
        if (function == null) {
            String error = "Callback cannot be null in " + functionName;
            log.error(error);
            throw new ScriptException(error);
        }
        final JaggeryContext context = getJaggeryContext();
        final Object[] params = Arrays.copyOfRange(args, 2, args.length);
        final Function callback = function;
        final ContextFactory factory = cx.getFactory();
        timeout = ((Number) args[1]).longValue();
        String uuid = UUID.randomUUID().toString();

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        final int tenantId = carbonContext.getTenantId();
        final String tenantDomain = carbonContext.getTenantDomain();
        final String applicationName = carbonContext.getApplicationName();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        ScheduledFuture future = timerExecutor.schedule(new Callable<Void>() {
            public Void call() throws Exception {
                //set the context classloader
                Thread currentThread = Thread.currentThread();
                ClassLoader originalClassLoader = currentThread.getContextClassLoader();
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                // child inherits context properties form the parent thread.
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(tenantId);
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setApplicationName(applicationName);

                try {
                    Context ctx = RhinoEngine.enterContext(factory);
                    RhinoEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, context);
                    callback.call(ctx, thisObj, thisObj, params);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                    RhinoEngine.exitContext();
                    currentThread.setContextClassLoader(originalClassLoader);
                }
                return null;
            }
        }, timeout, TimeUnit.MILLISECONDS);

        Map<String, ScheduledFuture> tasks = timeouts.get(context.getTenantDomain());
        if (tasks == null) {
            tasks = new HashMap<String, ScheduledFuture>();
            timeouts.put(context.getTenantDomain(), tasks);
        }
        tasks.put(uuid, future);
        return uuid;
    }

    public static String setInterval(Context cx, final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "setTimeout";
        int argsCount = args.length;
        if (argsCount < 2) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME, functionName, argsCount, false);
        }
        Function function = null;
        long interval;
        if (args[0] instanceof Function) {
            function = (Function) args[0];
        } else if (args[0] instanceof String) {
            function = getFunction(cx, thisObj, (String) args[0], functionName);
        } else {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "1",
                    "string|function", args[0], false);
        }
        if (!(args[1] instanceof Number)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "2",
                    "number", args[1], false);
        }
        if (function == null) {
            String error = "Callback cannot be null in " + functionName;
            log.error(error);
            throw new ScriptException(error);
        }
        final JaggeryContext context = getJaggeryContext();
        final Object[] params = Arrays.copyOfRange(args, 2, args.length);
        final Function callback = function;
        final ContextFactory factory = cx.getFactory();
        interval = ((Number) args[1]).longValue();
        String uuid = UUID.randomUUID().toString();

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        final int tenantId = carbonContext.getTenantId();
        final String tenantDomain = carbonContext.getTenantDomain();
        final String applicationName = carbonContext.getApplicationName();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        ScheduledFuture future = timerExecutor.scheduleAtFixedRate(new Runnable() {

            private boolean firstTime = true;

            @Override public void run() {
                //set the context classloader
                Thread currentThread = Thread.currentThread();
                ClassLoader originalClassLoader = currentThread.getContextClassLoader();
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                // child inherits context properties form the parent thread.
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantId(tenantId);
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setApplicationName(applicationName);

                try {
                    Context cx = RhinoEngine.enterContext(factory);
                    RhinoEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, context);
                    callback.call(cx, thisObj, thisObj, params);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                    RhinoEngine.exitContext();
                    currentThread.setContextClassLoader(originalClassLoader);
                }
            }
        }, interval, interval, TimeUnit.MILLISECONDS);

        Map<String, ScheduledFuture> tasks = intervals.get(context.getTenantDomain());
        if (tasks == null) {
            tasks = new HashMap<String, ScheduledFuture>();
            intervals.put(context.getTenantDomain(), tasks);
        }
        tasks.put(uuid, future);
        return uuid;
    }

    private static JaggeryContext getJaggeryContext() throws ScriptException {
        final JaggeryContext context = (JaggeryContext) RhinoEngine.getContextProperty(EngineConstants.JAGGERY_CONTEXT);
        if (context == null) {
            String error = "JaggeryContext instance cannot be found in the current thread : " + Thread.currentThread()
                    .getName();
            log.error(error);
            throw new ScriptException(error);
        }
        return context;
    }

    public static void clearTimeout(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "clearTimeout";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "1",
                    "string", args[0], false);
        }
        clearTimeout((String) args[0]);
    }

    public static void clearInterval(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "clearTimeout";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME, EngineConstants.GLOBAL_OBJECT_NAME, "1",
                    "string", args[0], false);
        }
        clearInterval((String) args[0]);
    }

    public static void exit(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        throw new Error();
    }

    public static void clearTimeout(String taskId) throws ScriptException {
        JaggeryContext context = getJaggeryContext();
        Map<String, ScheduledFuture> tasks = timeouts.get(context.getTenantDomain());
        if (tasks == null) {
            return;
        }
        ScheduledFuture future = tasks.get(taskId);
        future.cancel(true);
    }

    public static void clearInterval(String taskId) throws ScriptException {
        JaggeryContext context = getJaggeryContext();
        Map<String, ScheduledFuture> tasks = intervals.get(context.getTenantDomain());
        if (tasks == null) {
            return;
        }
        ScheduledFuture future = tasks.get(taskId);
        future.cancel(true);
    }

    public static void removeTasks(String tenantId) {
        intervals.remove(tenantId);
        timeouts.remove(tenantId);
    }

    private static Function getFunction(Context cx, Scriptable thisObj, String source, String functionName) {
        ScriptableObject scope = (ScriptableObject) cx
                .evaluateString(thisObj, "var fn=" + source + ";", functionName, 0, null);
        return (Function) scope.get("fn", scope);
    }
}
