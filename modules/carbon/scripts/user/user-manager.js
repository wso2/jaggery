(function (server, user) {

    var UserManager = function (serv, auth) {
        if (auth.username) {
            this.server = new server.Server(serv.url);
            if (!serv.authenticate(auth.username, auth.password)) {
                throw new Error('Unauthorized request for UserManager : ' + stringify(auth.username));
            }
            this.tenant = server.tenantId({
                domain: auth.domain,
                username: auth.username
            });
            var realmService = server.osgiService('org.wso2.carbon.user.core.service.RealmService'),
                realm = realmService.getTenantUserRealm(this.tenant);
            this.manager = realm.getUserStoreManager();
            this.authorizer = realm.getAuthorizationManager();
        } else {
            throw new Error('Unsupported authentication mechanism : ' + stringify(auth.username));
        }
    };
    user.UserManager = UserManager;

    UserManager.prototype.getUser = function (username) {
        if (!this.manager.isExistingUser(username)) {
            return null;
        }
        return new user.User(this, username);
    };

    UserManager.prototype.addUser = function (username, password, roles, claims, profile) {
        this.manager.addUser(username, password, roles || [], claims || null, profile);
    };

    UserManager.prototype.removeUser = function (username) {
        this.manager.deleteUser(username);
    };

    UserManager.prototype.getClaims = function (username) {
        return this.manager.getUserClaimValues(username);
    };

    UserManager.prototype.setClaims = function (username, claims, profile) {
        return this.manager.setUserClaimValues(username, claims, profile);
    };

    UserManager.prototype.addRole = function (role, users, permissions) {
        this.manager['addRole(java.lang.String,java.lang.String[],org.wso2.carbon.user.api.Permission[])']
            (role, users, permissions);
    };

    UserManager.prototype.removeRole = function (role) {
        this.manager.deleteRole(role);
    };

}(server, user));