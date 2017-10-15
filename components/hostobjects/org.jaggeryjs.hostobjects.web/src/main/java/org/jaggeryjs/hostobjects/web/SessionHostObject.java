package org.jaggeryjs.hostobjects.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;

public class SessionHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(SessionHostObject.class);

    private static final String hostObjectName = "Session";

    private HttpSession session;
    private HttpServletRequest servletRequest;

    public SessionHostObject() {
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }
        SessionHostObject sho = new SessionHostObject();
        if (args[0] instanceof HttpServletRequest) {
            sho.servletRequest = (HttpServletRequest) args[0];
        } else if (args[0] instanceof HttpSession) {
            sho.session = (HttpSession) args[0];
        } else {
            HostObjectUtil.getReservedHostObjectWarn(hostObjectName);
        }
        return sho;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static long jsFunction_getCreationTime(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getCreationTime";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        SessionHostObject sho = (SessionHostObject) thisObj;
        return sho.getSession().getCreationTime();
    }

    private HttpSession getSession() {
        return servletRequest.getSession();
    }

    private HttpSession getSession(boolean status) {
        if (status) {
            session = servletRequest.getSession(true);
        }
        session = servletRequest.getSession(false);
        return session;
    }

    public static long jsFunction_getLastAccessedTime(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getLastAccessedTime";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        SessionHostObject sho = (SessionHostObject) thisObj;
        return sho.getSession().getLastAccessedTime();
    }

    public static String jsFunction_getId(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws
            ScriptException {
        String functionName = "getSessionId";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        SessionHostObject sho = (SessionHostObject) thisObj;
        return sho.getSession().getId();
    }

    public long jsGet_maxInactive() throws ScriptException {
        return getSession().getMaxInactiveInterval();
    }

    public void jsSet_maxInactive(Object object) throws ScriptException {
        if (!(object instanceof Integer)) {
            HostObjectUtil.invalidProperty(hostObjectName, "maxInactive", "integer", object);
        }
        getSession().setMaxInactiveInterval((Integer) object);
    }

    public static boolean jsFunction_isNew(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws
            ScriptException {
        String functionName = "isNew";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        SessionHostObject sho = (SessionHostObject) thisObj;
        return sho.getSession().isNew();
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
        JSONWrapper wrapper = new JSONWrapper(args[1]);
        SessionHostObject sho = (SessionHostObject) thisObj;
        sho.getSession(true).setAttribute((String) args[0], wrapper);
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
        if (sho.session == null) {
            return null;
        }
        JSONWrapper wrapper;
        try {
            wrapper = (JSONWrapper) sho.session.getAttribute((String) args[0]);

        } catch (IllegalStateException e) {
            log.info("this is illegal state exception " + e.getMessage());
            return null;
        }
        if (wrapper == null) {
            return null;
        }
        if (!wrapper.isInitialized()) {
            wrapper.init(cx, thisObj);
        }
        return wrapper.object;
    }

    public static void jsFunction_invalidate(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "invalidate";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        SessionHostObject sho = (SessionHostObject) thisObj;
        if (sho.session != null) {
            sho.session.invalidate();
        }

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
        sho.getSession().removeAttribute(attr);
    }

    private static class JSONWrapper implements Serializable {

        private String json;
        private transient Object object;
        private transient boolean initialized = false;

        private JSONWrapper(Object object) {
            this.object = object;
            this.json = HostObjectUtil.serializeJSON(object);
            this.initialized = true;
        }

        private void init(Context cxt, Scriptable scope) {
            this.object = HostObjectUtil.parseJSON(cxt, scope, this.json);
            this.initialized = true;
        }

        private boolean isInitialized() {
            return this.initialized;
        }
    }
}