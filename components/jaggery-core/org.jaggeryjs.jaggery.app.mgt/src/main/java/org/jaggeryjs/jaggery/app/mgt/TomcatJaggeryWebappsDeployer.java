/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jaggeryjs.jaggery.app.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.*;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.log.LogHostObject;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.jaggery.core.manager.CommonManager;
import org.jaggeryjs.jaggery.core.manager.JaggerySecurityDomain;
import org.jaggeryjs.jaggery.core.manager.WebAppManager;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.session.CarbonTomcatClusterableSessionManager;
import org.wso2.carbon.webapp.mgt.*;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.webapp.mgt.utils.WebAppUtils;


import javax.servlet.ServletContext;
import java.io.*;
import java.util.*;

//import org.wso2.carbon.context.ApplicationContext;
//import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;

/**
 * This deployer is responsible for deploying/undeploying/updating those Jaggery apps.
 */

public class TomcatJaggeryWebappsDeployer extends TomcatGenericWebappsDeployer {

    private static Log log = LogFactory.getLog(TomcatJaggeryWebappsDeployer.class);

    /**
     * Constructor
     *
     * @param webContextPrefix The Web context prefix
     * @param tenantId         The tenant ID of the tenant to whom this deployer belongs to
     * @param tenantDomain     The tenant domain of the tenant to whom this deployer belongs to
     * @param webApplicationsHolderMap    JaggeryApplicationsHolder
     */
    public TomcatJaggeryWebappsDeployer(String webContextPrefix,
                                        int tenantId,
                                        String tenantDomain,
                                        Map<String, WebApplicationsHolder> webApplicationsHolderMap,
                                        ConfigurationContext configurationContext) {
        super(webContextPrefix, tenantId, tenantDomain, webApplicationsHolderMap, configurationContext);
    }

    /**
     * Deploy webapps
     *
     * @param webappFile                The webapp file to be deployed
     * @param webContextParams          context-params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    public void deploy(File webappFile,
                       List<WebContextParameter> webContextParams,
                       List<Object> applicationEventListeners) throws CarbonException {

        try {

            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            long lastModifiedTime = webappFile.lastModified();
            long configLastModified = 0;
            if (JaggeryDeploymentUtil.getConfig(webappFile) != null) {
                configLastModified = JaggeryDeploymentUtil.getConfig(webappFile).lastModified();
            }

            JaggeryApplication deployedWebapp =
                    (JaggeryApplication) (WebAppUtils.getWebappHolder(webappFile.getAbsolutePath(),
                            configurationContext)).getStartedWebapps().get(webappFile.getName());
            JaggeryApplication undeployedWebapp =
                    (JaggeryApplication) (WebAppUtils.getWebappHolder(webappFile.getAbsolutePath(),
                            configurationContext)).getStoppedWebapps().get(webappFile.getName());
            JaggeryApplication faultyWebapp =
                    (JaggeryApplication) (WebAppUtils.getWebappHolder(webappFile.getAbsolutePath(),
                            configurationContext)).getFaultyWebapps().get(webappFile.getName());
            if (deployedWebapp == null && faultyWebapp == null && undeployedWebapp == null) {
                handleHotDeployment(webappFile, webContextParams, applicationEventListeners);
            } else if (deployedWebapp != null && deployedWebapp.getLastModifiedTime() != lastModifiedTime &&
                    (configLastModified != 0 && deployedWebapp.getConfigDirLastModifiedTime() != configLastModified)) {
                undeploy(webappFile);
                handleHotDeployment(webappFile, webContextParams, applicationEventListeners);
            } else if (faultyWebapp != null && faultyWebapp.getLastModifiedTime() != lastModifiedTime &&
                    (configLastModified != 0 && faultyWebapp.getConfigDirLastModifiedTime() != configLastModified)) {
                handleHotDeployment(webappFile, webContextParams, applicationEventListeners);
            }
        } catch (Throwable t) {
            log.error("Error while Tomact jaggery web apps Deployment ", t);
        }
    }

    public void undeploy(File webappFile) throws CarbonException {
        //TODO : handle undeployment
        super.undeploy(webappFile);
    }

    /**
     * Handle the deployment of a an archive Jaggery app. i.e., a WAR
     *
     * @param webapp                    The WAR Jaggery app file
     * @param webContextParams          ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    protected void handleZipWebappDeployment(File webapp,
                                             List<WebContextParameter> webContextParams,
                                             List<Object> applicationEventListeners)
            throws CarbonException {
        synchronized (this) {
            String appPath = webapp.getAbsolutePath().substring(0, webapp.getAbsolutePath().indexOf(".zip"));
            try {
                JaggeryDeploymentUtil.unZip(new FileInputStream(webapp), appPath);
                if (!webapp.delete()) {
                    throw new CarbonException(appPath + "could not be deleted");
                }
            } catch (FileNotFoundException e) {
                throw new CarbonException(e);
            }
            File unzippedWebapp = new File(appPath);
            handleExplodedWebappDeployment(unzippedWebapp, webContextParams, applicationEventListeners);
        }
    }

    private void registerApplicationEventListeners(List<Object> applicationEventListeners,
                                                   Context context) {
        Object[] originalEventListeners = context.getApplicationEventListeners();
        Object[] newEventListeners = new Object[originalEventListeners.length + applicationEventListeners.size()];
        if (originalEventListeners.length != 0) {
            System.arraycopy(originalEventListeners, 0, newEventListeners, 0, originalEventListeners.length);
            int i = originalEventListeners.length;
            for (Object eventListener : applicationEventListeners) {
                newEventListeners[i++] = eventListener;
            }
        } else {
            newEventListeners =
                    applicationEventListeners.toArray(new Object[applicationEventListeners.size()]);
        }
        context.setApplicationEventListeners(newEventListeners);
    }

    /**
     * Deployment procedure of Jaggery apps
     *
     * @param webappFile                The Jaggery app file to be deployed
     * @param contextStr                jaggery app context string
     * @param webContextParams          context-params for this Jaggery app
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    protected void handleWebappDeployment(File webappFile, String contextStr,
                                          List<WebContextParameter> webContextParams,
                                          List<Object> applicationEventListeners) throws CarbonException {

        String filename = webappFile.getName();
        ArrayList<Object> listeners = new ArrayList<Object>(1);
        // listeners.add(new CarbonServletRequestListener());

        SecurityConstraint securityConstraint = new SecurityConstraint();
        securityConstraint.setAuthConstraint(true);

        SecurityCollection securityCollection = new SecurityCollection();
        securityCollection.setName("ConfigDir");
        securityCollection.setDescription("Jaggery Configuration Dir");
        securityCollection.addPattern("/" + JaggeryCoreConstants.JAGGERY_CONF_FILE);

        securityConstraint.addCollection(securityCollection);
        WebApplicationsHolder webApplicationsHolder =
                WebAppUtils.getWebappHolder(webappFile.getAbsolutePath(),configurationContext);

        try {
            JSONObject jaggeryConfigObj = readJaggeryConfig(webappFile);

            Tomcat tomcat = DataHolder.getCarbonTomcatService().getTomcat();

            Context context =
                    DataHolder.getCarbonTomcatService().addWebApp(contextStr, webappFile.getAbsolutePath(),
                            new JaggeryConfListener(jaggeryConfigObj, securityConstraint));
            //deploying web app for url mapping inside virtual host
            	 if (DataHolder.getHotUpdateService() != null) {
                List<String> hostNames = DataHolder.getHotUpdateService().getMappigsPerWebapp(contextStr);
                for (String hostName : hostNames) {
                    Host host = DataHolder.getHotUpdateService().addHost(hostName);
/*                    ApplicationContext.getCurrentApplicationContext().putUrlMappingForApplication(hostName, contextStr);
  */
                    Context contextForHost =
                            DataHolder.getCarbonTomcatService().addWebApp(host, "/", webappFile.getAbsolutePath(),
                                    new JaggeryConfListener(jaggeryConfigObj, securityConstraint));
                    log.info("Deployed JaggeryApp on host: " + contextForHost);
                }
            }

            if (isDistributable(context, jaggeryConfigObj)) {
                //Clusterable manager implementation as DeltaManager
                context.setDistributable(true);
                CarbonTomcatClusterableSessionManager sessionManager =
                        new CarbonTomcatClusterableSessionManager(tenantId);
                context.setManager(sessionManager);                
                
                Object alreadyinsertedSMMap = configurationContext.getProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP);
                if(alreadyinsertedSMMap != null){
                	((Map<String, CarbonTomcatClusterableSessionManager>) alreadyinsertedSMMap).put(context.getName(), sessionManager);
                }else{
                	sessionManagerMap.put(context.getName(), sessionManager);
                	configurationContext.setProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP,
                            sessionManagerMap);
                }
                
            } else {
                context.setManager(new CarbonTomcatSessionManager(tenantId));
            }

            context.setReloadable(true);
            JaggeryApplication webapp = new JaggeryApplication(this, context, webappFile);
            webapp.setServletContextParameters(webContextParams);
            webapp.setState("Started");
            webApplicationsHolder.getStartedWebapps().put(filename, webapp);
            webApplicationsHolder.getFaultyWebapps().remove(filename);
            registerApplicationEventListeners(applicationEventListeners, context);
            log.info("Deployed webapp: " + webapp);
        } catch (Throwable e) {
            //catching a Throwable here to avoid web-apps crashing the server during startup
            StandardContext context = new StandardContext();
            context.setName(webappFile.getName());
            context.addParameter(WebappsConstants.FAULTY_WEBAPP, "true");
            JaggeryApplication webapp = new JaggeryApplication(this, context, webappFile);
            webapp.setProperty(WebappsConstants.WEBAPP_FILTER, JaggeryConstants.JAGGERY_WEBAPP_FILTER_PROP);
            String msg = "Error while deploying webapp: " + webapp;
            log.error(msg, e);
            webapp.setFaultReason(new Exception(msg, e));
            webApplicationsHolder.getFaultyWebapps().put(filename, webapp);
            webApplicationsHolder.getStartedWebapps().remove(filename);
            throw new CarbonException(msg, e);
        }
    }

    private static class JaggeryConfListener implements LifecycleListener {
        private JSONObject jaggeryConfig;
        private SecurityConstraint securityConstraint;

        private JaggeryConfListener(JSONObject jaggeryConfig, SecurityConstraint securityConstraint) {
            this.jaggeryConfig = jaggeryConfig;
            this.securityConstraint = securityConstraint;
        }

        public void lifecycleEvent(LifecycleEvent event) {
            if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
                initJaggeryappDefaults((Context) event.getLifecycle(), this.jaggeryConfig, this.securityConstraint);
                return;
            }
            if (Lifecycle.START_EVENT.equals(event.getType())) {
                Context context = (Context) event.getLifecycle();
                try {
                    WebAppManager.getEngine().enterContext();
                    WebAppManager.deploy(context);
                    setDisplayName(context, jaggeryConfig);
                    if (jaggeryConfig != null) {
                        addSessionCreatedListners(context,
                                (JSONArray) jaggeryConfig.get(JaggeryCoreConstants.JaggeryConfigParams.SESSION_CREATED_LISTENER_SCRIPTS));
                        addSessionDestroyedListners(context,
                                (JSONArray) jaggeryConfig.get(JaggeryCoreConstants.JaggeryConfigParams.SESSION_DESTROYED_LISTENER_SCRIPTS));
                        executeScripts(context,
                                (JSONArray) jaggeryConfig.get(JaggeryCoreConstants.JaggeryConfigParams.INIT_SCRIPTS));
                        addUrlMappings(context, jaggeryConfig);
                    }
                } catch (ScriptException e) {
                    log.error(e.getMessage(), e);
                    try {
                        context.destroy();
                    } catch (LifecycleException e1) {
                        log.error(e1.getMessage(), e1);
                    }
                } finally {
                    RhinoEngine.exitContext();
                }
                return;
            }
            if (Lifecycle.STOP_EVENT.equals(event.getType())) {
                Context context = (Context) event.getLifecycle();
                try {
                    WebAppManager.getEngine().enterContext();
                    WebAppManager.undeploy(context);
                    if (jaggeryConfig != null) {
                        executeScripts(context,
                                (JSONArray) jaggeryConfig.get(JaggeryCoreConstants.JaggeryConfigParams.DESTROY_SCRIPTS));
                    }
                } catch (ScriptException e) {
                    log.error(e.getMessage(), e);
                } finally {
                    RhinoEngine.exitContext();
                }
                return;
            }
        }
    }

    private static void initJaggeryappDefaults(Context ctx, JSONObject jaggeryConfig, SecurityConstraint securityConstraint) {

        Tomcat.addServlet(ctx, JaggeryCoreConstants.JAGGERY_SERVLET_NAME, JaggeryCoreConstants.JAGGERY_SERVLET_CLASS);
        Tomcat.addServlet(ctx, JaggeryCoreConstants.JAGGERY_WEBSOCKET_SERVLET_NAME, JaggeryCoreConstants.JAGGERY_WEBSOCKET_SERVLET_CLASS);

        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(JaggeryCoreConstants.JAGGERY_FILTER_NAME);
        filterDef.setFilterClass(JaggeryCoreConstants.JAGGERY_FILTER_CLASS);
        ctx.addFilterDef(filterDef);

        FilterMap filter1mapping = new FilterMap();
        filter1mapping.setFilterName(JaggeryCoreConstants.JAGGERY_FILTER_NAME);
        filter1mapping.addURLPattern(JaggeryCoreConstants.JAGGERY_URL_PATTERN);
        ctx.addFilterMap(filter1mapping);

        ctx.addApplicationListener(JaggeryCoreConstants.JAGGERY_APPLICATION_SESSION_LISTENER);

        ctx.addConstraint(securityConstraint);
        addWelcomeFiles(ctx, jaggeryConfig);
        //jaggery conf params if null conf is not available
        if (jaggeryConfig != null) {
            setDisplayName(ctx, jaggeryConfig);
            addErrorPages(ctx, jaggeryConfig);
            addSecurityConstraints(ctx, jaggeryConfig);
            setLoginConfig(ctx, jaggeryConfig);
            addSecurityRoles(ctx, jaggeryConfig);
           // addUrlMappings(ctx, jaggeryConfig);
            addParameters(ctx, jaggeryConfig);
            addLogLevel(ctx, jaggeryConfig);
        }
    }

    private JSONObject readJaggeryConfig(File f) throws IOException {

        File confFile = new File(f.getAbsolutePath() + File.separator + JaggeryCoreConstants.JAGGERY_CONF_FILE);

        if (!confFile.exists()) {
            return null;
        }

        String jsonString = "";
        if (!confFile.isDirectory()) {
            FileInputStream fis = new FileInputStream(confFile);
            StringWriter writer = new StringWriter();
            IOUtils.copy(fis, writer, null);
            jsonString = writer.toString();
        }

        return (JSONObject) JSONValue.parse(jsonString);
    }

    private static void addSessionCreatedListners(Context context, JSONArray arr) {
        if (arr != null) {
            try {
                JaggeryContext sharedContext = WebAppManager.sharedJaggeryContext(context.getServletContext());
                CommonManager.setJaggeryContext(sharedContext);
                RhinoEngine engine = sharedContext.getEngine();
                org.mozilla.javascript.Context cx = engine.enterContext();
                ServletContext servletContext = (ServletContext) sharedContext.getProperty(
                        org.jaggeryjs.hostobjects.web.Constants.SERVLET_CONTEXT);

                List<String> jsListeners = new ArrayList<String>();

                Object[] scripts = arr.toArray();
                for (Object script : scripts) {

                    if (!(script instanceof String)) {
                        log.error("Invalid value for initScripts/destroyScripts in jaggery.conf : " + script);
                        continue;
                    }
                    String path = (String) script;
                    path = path.startsWith("/") ? path : "/" + path;
                    Stack<String> callstack = CommonManager.getCallstack(sharedContext);
                    callstack.push(path);

                    jsListeners.add(path);
                }

                servletContext.setAttribute(JaggeryCoreConstants.JS_CREATED_LISTENERS, jsListeners);

            } finally {
                if (org.mozilla.javascript.Context.getCurrentContext() != null) {
                    RhinoEngine.exitContext();
                }
            }
        }
    }

    private static void addSessionDestroyedListners(Context context, JSONArray arr) {
        if (arr != null) {
            try {
                JaggeryContext sharedContext = WebAppManager.sharedJaggeryContext(context.getServletContext());
                CommonManager.setJaggeryContext(sharedContext);
                RhinoEngine engine = sharedContext.getEngine();
                org.mozilla.javascript.Context cx = engine.enterContext();
                ServletContext servletContext = (ServletContext) sharedContext.getProperty(
                        org.jaggeryjs.hostobjects.web.Constants.SERVLET_CONTEXT);

                List<String> jsListeners = new ArrayList<String>();

                Object[] scripts = arr.toArray();
                for (Object script : scripts) {

                    if (!(script instanceof String)) {
                        log.error("Invalid value for initScripts/destroyScripts in jaggery.conf : " + script);
                        continue;
                    }
                    String path = (String) script;
                    path = path.startsWith("/") ? path : "/" + path;
                    Stack<String> callstack = CommonManager.getCallstack(sharedContext);
                    callstack.push(path);

                    jsListeners.add(path);
                }

                servletContext.setAttribute(JaggeryCoreConstants.JS_DESTROYED_LISTENERS, jsListeners);

            } finally {
                if (org.mozilla.javascript.Context.getCurrentContext() != null) {
                    RhinoEngine.exitContext();
                }
            }
        }
    }

    private static void executeScripts(Context context, JSONArray arr) {
        if (arr != null) {
            try {
                JaggeryContext sharedContext = WebAppManager.sharedJaggeryContext(context.getServletContext());
                CommonManager.setJaggeryContext(sharedContext);
                RhinoEngine engine = sharedContext.getEngine();
                org.mozilla.javascript.Context cx = engine.enterContext();
                ServletContext servletContext = (ServletContext) sharedContext.getProperty(
                        org.jaggeryjs.hostobjects.web.Constants.SERVLET_CONTEXT);
                ScriptableObject sharedScope = sharedContext.getScope();

                Object[] scripts = arr.toArray();
                for (Object script : scripts) {
                    if (!(script instanceof String)) {
                        log.error("Invalid value for initScripts/destroyScripts in jaggery.conf : " + script);
                        continue;
                    }
                    String path = (String) script;
                    path = path.startsWith("/") ? path : "/" + path;
                    Stack<String> callstack = CommonManager.getCallstack(sharedContext);
                    callstack.push(path);
                    String[] parts = WebAppManager.getKeys(servletContext.getContextPath(), path, path);
                    ScriptCachingContext sctx = new ScriptCachingContext(sharedContext.getTenantId(),
                            parts[0], parts[1], parts[2]);
                    sctx.setSecurityDomain(new JaggerySecurityDomain(path, servletContext));
                    engine.exec(new ScriptReader(servletContext.getResourceAsStream(path)) {
                        @Override
                        protected void build() throws IOException {
                            try {
                                sourceReader = new StringReader(HostObjectUtil.streamToString(sourceIn));
                            } catch (ScriptException e) {
                                throw new IOException(e);
                            }
                        }
                    }, sharedScope, sctx);
                }
            } catch (ScriptException e) {
                log.error(e.getMessage(), e);
            } finally {
                if (org.mozilla.javascript.Context.getCurrentContext() != null) {
                    RhinoEngine.exitContext();
                }
            }
        }
    }

    private static void addErrorPages(Context context, JSONObject obj) {
        JSONObject arr = (JSONObject) obj.get(JaggeryCoreConstants.JaggeryConfigParams.ERROR_PAGES);
        if (arr != null) {
            for (Object keys : arr.keySet()) {
                ErrorPage errPage = new ErrorPage();
                errPage.setErrorCode((String) keys);
                errPage.setLocation((String) arr.get(keys));
                context.addErrorPage(errPage);
            }
        }
    }

    private static void setLoginConfig(Context context, JSONObject obj) {
        JSONObject loginObj = (JSONObject) obj.get(JaggeryCoreConstants.JaggeryConfigParams.LOGIN_CONFIG);
        if (loginObj != null) {
            if (loginObj.get(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD).equals(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD_FORM)) {
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD_FORM);
                loginConfig.setLoginPage((String) ((JSONObject) loginObj.get(JaggeryCoreConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG)).get(JaggeryCoreConstants.JaggeryConfigParams.FORM_LOGIN_PAGE));
                loginConfig.setErrorPage((String) ((JSONObject) loginObj.get(JaggeryCoreConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG)).get(JaggeryCoreConstants.JaggeryConfigParams.FORM_ERROR_PAGE));
                context.setLoginConfig(loginConfig);
            } else if (loginObj.get(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD).equals(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD_BASIC)) {
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD_BASIC);
                context.setLoginConfig(loginConfig);
            }
        }
    }

    private static void addSecurityConstraints(Context context, JSONObject obj) {
        JSONArray arr = (JSONArray) obj.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINTS);
        if (arr != null) {
            for (Object anArr : arr) {
                JSONObject o = (JSONObject) anArr;
                SecurityConstraint securityConstraint = new SecurityConstraint();
                if (((JSONObject) o.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryCoreConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION) != null) {
                    JSONObject resCollection = (JSONObject) ((JSONObject) o.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryCoreConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION);
                    SecurityCollection secCollection = new SecurityCollection();
                    secCollection.setName((String) resCollection.get(JaggeryCoreConstants.JaggeryConfigParams.WEB_RES_NAME));

                    JSONArray arrPattern = (JSONArray) resCollection.get(JaggeryCoreConstants.JaggeryConfigParams.URL_PATTERNS);
                    for (Object anArrPattern : arrPattern) {
                        secCollection.addPattern((String) anArrPattern);
                    }

                    JSONArray methods = (JSONArray) resCollection.get(JaggeryCoreConstants.JaggeryConfigParams.HTTP_METHODS);
                    if (methods != null) {
                        for (Object method : methods) {
                            secCollection.addMethod((String) method);
                        }
                    }

                    securityConstraint.addCollection(secCollection);
                }

                if (((JSONObject) o.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryCoreConstants.JaggeryConfigParams.AUTH_ROLES) != null) {
                    JSONArray roles = (JSONArray) ((JSONObject) o.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryCoreConstants.JaggeryConfigParams.AUTH_ROLES);
                    for (Object role : roles) {
                        securityConstraint.addAuthRole((String) role);
                    }
                    securityConstraint.setAuthConstraint(true);
                }

                context.addConstraint(securityConstraint);
            }
        }
    }

    private static void addSecurityRoles(Context context, JSONObject obj) {
        JSONArray arr = (JSONArray) obj.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_ROLES);
        if (arr != null) {
            for (Object role : arr) {
                context.addSecurityRole((String) role);
            }
        }
    }

    private static void setDisplayName(Context context, JSONObject obj) {
        if (obj == null) {
            return;
        }
        String dName = (String) obj.get(JaggeryCoreConstants.JaggeryConfigParams.DISPLAY_NAME);
        if (dName != null) {
            context.setDisplayName(dName);
        }
    }

    private static boolean isDistributable(Context context, JSONObject obj) {
        if (obj != null) {
            if (obj.get(JaggeryCoreConstants.JaggeryConfigParams.DISTRIBUTABLE)
                    instanceof Boolean) {
                Boolean isDistributable = (Boolean) obj.get(JaggeryCoreConstants.
                        JaggeryConfigParams.DISTRIBUTABLE);
                if (isDistributable != null) {
                    return isDistributable.booleanValue();
                }
            } else if (obj.get(JaggeryCoreConstants.JaggeryConfigParams.DISTRIBUTABLE)
                    instanceof String) {
                String distributable = (String) obj.get(JaggeryCoreConstants.
                        JaggeryConfigParams.DISTRIBUTABLE);
                return (distributable != null && distributable.equalsIgnoreCase("true"));
            }
        }

        return false;
    }

    private static void addParameters(Context context, JSONObject obj) {
        if (obj != null) {
            Iterator<?> keys = obj.keySet().iterator();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (obj.get(key) instanceof String) {
                    context.addParameter(key, (String) obj.get(key));
                }
            }
        }
    }

    private static void addWelcomeFiles(Context context, JSONObject obj) {
        if (obj != null) {
            JSONArray arr = (JSONArray) obj.get(JaggeryCoreConstants.JaggeryConfigParams.WELCOME_FILES);
            if (arr != null) {
                for (Object role : arr) {
                    context.addWelcomeFile((String) role);
                }
            } else {
                context.addWelcomeFile("index.jag");
                context.addWelcomeFile("index.html");
            }
        } else {
            context.addWelcomeFile("index.jag");
            context.addWelcomeFile("index.html");
        }
    }

	private static void addUrlMappings(Context context, JSONObject obj) {
		Object test = context.getServletContext().getAttribute("org.jaggeryjs.serveFunction");
		JSONArray arr = null;
		if (test != null) {
			// URL mapping for progamticaly
			arr = new JSONArray();
			JSONObject js1 = new JSONObject();
			JSONObject js2 = new JSONObject();
			js1.put("url", "/*");
			js1.put("path", "/index.jag");
			arr.add(js1);
		} else {
			arr = (JSONArray) obj
					.get(JaggeryCoreConstants.JaggeryConfigParams.URL_MAPPINGS);
		}
		if (arr != null) {
			Map<String, Object> urlMappings = new HashMap<String, Object>();
			for (Object mapObj : arr) {
				JSONObject mapping = (JSONObject) mapObj;
				String url = (String) mapping
						.get(JaggeryCoreConstants.JaggeryConfigParams.URL_MAPPINGS_URL);
				String path = (String) mapping
						.get(JaggeryCoreConstants.JaggeryConfigParams.URL_MAPPINGS_PATH);
				if (url != null && path != null) {
					path = path.startsWith("/") ? path : "/" + path;
					FilterMap filterMap = new FilterMap();
					filterMap
							.setFilterName(JaggeryCoreConstants.JAGGERY_FILTER_NAME);
					filterMap.addURLPattern(url);
					context.addFilterMap(filterMap);
					if (url.equals("/")) {
						urlMappings.put("/", path);
						continue;
					}
					url = url.startsWith("/") ? url.substring(1) : url;
					List<String> parts = new ArrayList<String>(
							Arrays.asList(url.split("/", -1)));
					addMappings(urlMappings, parts, path);
				} else {
					log.error("Invalid url mapping in jaggery.conf url : "
							+ url + ", path : " + path);
				}
			}

			
				context.getServletContext().setAttribute(
						CommonManager.JAGGERY_URLS_MAP, urlMappings);
			
		}
	}

    private static void addLogLevel(Context cx, JSONObject jaggeryConfig) {
        String level = (String) jaggeryConfig.get(JaggeryCoreConstants.JaggeryConfigParams.LOG_LEVEL);
        if (level == null) {
            return;
        }
        ServletContext context = cx.getServletContext();
        context.setAttribute(LogHostObject.LOG_LEVEL, level);
    }

    private static void addMappings(Map<String, Object> map, List<String> parts, String path) {
        String part = parts.remove(0);
        if (parts.isEmpty()) {
            Object obj = map.get(part);
            if (obj != null) {
                log.error("Conflicting url patterns for the path : " + path);
                return;
            }
            if (part.startsWith("*")) {
                int dotIndex = part.lastIndexOf(".");
                if (dotIndex != -1) {
                    if (part.length() == dotIndex + 1) {
                        log.error("Extension cannot be found for the url pattern for " + path);
                        return;
                    }
                    String ext = part.substring(dotIndex + 1);
                    Object exts = map.get("*");
                    if (exts instanceof String) {
                        log.error("* wildcard mapping is already existing for " + path);
                        return;
                    }

                    Map<String, String> extsMap;
                    if (exts == null) {
                        extsMap = new HashMap<String, String>();
                    } else {
                        extsMap = (Map<String, String>) exts;
                        if (extsMap.get(ext) != null) {
                            log.error("Url mapping is already existing for " + path);
                            return;
                        }
                    }
                    extsMap.put(ext, path);
                    map.put(part, extsMap);
                    return;
                }
                map.put(part, path);
                return;
            }
            map.put(part, path);
            return;
        }
        Map<String, Object> childMap;
        Object obj = map.get(part);
        if (obj instanceof Map) {
            childMap = (Map<String, Object>) obj;
        } else {
            childMap = new HashMap<String, Object>();
            map.put(part, childMap);
            if (obj instanceof String) {
                childMap.put("/", obj);
            }
        }
        addMappings(childMap, parts, path);
    }
}

