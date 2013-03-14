var tenantId;

(function () {
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext;
    var context = PrivilegedCarbonContext.getCurrentContext();

    tenantId = function () {
        return context.getTenantId();
    };
}());