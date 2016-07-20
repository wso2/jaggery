/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.jaggeryjs.jaggery.core.manager;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.*;
import org.jaggeryjs.hostobjects.log.LogHostObject;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.ScriptableObject;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This JaggeryDeployerManager is responsible for processing jaggery.conf file and making relevant modifications
 * according to jaggery.conf file
 */
public class JaggeryDeployerManager {

    private static Log log = LogFactory.getLog(JaggeryDeployerManager.class);
    private static final String INDEX_HTML = "index.html";
    private static final String INDEX_JAG = "index.jag";
    private static final String JAGGERY_CONF = "jaggery.conf";
    private static final String WAR_EXTENSION = ".war";
    private static final String SECURITYCOLLECTION_NAME = "ConfigDir";
    private static final String SECURITYCOLLECTION_DESCRIPTION = "Jaggery Configuration Dir";

    /**
     * Process the jaggery.conf file of jaggery apps
     *
     * @param context context of the jaggery app
     */
    public static void processJaggeryApp(Context context, Path appBase) {
        JSONObject jaggeryConfigObj = readJaggeryConfig(context, appBase);
        SecurityConstraint securityConstraint = new SecurityConstraint();
        securityConstraint.setAuthConstraint(true);
        SecurityCollection securityCollection = new SecurityCollection();
        securityCollection.setName(SECURITYCOLLECTION_NAME);
        securityCollection.setDescription(SECURITYCOLLECTION_DESCRIPTION);
        securityConstraint.addCollection(securityCollection);
        initJaggeryappDefaults(context, jaggeryConfigObj, securityConstraint);
        try {
            WebAppManager.getEngine().enterContext();
            WebAppManager.deploy(context);
            setDisplayName(context, jaggeryConfigObj);
            if (jaggeryConfigObj != null) {
                addSessionCreatedListners(context, (JSONArray) jaggeryConfigObj
                        .get(JaggeryCoreConstants.JaggeryConfigParams.SESSION_CREATED_LISTENER_SCRIPTS));
                addSessionDestroyedListners(context, (JSONArray) jaggeryConfigObj
                        .get(JaggeryCoreConstants.JaggeryConfigParams.SESSION_DESTROYED_LISTENER_SCRIPTS));
                executeScripts(context,
                        (JSONArray) jaggeryConfigObj.get(JaggeryCoreConstants.JaggeryConfigParams.INIT_SCRIPTS));
                addUrlMappings(context, jaggeryConfigObj);
            }
        } catch (ScriptException e) {
            log.error(e.getMessage(), e);
            try {
                context.destroy();
            } catch (LifecycleException e1) {
                log.error(e1.getMessage(), e1);
            }
        }
    }

    private static void initJaggeryappDefaults(Context ctx, JSONObject jaggeryConfig,
            SecurityConstraint securityConstraint) {
        Tomcat.addServlet(ctx, JaggeryCoreConstants.JAGGERY_SERVLET_NAME, JaggeryCoreConstants.JAGGERY_SERVLET_CLASS);
        Tomcat.addServlet(ctx, JaggeryCoreConstants.JAGGERY_WEBSOCKET_SERVLET_NAME,
                JaggeryCoreConstants.JAGGERY_WEBSOCKET_SERVLET_CLASS);
        addFilters(ctx, jaggeryConfig);
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(JaggeryCoreConstants.JAGGERY_FILTER_NAME);
        filterDef.setFilterClass(JaggeryCoreConstants.JAGGERY_FILTER_CLASS);
        ctx.addFilterDef(filterDef);
        FilterMap filterMapping = new FilterMap();
        filterMapping.setFilterName(JaggeryCoreConstants.JAGGERY_FILTER_NAME);
        filterMapping.addURLPattern(JaggeryCoreConstants.JAGGERY_URL_PATTERN);
        ctx.addFilterMap(filterMapping);
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
            addParameters(ctx, jaggeryConfig);
            addLogLevel(ctx, jaggeryConfig);
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
                context.addWelcomeFile(INDEX_JAG);
                context.addWelcomeFile(INDEX_HTML);
            }
        } else {
            context.addWelcomeFile(INDEX_JAG);
            context.addWelcomeFile(INDEX_HTML);
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

    private static void addSecurityRoles(Context context, JSONObject obj) {
        JSONArray arr = (JSONArray) obj.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_ROLES);
        if (arr != null) {
            for (Object role : arr) {
                context.addSecurityRole((String) role);
            }
        }
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

    private static void setLoginConfig(Context context, JSONObject obj) {
        JSONObject loginObj = (JSONObject) obj.get(JaggeryCoreConstants.JaggeryConfigParams.LOGIN_CONFIG);
        if (loginObj != null) {
            if (loginObj.get(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD)
                    .equals(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD_FORM)) {
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD_FORM);
                loginConfig.setLoginPage(
                        (String) ((JSONObject) loginObj.get(JaggeryCoreConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG))
                                .get(JaggeryCoreConstants.JaggeryConfigParams.FORM_LOGIN_PAGE));
                loginConfig.setErrorPage(
                        (String) ((JSONObject) loginObj.get(JaggeryCoreConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG))
                                .get(JaggeryCoreConstants.JaggeryConfigParams.FORM_ERROR_PAGE));
                context.setLoginConfig(loginConfig);
            } else if (loginObj.get(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD)
                    .equals(JaggeryCoreConstants.JaggeryConfigParams.AUTH_METHOD_BASIC)) {
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
                if (((JSONObject) o.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINT))
                        .get(JaggeryCoreConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION) != null) {
                    JSONObject resCollection = (JSONObject) ((JSONObject) o
                            .get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINT))
                            .get(JaggeryCoreConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION);
                    SecurityCollection secCollection = new SecurityCollection();
                    secCollection
                            .setName((String) resCollection.get(JaggeryCoreConstants.JaggeryConfigParams.WEB_RES_NAME));

                    JSONArray arrPattern = (JSONArray) resCollection
                            .get(JaggeryCoreConstants.JaggeryConfigParams.URL_PATTERNS);
                    for (Object anArrPattern : arrPattern) {
                        secCollection.addPattern((String) anArrPattern);
                    }

                    JSONArray methods = (JSONArray) resCollection
                            .get(JaggeryCoreConstants.JaggeryConfigParams.HTTP_METHODS);
                    if (methods != null) {
                        for (Object method : methods) {
                            secCollection.addMethod((String) method);
                        }
                    }

                    securityConstraint.addCollection(secCollection);
                }

                if (((JSONObject) o.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINT))
                        .get(JaggeryCoreConstants.JaggeryConfigParams.AUTH_ROLES) != null) {
                    JSONArray roles = (JSONArray) ((JSONObject) o
                            .get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINT))
                            .get(JaggeryCoreConstants.JaggeryConfigParams.AUTH_ROLES);
                    for (Object role : roles) {
                        securityConstraint.addAuthRole((String) role);
                    }
                    securityConstraint.setAuthConstraint(true);
                }

                if (((JSONObject) o.get(JaggeryCoreConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).
                        get(JaggeryCoreConstants.JaggeryConfigParams.USER_DATA_CONSTRAINT) != null) {
                    JSONObject userDataConstraint = (JSONObject) ((JSONObject) o.get(JaggeryCoreConstants.
                            JaggeryConfigParams.SECURITY_CONSTRAINT)).
                            get(JaggeryCoreConstants.JaggeryConfigParams.USER_DATA_CONSTRAINT);
                    String transportGuarantee = (String) userDataConstraint.get(JaggeryCoreConstants.
                            JaggeryConfigParams.TRANSPORT_GUARANTEE);
                    securityConstraint.setUserConstraint(transportGuarantee);
                }

                context.addConstraint(securityConstraint);
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

    private static void addFilters(Context ctx, JSONObject jaggeryConfig) {
        if (jaggeryConfig != null) {
            JSONArray arrFilters = (JSONArray) jaggeryConfig.get(JaggeryCoreConstants.JaggeryConfigParams.FILTERS);
            JSONArray arrFilterMappings = (JSONArray) jaggeryConfig
                    .get(JaggeryCoreConstants.JaggeryConfigParams.FILTER_MAPPINGS);

            if (arrFilters != null) {
                for (Object filterObj : arrFilters) {
                    JSONObject filter = (JSONObject) filterObj;
                    String name = (String) filter.get(JaggeryCoreConstants.JaggeryConfigParams.FILTERS_NAME);
                    String clazz = (String) filter.get(JaggeryCoreConstants.JaggeryConfigParams.FILTERS_CLASS);

                    FilterDef filterDef = new FilterDef();
                    filterDef.setFilterName(name);
                    filterDef.setFilterClass(clazz);

                    JSONArray arrParams = (JSONArray) filter
                            .get(JaggeryCoreConstants.JaggeryConfigParams.FILTERS_PARAMS);
                    if (arrParams != null) {
                        for (Object paramObj : arrParams) {
                            JSONObject param = (JSONObject) paramObj;

                            String paramName = (String) param
                                    .get(JaggeryCoreConstants.JaggeryConfigParams.FILTERS_PARAMS_NAME);
                            String paramValue = (String) param
                                    .get(JaggeryCoreConstants.JaggeryConfigParams.FILTERS_PARAMS_VALUE);

                            filterDef.addInitParameter(paramName, paramValue);
                        }
                    }
                    ctx.addFilterDef(filterDef);
                }
            }

            if (arrFilterMappings != null) {
                for (Object filterMappingObj : arrFilterMappings) {
                    JSONObject mapping = (JSONObject) filterMappingObj;
                    String name = (String) mapping.get(JaggeryCoreConstants.JaggeryConfigParams.FILTER_MAPPINGS_NAME);
                    String url = (String) mapping.get(JaggeryCoreConstants.JaggeryConfigParams.FILTER_MAPPINGS_URL);

                    FilterMap filterMapping = new FilterMap();
                    filterMapping.setFilterName(name);
                    filterMapping.addURLPattern(url);
                    ctx.addFilterMap(filterMapping);
                }
            }
        }
    }

    private static JSONObject readJaggeryConfig(Context context, Path appBase) {
        String content = null;
        String path = null;
        if (context.getDocBase().contains(WAR_EXTENSION)) {
            try {
                if (!appBase.endsWith("/")) {
                    path = appBase + File.separator + context.getDocBase();
                } else {
                    path = appBase + context.getDocBase();
                }
                ZipFile zip = new ZipFile(path);
                for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    if (entry.getName().toLowerCase().contains(JAGGERY_CONF)) {
                        InputStream inputStream = zip.getInputStream(entry);
                        content = IOUtils.toString(inputStream);
                    }
                }
            } catch (IOException e) {
                log.error(
                        "Error occuered when the accessing the jaggery.conf file of " + context.getPath().substring(1),
                        e);
            }
        } else {
            File file = new File(appBase + context.getPath() + File.separator + JAGGERY_CONF);
            try {
                content = FileUtils.readFileToString(file);
            } catch (IOException e) {
                log.error("IOException is thrown when accessing the jaggery.conf file of " + context.getPath()
                        .substring(1), e);
            }
        }
        JSONObject jaggeryConfig = null;
        try {
            JSONParser jp = new JSONParser();
            jaggeryConfig = (JSONObject) jp.parse(content);
        } catch (ParseException e) {
            log.error("Error in parsing the jaggery.conf file", e);
        }
        return jaggeryConfig;
    }

    private static void addSessionCreatedListners(Context context, JSONArray arr) {
        if (arr != null) {
            try {
                JaggeryContext sharedContext = WebAppManager.sharedJaggeryContext(context.getServletContext());
                CommonManager.setJaggeryContext(sharedContext);
                RhinoEngine engine = sharedContext.getEngine();
                org.mozilla.javascript.Context cx = engine.enterContext();
                ServletContext servletContext = (ServletContext) sharedContext
                        .getProperty(org.jaggeryjs.hostobjects.web.Constants.SERVLET_CONTEXT);

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
                ServletContext servletContext = (ServletContext) sharedContext
                        .getProperty(org.jaggeryjs.hostobjects.web.Constants.SERVLET_CONTEXT);

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
                ServletContext servletContext = (ServletContext) sharedContext
                        .getProperty(org.jaggeryjs.hostobjects.web.Constants.SERVLET_CONTEXT);
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
                    ScriptCachingContext sctx = new ScriptCachingContext(sharedContext.getTenantDomain(), parts[0],
                            parts[1], parts[2]);
                    sctx.setSecurityDomain(new JaggerySecurityDomain(path, servletContext));
                    engine.exec(new ScriptReader(servletContext.getResourceAsStream(path)) {
                        @Override protected void build() throws IOException {
                            try {
                                sourceReader = new StringReader(HostObjectUtil.streamToString(sourceIn));
                            } catch (ScriptException e) {
                                // throw new IOException(e);
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

    private static void setDisplayName(Context context, JSONObject obj) {
        if (obj == null) {
            return;
        }
        String dName = (String) obj.get(JaggeryCoreConstants.JaggeryConfigParams.DISPLAY_NAME);
        if (dName != null) {
            context.setDisplayName(dName);
        }
    }

    private static void addUrlMappings(Context context, JSONObject obj) {
        Object test = context.getServletContext().getAttribute("org.jaggeryjs.serveFunction");
        JSONArray arr = null;
        if (test != null) {
            // URL mapping for progamticaly
            arr = new JSONArray();
            JSONObject urlJSONObj = new JSONObject();
            JSONObject pathJSONObj = new JSONObject();
            urlJSONObj.put("url", "/*");
            pathJSONObj.put("path", File.separator + INDEX_JAG);
            arr.add(urlJSONObj);
        } else {
            arr = (JSONArray) obj.get(JaggeryCoreConstants.JaggeryConfigParams.URL_MAPPINGS);
        }
        if (arr != null) {
            Map<String, Object> urlMappings = new HashMap<String, Object>();
            for (Object mapObj : arr) {
                JSONObject mapping = (JSONObject) mapObj;
                String url = (String) mapping.get(JaggeryCoreConstants.JaggeryConfigParams.URL_MAPPINGS_URL);
                String path = (String) mapping.get(JaggeryCoreConstants.JaggeryConfigParams.URL_MAPPINGS_PATH);
                if (url != null && path != null) {
                    path = path.startsWith("/") ? path : "/" + path;
                    FilterMap filterMap = new FilterMap();
                    filterMap.setFilterName(JaggeryCoreConstants.JAGGERY_FILTER_NAME);
                    filterMap.addURLPattern(url);

                    context.addFilterMap(filterMap);
                    if (url.equals("/")) {
                        urlMappings.put("/", path);
                        continue;
                    }
                    url = url.startsWith("/") ? url.substring(1) : url;
                    List<String> parts = new ArrayList<String>(Arrays.asList(url.split("/", -1)));
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

    public static class JaggeryConfListener implements LifecycleListener {
        private JSONObject jaggeryConfig;
        private SecurityConstraint securityConstraint;

        public JaggeryConfListener(JSONObject jaggeryConfig, SecurityConstraint securityConstraint) {
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
                        addSessionCreatedListners(context, (JSONArray) jaggeryConfig
                                .get(JaggeryCoreConstants.JaggeryConfigParams.SESSION_CREATED_LISTENER_SCRIPTS));
                        addSessionDestroyedListners(context, (JSONArray) jaggeryConfig
                                .get(JaggeryCoreConstants.JaggeryConfigParams.SESSION_DESTROYED_LISTENER_SCRIPTS));
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
                        executeScripts(context, (JSONArray) jaggeryConfig
                                .get(JaggeryCoreConstants.JaggeryConfigParams.DESTROY_SCRIPTS));
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

}
