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
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.manager.CommonManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
//import org.wso2.carbon.context.ApplicationContext;
//import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.core.session.CarbonTomcatClusterableSessionManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.webapp.mgt.*;

import java.io.*;
import java.util.*;

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
     * @param webappsHolder    JaggeryApplicationsHolder
     */
    public TomcatJaggeryWebappsDeployer(String webContextPrefix,
                                        int tenantId,
                                        String tenantDomain,
                                        WebApplicationsHolder webappsHolder,
                                        ConfigurationContext configurationContext) {
        super(webContextPrefix, tenantId, tenantDomain, webappsHolder, configurationContext);
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
                    (JaggeryApplication) webappsHolder.getStartedWebapps().get(webappFile.getName());
            JaggeryApplication undeployedWebapp =
                    (JaggeryApplication) webappsHolder.getStoppedWebapps().get(webappFile.getName());
            JaggeryApplication faultyWebapp =
                    (JaggeryApplication) webappsHolder.getFaultyWebapps().get(webappFile.getName());
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
        } catch(Throwable t){
            log.error("Error while Tomact jaggery web apps Deployment " ,t);
            }
       
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
        ServletParameter jaggeryServletParameter = new ServletParameter();
        ServletParameter jsspParameter = new ServletParameter();

        jaggeryServletParameter.setServletName(JaggeryConstants.JAGGERY_SERVLET_NAME);
        jaggeryServletParameter.setServletClass(JaggeryConstants.JAGGERY_SERVLET_CLASS);

        jsspParameter.setServletName(JaggeryConstants.JSSP_NAME);
        jsspParameter.setServletClass(JaggeryConstants.JSSP_CLASS);
        jsspParameter.setLoadOnStartup(2);
        HashMap<String, String> jsspInitParamMap = new HashMap<String, String>();
        jsspInitParamMap.put("fork", "false");
        jsspParameter.setInitParams(jsspInitParamMap);

        List<ServletParameter> servletParameters =
                new ArrayList<ServletParameter>();
        servletParameters.add(jaggeryServletParameter);
        servletParameters.add(jsspParameter);

        ServletMappingParameter jaggeryServletMappingParameter = new ServletMappingParameter();
        ServletMappingParameter jsspMappingParameter = new ServletMappingParameter();

        jaggeryServletMappingParameter.setServletName(JaggeryConstants.JAGGERY_SERVLET_NAME);
        jaggeryServletMappingParameter.setUrlPattern(JaggeryConstants.JAGGERY_SERVLET_URL_PATTERN);

        SecurityConstraint securityConstraint = new SecurityConstraint();
        securityConstraint.setAuthConstraint(true);

        SecurityCollection securityCollection = new SecurityCollection();
        securityCollection.setName("ConfigDir");
        securityCollection.setDescription("Jaggery Configuration Dir");
        securityCollection.addPattern("/" + JaggeryConstants.JAGGERY_CONF_FILE);

        securityConstraint.addCollection(securityCollection);

        List<ServletMappingParameter> servletMappingParameters =
                new ArrayList<ServletMappingParameter>();
        servletMappingParameters.add(jaggeryServletMappingParameter);
        servletMappingParameters.add(jsspMappingParameter);

        try {
            JSONObject jaggeryConfigObj = readJaggeryConfig(webappFile);

            Tomcat tomcat = DataHolder.getCarbonTomcatService().getTomcat();

            Context context =
                    DataHolder.getCarbonTomcatService().addWebApp(contextStr, webappFile.getAbsolutePath(),
                            new JaggeryConfListener(tomcat, servletParameters, servletMappingParameters, jaggeryConfigObj, securityConstraint));
            //deploying web app for url mapping inside virtual host
            if (DataHolder.getHotUpdateService() != null) {
                List<String> hostNames = DataHolder.getHotUpdateService().getMappigsPerWebapp(contextStr);
                for (String hostName : hostNames) {
                    Host host = DataHolder.getHotUpdateService().addHost(hostName);
/*                    ApplicationContext.getCurrentApplicationContext().putUrlMappingForApplication(hostName, contextStr);
  */                  Context contextForHost =
                            DataHolder.getCarbonTomcatService().addWebApp(host, "/", webappFile.getAbsolutePath(),
                                    new JaggeryConfListener(tomcat, servletParameters, servletMappingParameters, jaggeryConfigObj, securityConstraint));
                    log.info("Deployed JaggeryApp on host: " + contextForHost);
                }
            }

            if (isDistributable(context, jaggeryConfigObj)) {
                //Clusterable manager implementation as DeltaManager
                context.setDistributable(true);
                CarbonTomcatClusterableSessionManager sessionManager =
                        new CarbonTomcatClusterableSessionManager(tenantId);
                context.setManager(sessionManager);
                sessionManagerMap.put(context.getName(), sessionManager);
                configurationContext.setProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP,
                        sessionManagerMap);
            } else {
                context.setManager(new CarbonTomcatSessionManager(tenantId));
            }

            context.setReloadable(true);
            JaggeryApplication webapp = new JaggeryApplication(this, context, webappFile);
            webapp.setServletParameters(servletParameters);
            webapp.setServletMappingParameters(servletMappingParameters);
            webapp.setServletContextParameters(webContextParams);
            webapp.setState("Started");
            webappsHolder.getStartedWebapps().put(filename, webapp);
            webappsHolder.getFaultyWebapps().remove(filename);
            registerApplicationEventListeners(applicationEventListeners, context);
            log.info("Deployed webapp: " + webapp);
        } catch (Throwable e) {
            //catching a Throwable here to avoid web-apps crashing the server during startup
            StandardContext context = new StandardContext();
            context.setName(webappFile.getName());
            JaggeryApplication webapp = new JaggeryApplication(this, context, webappFile);
            webapp.setProperty(WebappsConstants.WEBAPP_FILTER, JaggeryConstants.JAGGERY_WEBAPP_FILTER_PROP);
            String msg = "Error while deploying webapp: " + webapp;
            log.error(msg, e);
            webapp.setFaultReason(new Exception(msg, e));
            webappsHolder.getFaultyWebapps().put(filename, webapp);
            webappsHolder.getStartedWebapps().remove(filename);
            throw new CarbonException(msg, e);
        }
    }

    private static class JaggeryConfListener implements LifecycleListener {
        private List<ServletParameter> servletParameters;
        private List<ServletMappingParameter> servletMappingParameters;
        private JSONObject jaggeryConfig;
        private Tomcat tomcat;
        private SecurityConstraint securityConstraint;

        private JaggeryConfListener(Tomcat tomcat, List<ServletParameter> servletParameters,
                                    List<ServletMappingParameter> servletMappingParameters,
                                    JSONObject jaggeryConfig, SecurityConstraint securityConstraint) {
            this.servletParameters = servletParameters;
            this.servletMappingParameters = servletMappingParameters;
            this.jaggeryConfig = jaggeryConfig;
            this.tomcat = tomcat;
            this.securityConstraint = securityConstraint;
        }

        public void lifecycleEvent(LifecycleEvent event) {
            if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
                initJaggeryappDefaults((Context) event.getLifecycle(), this.tomcat,
                        this.jaggeryConfig, this.servletParameters, this.servletMappingParameters, this.securityConstraint);
            }
            if (Lifecycle.START_EVENT.equals(event.getType())) {
                setDisplayName(((Context) event.getLifecycle()), jaggeryConfig);
            }
        }
    }

    private static void initJaggeryappDefaults(Context ctx, Tomcat tomcat,
                                               JSONObject jaggeryConfig, List<ServletParameter> servletParameters,
                                               List<ServletMappingParameter> servletMappingParameters, SecurityConstraint securityConstraint) {

        for (ServletParameter servletParameter : servletParameters) {
            if (servletParameter.getServletName() != null && servletParameter.getServletClass() != null) {
                Wrapper servletWrapper = tomcat.addServlet(
                        ctx, servletParameter.getServletName(), servletParameter.getServletClass());

                if (servletParameter.getInitParams() != null) {
                    for (Map.Entry<String, String> entry : servletParameter.getInitParams().entrySet()) {
                        servletWrapper.addInitParameter(entry.getKey(), entry.getValue());
                    }
                }

                if (servletParameter.getLoadOnStartup() != 0) {
                    servletWrapper.setLoadOnStartup(servletParameter.getLoadOnStartup());
                }
            }
        }

        for (ServletMappingParameter servletMappingParameter : servletMappingParameters) {
            if (servletMappingParameter.getServletName() != null && servletMappingParameter.getUrlPattern() != null) {
                ctx.addServletMapping(servletMappingParameter.getUrlPattern(), servletMappingParameter.getServletName());
            }
        }

        ctx.addConstraint(securityConstraint);
        addWelcomeFiles(ctx, jaggeryConfig);
        //jaggery conf params if null conf is not available
        if (jaggeryConfig != null) {
            setDisplayName(ctx, jaggeryConfig);
            addErrorPages(ctx, jaggeryConfig);
            addSecurityConstraints(ctx, jaggeryConfig);
            setLoginConfig(ctx, jaggeryConfig);
            addSecurityRoles(ctx, jaggeryConfig);
            addUrlMappings(ctx, jaggeryConfig);
            addParameters(ctx, jaggeryConfig);
        }
    }

    private JSONObject readJaggeryConfig(File f) throws IOException {

        File confFile = new File(f.getAbsolutePath() + File.separator + JaggeryConstants.JAGGERY_CONF_FILE);

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

    private static void addErrorPages(Context context, JSONObject obj) {
        JSONObject arr = (JSONObject) obj.get(JaggeryConstants.JaggeryConfigParams.ERROR_PAGES);
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
        JSONObject loginObj = (JSONObject) obj.get(JaggeryConstants.JaggeryConfigParams.LOGIN_CONFIG);
        if (loginObj != null) {
            if (loginObj.get(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD).equals(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_FORM)) {
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_FORM);
                loginConfig.setLoginPage((String) ((JSONObject) loginObj.get(JaggeryConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG)).get(JaggeryConstants.JaggeryConfigParams.FORM_LOGIN_PAGE));
                loginConfig.setErrorPage((String) ((JSONObject) loginObj.get(JaggeryConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG)).get(JaggeryConstants.JaggeryConfigParams.FORM_ERROR_PAGE));
                context.setLoginConfig(loginConfig);

            } else if (loginObj.get(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD).equals(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_BASIC)) {
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_BASIC);
                context.setLoginConfig(loginConfig);

            }
        }
    }

    private static void addSecurityConstraints(Context context, JSONObject obj) {
        JSONArray arr = (JSONArray) obj.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINTS);
        if (arr != null) {
            for (Object anArr : arr) {
                JSONObject o = (JSONObject) anArr;
                SecurityConstraint securityConstraint = new SecurityConstraint();
                if (((JSONObject) o.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION) != null) {
                    JSONObject resCollection = (JSONObject) ((JSONObject) o.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION);
                    SecurityCollection secCollection = new SecurityCollection();
                    secCollection.setName((String) resCollection.get(JaggeryConstants.JaggeryConfigParams.WEB_RES_NAME));

                    JSONArray arrPattern = (JSONArray) resCollection.get(JaggeryConstants.JaggeryConfigParams.URL_PATTERNS);
                    for (Object anArrPattern : arrPattern) {
                        secCollection.addPattern((String) anArrPattern);
                    }

                    JSONArray methods = (JSONArray) resCollection.get(JaggeryConstants.JaggeryConfigParams.HTTP_METHODS);
                    if (methods != null) {
                        for (Object method : methods) {
                            secCollection.addMethod((String) method);
                        }
                    }

                    securityConstraint.addCollection(secCollection);
                }

                if (((JSONObject) o.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.AUTH_ROLES) != null) {
                    JSONArray roles = (JSONArray) ((JSONObject) o.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.AUTH_ROLES);
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
        JSONArray arr = (JSONArray) obj.get(JaggeryConstants.JaggeryConfigParams.SECURITY_ROLES);
        if (arr != null) {
            for (Object role : arr) {
                context.addSecurityRole((String) role);
            }
        }
    }

    private static void setDisplayName(Context context, JSONObject obj) {
        if(obj == null) {
            return;
        }
        String dName = (String) obj.get(JaggeryConstants.JaggeryConfigParams.DISPLAY_NAME);
        if (dName != null) {
            context.setDisplayName(dName);
        }
    }

    private static boolean isDistributable(Context context, JSONObject obj) {
        if (obj != null) {
            if (obj.get(JaggeryConstants.JaggeryConfigParams.DISTRIBUTABLE)
                    instanceof Boolean) {
                Boolean isDistributable = (Boolean) obj.get(JaggeryConstants.
                        JaggeryConfigParams.DISTRIBUTABLE);
                if (isDistributable != null) {
                    return isDistributable.booleanValue();
                }
            } else if (obj.get(JaggeryConstants.JaggeryConfigParams.DISTRIBUTABLE)
                    instanceof String) {
                String distributable = (String) obj.get(JaggeryConstants.
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
            JSONArray arr = (JSONArray) obj.get(JaggeryConstants.JaggeryConfigParams.WELCOME_FILES);
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
        JSONArray arr = (JSONArray) obj.get(JaggeryConstants.JaggeryConfigParams.URL_MAPPINGS);
        if (arr != null) {
            Map<String, Object> urlMappings = new HashMap<String, Object>();
            for (Object mapObj : arr) {
                JSONObject mapping = (JSONObject) mapObj;
                String url = (String) mapping.get(JaggeryConstants.JaggeryConfigParams.URL_MAPPINGS_URL);
                String path = (String) mapping.get(JaggeryConstants.JaggeryConfigParams.URL_MAPPINGS_PATH);
                if (url != null && path != null) {
                    path = path.startsWith("/") ? path : "/" + path;
                    context.addServletMapping(url, JaggeryConstants.JAGGERY_SERVLET_NAME);
                    if (url.equals("/")) {
                        urlMappings.put("/", path);
                        continue;
                    }
                    url = url.startsWith("/") ? url.substring(1) : url;
                    List<String> parts = new ArrayList<String>(Arrays.asList(url.split("/")));
                    addMappings(urlMappings, parts, path);
                } else {
                    log.error("Invalid url mapping in jaggery.conf url : " + url + ", path : " + path);
                }
            }
            context.getServletContext().setAttribute(CommonManager.JAGGERY_URLS_MAP, urlMappings);
        }
    }

    private static void addMappings(Map<String, Object> map, List<String> parts, String path) {
        String part = parts.remove(0);
        if (parts.isEmpty()) {
            Object obj = map.get(part);
            if (obj != null) {
                log.error("Conflicting url patterns for the path : " + path);
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

