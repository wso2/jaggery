/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jaggeryjs.jaggery.app.mgt;

/**
 * Web Application Constants
 */
public final class JaggeryConstants {
    public static final String WEBAPP_PREFIX = "jaggeryapps";
    public static final String WEBAPP_DEPLOYMENT_FOLDER = "jaggeryapps";
    public static final String WEBAPP_DEPLOYMENT_FOLDER_IN_JAGGERY = "apps";
    public static final String WEBAPP_EXTENSION = "zip";
    public static final String JAGGERY_SERVLET_NAME = "JaggeryServlet";
    public static final String JAGGERY_SERVLET_CLASS =
            "org.jaggeryjs.jaggery.core.JaggeryServlet";
    public static final String JAGGERY_SERVLET_URL_PATTERN = "*.jag";
    public static final String JSSP_NAME = "jssp";
    public static final String JSSP_CLASS = "org.apache.jasper.servlet.JspServlet";
    public static final String JAGGERY_CONF_FILE = "jaggery.conf";
    public static final String JAGGERY_WEBAPP_FILTER_PROP = "jaggeryWebapp";

    public static final class JaggeryConfigParams {

        public static final String ERROR_PAGES = "errorPages";
        public static final String ERROR_CODE = "errorCode";
        public static final String LOCATION = "location";
        public static final String LOGIN_CONFIG = "loginConfig";
        public static final String AUTH_METHOD = "authMethod";
        public static final String AUTH_METHOD_FORM = "FORM";
        public static final String FORM_LOGIN_CONFIG = "formLoginConfig";
        public static final String FORM_LOGIN_PAGE = "formLoginPage";
        public static final String FORM_ERROR_PAGE = "formErrorPage";
        public static final String AUTH_METHOD_BASIC = "BASIC";
        public static final String SECURITY_CONSTRAINTS = "securityConstraints";
        public static final String SECURITY_CONSTRAINT = "securityConstraint";
        public static final String WEB_RESOURCE_COLLECTION = "webResourceCollection";
        public static final String WEB_RES_NAME = "name";
        public static final String URL_PATTERNS = "urlPatterns";
        public static final String HTTP_METHODS = "methods";
        public static final String AUTH_ROLES = "authRoles";
        public static final String SECURITY_ROLES = "securityRoles";
        public static final String DISPLAY_NAME = "displayName";
        public static final String WELCOME_FILES = "welcomeFiles";
        public static final String URL_MAPPINGS = "urlMappings";
        public static final String URL_MAPPINGS_URL = "url";
        public static final String URL_MAPPINGS_PATH = "path";
        public static final String DISTRIBUTABLE = "distributable";
    }

    public static final class WebappState {
        public static final String STARTED = "started";
        public static final String STOPPED = "stopped";

        private WebappState() {}
    }

    private JaggeryConstants() {
    }
}