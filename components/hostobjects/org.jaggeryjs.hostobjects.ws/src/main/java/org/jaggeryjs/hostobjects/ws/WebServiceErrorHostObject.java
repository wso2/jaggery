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
package org.jaggeryjs.hostobjects.ws;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

public class WebServiceErrorHostObject extends ScriptableObject {

    private String code;

    private String reason;

    private String detail;

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        WebServiceErrorHostObject webServiceErrorHostObject = new WebServiceErrorHostObject();
        switch (args.length) {
            case 0:
                throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
            case 1:
                if (args[0] instanceof WebServiceErrorHostObject) {
                    return (WebServiceErrorHostObject) args[0];
                }
                if (args[0] instanceof String) {
                    webServiceErrorHostObject.reason = (String) args[0];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                break;
            case 2:
                if (args[0] instanceof String) {
                    webServiceErrorHostObject.reason = (String) args[0];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[1] instanceof String) {
                    webServiceErrorHostObject.detail = (String) args[1];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                break;
            case 3:
                if (args[0] instanceof String) {
                    webServiceErrorHostObject.reason = (String) args[0];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[1] instanceof String) {
                    webServiceErrorHostObject.detail = (String) args[1];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[2] instanceof String) {
                    webServiceErrorHostObject.code = (String) args[2];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                break;
            default:
                throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
        }

        return webServiceErrorHostObject;
    }

    public String getClassName() {
        return "WebServiceError";
    }

    public String jsGet_code() {
        return code;
    }

    public void jsSet_code(String code) {
        this.code = code;
    }

    public String jsGet_reason() {
        return reason;
    }

    public void jsSet_reason(String reason) {
        this.reason = reason;
    }

    public String jsGet_detail() {
        return detail;
    }

    public void jsSet_detail(String detail) {
        this.detail = detail;
    }
}