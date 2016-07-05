package org.jaggeryjs.jaggery.core.manager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.engine.*;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.security.RhinoSecurityController;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Shares the common functionality, of initialization of functions, and Host Objects
 */
public final class CommandLineManager {

    private static final Log log = LogFactory.getLog(CommandLineManager.class);
    private static final RhinoEngine RHINO_ENGINE;

    public static final int ENV_COMMAND_LINE = 1;
    public static final String JAGGERY_CONTEXT = "jaggeryContext";

    private CommandLineManager(String jaggeryDir) throws ScriptException {
        CommonManager.getInstance().initialize(jaggeryDir, null);
    }

    static {
        RHINO_ENGINE = new RhinoEngine(new CacheManager(null), new RhinoContextFactory(new RhinoSecurityController()));
        initEngine();
    }

    public static RhinoEngine getCommandLineEngine() {
        return RHINO_ENGINE;
    }

    protected static void initEngine() {
        try {
            InputStream inputStream = CommandLineManager.class.
                    getClassLoader().getResourceAsStream("META-INF/hostobjects.xml");
            StAXOMBuilder builder = new StAXOMBuilder(inputStream);
            OMElement document = builder.getDocumentElement();
            Iterator itr = document.getChildrenWithLocalName("hostObject");
            String msg = "Error while registering HostObject : ";
            while (itr.hasNext()) {
                OMElement hostObject = (OMElement) itr.next();
                String name = hostObject.getFirstChildWithName(new QName(null, "name")).getText();
                String className = hostObject.getFirstChildWithName(new QName(null, "className")).getText();
                JavaScriptHostObject ho = new JavaScriptHostObject(name);
                try {
                    ho.setClazz(Class.forName(className));
                    RHINO_ENGINE.defineHostObject(ho);
                } catch (ClassNotFoundException e) {
                    msg += name + " " + e.getMessage();
                    log.error(msg, e);
                }
            }
        } catch (XMLStreamException e) {
            log.error("Error while reading the hostobjects.xml", e);
        }
        initGlobalProperties();
        //engine.sealEngine();
    }

    private static void initGlobalProperties() {
        JavaScriptMethod method = new JavaScriptMethod("print");
        method.setMethodName("print");
        method.setClazz(CommandLineManager.class);
        RHINO_ENGINE.defineMethod(method);
    }

    /**
     * Method responsible of writing to the output stream
     */
    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "print";
        JaggeryContext jaggeryContext = CommandLineManager.getJaggeryContext();
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs("RhinoTopLevel", functionName, argsCount, false);
        }
        PrintWriter writer = new PrintWriter((OutputStream) jaggeryContext.getProperty(
                CommonManager.JAGGERY_OUTPUT_STREAM));
        writer.write(HostObjectUtil.serializeObject(args[0]));
        writer.flush();
    }

    public static JaggeryContext getJaggeryContext() {
        return (JaggeryContext) RhinoEngine.getContextProperty(CommandLineManager.JAGGERY_CONTEXT);
    }

    public static void setJaggeryContext(JaggeryContext jaggeryContext) {
        RhinoEngine.putContextProperty(CommandLineManager.JAGGERY_CONTEXT, jaggeryContext);
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public static void include(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "include";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(CommonManager.HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }

        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        Stack<String> includesCallstack = CommonManager.getCallstack(jaggeryContext);
        Map<String, Boolean> includedScripts = CommonManager.getIncludes(jaggeryContext);
        String parent = includesCallstack.lastElement();
        String fileURL = (String) args[0];

        if (CommonManager.isHTTP(fileURL) || CommonManager.isHTTP(parent)) {
            CommonManager.include(cx, thisObj, args, funObj);
            return;
        }

        ScriptableObject scope = jaggeryContext.getScope();
        RhinoEngine engine = jaggeryContext.getEngine();

        if (fileURL.startsWith("/")) {
            fileURL = includesCallstack.firstElement() + fileURL;
        } else {
            fileURL = FilenameUtils.getFullPath(parent) + fileURL;
        }
        fileURL = FilenameUtils.normalize(fileURL);
        if (includesCallstack.search(fileURL) != -1) {
            return;
        }
        ScriptReader source = null;
        try {
            source = new ScriptReader(new FileInputStream(fileURL));
            includedScripts.put(fileURL, true);
            includesCallstack.push(fileURL);
            engine.exec(source, scope, null);
            includesCallstack.pop();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public static void include_once(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "include_once";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(CommonManager.HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(CommonManager.HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }

        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        Stack<String> includesCallstack = CommonManager.getCallstack(jaggeryContext);
        Map<String, Boolean> includedScripts = CommonManager.getIncludes(jaggeryContext);
        String parent = includesCallstack.lastElement();
        String fileURL = (String) args[0];

        if (CommonManager.isHTTP(fileURL) || CommonManager.isHTTP(parent)) {
            CommonManager.include_once(cx, thisObj, args, funObj);
            return;
        }

        ScriptableObject scope = jaggeryContext.getScope();
        RhinoEngine engine = jaggeryContext.getEngine();

        if (fileURL.startsWith("/")) {
            fileURL = includesCallstack.firstElement() + fileURL;
        } else {
            fileURL = FilenameUtils.getFullPath(parent) + fileURL;
        }
        fileURL = FilenameUtils.normalize(fileURL);
        if (includesCallstack.search(fileURL) != -1) {
            return;
        }
        if (includedScripts.get(fileURL) != null) {
            return;
        }

        try {
            ScriptReader source = new ScriptReader(new FileInputStream(fileURL));
            includedScripts.put(fileURL, true);
            includesCallstack.push(fileURL);
            engine.exec(source, scope, null);
            includesCallstack.pop();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

}
