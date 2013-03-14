var osgiService;

(function () {
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext;
    var context = PrivilegedCarbonContext.getCurrentContext();
    var Class = java.lang.Class;

    osgiService = function (clazz) {
        return context.getOSGiService(Class.forName(clazz));
    };
}());