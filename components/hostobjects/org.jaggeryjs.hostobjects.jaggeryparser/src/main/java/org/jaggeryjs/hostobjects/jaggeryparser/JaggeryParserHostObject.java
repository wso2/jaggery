package org.jaggeryjs.hostobjects.jaggeryparser;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.stream.StreamHostObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JaggeryParserHostObject extends ScriptableObject {

    public static final String HOSTOBJECT_NAME = "JaggeryParser";

    private static final Log log = LogFactory.getLog(JaggeryParserHostObject.class);

    @Override
    public String getClassName() {
        return HOSTOBJECT_NAME;
    }

    /**
     * Constructor for JaggeryParser host object
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, HOSTOBJECT_NAME, argsCount, true);
        }
        return new JaggeryParserHostObject();
    }

    /**
     * Parses a Jaggery string | stream passed into the parse method and returns a parsed stream
     */
    public static Scriptable jsFunction_parse(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "parse";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        InputStream source = null;
        if(args[0] instanceof String) {
            source = new ByteArrayInputStream(((String)args[0]).getBytes());
        } else if(args[0] instanceof StreamHostObject) {
            source = ((StreamHostObject) args[0]).getStream();
        } else {
            HostObjectUtil.invalidArgsError(HOSTOBJECT_NAME, functionName, "1", "string|stream", args[0], false);
        }
        return  cx.newObject(thisObj, "Stream", new Object[]{JaggeryParser.parse(source)});
    }
}
