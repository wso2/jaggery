TestAppUtil = new function() {
    this.makeRequest = function(u, d, callback) {
        $.ajax({
            type: "GET",
            url: u,
            data: d,
            dataType: "text",
            success: callback
        });
    };
    
    this.makePost = function(u, d, callback) {
        $.ajax({
            type: "POST",
            url: u,
            data: d,
            dataType: "text",
            success: callback
        });
    };
    this.makeJsonRequest = function(u, d, callback) {
        $.ajax({
            type: "GET",
            url: u,
            data: d,
            async: false,
            dataType: "json",
            contentType: "application/json",
            success: callback,
            done:callback
        });
    };
};


