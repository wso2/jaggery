package org.jaggeryjs.hostobjects.uri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.wso2.uri.template.URITemplate;
import org.wso2.uri.template.URITemplateException;

import java.util.HashMap;
import java.util.Map;


/**
 * A usecase would be as following
 * <p/>
 * var urlMatcher = new URIMatcher(request.uri);
 * if(urlMatcher.match('/{id}/{name}')) {
 * log(urlMatcher.elements.id)
 * }
 */
public class URIMatcherHostObject extends ScriptableObject {
    private static final Log log = LogFactory.getLog(URIMatcherHostObject.class);

    private static final String hostObjectName = "URIMatcher";

    private String uriToBeMatched;

    private NativeObject uriParts;

    private Context cx;

    public URIMatcherHostObject() {
    }

    /**
     * URIMatcher constructor that takes the URI to be matched as the argument
     *
     * @param cx
     * @param args
     * @param ctorObj
     * @param inNewExpr
     * @return
     * @throws ScriptException
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, "URIMatcher", "1", "string", args[0], false);
        }
        URIMatcherHostObject uriho = new URIMatcherHostObject();
        uriho.uriToBeMatched = (String) args[0];
        uriho.cx = cx;
        return uriho;
    }

    /**
     * Match function that takes the URI template as an argument
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static NativeObject jsFunction_match(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "match";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs("RhinoTopLevel", functionName, argsCount, false);
        }

        String template = (String) args[0];
        URIMatcherHostObject uriho = (URIMatcherHostObject) thisObj;
        Map<String, String> urlParts = new HashMap<String, String>();

        try {
            URITemplate uriTemplate = new URITemplate(template);
            boolean uriMatch = uriTemplate.matches(uriho.uriToBeMatched, urlParts);
            if (!uriMatch) {
                return null;
            }
        } catch (URITemplateException e) {
            throw new ScriptException(e);
        }

        NativeObject nobj = new NativeObject();
        for (Map.Entry<String, String> entry : urlParts.entrySet()) {
            nobj.defineProperty(entry.getKey(), entry.getValue(), NativeObject.READONLY);
        }

        uriho.uriParts = nobj;

        return nobj;
    }

    public static NativeObject jsFunction_elements(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "elements";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs("RhinoTopLevel", functionName, argsCount, false);
        }
        URIMatcherHostObject uriho = (URIMatcherHostObject) thisObj;
        return uriho.uriParts;
    }


    @Override
    public String getClassName() {
        return hostObjectName;
    }
}
