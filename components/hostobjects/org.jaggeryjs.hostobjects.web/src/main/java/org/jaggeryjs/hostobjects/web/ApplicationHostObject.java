package org.jaggeryjs.hostobjects.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import javax.servlet.ServletContext;

public class ApplicationHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(SessionHostObject.class);

    private static final String hostObjectName = "Application";

    private static final String ATTRIBUTE_PREFIX = "org.jaggeryjs.";

    private ServletContext servletContext;

    public ApplicationHostObject() {

    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }
        if (!(args[0] instanceof ServletContext)) {
            HostObjectUtil.getReservedHostObjectWarn(hostObjectName);
        }
        ApplicationHostObject aho = new ApplicationHostObject();
        aho.servletContext = (ServletContext) args[0];
        return aho;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static void jsFunction_put(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "put";
        int argsCount = args.length;
        if (argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        ApplicationHostObject sho = (ApplicationHostObject) thisObj;
        sho.servletContext.setAttribute(filterAttributeName((String) args[0]), args[1]);
    }

    public static Object jsFunction_get(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "get";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(
                    hostObjectName, functionName, "1", "string", args[0], false);
        }
        ApplicationHostObject sho = (ApplicationHostObject) thisObj;
        return sho.servletContext.getAttribute(filterAttributeName((String) args[0]));
    }

    public static void jsFunction_remove(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "remove";
        int argsCount = args.length;
        if (argsCount == 0 || argsCount > 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(
                    hostObjectName, functionName, "1", "string", args[0], false);
        }

        ApplicationHostObject sho = (ApplicationHostObject) thisObj;
        sho.servletContext.removeAttribute(filterAttributeName((String) args[0]));
    }

    private static String filterAttributeName(String name) {
        return ATTRIBUTE_PREFIX + name;
    }
}