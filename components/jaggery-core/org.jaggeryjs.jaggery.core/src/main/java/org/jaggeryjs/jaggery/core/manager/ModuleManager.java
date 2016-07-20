package org.jaggeryjs.jaggery.core.manager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.HostObject;
import org.jaggeryjs.jaggery.core.Method;
import org.jaggeryjs.jaggery.core.Module;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.engine.*;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.security.RhinoSecurityDomain;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
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

    Map<String, JavaScriptModule> getModules() {
        return this.modules;
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    JavaScriptModule getModule(String name) throws ScriptException {
        if (isModuleRefreshEnabled()) {
            this.modules.remove(name);
            File module = new File(this.modulesDir + File.separator + name);
            loadModule(module);
        }
        return this.modules.get(name);
    }

    private void initModule(InputStream modulesXML, boolean isCustom) throws ScriptException {
        try {
            Context cx = RhinoEngine.enterGlobalContext();

            JAXBContext jaxbContext = JAXBContext.newInstance(Module.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Module moduleObject = (Module) jaxbUnmarshaller.unmarshal(modulesXML);
            String moduleName = moduleObject.getName();

            if (modules.get(moduleName) != null) {
                log.info("A module with the name : " + moduleName + " already exists and it will be overwritten.");
            }

            String namespace = moduleObject.getNamespace();
            boolean expose = (Boolean.parseBoolean(moduleObject.getExpose()) == true);

            JavaScriptModule module = new JavaScriptModule(moduleName);
            module.setNamespace(namespace);
            module.setExpose(expose);

            initHostObjects(moduleObject, module);
            initMethods(moduleObject, module);
            initScripts(moduleObject, cx, module, isCustom);

            modules.put(moduleName, module);

        } catch (JAXBException e) {
            String msg = "Error while reading the module.xml";
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } finally {
            RhinoEngine.exitContext();
        }
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    private void initScripts(Module moduleObject, Context cx, JavaScriptModule module, boolean isCustom)
            throws ScriptException {
        String name = null;
        String path = null;
        JavaScriptScript script;
        List scriptList = moduleObject.getScripts();
        Iterator itr = scriptList.iterator();
        while (itr.hasNext()) {
            try {
                //process methods
                org.jaggeryjs.jaggery.core.Script scriptObject = (org.jaggeryjs.jaggery.core.Script) itr.next();
                name = scriptObject.getName();
                path = scriptObject.getPath();
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
                    sctx = new ScriptCachingContext(String.valueOf(MultitenantConstants.SUPER_TENANT_ID),
                            '<' + module.getName() + '>', filteredPath.substring(0, endIndex),
                            filteredPath.substring(endIndex));
                } else {
                    reader = new InputStreamReader(ModuleManager.class.getClassLoader().getResourceAsStream(path));
                    fileName = modulesDir + File.separator + name;
                    int endIndex = path.lastIndexOf('/');
                    sctx = new ScriptCachingContext(String.valueOf(MultitenantConstants.SUPER_TENANT_ID),
                            "<<" + name + ">>", '/' + path.substring(0, endIndex), path.substring(endIndex));
                }
                CacheManager cacheManager = new CacheManager(null);

                sctx.setSecurityDomain(new RhinoSecurityDomain() {
                    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
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

    private void initMethods(Module moduleObject, JavaScriptModule module) throws ScriptException {
        String name = null;
        String className = null;
        String attribute;
        JavaScriptMethod method;
        List methodsList = moduleObject.getMethods();
        Iterator itr = methodsList.iterator();
        while (itr.hasNext()) {
            try {
                //process methods
                Method methodObject = (Method) itr.next();
                name = methodObject.getName();
                className = methodObject.getClassName();
                attribute = methodObject.getReadOnly();
                method = new JavaScriptMethod(name);
                method.setClazz(Class.forName(className));
                method.setMethodName(name);
                if (attribute != null) {
                    method.setAttribute((Boolean.parseBoolean(attribute) == true) ?
                            ScriptableObject.READONLY :
                            ScriptableObject.PERMANENT);
                }
                module.addMethod(method);
            } catch (ClassNotFoundException e) {
                String msg = "Error registering method. Class cannot be found, name : " +
                        name + ", class : " + className;
                log.error(msg, e);
            }
        }
    }

    private void initHostObjects(Module moduleObject, JavaScriptModule jaggeryModule) throws ScriptException {
        List hostObjectList = moduleObject.getHostObjects();
        Iterator itr = hostObjectList.iterator();
        String msg = "Error while adding HostObject : ";
        String name;
        String className;
        String attribute;
        JavaScriptHostObject hostObject;
        while (itr.hasNext()) {
            //process hostobject
            HostObject hostObjectObject = (HostObject) itr.next();
            name = hostObjectObject.getName();
            className = hostObjectObject.getClassName();
            attribute = hostObjectObject.getReadOnly();
            hostObject = new JavaScriptHostObject(name);
            if (attribute != null) {
                hostObject.setAttribute((Boolean.parseBoolean(attribute) == true) ?
                        ScriptableObject.READONLY :
                        ScriptableObject.PERMANENT);
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
