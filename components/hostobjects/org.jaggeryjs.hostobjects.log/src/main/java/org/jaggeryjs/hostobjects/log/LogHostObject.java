package org.jaggeryjs.hostobjects.log;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jaggeryjs.jaggery.core.manager.JaggeryContext;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class LogHostObject extends ScriptableObject {

    public static final String HOSTOBJECT_NAME = "Log";
    private static final String ROOT_LOGGER = "JAGGERY";
    private static final String LOG_LEVEL_INFO = "info";
    private static final String LOG_LEVEL_WARN = "warn";
    private static final String LOG_LEVEL_DEBUG = "debug";
    private static final String LOG_LEVEL_ERROR = "error";
    private static final String LOG_LEVEL_FATAL = "fatal";

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
        JaggeryContext context = ((JaggeryContext) cx.getThreadLocal("jaggeryContext"));
        if (argsCount == 1 && (args[0] instanceof String)) {
            loggerName = (String) args[0];
        } else {
            String requestString = context.getIncludesCallstack().peek();
            loggerName = ROOT_LOGGER + requestString.replace(".jag", ":jag").replace(".js", ":js").replace("/", ".");
        }
        LogHostObject logObj = new LogHostObject();
        logObj.logger = Logger.getLogger(loggerName);

        String logLevel = context.getLogLevel();
        if (LOG_LEVEL_INFO.equalsIgnoreCase(logLevel)) {
            logObj.logger.setLevel(Level.INFO);
        } else if (LOG_LEVEL_WARN.equalsIgnoreCase(logLevel)) {
            logObj.logger.setLevel(Level.WARN);
        } else if (LOG_LEVEL_DEBUG.equalsIgnoreCase(logLevel)) {
            logObj.logger.setLevel(Level.DEBUG);
        } else if (LOG_LEVEL_ERROR.equalsIgnoreCase(logLevel)) {
            logObj.logger.setLevel(Level.ERROR);
        } else {
            logObj.logger.setLevel(Level.FATAL);
        }
        return logObj;
    }

    //prints a debug message
    public static void jsFunction_debug(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "debug";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.debug(HostObjectUtil.serializeObject(args[0]));
    }

    //prints an info message
    public static void jsFunction_info(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "info";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.info(HostObjectUtil.serializeObject(args[0]));
    }

    //prints an error message
    public static void jsFunction_error(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "error";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.error(HostObjectUtil.serializeObject(args[0]));
    }

    //prints a warning message
    public static void jsFunction_warn(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "warn";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.warn(HostObjectUtil.serializeObject(args[0]));
    }

    //prints a fatal message
    public static void jsFunction_fatal(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "fatal";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.fatal(HostObjectUtil.serializeObject(args[0]));
    }

    //check if debug is enabled
    public boolean jsFunction_isDebugEnabled() throws ScriptException {
        return this.logger.isDebugEnabled();
    }

    //check if trace is anabled
    public boolean jsFunction_isTraceEnabled() throws ScriptException {
        return this.logger.isTraceEnabled();
    }
}
