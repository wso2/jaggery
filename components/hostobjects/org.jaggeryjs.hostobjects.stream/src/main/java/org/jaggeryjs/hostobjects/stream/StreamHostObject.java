package org.jaggeryjs.hostobjects.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(StreamHostObject.class);

    private static final String hostObjectName = "Stream";
    private InputStream stream = null;

    public StreamHostObject() {

    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }

        StreamHostObject sho = new StreamHostObject();
        Object obj = args[0];
        if (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }

        if (obj instanceof String) {
            sho.stream = new ByteArrayInputStream(((String) args[0]).getBytes());
            
        } else if (obj instanceof InputStream) {
            sho.stream = (InputStream) obj;
        } else {
            HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "1", "string", args[0], true);
        }
        return sho;
    }

    public static String jsFunction_toString(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "toString";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        StreamHostObject fho = (StreamHostObject) thisObj;
        return HostObjectUtil.streamToString(fho.stream);
    }
    

/*
    public static void jsFunction_pipe(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "pipe";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        Object obj = args[0];
        StreamHostObject fho = (StreamHostObject) thisObj;
        OutputStream out = null;
        if (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }
        if (obj instanceof OutputStream) {
            out = (OutputStream) obj;
        } else if (obj instanceof FileHostObject) {
            FileHostObject file = (FileHostObject) obj;
            out = file.getOutputStream();
        }
        if (out == null) {
            throw new ScriptException("Unable to pipe the stream. Please specify an out stream.");
        }
        try {
            IOUtils.copyLarge(fho.getStream(), out);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }*/

    public static Object jsFunction_getStream(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getStream";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        StreamHostObject sho = (StreamHostObject) thisObj;
        return sho.getStream();
    }

    public InputStream getStream() {
        return stream;
    }
}
