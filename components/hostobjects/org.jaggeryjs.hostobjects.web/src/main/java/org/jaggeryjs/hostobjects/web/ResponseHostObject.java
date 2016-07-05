package org.jaggeryjs.hostobjects.web;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.NativeObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(ResponseHostObject.class);

    private static final String hostObjectName = "Response";

    private HttpServletResponse response;

    private int status = 0;

    private String content = null;

    public ResponseHostObject() {

    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }
        if (!(args[0] instanceof HttpServletResponse)) {
            HostObjectUtil.getReservedHostObjectWarn(hostObjectName);
        }
        ResponseHostObject rho = new ResponseHostObject();
        rho.response = (HttpServletResponse) args[0];
        return rho;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public void jsSet_content(Object object) throws ScriptException {
        try {
            String content = HostObjectUtil.serializeObject(object);
            response.getOutputStream().write(content.getBytes("UTF-8"));
        } catch (IOException e) {
            String msg = "Error occurred while reading Servlet OutputStream";
            log.error(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    public String jsGet_content() throws ScriptException {
        return content;
    }

    public void jsSet_contentType(Object object) throws ScriptException {
        if (!(object instanceof String)) {
            HostObjectUtil.invalidProperty(hostObjectName, "contentType", "string", object);
        }
        response.setContentType((String) object);
    }

    public String jsGet_contentType() throws ScriptException {
        return response.getContentType();
    }

    public void jsSet_characterEncoding(Object object) throws ScriptException {
        if (!(object instanceof String)) {
            HostObjectUtil.invalidProperty(hostObjectName, "characterEncoding", "string", object);
        }
        response.setCharacterEncoding((String) object);
    }

    public String jsGet_characterEncoding() throws ScriptException {
        return response.getCharacterEncoding();
    }

    public void jsSet_status(Object object) throws ScriptException {
        if (!(object instanceof Integer)) {
            HostObjectUtil.invalidProperty(hostObjectName, "status", "integer", object);
        }
        this.status = (Integer) object;
        response.setStatus(this.status);
    }

    public int jsGet_status() throws ScriptException {
        return this.status;
    }

    public static void jsFunction_addHeader(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "addHeader";
        int argsCount = args.length;
        if (argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        if (!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "string", args[1], false);
        }
        ResponseHostObject rho = (ResponseHostObject) thisObj;
        rho.response.addHeader((String) args[0], (String) args[1]);
    }

    public static void jsFunction_sendError(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "sendError";
        int argsCount = args.length;
        if (argsCount > 2 || argsCount < 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof Integer)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "integer", args[0], false);
        }
        ResponseHostObject rho = (ResponseHostObject) thisObj;
        if (argsCount == 2) {
            if (!(args[1] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "string", args[1], false);
            }
            try {
                rho.response.sendError((Integer) args[0], (String) args[1]);
            } catch (IOException e) {
                String msg = "Error sending error. Status : " + args[0] + ", Message : " + args[1];
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            }
        } else {
            try {
                rho.response.sendError((Integer) args[0]);
            } catch (IOException e) {
                String msg = "Error sending error. Status : " + args[0];
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            }
        }
    }

    @SuppressFBWarnings("UNVALIDATED_REDIRECT")
    public static void jsFunction_sendRedirect(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "sendRedirect";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        ResponseHostObject rho = (ResponseHostObject) thisObj;
        try {
            rho.response.sendRedirect((String) args[0]);
        } catch (IOException e) {
            String msg = "Error sending redirect : " + args[0];
            log.warn(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    public static void jsFunction_addCookie(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "addCookie";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof NativeObject)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        NativeObject jcookie = (NativeObject) args[0];
        Gson gson = new Gson();
        Cookie cookie = gson.fromJson(HostObjectUtil.serializeJSON(jcookie), Cookie.class);
        ResponseHostObject rho = (ResponseHostObject) thisObj;
        rho.response.addCookie(cookie);
    }
}