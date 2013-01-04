package org.jaggeryjs.hostobjects.web;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * TODO :
 * getLocale
 * getPathTransalted
 */

public class RequestHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(RequestHostObject.class);

    private static final String hostObjectName = "Request";

    private HttpServletRequest request;

    private boolean isMultipart = false;

    private Map<String, FileItem> parameterMap = new HashMap<String, FileItem>();

    private Scriptable parameters = null;

    private Scriptable files = null;

    private Scriptable cookies = null;

    private Scriptable locales = null;

    private Object content = null;

    private Context context;

    public RequestHostObject() {

    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }
        if (!(args[0] instanceof HttpServletRequest)) {
            HostObjectUtil.getReservedHostObjectWarn(hostObjectName);
        }
        RequestHostObject rho = new RequestHostObject();
        rho.request = (HttpServletRequest) args[0];
        rho.isMultipart = ServletFileUpload.isMultipartContent(rho.request);
        rho.context = cx;
        return rho;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static Object jsFunction_getContent(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getContent";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        try {
            if (rho.content != null) {
                return rho.content;
            }
            String data = HostObjectUtil.streamToString(rho.request.getInputStream());
            String contentType = rho.request.getContentType();
            if (contentType != null && (
                    contentType.equals("application/json") ||
                            contentType.equals("application/json/badgerfish"))) {
                rho.content = cx.evaluateString(thisObj, data, "wso2js", 1, null);
            } else {
                rho.content = data;
            }
            return rho.content;
        } catch (IOException e) {
            String msg = "Error occurred while reading Servlet InputStream";
            log.warn(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    public static String jsFunction_getMethod(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getMethod";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getMethod();
    }

    public static String jsFunction_getContextPath(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getContextPath";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getContextPath();
    }

    public static String jsFunction_getPathTranslated(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getPathTranslated";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getPathTranslated();
    }

    public static String jsFunction_getProtocol(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getProtocol";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getProtocol();
    }

    public static String jsFunction_getQueryString(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getQueryString";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getQueryString();
    }

    public static String jsFunction_getContentType(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getContentType";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getContentType();
    }

    public static int jsFunction_getContentLength(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getContentLength";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getContentLength();
    }

    public static Scriptable jsFunction_getAllParameters(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getAllParameters";
        int argsCount = args.length;
        if (argsCount > 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        RequestHostObject rho = (RequestHostObject) thisObj;
        if (!rho.isMultipart) {
            parseParameters(rho);
            return rho.parameters;
        }
        String encoding = null;
        if (argsCount == 1) {
            if (!(args[0] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
            }
            encoding = (String) args[0];
        }
        parseMultipart(rho);
        parseMultipartParams(rho, encoding);
        return rho.parameters;
    }

    public static Scriptable jsFunction_getAllFiles(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getAllFiles";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        if (!rho.isMultipart) {
            return null;
        }
        parseMultipart(rho);
        return rho.files;
    }

    public static String jsFunction_getRequestURI(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getRequestURI";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getRequestURI();
    }

    public static String jsFunction_getRequestURL(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getRequestURL";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        String strURL = rho.request.getRequestURL().toString();
        return strURL;
    }

    public static boolean jsFunction_isSecure(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "isSecure";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.isSecure();
    }

    public static String jsFunction_getHeader(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getHeader";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(
                    hostObjectName, functionName, "1", "string", args[0], false);
        }
        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getHeader((String) args[0]);
    }

    public static Scriptable jsFunction_getAllHeaders(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getAllHeaders";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        RequestHostObject rho = (RequestHostObject) thisObj;
        Enumeration<String> names = rho.request.getHeaderNames();
        Scriptable headers = cx.newObject(thisObj);
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, headers, rho.request.getHeader(name));
        }
        return headers;
    }

    public static String jsFunction_getRemoteAddr(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getRemoteAddr";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getRemoteAddr();
    }

    public static String jsFunction_getPathInfo(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getPathInfo";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getPathInfo();
    }

    public static String jsFunction_getLocale(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getLocale";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;

        return rho.request.getLocale().getLanguage();
    }

    public static String jsFunction_getAllLocales(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getLocale";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getLocale().getLanguage();
    }

    public static int jsFunction_getLocalPort(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getLocalPort";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getLocalPort();
    }

    public static String jsFunction_getParameter(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getParameter";
        int argsCount = args.length;
        if (argsCount != 1 && argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        if (argsCount == 2 && !(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "string", args[1], false);
        }

        String parameter = (String) args[0];
        RequestHostObject rho = (RequestHostObject) thisObj;
        if (!rho.isMultipart) {
            return rho.request.getParameter(parameter);
        }
        parseMultipart(rho);
        FileItem item = rho.parameterMap.get(parameter);
        if (item == null) {
            return null;
        }
        if (argsCount == 1) {
            return item.getString();
        }
        try {
            return item.getString((String) args[1]);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    public static Scriptable jsFunction_getFile(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getFile";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        RequestHostObject rho = (RequestHostObject) thisObj;
        if (!rho.isMultipart) {
            return null;
        }
        parseMultipart(rho);
        return (Scriptable) rho.files.get((String) args[0], thisObj);
    }

    public HttpServletRequest getHttpServletRequest() {
        return this.request;
    }

    private static void parseMultipart(RequestHostObject rho) throws ScriptException {
        if (rho.files != null) {
            return;
        }
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List items = null;
        try {
            items = upload.parseRequest(rho.request);
        } catch (FileUploadException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
        // Process the uploaded items
        String name;
        rho.files = rho.context.newObject(rho);
        for (Object obj : items) {
            FileItem item = (FileItem) obj;
            name = item.getFieldName();
            if (item.isFormField()) {
                rho.parameterMap.put(name, item);
            } else {
                rho.files.put(item.getFieldName(), rho.files, rho.context.newObject(rho, "File", new Object[]{item}));
            }
        }
    }

    private static void parseMultipartParams(RequestHostObject rho, String encoding) throws ScriptException {
        if (rho.parameters != null) {
            return;
        }
        rho.parameters = rho.context.newObject(rho);
        for (String name : rho.parameterMap.keySet()) {
            try {
                rho.parameters.put(name, rho.parameters, rho.parameterMap.get(name).getString(encoding));
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        }
    }

    private static void parseParameters(RequestHostObject rho) {
        if (rho.parameters != null) {
            return;
        }
        rho.parameters = rho.context.newObject(rho);
        Enumeration params = rho.request.getParameterNames();
        while (params.hasMoreElements()) {
            String name = (String) params.nextElement();
            rho.parameters.put(name, rho.parameters, rho.request.getParameter(name));
        }
    }

    public static Scriptable jsFunction_getCookie(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getCookie";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        RequestHostObject rho = (RequestHostObject) thisObj;
        if (rho.cookies == null) {
            parseCookies(cx, thisObj, rho);
        }
        return (Scriptable) rho.cookies.get((String) args[0], rho.cookies);
    }

    public static Scriptable jsFunction_getAllCookies(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getAllCookies";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        RequestHostObject rho = (RequestHostObject) thisObj;
        if (rho.cookies == null) {
            parseCookies(cx, thisObj, rho);
        }
        return rho.cookies;
    }

    private static void parseCookies(Context cx, Scriptable thisObj, RequestHostObject rho) {
        for (Cookie cookie : rho.request.getCookies()) {
            Scriptable o = cx.newObject(thisObj);
            o.put("name", o, cookie.getName());
            o.put("value", o, cookie.getValue());
            o.put("comment", o, cookie.getComment());
            o.put("domain", o, cookie.getDomain());
            o.put("maxAge", o, cookie.getMaxAge());
            o.put("path", o, cookie.getPath());
            o.put("secure", o, cookie.getSecure());
            o.put("version", o, cookie.getVersion());
            rho.cookies.put(cookie.getName(), rho.cookies, o);
        }
    }
}
