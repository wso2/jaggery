package org.jaggeryjs.jaggery.core.manager;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.engine.*;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.security.RhinoSecurityController;
import org.jaggeryjs.scriptengine.security.RhinoSecurityDomain;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModuleManager {

    private static final String NAMESPACE = "namespace";

    private static final String EXPOSE = "expose";

    private static final String NAME = "name";

    private static final String READ_ONLY = "readOnly";

    private static final Log log = LogFactory.getLog(ModuleManager.class);

    public static final String MODULE_NAMESPACE = "http://wso2.org/projects/jaggery/module.xml";

    private static final String MODULE_FILE = "module.xml";

    private static final String MODULE_REFRESH = "modRefresh";

    private final Map<String, JavaScriptModule> modules = new HashMap<String, JavaScriptModule>();

    private String modulesDir = null;

    private static boolean isModuleRefreshEnabled;

    static {
        isModuleRefreshEnabled = "true".equalsIgnoreCase(System.getProperty(MODULE_REFRESH));
    }

    public ModuleManager(String modulesDir) throws ScriptException {
        this.modulesDir = modulesDir;
        init();
    }

    public static boolean isModuleRefreshEnabled() {
        return isModuleRefreshEnabled;
    }

    private void init() throws ScriptException {
        //load framework modules, we use jaggery.home to check whether it is a pure jaggery server
        // and loads even core modules from modules directory. Else, from the modules.xml
        InputStream xmlStream = ModuleManager.class.
                getClassLoader().getResourceAsStream("META-INF/" + MODULE_FILE);
        initModule(xmlStream, false);

        RhinoEngine.enterGlobalContext();
        //load user-defined modules
        File modulesDir = new File(this.modulesDir);
        File[] modules = modulesDir.listFiles();
        if (modules != null) {
            for (File module : modules) {
                loadModule(module);
            }
        }
        RhinoEngine.exitContext();
    }

    private void loadModule(File module) throws ScriptException {
        if (module.isDirectory()) {
            File moduleConfig = new File(module, MODULE_FILE);
            if (moduleConfig.exists()) {
                try {
                    initModule(new FileInputStream(moduleConfig), true);
                } catch (FileNotFoundException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public Map<String, JavaScriptModule> getModules() {
        return this.modules;
    }

    public JavaScriptModule getModule(String name) throws ScriptException {
        if (isModuleRefreshEnabled) {
            this.modules.remove(name);
            File module = new File(this.modulesDir + File.separator + name);
            loadModule(module);
        }
        return this.modules.get(name);
    }

    private void initModule(InputStream modulesXML, boolean isCustom) throws ScriptException {
        try {
            Context cx = RhinoEngine.enterGlobalContext();
            StAXOMBuilder builder = new StAXOMBuilder(modulesXML);
            OMElement document = builder.getDocumentElement();

            OMNamespace ns = document.getNamespace();
            if (ns == null || !MODULE_NAMESPACE.equals(document.getNamespace().getNamespaceURI())) {
                log.warn("A module xml found without the proper namespace");
                return;
            }

            String moduleName = document.getAttributeValue(new QName(null, NAME));

            if (modules.get(moduleName) != null) {
                log.info("A module with the name : " + moduleName + " already exists and it will be overwritten.");
            }

            String namespace = document.getAttributeValue(new QName(null, NAMESPACE));
            boolean expose = "true".equalsIgnoreCase(document.getAttributeValue(new QName(null, EXPOSE)));

            JavaScriptModule module = new JavaScriptModule(moduleName);
            module.setNamespace(namespace);
            module.setExpose(expose);

            initHostObjects(document, module);
            initMethods(document, module);
            initScripts(document, cx, module, isCustom);

            modules.put(moduleName, module);
        } catch (XMLStreamException e) {
            String msg = "Error while reading the module.xml";
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } finally {
            RhinoEngine.exitContext();
        }
    }

    private void initScripts(OMElement moduleOM, Context cx, JavaScriptModule module, boolean isCustom)
            throws ScriptException {
        String name = null;
        String path = null;
        JavaScriptScript script;
        Iterator itr = moduleOM.getChildrenWithName(new QName(MODULE_NAMESPACE, "script"));
        while (itr.hasNext()) {
            try {
                //process methods
                OMElement scriptOM = (OMElement) itr.next();
                name = scriptOM.getFirstChildWithName(
                        new QName(MODULE_NAMESPACE, NAME)).getText();
                path = scriptOM.getFirstChildWithName(
                        new QName(MODULE_NAMESPACE, "path")).getText();
                script = new JavaScriptScript(name);

                Reader reader;
                final String fileName;
                ScriptCachingContext sctx;
                if (isCustom) {
                    String filteredPath = filterPath(path);
                    fileName = modulesDir + File.separator + module.getName() +
                            File.separator + filterPath(path);
                    reader = new FileReader(fileName);
                    int endIndex = filteredPath.lastIndexOf(File.separator);
                    sctx = new ScriptCachingContext(
                            String.valueOf(MultitenantConstants.SUPER_TENANT_ID),
                            '<' + module.getName() + '>',
                            filteredPath.substring(0, endIndex),
                            filteredPath.substring(endIndex));
                } else {
                    reader = new InputStreamReader(ModuleManager.class.getClassLoader().getResourceAsStream(path));
                    fileName = modulesDir + File.separator + name;
                    int endIndex = path.lastIndexOf('/');
                     sctx = new ScriptCachingContext(
                            String.valueOf(MultitenantConstants.SUPER_TENANT_ID),
                            "<<" +  name + ">>",
                            '/' + path.substring(0, endIndex),
                            path.substring(endIndex));
                }
                CacheManager cacheManager = new CacheManager(null);

                sctx.setSecurityDomain(new RhinoSecurityDomain() {
                    @Override
                    public CodeSource getCodeSource() throws ScriptException {
                        try {
                            URL url = new File(fileName).getCanonicalFile().toURI().toURL();
                            return new CodeSource(url, (Certificate[]) null);
                        } catch (MalformedURLException e) {
                            throw new ScriptException(e);
                        } catch (IOException e) {
                            throw new ScriptException(e);
                        }
                    }
                });
                sctx.setSourceModifiedTime(1);

                Script cachedScript = cacheManager.getScriptObject(reader, sctx);
                if (cachedScript == null) {
                    cacheManager.cacheScript(reader, sctx);
                    cachedScript = cacheManager.getScriptObject(reader, sctx);
                }
                script.setScript(cachedScript);
                module.addScript(script);
            } catch (FileNotFoundException e) {
                String msg = "Error executing script. Script cannot be found, name : " + name + ", path : " + path;
                log.error(msg, e);
                throw new ScriptException(msg, e);
            }
        }
    }

    private void initMethods(OMElement moduleOM, JavaScriptModule module) throws ScriptException {
        String name = null;
        String className = null;
        OMAttribute attribute;
        JavaScriptMethod method;
        Iterator itr = moduleOM.getChildrenWithName(new QName(MODULE_NAMESPACE, "method"));
        while (itr.hasNext()) {
            try {
                //process methods
                OMElement methodOM = (OMElement) itr.next();
                name = methodOM.getFirstChildWithName(
                        new QName(MODULE_NAMESPACE, NAME)).getText();
                className = methodOM.getFirstChildWithName(
                        new QName(MODULE_NAMESPACE, "className")).getText();
                attribute = methodOM.getAttribute(new QName(MODULE_NAMESPACE, READ_ONLY));
                method = new JavaScriptMethod(name);
                method.setClazz(Class.forName(className));
                method.setMethodName(name);
                if (attribute != null) {
                    method.setAttribute("true".equals(attribute.getAttributeValue()) ?
                            ScriptableObject.READONLY : ScriptableObject.PERMANENT);
                }
                module.addMethod(method);
            } catch (ClassNotFoundException e) {
                String msg = "Error registering method. Class cannot be found, name : " +
                        name + ", class : " + className;
                log.error(msg, e);
            }
        }
    }

    private void initHostObjects(OMElement moduleOM, JavaScriptModule jaggeryModule) throws ScriptException {
        Iterator itr = moduleOM.getChildrenWithName(new QName(MODULE_NAMESPACE, "hostObject"));
        String msg = "Error while adding HostObject : ";
        String name;
        String className;
        OMAttribute attribute;
        JavaScriptHostObject hostObject;
        while (itr.hasNext()) {
            //process hostobject
            OMElement hostObjectOM = (OMElement) itr.next();
            name = hostObjectOM.getFirstChildWithName(
                    new QName(MODULE_NAMESPACE, NAME)).getText();
            className = hostObjectOM.getFirstChildWithName(
                    new QName(MODULE_NAMESPACE, "className")).getText();
            attribute = hostObjectOM.getAttribute(new QName(MODULE_NAMESPACE, READ_ONLY));
            hostObject = new JavaScriptHostObject(name);
            if (attribute != null) {
                hostObject.setAttribute("true".equals(attribute.getAttributeValue()) ?
                        ScriptableObject.READONLY : ScriptableObject.PERMANENT);
            }
            try {
                hostObject.setClazz(Class.forName(className));
                jaggeryModule.addHostObject(hostObject);
            } catch (ClassNotFoundException e) {
                msg += name + " " + e.getMessage();
                log.error(msg, e);
            }
        }
    }

    private String filterPath(String path) {
        String pathToReturn = path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
        if (!pathToReturn.startsWith(File.separator)) {
            pathToReturn = File.separator + pathToReturn;
        }
        return pathToReturn;
    }

}
