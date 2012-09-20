package org.jaggeryjs.hostobjects.log;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.jaggery.core.manager.WebAppContext;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;

public class LogHostObject extends ScriptableObject {

    public static final String HOSTOBJECT_NAME = "Log";
    private static final String ROOT_LOGGER = "JAGGERY";

    private static Logger logger = Logger.getLogger(LogHostObject.class.getName());

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
        if (argsCount == 1 && (args[0] instanceof String)) {
            loggerName = (String) args[0];
        } else {
            String requestString = ((WebAppContext) cx.getThreadLocal("jaggeryContext")).getServletRequest().getRequestURI();
            loggerName = ROOT_LOGGER + requestString.replace(".jag", ":jag").replace(".js", ":js").replace("/", ".");
        }
        LogHostObject logObj = new LogHostObject();
        logObj.logger = Logger.getLogger(loggerName);

        //TODO need to remove this once the set from config is implemented
        logObj.logger.setLevel(Level.DEBUG);
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
        logObj.logger.debug(HostObjectUtil.serializeJSON(args[0]));
    }

    //prints an info message
    public static void jsFunction_info(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "info";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.info(HostObjectUtil.serializeJSON(args[0]));
    }

    //prints an error message
    public static void jsFunction_error(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "error";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.error(HostObjectUtil.serializeJSON(args[0]));
    }

    //prints a warning message
    public static void jsFunction_warn(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "warn";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.warn(HostObjectUtil.serializeJSON(args[0]));
    }

    //prints a fatal message
    public static void jsFunction_fatal(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "fatal";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.fatal(HostObjectUtil.serializeJSON(args[0]));
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
