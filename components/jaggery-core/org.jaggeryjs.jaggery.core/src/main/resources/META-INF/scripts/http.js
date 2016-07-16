
var get, post, put, del, head, options, trace, connect;

/**
 *
 * @param url, data, callback, type
 * data = xml | json | text
 * post(url, data, headers, type, callback)
 */
(function() {
    var formatData = function(xhr, type) {
        var data = xhr.responseText;
        type = type.toLowerCase();
        if (type === "xml") {
            return new XML(data.replace(/<\?xml.*?\?>/, "").replace(/<!--[\s\S]*?-->/g, ""));
        } else if (type === "json") {
            return JSON.parse(data);
        } else {
            return data;
        }
    };

    var string = function(o) {
        return typeof o === 'string' || o instanceof String;
    };

    var getError = function(method, type) {
        return "Invalid argument for " + method.toLowerCase() + "() method : " + type;
    };

    var httpCall = function(method, args) {
        var xhr = new XMLHttpRequest();
        var url = args[0];
        var data = null;
        var callback = null;
        var dataType = "text";
        var that = this;
        var type = typeof url;
        var headers = null;
        var count = args.length;
        var username = null;
        var password = null;
        method = method.toUpperCase();

        if (count === 0 || count > 5) {
            throw "Invalid number of arguments for " + method + "() method : " + count;
        }

        if (!string(url)) {
            throw getError(method, type);
        }

        if (count === 2) {
            if (args[1] !== null) {
                type = typeof args[1];
                if (type === "object" || string(args[1])) {
                    data = args[1];
                } else if (type === "function") {
                    callback = args[1];
                } else {
                    throw getError(method, type);
                }
            }
        } else if (count === 3) {
            if (args[1] != null) {
                type = typeof args[1];
                if (type === "object" || string(args[1])) {
                    data = args[1];
                } else {
                    throw getError(method, type);
                }
            }

            if (args[2] != null) {
                type = typeof args[2];
                if (type === "function") {
                    callback = args[2];
                } else if (string(args[2])) {
                    dataType = args[2];
                } else if (type === "object") {
                    headers = args[2];
                } else {
                    throw getError(method, type);
                }
            }
        } else if (count === 4) {
            if (args[1] != null) {
                type = typeof args[1];
                if (type === "object" || string(args[1])) {
                    data = args[1];
                } else {
                    throw getError(method, type);
                }
            }

            if (args[2] != null) {
                type = typeof args[2];
                if (string(args[2])) {
                    dataType = args[2];
                } else if (type === "object") {
                    headers = args[2];
                } else {
                    throw getError(method, type);
                }
            }

            if (args[3] != null) {
                type = typeof args[3];
                if (type === "function") {
                    callback = args[3];
                } else if (string(args[3])) {
                    if (!string(args[2])) {
                        dataType = args[3];
                    } else {
                        throw getError(method, type);
                    }
                } else {
                    throw getError(method, type);
                }
            }
        } else if (count >= 5) {
            if (args[1] != null) {
                type = typeof args[1];
                if (type === "object" || string(args[1])) {
                    data = args[1];
                } else {
                    throw getError(method, type);
                }
            }

            if (args[2] != null) {
                type = typeof args[2];
                if (type === "object") {
                    headers = args[2];
                } else {
                    throw getError(method, type);
                }
            }

            if (args[3] != null) {
                type = typeof args[3];
                if (string(args[3])) {
                    dataType = args[3];
                } else {
                    throw getError(method, type);
                }
            }

            if (args[4] != null) {
                type = typeof args[4];
                if (type === "function") {
                    callback = args[4];
                } else {
                    throw getError(method, type);
                }
            }
        }
        // collect username and password
        if (count === 7) {
            if (args[5] !== null) {
                if (string(args[5])) {
                    username = args[5];
                } else {
                    throw getError(method, "argument username must be a string");
                }
            }
            if (args[6] !== null) {
                if (string(args[6])) {
                    password = args[6];
                } else {
                    throw getError(method, "argument password must be a string");
                }
            }
        }

        var query;
        var first = true;
        if (string(data)) {
            query = data;
        } else {
            query = "";
            for (var name in data) {
                query = query || "";
                if (data.hasOwnProperty(name)) {
                    query += (first ? "" : "&") + name + "=" + encodeURIComponent(data[name]);
                    first = first ? false : first;
                }
            }
        }

        if (method === "GET") {
            url = query ? url + "?" + query : url;
            query = null;
        } else if (method === "POST") {
            if(headers && !headers["Content-Type"]) {
                headers["Content-Type"] = "application/x-www-form-urlencoded; charset=UTF-8";
            }
        }
        for (name in headers) {
            if (headers.hasOwnProperty(name)) {
                xhr.setRequestHeader(name, headers[name]);
            }
        }

        if (callback) {
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    callback.call(that, formatData(xhr, dataType), xhr);
                }
            };
        }

        
        if (password !== null && username !== null) {
            xhr.open(method, url, callback !== null, username, password);
        } else {
            xhr.open(method, url, callback !== null);
        }

        xhr.send(query);
        return callback !== null ? xhr : {
            data : formatData(xhr, dataType),
            xhr : xhr
        };
    };

    get = function() {
        return httpCall("GET", arguments);
    };

    post = function() {
        return httpCall("POST", arguments);
    };

    put = function() {
        return httpCall("PUT", arguments);
    };

    del = function() {
        return httpCall("DELETE", arguments);
    };

    head = function() {
        return httpCall("HEAD", arguments);
    };

    options = function() {
        return httpCall("OPTIONS", arguments);
    };

    trace = function() {
        return httpCall("TRACE", arguments);
    };

    connect = function() {
        return httpCall("CONNECT", arguments);
    };
})();
