package org.jaggeryjs.hostobjects.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import javax.servlet.http.HttpSession;

public class SessionHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(SessionHostObject.class);

    private static final String hostObjectName = "Session";

    private HttpSession session;

    public SessionHostObject() {

    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }
        if (!(args[0] instanceof HttpSession)) {
            HostObjectUtil.getReservedHostObjectWarn(hostObjectName);
        }
        SessionHostObject sho = new SessionHostObject();
        sho.session = (HttpSession) args[0];
        return sho;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static long jsFunction_getCreationTime(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getCreationTime";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        SessionHostObject sho = (SessionHostObject) thisObj;
        return sho.session.getCreationTime();
    }

    public static long jsFunction_getLastAccessedTime(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getLastAccessedTime";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        SessionHostObject sho = (SessionHostObject) thisObj;
        return sho.session.getLastAccessedTime();
    }

    public static String jsFunction_getId(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getSessionId";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        SessionHostObject sho = (SessionHostObject) thisObj;
        return sho.session.getId();
    }
    
    public long jsGet_maxInactive() throws ScriptException {
        return session.getMaxInactiveInterval();
    }

    public void jsSet_maxInactive(Object object) throws ScriptException {
        if (!(object instanceof Integer)) {
            HostObjectUtil.invalidProperty(hostObjectName, "maxInactive", "integer", object);
        }
        session.setMaxInactiveInterval((Integer) object);
    }

    public static boolean jsFunction_isNew(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "isNew";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        SessionHostObject sho = (SessionHostObject) thisObj;
        return sho.session.isNew();
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
        SessionHostObject sho = (SessionHostObject) thisObj;
        sho.session.setAttribute((String) args[0], args[1]);
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
        SessionHostObject sho = (SessionHostObject) thisObj;
        return sho.session.getAttribute((String) args[0]);
    }

    public static void jsFunction_invalidate(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "invalidate";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        SessionHostObject sho = (SessionHostObject) thisObj;
        sho.session.invalidate();
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

        String attr = (String) args[0];

        SessionHostObject sho = (SessionHostObject) thisObj;
        sho.session.removeAttribute(attr);
    }


}