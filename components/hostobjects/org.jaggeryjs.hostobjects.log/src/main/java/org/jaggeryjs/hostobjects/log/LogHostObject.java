/*
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
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
        Logger currentLogger = Logger.getLogger(loggerName);
        String appLogLevel = (String) context.getProperty(LOG_LEVEL);
        if(currentLogger.getLevel() == null){
            currentLogger.setLevel(Level.toLevel(appLogLevel));
        }
        logObj.logger = currentLogger;
        return logObj;
    }

    //prints a debug message
    public static void jsFunction_debug(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        log(LOG_LEVEL_DEBUG, thisObj, args);
    }

    //prints a trace message
    public static void jsFunction_trace(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        log(LOG_LEVEL_TRACE, thisObj, args);
    }

    //prints an info message
    public static void jsFunction_info(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        log(LOG_LEVEL_INFO, thisObj, args);
    }

    //prints an error message
    public static void jsFunction_error(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        log(LOG_LEVEL_ERROR, thisObj, args);
    }

    //prints a warning message
    public static void jsFunction_warn(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        log(LOG_LEVEL_WARN, thisObj, args);
    }

    //prints a fatal message
    public static void jsFunction_fatal(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        log(LOG_LEVEL_FATAL, thisObj, args);
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

    private static void log(String logLevel, Scriptable thisObj, Object[] args) throws ScriptException {
        int argsCount = args.length;
        if (argsCount == 0) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, logLevel, argsCount, false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        String message = HostObjectUtil.serializeObject(args[0]);
        if (argsCount == 1) {
            logMessage(logObj.logger, logLevel, message);
            return;
        }
        Throwable exception = getThrowable(args[1]);
        if (exception == null) {
            logMessage(logObj.logger, logLevel, message);
            log.warn(WARN_NON_THROWABLE);
            return;
        }
        logException(logObj.logger, logLevel, message, exception);
    }

    private static void logMessage(Logger log, String logLevel, String message) {
        if (LOG_LEVEL_DEBUG.equals(logLevel)) {
            log.debug(message);
            return;
        }
        if (LOG_LEVEL_ERROR.equals(logLevel)) {
            log.error(message);
            return;
        }
        if (LOG_LEVEL_FATAL.equals(logLevel)) {
            log.fatal(message);
            return;
        }
        if (LOG_LEVEL_INFO.equals(logLevel)) {
            log.info(message);
            return;
        }
        if (LOG_LEVEL_TRACE.equals(logLevel)) {
            log.trace(message);
            return;
        }
        if (LOG_LEVEL_WARN.equals(logLevel)) {
            log.warn(message);
        }
    }

    private static void logException(Logger log, String logLevel, String message, Throwable throwable) {
        if (LOG_LEVEL_DEBUG.equals(logLevel)) {
            log.debug(message, throwable);
            return;
        }
        if (LOG_LEVEL_ERROR.equals(logLevel)) {
            log.error(message, throwable);
            return;
        }
        if (LOG_LEVEL_FATAL.equals(logLevel)) {
            log.fatal(message, throwable);
            return;
        }
        if (LOG_LEVEL_INFO.equals(logLevel)) {
            log.info(message, throwable);
            return;
        }
        if (LOG_LEVEL_TRACE.equals(logLevel)) {
            log.trace(message, throwable);
            return;
        }
        if (LOG_LEVEL_WARN.equals(logLevel)) {
            log.warn(message, throwable);
        }
    }
}