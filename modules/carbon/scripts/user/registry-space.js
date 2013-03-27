(function (server, registry, user) {

    var Space = function (user, space, options) {
        var serv = new server.Server(options.serverUrl);
        this.registry = new registry.Registry(serv, {
            username: options.username || user,
            password: options.password
        });
        this.prefix = options.path + '/' + user + '/' + space;
    };
    user.Space = Space;

    Space.prototype.put = function (key, value) {
        value = (!(value instanceof String) && typeof value !== "string") ? stringify(value) : value;
        this.registry.put(this.prefix + '/' + key, {
            content: value
        });
    };

    Space.prototype.get = function (key) {
        return this.registry.content(this.prefix + '/' + key);
    };

    Space.prototype.remove = function (key) {
        this.registry.remove(this.prefix + '/' + key);
    };

    Space.prototype.find = function (filter) {

    };


}(server, registry, user));