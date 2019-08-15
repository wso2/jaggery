package org.jaggeryjs.jaggery.core;

public final class JaggeryCoreConstants {

    public static final String JAGGERY_SERVLET_NAME = "JaggeryServlet";
    public static final String JAGGERY_SERVLET_CLASS = "org.jaggeryjs.jaggery.core.JaggeryServlet";

    public static final String JAGGERY_APPLICATION_SESSION_LISTENER = "org.jaggeryjs.jaggery.core.listeners.WebAppSessionListener";

    public static final String JS_CREATED_LISTENERS = "js.created.listeners";
    public static final String JS_DESTROYED_LISTENERS = "js.destroyed.listeners";

    public static final String JAGGERY_URL_PATTERN = "*.jag";

    public static final String JAGGERY_FILTER_NAME = "JaggeryFilter";
    public static final String JAGGERY_FILTER_CLASS = "org.jaggeryjs.jaggery.core.JaggeryFilter";

    public static final String JAGGERY_OUTPUT_STREAM = "JaggeryOutputStream";

    public static final String JAGGERY_CONF_FILE = "jaggery.conf";

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
        public static final String INIT_SCRIPTS = "initScripts";
        public static final String DESTROY_SCRIPTS = "destroyScripts";
        public static final String LOG_LEVEL = "logLevel";
        public static final String SESSION_CREATED_LISTENER_SCRIPTS = "sessionCreatedListeners";
        public static final String SESSION_DESTROYED_LISTENER_SCRIPTS = "sessionDestroyedListeners";
        public static final String USER_DATA_CONSTRAINT = "userDataConstraint";
        public static final String TRANSPORT_GUARANTEE = "transportGuarantee";
        public static final String FILTERS = "filters";
        public static final String FILTERS_NAME = "name";
        public static final String FILTERS_CLASS = "class";
        public static final String FILTERS_PARAMS = "params";
        public static final String FILTERS_PARAMS_NAME = "name";
        public static final String FILTERS_PARAMS_VALUE = "value";
        public static final String FILTER_MAPPINGS = "filterMappings";
        public static final String FILTER_MAPPINGS_NAME = "name";
        public static final String FILTER_MAPPINGS_URL = "url";
        public static final String SERVLETS = "servlets";
        public static final String SERVLETS_NAME = "name";
        public static final String SERVLETS_CLASS = "class";
        public static final String SERVLETS_PARAMS = "params";
        public static final String SERVLETS_PARAMS_NAME = "name";
        public static final String SERVLETS_PARAMS_VALUE = "value";
        public static final String SERVLET_MAPPINGS = "servletMappings";
        public static final String SERVLET_MAPPINGS_NAME = "name";
        public static final String SERVLET_MAPPINGS_URL = "url";
        public static final String LISTENERS = "listeners";
        public static final String LISTENERS_CLASS = "class";
        public static final String CONTEXT_PARAMS = "contextParams";
        public static final String CONTEXT_PARAMS_NAME = "name";
        public static final String CONTEXT_PARAMS_VALUE = "value";
        public static final String EXCLUDE_FROM_DEPLOYMENT = "excludeFromDeployment";
    }

}
