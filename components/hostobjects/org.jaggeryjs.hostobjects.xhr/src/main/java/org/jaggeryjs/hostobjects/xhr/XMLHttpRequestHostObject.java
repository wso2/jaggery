package org.jaggeryjs.hostobjects.xhr;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TODO : need to add basic auth
public class XMLHttpRequestHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(XMLHttpRequestHostObject.class);

    /**
     * XHR constants
     */
    private static final short UNSENT = 0;
    private static final short OPENED = 1;
    private static final short HEADERS_RECEIVED = 2;
    private static final short LOADING = 3;
    private static final short DONE = 4;

    /**
     * XHR properties
     */
    private short readyState;
    private StatusLine statusLine;
    private String responseText;
    private Scriptable responseXML;
    private Function onreadystatechange;

    private static final String hostObjectName = "XMLHttpRequest";

    private Context context = null;

    private String methodName = null;
    private String url = null;
    private boolean async = false;
    private String username = null;
    private String password = null;
    private List<Header> requestHeaders = new ArrayList<Header>();

    private HttpMethodBase method = null;

    private Header[] responseHeaders = null;
    private String responseType = null;

    /**
     * flags
     */
    private boolean sent = false;
    private boolean error = false;

    private HttpClient httpClient = null;

    public XMLHttpRequestHostObject() {
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }
        XMLHttpRequestHostObject xhr = new XMLHttpRequestHostObject();
        xhr.context = cx;
        return xhr;
    }

    /**
     * This corresponds to the readyonly readyState property of XHR.
     *
     * @return
     * @throws ScriptException
     */
    public short jsGet_readyState() throws ScriptException {
        return this.readyState;
    }

    /**
     * This corresponds to the set method of onreadystatechange property of XHR.
     *
     * @return
     * @throws ScriptException
     */
    public Function jsGet_onreadystatechange() throws ScriptException {
        return this.onreadystatechange;
    }

    /**
     * This corresponds to the get method of onreadystatechange property of XHR.
     *
     * @return
     * @throws ScriptException
     */
    public void jsSet_onreadystatechange(Object object) throws ScriptException {
        if (!(object instanceof Function)) {
            HostObjectUtil.invalidProperty(hostObjectName, "onreadystatechange", "Function", object);
        }
        this.onreadystatechange = (Function) object;
    }

    /**
     * This corresponds to the readyonly status property of XHR.
     *
     * @return
     * @throws ScriptException
     */
    public int jsGet_status() throws ScriptException {
        return this.statusLine.getStatusCode();
    }

    /**
     * This corresponds to the readyonly statusText property of XHR.
     *
     * @return
     * @throws ScriptException
     */
    public String jsGet_statusText() throws ScriptException {
        return this.statusLine.getReasonPhrase();
    }

    /**
     * This corresponds to the readyonly responseText property of XHR.
     *
     * @return
     * @throws ScriptException
     */
    public String jsGet_responseText() throws ScriptException {
        if (this.readyState == LOADING || this.readyState == DONE) {
            return this.responseText;
        } else {
            return "";
        }
    }

    /**
     * This corresponds to the readyonly responseXML property of XHR.
     *
     * @return
     * @throws ScriptException
     */
    public Scriptable jsGet_responseXML() throws ScriptException {

        if (!(this.readyState == LOADING || this.readyState == DONE)) {
            return null;
        }
        if (this.responseType != null && !(this.responseType.equals("text/xml") ||
                this.responseType.equals("application/xml") ||
                this.responseType.endsWith("+xml"))) {
            return null;
        }
        try {
            if (this.responseXML != null) {
                return this.responseXML;
            }
            this.responseXML = this.context.newObject(
                    this, "XML", new Object[]{AXIOMUtil.stringToOM(this.responseText)});
            return this.responseXML;
        } catch (XMLStreamException e) {
            throw new ScriptException("Error while converting response of " + this.url + " to a XML", e);
        }
    }

    /**
     * This corresponds to the open() method of XHR. It accepts following params
     * open(method, url, async, user, password)
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws ScriptException
     */
    public static void jsFunction_open(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "open";
        XMLHttpRequestHostObject xhr = (XMLHttpRequestHostObject) thisObj;
        int argsCount = args.length;
        if (argsCount < 2 || argsCount > 5) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        } else if (argsCount == 2) {
            setMethod(functionName, xhr, args[0]);
            setURL(functionName, xhr, args[1]);
        } else if (argsCount == 3) {
            setMethod(functionName, xhr, args[0]);
            setURL(functionName, xhr, args[1]);
            setAsync(functionName, xhr, args[2]);
        } else if (argsCount == 4) {
            setMethod(functionName, xhr, args[0]);
            setURL(functionName, xhr, args[1]);
            setUsername(functionName, xhr, args[2], "3");
            setPassword(functionName, xhr, args[3], "4");
        } else {
            setMethod(functionName, xhr, args[0]);
            setURL(functionName, xhr, args[1]);
            setAsync(functionName, xhr, args[2]);
            setUsername(functionName, xhr, args[3], "4");
            setPassword(functionName, xhr, args[4], "5");
        }
        updateReadyState(xhr, OPENED);
    }

    /**
     * This corresponds to the setRequestHeader() method of XHR.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static void jsFunction_setRequestHeader(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "setRequestHeader";
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
        XMLHttpRequestHostObject xhr = (XMLHttpRequestHostObject) thisObj;
        //TODO : case sensitive validation for the headers specified in the XMLHttpRequest
        xhr.requestHeaders.add(new Header((String) args[0], (String) args[1]));
    }

    /**
     * This corresponds to the send() method of XHR.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static void jsFunction_send(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "send";
        XMLHttpRequestHostObject xhr = (XMLHttpRequestHostObject) thisObj;
        if (xhr.readyState != OPENED) {
            throw new ScriptException("Invalid state, cannot invoke send() method. readyState : " + xhr.readyState);
        }
        if (xhr.sent) {
            xhr.method.abort();
            throw new ScriptException("Invalid state, cannot invoke send() method while a request is active. " +
                    "Request aborted.");
        }
        int argsCount = args.length;
        if (argsCount > 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        Object obj = argsCount == 1 ? args[0] : null;
        xhr.send(cx, obj);
    }

    /**
     * This corresponds to the abort() method of XHR.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static void jsFunction_abort(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        XMLHttpRequestHostObject xhr = (XMLHttpRequestHostObject) thisObj;
        //abort is only allowed for async calls
        if (xhr.async) {
            xhr.method.abort();
        }
    }

    /**
     * This corresponds to the getResponseHeader() method of XHR.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static String jsFunction_getResponseHeader(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getResponseHeader";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        XMLHttpRequestHostObject xhr = (XMLHttpRequestHostObject) thisObj;
        // 1
        String header = (String) args[0];
        if (xhr.readyState == UNSENT || xhr.readyState == OPENED) {
            return null;
        }
        // 2
        if (xhr.error) {
            return null;
        }
        // 5
        if (isInvalidHeader(header)) {
            return null;
        }
        if (xhr.responseHeaders == null) {
            return null;
        }
        for (Header h : xhr.responseHeaders) {
            if (h.getName().equalsIgnoreCase(header)) {
                return h.getValue();
            }
        }
        return null;
    }

    /**
     * This corresponds to the getAllResponseHeaders() method of XHR.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static String jsFunction_getAllResponseHeaders(Context cx, Scriptable thisObj, Object[] args,
                                                          Function funObj) throws ScriptException {
        String functionName = "getResponseHeader";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        XMLHttpRequestHostObject xhr = (XMLHttpRequestHostObject) thisObj;
        // 1
        if (xhr.readyState == UNSENT || xhr.readyState == OPENED) {
            return null;
        }
        // 2
        if (xhr.error) {
            return null;
        }
        // 5
        StringBuffer hBuf = new StringBuffer();
        String headers = "";
        if (xhr.responseHeaders == null) {
            return headers;
        }
        for (Header h : xhr.responseHeaders) {
            String header = h.getName();
            if (isInvalidHeader(header)) {
                continue;
            }
            hBuf.append(h.getName() + ": " + h.getValue() + "\r\n");
        }
        headers = hBuf.toString();
        return headers;
    }

    private static void setURL(String functionName, XMLHttpRequestHostObject xhr, Object arg) throws ScriptException {
        if (arg instanceof String) {
            String url = (String) arg;
            String formattedUrl = url.toLowerCase();
            if (!(formattedUrl.startsWith("http://") || formattedUrl.startsWith("https://"))) {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2",
                        "A urls begins with either 'http://' or 'https://'", url, false);
            }
            int lastIndex = url.indexOf("#");
            lastIndex = (lastIndex == -1) ? url.length() : lastIndex;
            xhr.url = url.substring(0, lastIndex);
        } else {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "string", arg, false);
        }
    }

    //TODO add other HTTP methods too
    private static void setMethod(String functionName, XMLHttpRequestHostObject xhr, Object arg)
            throws ScriptException {
        if (arg instanceof String) {
            String methodName = ((String) arg).toUpperCase();
            if ("GET".equals(methodName) || "HEAD".equals(methodName) || "POST".equals(methodName) ||
                    "PUT".equals(methodName) || "DELETE".equals(methodName) || "TRACE".equals(methodName) ||
                    "OPTIONS".equals(methodName)) {
                xhr.methodName = methodName;
            } else {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1",
                        "GET, HEAD, POST, PUT, DELETE, TRACE or OPTIONS", methodName, false);
            }
        } else {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", arg, false);
        }
    }

    private static void setAsync(String functionName, XMLHttpRequestHostObject xhr, Object arg)
            throws ScriptException {
        if (arg instanceof Boolean) {
            xhr.async = (Boolean) arg;
        } else {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "3", "boolean", arg.toString(), false);
        }
    }

    private static void setUsername(String functionName, XMLHttpRequestHostObject xhr, Object arg, String index)
            throws ScriptException {
        if (arg instanceof String) {
            xhr.username = (String) arg;
        } else {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, index, "string", arg.toString(), false);
        }
    }

    private static void setPassword(String functionName, XMLHttpRequestHostObject xhr, Object arg, String index)
            throws ScriptException {
        if (arg instanceof String) {
            xhr.password = (String) arg;
        } else {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, index, "string", arg.toString(), false);
        }
    }

    private static void updateReadyState(XMLHttpRequestHostObject xhr, short readyState) {
        xhr.readyState = readyState;
        if (xhr.async && xhr.onreadystatechange != null) {
            try {
                xhr.onreadystatechange.call(xhr.context, xhr, xhr, new Object[0]);
            } catch (Exception e) {
                log.warn("Error executing XHR callback for " + xhr.url, e);
                e.printStackTrace();
            }
        }
    }

    private void send(Context cx, Object obj) throws ScriptException {
        final HttpMethodBase method;
        if ("GET".equalsIgnoreCase(methodName)) {
            method = new GetMethod(this.url);
        } else if ("HEAD".equalsIgnoreCase(methodName)) {
            method = new HeadMethod(this.url);
        } else if ("POST".equalsIgnoreCase(methodName)) {
            PostMethod post = new PostMethod(this.url);
            String content = getRequestContent(obj);
            if (content != null) {
                post.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(content.getBytes())));
            }
            method = post;
        } else if ("PUT".equalsIgnoreCase(methodName)) {
            PutMethod put = new PutMethod(this.url);
            String content = getRequestContent(obj);
            if (content != null) {
                put.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(content.getBytes())));
            }
            method = put;
        } else if ("DELETE".equalsIgnoreCase(methodName)) {
            method = new DeleteMethod(this.url);
        } else if ("TRACE".equalsIgnoreCase(methodName)) {
            method = new TraceMethod(this.url);
        } else if ("OPTIONS".equalsIgnoreCase(methodName)) {
            method = new OptionsMethod(this.url);
        } else {
            throw new ScriptException("Unknown HTTP method : " + methodName);
        }
        for (Header header : requestHeaders) {
            method.addRequestHeader(header);
        }
        if (username != null) {
            httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        }
        this.method = method;
        final XMLHttpRequestHostObject xhr = this;
        if (async) {
            updateReadyState(xhr, LOADING);
            final ContextFactory factory = cx.getFactory();
            final ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(new Callable() {
                public Object call() throws Exception {
                    RhinoEngine.enterContext(factory);
                    try {
                        executeRequest(xhr);
                    } catch (ScriptException e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        es.shutdown();
                        RhinoEngine.exitContext();
                    }
                    return null;
                }
            });
        } else {
            executeRequest(xhr);
        }
    }

    private static void executeRequest(XMLHttpRequestHostObject xhr) throws ScriptException {
        try {
            xhr.httpClient.executeMethod(xhr.method);
            xhr.statusLine = xhr.method.getStatusLine();
            xhr.responseHeaders = xhr.method.getResponseHeaders();
            updateReadyState(xhr, HEADERS_RECEIVED);

            byte[] response = xhr.method.getResponseBody();
            if (response.length > 0) {
                xhr.responseType = xhr.method.getResponseHeader("Content-Type").getValue();
                xhr.responseText = new String(response);
            }
            updateReadyState(xhr, DONE);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        } finally {
            xhr.method.releaseConnection();
        }
    }

    private static String getRequestContent(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Undefined) {
            return null;
        }
        return HostObjectUtil.serializeObject(obj);
    }

    private static boolean isInvalidHeader(String header) {
        if ((header.equalsIgnoreCase("Set-Cookie") || header.equalsIgnoreCase("Set-Cookie2"))
                && !(header.equals("Set-Cookie") || header.equals("Set-Cookie2"))) {
            return true;
        }
        return false;
    }

}