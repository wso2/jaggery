package org.jaggeryjs.scriptengine.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.EngineConstants;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;


public class RhinoTopLevel extends ImporterTopLevel {

    private static final Log log = LogFactory.getLog(RhinoTopLevel.class);

    public RhinoTopLevel(Context context, boolean sealed) {
        super(context, sealed);
    }

    public static Object parse(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "parse";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME,
                    functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(EngineConstants.GLOBAL_OBJECT_NAME,
                    EngineConstants.GLOBAL_OBJECT_NAME, "1", "string", args[0], true);
        }
        return HostObjectUtil.parseJSON(thisObj, (String) args[0]);
    }

    public static String stringify(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "stringify";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(EngineConstants.GLOBAL_OBJECT_NAME,
                    functionName, argsCount, false);
        }
        return HostObjectUtil.serializeJSON(args[0]);
    }
}
