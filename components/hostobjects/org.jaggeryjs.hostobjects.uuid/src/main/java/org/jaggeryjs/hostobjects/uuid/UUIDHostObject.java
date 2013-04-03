package org.jaggeryjs.hostobjects.uuid;

/*
 * Copyright 2006,2013 WSO2, Inc. http://www.wso2.org
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


import java.util.UUID;

/**
 * var process = require('uuid');
 */
public class UUIDHostObject {

    private static final Log log = LogFactory.getLog(UUIDHostObject.class);
    private static final String MODULE_NAME = "uuid";

    public static String generate(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "generate";
        int argsCount = args.length;
        if (argsCount != 0 && argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(MODULE_NAME, functionName, argsCount, true);
        }
        if (argsCount == 2) {
            if (!(args[0] instanceof Number)) {
                HostObjectUtil.invalidArgsError(MODULE_NAME, functionName, "1", "number", args[1], true);
            }

            if (!(args[1] instanceof Number)) {
                HostObjectUtil.invalidArgsError(MODULE_NAME, functionName, "2", "number", args[1], true);
            }
            return new UUID(((Number) args[0]).longValue(), ((Number) args[1]).longValue()).toString();
        }
        return UUID.randomUUID().toString();
    }
}
