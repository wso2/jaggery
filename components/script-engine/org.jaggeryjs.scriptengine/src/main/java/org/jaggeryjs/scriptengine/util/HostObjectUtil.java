package org.jaggeryjs.scriptengine.util;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.*;
import org.mozilla.javascript.xml.XMLObject;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TimeZone;

public class HostObjectUtil {

    private static final Log log = LogFactory.getLog(HostObjectUtil.class);

    private static final String FORMAT_DATE_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final DateFormat dateFormat = new SimpleDateFormat(FORMAT_DATE_ISO);

    static {
        dateFormat.setTimeZone(TimeZone.getDefault());
    }

    public static void invalidArgsError(String object, String function, String index, String required, Object provided,
                                        boolean constructor) throws ScriptException {
        String msg = "Invalid argument for the " + (constructor ? "constructor" : "function") + ". Object : " +
                object + ", " + (constructor ? "Constructor" : "Method") + " : " + function +
                ", Param Index : " + index + ", Required Type : " + required + ", Provided Type : " +
                provided.toString();
        log.warn(msg);
        throw new ScriptException(msg);
    }

    public static void invalidNumberOfArgs(String object, String function, int size, boolean constructor) throws ScriptException {
        String msg = "Invalid number of arguments have been provided. Object : " + object + ", " +
                (constructor ? "Constructor" : "Method") + " : " + function + ", Args Count : " + size;
        log.warn(msg);
        throw new ScriptException(msg);
    }

    public static void invalidProperty(String object, String property, String required, Object provided) throws ScriptException {
        String msg = "Invalid property value. Object : " + object + ", Property : " + property + ", Required Type : " + required +
                ", Provided Type : " + provided.toString();
        log.warn(msg);
        throw new ScriptException(msg);
    }

    public static void getReservedHostObjectWarn(String object) throws ScriptException {
        String msg = object + " Object has been reserved and cannot be instantiated by a script.";
        log.warn(msg);
        throw new ScriptException(msg);
    }

    public static Object parseJSON(Context cx, Scriptable scope, String json) {
        return NativeJSON.parse(cx,scope,json, new Callable() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                return args[1];
            }
        });
    }

    public static String serializeJSON(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }

        if (obj == null) {
            return "null";
        }
        if (obj instanceof Undefined) {
            return "null";
        }
        if (obj instanceof Boolean) {
            return Boolean.toString((Boolean) obj);
        }
        if (obj instanceof String) {
            return serializeString((String) obj);
        }
        if (obj instanceof ConsString) {
            return serializeString(obj.toString());
        }
        if (obj instanceof Number) {
            return obj.toString();
        }
        if (obj instanceof XMLObject) {
            return serializeString(serializeXML((ScriptableObject) obj));
        }

        if (obj instanceof NativeObject) {
            return serializeNativeObject((NativeObject) obj);
        }

        if (obj instanceof NativeArray) {
            return serializeNativeArray((NativeArray) obj);
        }

        if (obj instanceof Object[]) {
            return serializeObjectArray((Object[]) obj);
        }

        if (obj instanceof Scriptable) {
            Scriptable object = (Scriptable) obj;
            String jsClass = object.getClassName();
            if ("Date".equals(jsClass)) {
                return serializeString(serializeNativeDate(object));
            } else if ("Error".equals(jsClass)) {
                return serializeString(serializeNativeError(object));
            }
        }

        return "{}";
    }

    public static String serializeObject(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }

        if (obj instanceof String ||
                obj instanceof ConsString ||
                obj instanceof Integer ||
                obj instanceof Long ||
                obj instanceof Float ||
                obj instanceof Double ||
                obj instanceof Short ||
                obj instanceof BigInteger ||
                obj instanceof BigDecimal ||
                obj instanceof Boolean) {
            return obj.toString();
        }

        if (obj instanceof XMLObject) {
            return serializeXML((ScriptableObject) obj);
        }

        if (obj instanceof Scriptable) {
            Scriptable object = (Scriptable) obj;
            String jsClass = object.getClassName();
            if ("Date".equals(jsClass)) {
                return serializeNativeDate(object);
            } else if ("Error".equals(jsClass)) {
                return serializeNativeError(object);
            } else if("String".equals(jsClass)) {
                return obj.toString();
            }
        }

        return serializeJSON(obj);
    }

    public static String streamToString(InputStream is) throws ScriptException {
        try {
            return IOUtils.toString(is, "UTF-8");
        } catch (IOException e) {
            log.error(e);
            throw new ScriptException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.warn("Error while closing the input stream", e);
                }
            }
        }
    }

    public static String readerToString(Reader reader) throws ScriptException {
        StringBuilder sb = new StringBuilder();
        try {
            int data = reader.read();
            while (data != -1) {
                sb.append((char) data);
                data = reader.read();
            }
            return sb.toString();
        } catch (IOException e) {
            String msg = "Error while reading the content from the Reader";
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.warn(e);
            }
        }
    }

    public static int getURL(String urlString, String username,
                             String password) throws IOException {

        HttpMethod method = new GetMethod(urlString);
        URL url = new URL(urlString);
        MultiThreadedHttpConnectionManager connectionManager =
                new MultiThreadedHttpConnectionManager();
        HttpClient httpClient = new HttpClient(connectionManager);
        // We should not use method.setURI and set the complete URI here.
        // If we do so commons-httpclient will not use our custom socket factory.
        // Hence we set the path and query separatly
        method.setPath(url.getPath());
        method.setQueryString(url.getQuery());
        method.setRequestHeader("Host", url.getHost());
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

        // If a username and a password is provided we support basic auth
        if ((username != null) && (password != null)) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            int port = url.getPort();
            httpClient.getState()
                    .setCredentials(new AuthScope(url.getHost(), port), creds);
        }
        return httpClient.executeMethod(method);
    }

    private static String serializeObjectArray(Object[] obj) {
        StringWriter json = new StringWriter();
        json.append("[");
        boolean first = true;
        for (Object value : obj) {
            if (!first) {
                json.append(", ");
            } else {
                first = false;
            }
            json.append(serializeJSON(value));
        }
        json.append("]");
        return json.toString();
    }

    private static String serializeNativeDate(Scriptable obj) {
        Double time = (Double) ScriptableObject.callMethod(obj, "getTime", new Object[0]);
        Date date = new Date(Math.round(time));
        return dateFormat.format(date);
    }

    private static String serializeNativeError(Scriptable object) {
        Object o = object.get("rhinoException", object);
        if (o == null) {
            o = object.get("javaException", object);
        }

        if (o instanceof Wrapper) {
            o = ((Wrapper) o).unwrap();
        }

        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            StringWriter errors = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errors));
            return errors.toString();
        }

        return serializeJSON(o);
    }

    private static String serializeString(String obj) {
        return "\"" + obj.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n")
                .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029") + "\"";
    }

    private static String serializeXML(ScriptableObject obj) {
        return (String) ScriptableObject.callMethod(obj, "toXMLString", new Object[0]);
    }

    private static String serializeNativeArray(NativeArray obj) {
        StringWriter json = new StringWriter();
        json.append("[");
        Object[] ids = obj.getIds();
        boolean first = true;
        for (Object id : ids) {
            if (!(id instanceof Integer)) {
                continue;
            }
            Object value = obj.get((Integer) id, obj);
            if (!first) {
                json.append(", ");
            } else {
                first = false;
            }
            json.append(serializeJSON(value));
        }
        json.append("]");
        return json.toString();
    }

    private static String serializeNativeObject(NativeObject obj) {
        StringWriter json = new StringWriter();
        json.append("{");
        Object[] ids = obj.getIds();
        boolean first = true;
        for (Object id : ids) {
            String key = (!(id instanceof String)) ? id.toString() : (String) id;
            Object value = (id instanceof Integer) ? obj.get((Integer) id, obj) : obj.get((String) id, obj);
            if (!first) {
                json.append(", ");
            } else {
                first = false;
            }
            json.append("\"").append(key).append("\" : ").append(serializeJSON(value));
        }
        json.append("}");
        return json.toString();
    }
}