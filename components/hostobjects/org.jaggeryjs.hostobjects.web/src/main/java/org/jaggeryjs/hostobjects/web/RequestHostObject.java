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
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class RequestHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(RequestHostObject.class);

    private static final String hostObjectName = "Request";

    private HttpServletRequest request;

    private boolean isMultipart = false;

    private boolean isParsed = false;

    private Map<String, FileItem> parameterMap = new HashMap<String, FileItem>();
    private Map<String, Scriptable> fileMap = new HashMap<String, Scriptable>();

    private Scriptable parameterFields = null;
    private Scriptable fileFields = null;

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

    public static Object jsFunction_getContent(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
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

    public static String jsFunction_getMethod(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getMethod";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getMethod();
    }

    public static String jsFunction_getContextPath(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getContextPath";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getContextPath();
    }

    public static String jsFunction_getPathTranslated(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getPathTranslated";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getPathTranslated();
    }

    public static String jsFunction_getProtocol(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getProtocol";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getProtocol();
    }

    public static String jsFunction_getQueryString(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getQueryString";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getQueryString();
    }

    public static String jsFunction_getContentType(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
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

    public static Scriptable jsFunction_getParameterMap(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getParameterMap";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        parse(rho);
        return rho.parameterFields;
    }

    public static Scriptable jsFunction_getFileMap(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getFileMap";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        parse(rho);
        return rho.fileFields;
    }

    public static String jsFunction_getRequestURI(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getRequestURI";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getRequestURI();
    }
    
    public static String jsFunction_getRequestURL(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getRequestURL";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        String strURL =  rho.request.getRequestURL().toString();
        return strURL;
    }
    
    public static boolean jsFunction_isSecure(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getRequestURL";
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

    public static String jsFunction_getRemoteAddr(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getRemoteAddr";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getRemoteAddr();
    }

    public static String jsFunction_getPathInfo(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getPathInfo";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getPathInfo();
    }

    public static String jsFunction_getLocale(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getLocale";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getLocale().getLanguage();
    }

    public static int jsFunction_getLocalPort(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
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
        parse(rho);
        FileItem item = rho.parameterMap.get(parameter);
        if(item == null) {
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
        String parameter = (String) args[0];
        RequestHostObject rho = (RequestHostObject) thisObj;
        if (rho.isMultipart) {
            parse(rho);
            return rho.fileMap.get(parameter);
        } else {
            return null;
        }
    }

    public HttpServletRequest getHttpServletRequest() {
        return this.request;
    }

    private static void parse(RequestHostObject rho) throws ScriptException {
        if (rho.isParsed) {
            return;
        }
        if (rho.isMultipart) {
            try {
                parseMultipart(rho);
            } catch (FileUploadException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        } else {
            try {
                parseParameters(rho);
            } catch (FileUploadException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        }
    }

    private static void parseMultipart(RequestHostObject rho) throws FileUploadException {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List items = upload.parseRequest(rho.request);

        List<String> paramsNames = new ArrayList<String>();
        List<String> fileNames = new ArrayList<String>();

        // Process the uploaded items
        String name;
        for (Object obj : items) {
            FileItem item = (FileItem) obj;
            name = item.getFieldName();
            if (item.isFormField()) {
                rho.parameterMap.put(name, item);
                paramsNames.add(name);
            } else {
                rho.fileMap.put(item.getFieldName(), rho.context.newObject(rho, "File", new Object[]{item}));
                fileNames.add(name);
            }
        }

        rho.parameterFields = rho.context.newArray(rho, paramsNames.toArray());
        rho.fileFields = rho.context.newArray(rho, fileNames.toArray());

        rho.isParsed = true;
    }

    private static void parseParameters(RequestHostObject rho) throws FileUploadException {
        List<String> paramsNames = new ArrayList<String>();

        Enumeration params = rho.request.getParameterNames();
        while (params.hasMoreElements()) {
            paramsNames.add((String) params.nextElement());
        }

        rho.parameterFields = rho.context.newArray(rho, paramsNames.toArray());
        rho.isParsed = true;
    }

    public static Cookie[] jsFunction_getCookies(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getCookies";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getCookies();
    }


}
