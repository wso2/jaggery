package org.jaggeryjs.hostobjects.uuid;

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
import org.mozilla.javascript.ScriptableObject;

import java.util.UUID;

/**
 * var uuid = new UUID();
 */
public class UUIDHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(UUIDHostObject.class);

    private static final String hostObjectName = "UUID";

    private UUID uuid;

    /**
     * UUID()
     * UUID(mostSignificant, leastSignificant)
     */
    public static Scriptable jsConstructor(final Context cx, Object[] args, final Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 0 && argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }

        UUIDHostObject uho = new UUIDHostObject();
        if (argsCount == 0) {
            uho.uuid = UUID.randomUUID();
        } else if (argsCount == 2) {
            if (!(args[0] instanceof Number)) {
                HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "1", "number", args[1], true);
            }

            if (!(args[1] instanceof Number)) {
                HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "2", "number", args[1], true);
            }
            uho.uuid = new UUID(((Number) args[0]).longValue(), ((Number) args[1]).longValue());
        }
        return uho;
    }

    public String getClassName() {
        return hostObjectName;
    }

    public String jsFunction_toString()
            throws ScriptException {
        return this.uuid.toString();
    }
}
