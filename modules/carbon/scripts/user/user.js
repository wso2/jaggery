var user = {};

(function (user) {

    var User = function (manager, username) {
        this.um = manager;
        this.username = username;
    };
    user.User = User;

    User.prototype.getClaims = function () {
        return this.um.manager.getClaims(this.username);
    };

    User.prototype.setClaims = function (claims, profile) {
        this.um.manager.setUserClaimValues(this.username, claims, profile);
    };

    User.prototype.getRoles = function () {
        return this.um.manager.getRoleListOfUser(this.username);
    };

    User.prototype.addRoles = function (roles) {
        return this.um.manager.updateRoleListOfUser(this.username, [], roles);
    };

    User.prototype.removeRoles = function (roles) {
        return this.um.manager.updateRoleListOfUser(this.username, roles, []);
    };

    User.prototype.updateRoles = function (remove, add) {
        return this.um.manager.updateRoleListOfUser(this.username, remove, add);
    };

    User.prototype.isAuthorized = function (permission, action) {
        var i,
            roles = this.getRoles(),
            length = roles.length;
        for (i = 0; i < length; i++) {
            if (this.um.authorizer.isRoleAuthorized(roles[i], permission, action)) {
                return true;
            }
        }
        return false;
    };

}(user));