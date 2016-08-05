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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Manager;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;
import org.jaggeryjs.jaggery.core.manager.JaggeryDeployerManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.session.CarbonTomcatClusterableSessionManager;
import org.wso2.carbon.webapp.mgt.*;
import org.wso2.carbon.webapp.mgt.utils.WebAppUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * @param webContextPrefix         The Web context prefix
     * @param tenantId                 The tenant ID of the tenant to whom this deployer belongs to
     * @param tenantDomain             The tenant domain of the tenant to whom this deployer belongs to
     * @param webApplicationsHolderMap JaggeryApplicationsHolder
     */
    public TomcatJaggeryWebappsDeployer(String webContextPrefix, int tenantId, String tenantDomain,
                                        Map<String, WebApplicationsHolder> webApplicationsHolderMap, ConfigurationContext configurationContext) {
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
    public void deploy(File webappFile, List<WebContextParameter> webContextParams,
                       List<Object> applicationEventListeners) throws CarbonException {

        try {

            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            long lastModifiedTime = webappFile.lastModified();
            long configLastModified = 0;
            if (JaggeryDeploymentUtil.getConfig(webappFile) != null) {
                configLastModified = JaggeryDeploymentUtil.getConfig(webappFile).lastModified();
            }

            JaggeryApplication deployedWebapp = (JaggeryApplication) (WebAppUtils
                    .getWebappHolder(webappFile.getAbsolutePath(), configurationContext)).getStartedWebapps()
                    .get(webappFile.getName());
            JaggeryApplication undeployedWebapp = (JaggeryApplication) (WebAppUtils
                    .getWebappHolder(webappFile.getAbsolutePath(), configurationContext)).getStoppedWebapps()
                    .get(webappFile.getName());
            JaggeryApplication faultyWebapp = (JaggeryApplication) (WebAppUtils
                    .getWebappHolder(webappFile.getAbsolutePath(), configurationContext)).getFaultyWebapps()
                    .get(webappFile.getName());
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
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    @SuppressWarnings("unchecked")
    protected void handleZipWebappDeployment(File webapp,
                                             List<WebContextParameter> webContextParams, List<Object> applicationEventListeners)
            throws CarbonException {
        synchronized (this) {
            String appPath = webapp.getAbsolutePath().substring(0, webapp.getAbsolutePath().indexOf(".zip"));
            String excludeJsonProperty = JaggeryCoreConstants.JaggeryConfigParams.EXCLUDE_FROM_DEPLOYMENT;
            JSONObject jsonConf = null;
            File webAppRoot = null;
            boolean doUnzip = true;
            try {
                for (Map.Entry<String, WebApplicationsHolder> entry : webApplicationsHolderMap.entrySet()) {
                    WebApplicationsHolder value = entry.getValue();
                    for (Map.Entry<String, WebApplication> holder : value.getStartedWebapps().entrySet()) {
                        WebApplication holderValue = holder.getValue();
                        webAppRoot = holderValue.getWebappFile();
                        if (appPath.contains(webAppRoot.getAbsolutePath())) {
                            jsonConf = readJaggeryConfig(webAppRoot);
                            break;
                        }
                    }
                }

                if (jsonConf != null) {
                    Object excludedDirListJson = jsonConf.get(excludeJsonProperty);
                    List<String> dirListToExclude = excludedDirListJson == null ? null : (List<String>) excludedDirListJson;
                    if (dirListToExclude != null) {
                        for (String excludedDir : dirListToExclude) {
                            String relativePath = appPath.substring(webAppRoot.getAbsolutePath().length(), appPath.length());
                            if (relativePath.contains(excludedDir)) {
                                doUnzip = false;
                                break;
                            }
                        }
                    }
                }

                if (doUnzip) {
                    JaggeryDeploymentUtil.unZip(new FileInputStream(webapp), appPath);
                    if (!webapp.delete()) {
                        throw new CarbonException(appPath + "could not be deleted");
                    }
                }
            } catch (IOException e) {
                throw new CarbonException(e);
            } catch (ClassCastException e) {
                throw new CarbonException(e);
            } catch (NullPointerException e) {
                throw new CarbonException(e);
            }

            if (doUnzip) {
                File unzippedWebapp = new File(appPath);
                handleExplodedWebappDeployment(unzippedWebapp, webContextParams, applicationEventListeners);
            }
        }
    }

    /**
     * Register event listeners for application.
     *
     * @param applicationEventListeners List of application listeners
     * @param context                   Current context
     */
    private void registerApplicationEventListeners(List<Object> applicationEventListeners, Context context) {
        Object[] originalEventListeners = context.getApplicationEventListeners();
        Object[] newEventListeners = new Object[originalEventListeners.length + applicationEventListeners.size()];
        if (originalEventListeners.length != 0) {
            System.arraycopy(originalEventListeners, 0, newEventListeners, 0, originalEventListeners.length);
            int i = originalEventListeners.length;
            for (Object eventListener : applicationEventListeners) {
                newEventListeners[i++] = eventListener;
            }
        } else {
            newEventListeners = applicationEventListeners.toArray(new Object[applicationEventListeners.size()]);
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
                                          List<WebContextParameter> webContextParams, List<Object> applicationEventListeners) throws CarbonException {

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
        WebApplicationsHolder webApplicationsHolder = WebAppUtils
                .getWebappHolder(webappFile.getAbsolutePath(), configurationContext);

        try {
            JSONObject jaggeryConfigObj = readJaggeryConfig(webappFile);

            Tomcat tomcat = DataHolder.getCarbonTomcatService().getTomcat();

            Context context = DataHolder.getCarbonTomcatService().addWebApp(contextStr, webappFile.getAbsolutePath(),
                    new JaggeryDeployerManager.JaggeryConfListener(jaggeryConfigObj, securityConstraint));
            //deploying web app for url mapping inside virtual host
            if (DataHolder.getHotUpdateService() != null) {
                List<String> hostNames = DataHolder.getHotUpdateService().getMappigsPerWebapp(contextStr);
                for (String hostName : hostNames) {
                    Host host = DataHolder.getHotUpdateService().addHost(hostName);
/*                    ApplicationContext.getCurrentApplicationContext().putUrlMappingForApplication(hostName, contextStr);
  */
                    Context contextForHost = DataHolder.getCarbonTomcatService()
                            .addWebApp(host, "/", webappFile.getAbsolutePath(),
                                    new JaggeryDeployerManager.JaggeryConfListener(jaggeryConfigObj,
                                            securityConstraint));
                    log.info("Deployed JaggeryApp on host: " + contextForHost);
                }
            }

            Manager manager = context.getManager();
            if (isDistributable(context, jaggeryConfigObj)) {
                //Clusterable manager implementation as DeltaManager
                context.setDistributable(true);

                // Using clusterable manager
                CarbonTomcatClusterableSessionManager sessionManager;
                if (manager instanceof CarbonTomcatClusterableSessionManager) {
                    sessionManager = (CarbonTomcatClusterableSessionManager) manager;
                    sessionManager.setOwnerTenantId(tenantId);
                } else {
                    sessionManager = new CarbonTomcatClusterableSessionManager(tenantId);
                    context.setManager(sessionManager);
                }

                Object alreadyinsertedSMMap = configurationContext
                        .getProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP);
                if (alreadyinsertedSMMap != null) {
                    ((Map<String, CarbonTomcatClusterableSessionManager>) alreadyinsertedSMMap)
                            .put(context.getName(), sessionManager);
                } else {
                    sessionManagerMap.put(context.getName(), sessionManager);
                    configurationContext.setProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP, sessionManagerMap);
                }

            } else {
                if (manager instanceof CarbonTomcatSessionManager) {
                    ((CarbonTomcatSessionManager) manager).setOwnerTenantId(tenantId);
                } else if (manager instanceof CarbonTomcatSessionPersistentManager) {
                    ((CarbonTomcatSessionPersistentManager) manager).setOwnerTenantId(tenantId);
                    log.debug(manager.getInfo() +
                            " enabled Tomcat HTTP Session Persistent mode using " +
                            ((CarbonTomcatSessionPersistentManager) manager).getStore().getInfo());
                } else {
                    context.setManager(new CarbonTomcatSessionManager(tenantId));
                }
            }
            context.setReloadable(false);
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

    /**
     * Get the config file for Jaggery application.
     *
     * @param f location to the Jaggery conf file
     * @return confFile JSONObject of conf file
     */
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

    /**
     * Check whether distributable or not
     *
     * @param context Current context
     * @param obj     Jaggery conf json object
     * @return true if distributable else false
     */
    private static boolean isDistributable(Context context, JSONObject obj) {
        if (obj != null) {
            if (obj.get(JaggeryCoreConstants.JaggeryConfigParams.DISTRIBUTABLE) instanceof Boolean) {
                Boolean isDistributable = (Boolean) obj.get(JaggeryCoreConstants.
                        JaggeryConfigParams.DISTRIBUTABLE);
                if (isDistributable != null) {
                    return isDistributable.booleanValue();
                }
            } else if (obj.get(JaggeryCoreConstants.JaggeryConfigParams.DISTRIBUTABLE) instanceof String) {
                String distributable = (String) obj.get(JaggeryCoreConstants.
                        JaggeryConfigParams.DISTRIBUTABLE);
                return (distributable != null && distributable.equalsIgnoreCase("true"));
            }
        }

        return false;
    }

    /**
     * Add parameters for context
     *
     * @param ctx           Current context
     * @param jaggeryConfig Jaggery config json object
     */
    private static void addContextParams(Context ctx, JSONObject jaggeryConfig) {
        if (jaggeryConfig != null) {
            JSONArray arrContextParams = (JSONArray) jaggeryConfig
                    .get(JaggeryCoreConstants.JaggeryConfigParams.CONTEXT_PARAMS);

            if (arrContextParams != null) {
                for (Object contextParamObj : arrContextParams) {
                    JSONObject contextParam = (JSONObject) contextParamObj;
                    String name = (String) contextParam
                            .get(JaggeryCoreConstants.JaggeryConfigParams.CONTEXT_PARAMS_NAME);
                    String value = (String) contextParam
                            .get(JaggeryCoreConstants.JaggeryConfigParams.CONTEXT_PARAMS_VALUE);

                    ctx.addParameter(name, value);
                }
            }
        }
    }

    /**
     * Add listeners
     *
     * @param ctx           Current context
     * @param jaggeryConfig Jaggery config json object
     */
    private static void addListeners(Context ctx, JSONObject jaggeryConfig) {
        if (jaggeryConfig != null) {
            JSONArray arrListeners = (JSONArray) jaggeryConfig.get(JaggeryCoreConstants.JaggeryConfigParams.LISTENERS);

            if (arrListeners != null) {
                for (Object listenerObj : arrListeners) {
                    JSONObject listener = (JSONObject) listenerObj;
                    String clazz = (String) listener.get(JaggeryCoreConstants.JaggeryConfigParams.LISTENERS_CLASS);

                    ctx.addApplicationListener(clazz);
                }
            }
        }
    }

    /**
     * Add servlets
     *
     * @param ctx           Current context
     * @param jaggeryConfig Jaggery config json object
     */
    private static void addServlets(Context ctx, JSONObject jaggeryConfig) {
        if (jaggeryConfig != null) {
            JSONArray arrServlets = (JSONArray) jaggeryConfig.get(JaggeryCoreConstants.JaggeryConfigParams.SERVLETS);
            JSONArray arrServletMappings = (JSONArray) jaggeryConfig
                    .get(JaggeryCoreConstants.JaggeryConfigParams.SERVLET_MAPPINGS);

            if (arrServlets != null) {
                for (Object servletObj : arrServlets) {
                    JSONObject servlet = (JSONObject) servletObj;
                    String name = (String) servlet.get(JaggeryCoreConstants.JaggeryConfigParams.SERVLETS_NAME);
                    String clazz = (String) servlet.get(JaggeryCoreConstants.JaggeryConfigParams.SERVLETS_CLASS);

                    Wrapper servletWrapper = Tomcat.addServlet(ctx, name, clazz);

                    JSONArray arrParams = (JSONArray) servlet
                            .get(JaggeryCoreConstants.JaggeryConfigParams.SERVLETS_PARAMS);
                    if (arrParams != null) {
                        for (Object paramObj : arrParams) {
                            JSONObject param = (JSONObject) paramObj;

                            String paramName = (String) param
                                    .get(JaggeryCoreConstants.JaggeryConfigParams.SERVLETS_PARAMS_NAME);
                            String paramValue = (String) param
                                    .get(JaggeryCoreConstants.JaggeryConfigParams.SERVLETS_PARAMS_VALUE);

                            servletWrapper.addInitParameter(paramName, paramValue);
                        }
                    }
                }
            }

            if (arrServletMappings != null) {
                for (Object servletMappingObj : arrServletMappings) {
                    JSONObject mapping = (JSONObject) servletMappingObj;
                    String name = (String) mapping.get(JaggeryCoreConstants.JaggeryConfigParams.SERVLET_MAPPINGS_NAME);
                    String url = (String) mapping.get(JaggeryCoreConstants.JaggeryConfigParams.SERVLET_MAPPINGS_URL);

                    ctx.addServletMapping(url, name);
                }
            }
        }
    }
}

