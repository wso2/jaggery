package org.jaggeryjs.hostobjects.xhr;


import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * supports FormData interface defined in https://dvcs.w3.org/hg/xhr/raw-file/tip/Overview.html section
 * '5 Interface FormData'.
 */
public class FormDataHostObject extends ScriptableObject implements Iterable<Map.Entry<String, String>> {
    private static final String HOST_OBJECT_NAME = "FormData";
    private Map<String, String> entities = new LinkedHashMap<String, String>();
    private Context context;

    @Override
    public String getClassName() {
        return HOST_OBJECT_NAME;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME, HOST_OBJECT_NAME, argsCount, true);
        }
        FormDataHostObject fd = new FormDataHostObject();
        fd.context = cx;
        return fd;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return Collections.unmodifiableMap(entities).entrySet().iterator();
    }

    public static void jsFunction_append(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        FormDataHostObject fd = (FormDataHostObject) thisObj;
        int argsCount = args.length;
        if (argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME, "append", argsCount, false);
        } else {
            Object key = args[0];
            Object value = args[1];
            if (key instanceof String && value instanceof String) {
                fd.entities.put((String) key, (String) value);
            } else {
                throw new UnsupportedOperationException("non-String multi part bodies are not supported yet.");
            }
        }
    }
}
