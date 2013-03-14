var registry = {};

(function (registry) {

    registry.Registry = function (server, auth) {
        var osgi = require('registry-osgi.js').registry,
            o = new osgi.Registry(server, auth);
        o.prototype = this;
        return o;
    };

}(registry));