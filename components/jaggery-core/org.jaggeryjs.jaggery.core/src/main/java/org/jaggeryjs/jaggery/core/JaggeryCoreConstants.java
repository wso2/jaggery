package org.jaggeryjs.jaggery.core;

public final class JaggeryCoreConstants {

    public static final String JAGGERY_SERVLET_NAME = "JaggeryServlet";
    public static final String JAGGERY_SERVLET_CLASS = "org.jaggeryjs.jaggery.core.JaggeryServlet";

    public static final String JAGGERY_APPLICATION_SESSION_LISTENER = "org.jaggeryjs.jaggery.core.listeners.WebAppSessionListener";

    public static final String JS_CREATED_LISTENERS = "js.created.listeners";
    public static final String JS_DESTROYED_LISTENERS = "js.destroyed.listeners";

    public static final String JAGGERY_URL_PATTERN = "*.jag";

    public static final String JAGGERY_WEBSOCKET_SERVLET_NAME = "JaggeryWebSocketServlet";
    public static final String JAGGERY_WEBSOCKET_SERVLET_CLASS = "org.jaggeryjs.jaggery.core.websocket.JaggeryWebSocketServlet";

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
    }

}
