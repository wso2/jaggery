package org.jaggeryjs.hostobjects.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;

import java.io.ByteArrayInputStream;
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
        if(args[0] instanceof String) {
            sho.stream = new ByteArrayInputStream(((String)args[0]).getBytes());
        } else if (args[0] instanceof InputStream) {
            sho.stream = (InputStream) args[0];
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

    public InputStream getStream() {
        return stream;
    }
}
