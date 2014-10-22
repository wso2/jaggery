package org.jaggeryjs.hostobjects.log;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jaggeryjs.scriptengine.EngineConstants;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

import java.util.Stack;

public class LogHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(LogHostObject.class);

    public static final String LOG_LEVEL = "hostobject.log.loglevel";
    //TODO : move this to a constants class
    public static final String JAGGERY_INCLUDES_CALLSTACK = "jaggery.includes.callstack";

    private static final String HOSTOBJECT_NAME = "Log";
    private static final String ROOT_LOGGER = "JAGGERY";
    private static final String LOG_LEVEL_INFO = "info";
    private static final String LOG_LEVEL_WARN = "warn";
    private static final String LOG_LEVEL_DEBUG = "debug";
    private static final String LOG_LEVEL_ERROR = "error";
    private static final String LOG_LEVEL_FATAL = "fatal";
    private static final String LOG_LEVEL_TRACE = "trace";

    private static final String RHINO_EXCEPTION_KEY = "rhinoException";

    private static final String JAVA_EXCEPTION_KEY = "javaException";

    private static final String WARN_NON_THROWABLE = "Non throwable Java object has been passed as an argument";

    private Logger logger;

    @Override
    public String getClassName() {
        return HOSTOBJECT_NAME;
    }

    /**
     * Creates a new log object for the requested resource.
     * logger name will be the resource name separated by a . (dot)
     * i.e if resource is /foo/bar/mar.jag
     * the loger name will be
     * JAGGERY.foo.bar.mar
     * <p/>
     * by default the log level is set to debug
     *
     * @param cx
     * @param args
     * @param ctorObj
     * @param inNewExpr
     * @return
     * @throws ScriptException
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        int argsCount = args.length;
        if (argsCount > 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, HOSTOBJECT_NAME, argsCount, true);
        }
        String loggerName;
        JaggeryContext context = (JaggeryContext) RhinoEngine.getContextProperty(EngineConstants.JAGGERY_CONTEXT);
        if (argsCount == 1 && (args[0] instanceof String)) {
            loggerName = (String) args[0];
        } else {
            String requestString = ((Stack<String>) context.getProperty(JAGGERY_INCLUDES_CALLSTACK)).peek();
            loggerName = ROOT_LOGGER + requestString.replace(".jag", ":jag").replace(".js", ":js").replace("/", ".");
        }
        LogHostObject logObj = new LogHostObject();
        logObj.logger = Logger.getLogger(loggerName);

        String logLevel = (String) context.getProperty(LOG_LEVEL);
        if (LOG_LEVEL_FATAL.equalsIgnoreCase(logLevel)) {
            logObj.logger.setLevel(Level.FATAL);
        } else if (LOG_LEVEL_WARN.equalsIgnoreCase(logLevel)) {
            logObj.logger.setLevel(Level.WARN);
        } else if (LOG_LEVEL_DEBUG.equalsIgnoreCase(logLevel)) {
            logObj.logger.setLevel(Level.DEBUG);
        } else if (LOG_LEVEL_ERROR.equalsIgnoreCase(logLevel)) {
            logObj.logger.setLevel(Level.ERROR);
        } else if (LOG_LEVEL_TRACE.equalsIgnoreCase(logLevel)) {
            logObj.logger.setLevel(Level.TRACE);
        } else {
            logObj.logger.setLevel(Level.INFO);
        }
        return logObj;
    }

    //prints a debug message
    public static void jsFunction_debug(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "debug";
        int argsCount = args.length;
        if (argsCount == 0) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        String message = HostObjectUtil.serializeObject(args[0]);
        if (argsCount == 1) {
            logObj.logger.debug(message);
            return;
        }
        Throwable exception = getThrowable(args[1]);
        if (exception == null) {
            logObj.logger.debug(message);
            log.warn(WARN_NON_THROWABLE);
            return;
        }
        logObj.logger.debug(message, exception);
    }

    //prints a trace message
    public static void jsFunction_trace(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "trace";
        int argsCount = args.length;
        if (argsCount == 0) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        String message = HostObjectUtil.serializeObject(args[0]);
        if (argsCount == 1) {
            logObj.logger.trace(message);
            return;
        }
        Throwable exception = getThrowable(args[1]);
        if (exception == null) {
            logObj.logger.trace(message);
            log.warn(WARN_NON_THROWABLE);
            return;
        }
        logObj.logger.trace(message, exception);
    }

    //prints an info message
    public static void jsFunction_info(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "info";
        int argsCount = args.length;
        if (argsCount == 0) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        String message = HostObjectUtil.serializeObject(args[0]);
        if (argsCount == 1) {
            logObj.logger.info(message);
            return;
        }
        Throwable exception = getThrowable(args[1]);
        if (exception == null) {
            logObj.logger.info(message);
            log.warn(WARN_NON_THROWABLE);
            return;
        }
        logObj.logger.info(message, exception);
    }

    //prints an error message
    public static void jsFunction_error(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "error";
        int argsCount = args.length;
        if (argsCount == 0) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        String message = HostObjectUtil.serializeObject(args[0]);
        if (argsCount == 1) {
            logObj.logger.error(message);
            return;
        }
        Throwable exception = getThrowable(args[1]);
        if (exception == null) {
            logObj.logger.error(message);
            log.warn(WARN_NON_THROWABLE);
            return;
        }
        logObj.logger.error(message, exception);
    }

    //prints a warning message
    public static void jsFunction_warn(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "warn";
        int argsCount = args.length;
        if (argsCount == 0) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        String message = HostObjectUtil.serializeObject(args[0]);
        if (argsCount == 1) {
            logObj.logger.warn(message);
            return;
        }
        Throwable exception = getThrowable(args[1]);
        if (exception == null) {
            logObj.logger.warn(message);
            log.warn(WARN_NON_THROWABLE);
            return;
        }
        logObj.logger.warn(message, exception);
    }

    //prints a fatal message
    public static void jsFunction_fatal(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "fatal";
        int argsCount = args.length;
        if (argsCount == 0) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        String message = HostObjectUtil.serializeObject(args[0]);
        if (argsCount == 1) {
            logObj.logger.fatal(message);
            return;
        }
        Throwable exception = getThrowable(args[1]);
        if (exception == null) {
            logObj.logger.fatal(message);
            log.warn(WARN_NON_THROWABLE);
            return;
        }
        logObj.logger.fatal(message, exception);
    }

    //check if debug is enabled
    public boolean jsFunction_isDebugEnabled() throws ScriptException {
        return this.logger.isDebugEnabled();
    }

    //check if trace is anabled
    public boolean jsFunction_isTraceEnabled() throws ScriptException {
        return this.logger.isTraceEnabled();
    }

    private static Throwable getThrowable(Object error) {
        if (error instanceof Throwable) {
            return (Throwable) error;
        }
        if (!(error instanceof Scriptable)) {
            return null;
        }
        Scriptable scriptable = (Scriptable) error;
        if (!"Error".equals(scriptable.getClassName())) {
            return null;
        }
        error = scriptable.get(RHINO_EXCEPTION_KEY, scriptable);
        if (error == null) {
            error = scriptable.get(JAVA_EXCEPTION_KEY, scriptable);
        }
        if (error instanceof Wrapper) {
            error = ((Wrapper) error).unwrap();
        }
        if (!(error instanceof Throwable)) {
            return null;
        }
        return (Throwable) error;
    }
}
