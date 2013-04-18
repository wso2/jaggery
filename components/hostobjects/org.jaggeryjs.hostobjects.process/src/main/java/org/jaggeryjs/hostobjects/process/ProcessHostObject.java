package org.jaggeryjs.hostobjects.process;

/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.util.Map;
import java.util.Properties;

/**
 * var process = require('process');
 */
public class ProcessHostObject {

    private static final Log log = LogFactory.getLog(ProcessHostObject.class);

    private static final String MODULE_NAME = "process";

    public static String getEnv(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getEnv";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(MODULE_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(MODULE_NAME, MODULE_NAME, "1", "string", args[0], false);
        }
        return System.getenv((String) args[0]);
    }

    public static Scriptable getEnvs(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getEnvs";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(MODULE_NAME, functionName, argsCount, false);
        }
        Map<String, String> envs = System.getenv();
        Scriptable object = cx.newObject(thisObj);
        for (String name : envs.keySet()) {
            object.put(name, object, envs.get(name));
        }
        return object;
    }

    public static String setProperty(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "setProperty";
        int argsCount = args.length;
        if (argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(MODULE_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(MODULE_NAME, MODULE_NAME, "1", "string", args[0], false);
        }
        if (!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(MODULE_NAME, MODULE_NAME, "2", "string", args[1], false);
        }
        return System.setProperty((String) args[0], (String) args[1]);
    }

    public static String getProperty(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getProperty";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(MODULE_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(MODULE_NAME, MODULE_NAME, "1", "string", args[0], false);
        }
        return System.getProperty((String) args[0]);
    }

    public static Scriptable getProperties(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getProperties";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(MODULE_NAME, functionName, argsCount, false);
        }
        Properties properties = System.getProperties();
        Scriptable object = cx.newObject(thisObj);
        for (String name : properties.stringPropertyNames()) {
            object.put(name, object, properties.getProperty(name));
        }
        return object;
    }
}
