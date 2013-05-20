package org.jaggeryjs.scriptengine.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.EngineConstants;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;

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

    public static Object parse(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "parse";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME,
                    functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME,
                    EngineConstants.GLOBAL_OBJECT_NAME, "1", "string", args[0], false);
        }
        return HostObjectUtil.parseJSON(thisObj, (String) args[0]);
    }

    public static String stringify(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "stringify";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME,
                    functionName, argsCount, false);
        }
        return HostObjectUtil.serializeJSON(args[0]);
    }

    public static String setTimeout(Context cx, final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "setTimeout";
        int argsCount = args.length;
        if (argsCount < 2) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME,
                    functionName, argsCount, false);
        }
        Function function = null;
        long timeout;
        if (args[0] instanceof Function) {
            function = (Function) args[0];
        } else if (args[0] instanceof String) {
            function = getFunction(cx, thisObj, (String) args[0], functionName);
        } else {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME,
                    EngineConstants.GLOBAL_OBJECT_NAME, "1", "string|function", args[0], false);
        }
        if (!(args[1] instanceof Number)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME,
                    EngineConstants.GLOBAL_OBJECT_NAME, "2", "number", args[1], false);
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
        ScheduledFuture future = timerExecutor.schedule(new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    Context ctx = RhinoEngine.enterContext(factory);
                    RhinoEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, context);
                    callback.call(ctx, thisObj, thisObj, params);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    RhinoEngine.exitContext();
                }
                return null;
            }
        }, timeout, TimeUnit.MILLISECONDS);

        Map<String, ScheduledFuture> tasks = timeouts.get(context.getTenantId());
        if (tasks == null) {
            tasks = new HashMap<String, ScheduledFuture>();
            timeouts.put(context.getTenantId(), tasks);
        }
        tasks.put(uuid, future);
        return uuid;
    }

    public static String setInterval(Context cx, final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "setTimeout";
        int argsCount = args.length;
        if (argsCount < 2) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME,
                    functionName, argsCount, false);
        }
        Function function = null;
        long interval;
        if (args[0] instanceof Function) {
            function = (Function) args[0];
        } else if (args[0] instanceof String) {
            function = getFunction(cx, thisObj, (String) args[0], functionName);
        } else {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME,
                    EngineConstants.GLOBAL_OBJECT_NAME, "1", "string|function", args[0], false);
        }
        if (!(args[1] instanceof Number)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME,
                    EngineConstants.GLOBAL_OBJECT_NAME, "2", "number", args[1], false);
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
        ScheduledFuture future = timerExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Context cx = RhinoEngine.enterContext(factory);
                    RhinoEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, context);
                    callback.call(cx, thisObj, thisObj, params);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    RhinoEngine.exitContext();
                }
            }
        }, interval, interval, TimeUnit.MILLISECONDS);

        Map<String, ScheduledFuture> tasks = intervals.get(context.getTenantId());
        if (tasks == null) {
            tasks = new HashMap<String, ScheduledFuture>();
            intervals.put(context.getTenantId(), tasks);
        }
        tasks.put(uuid, future);
        return uuid;
    }

    private static JaggeryContext getJaggeryContext() throws ScriptException {
        final JaggeryContext context = (JaggeryContext) RhinoEngine.getContextProperty(EngineConstants.JAGGERY_CONTEXT);
        if (context == null) {
            String error = "JaggeryContext instance cannot be found in the current thread : " +
                    Thread.currentThread().getName();
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
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME,
                    functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME,
                    EngineConstants.GLOBAL_OBJECT_NAME, "1", "string", args[0], false);
        }
        clearTimeout((String) args[0]);
    }

    public static void clearInterval(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "clearTimeout";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME,
                    functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME,
                    EngineConstants.GLOBAL_OBJECT_NAME, "1", "string", args[0], false);
        }
        clearInterval((String) args[0]);
    }

    public static void clearTimeout(String taskId) throws ScriptException {
        JaggeryContext context = getJaggeryContext();
        Map<String, ScheduledFuture> tasks = timeouts.get(context.getTenantId());
        if (tasks == null) {
            return;
        }
        ScheduledFuture future = tasks.get(taskId);
        future.cancel(true);
    }

    public static void clearInterval(String taskId) throws ScriptException {
        JaggeryContext context = getJaggeryContext();
        Map<String, ScheduledFuture> tasks = intervals.get(context.getTenantId());
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
        ScriptableObject scope = (ScriptableObject) cx.evaluateString(
                thisObj, "var fn=" + source + ";", functionName, 0, null);
        return (Function) scope.get("fn", scope);
    }
}
