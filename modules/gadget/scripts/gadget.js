(function () {

    var log = new Log('Gadget.module.jaggery');

    this.url;
    this.id;
    this.applyDefaultStyles = false;
    this.title;
    this.prefs = {};
    this.container;
    this.owner = 'john.doe';
    this.viewer = 'john.doe';
    this.appId = 0;
    this.view;
    this.country;
    this.language;
    this.specVersion = 2.0;

    this.shindigBaseUrl = 'http://localhost:9763/gadgets/ifr';

    this.toHTML = function () {
        var iframeUrl;
        if (this.url) {
            iframeUrl = shindigBaseUrl + '?url=' + encodeURIComponent(this.url);
        } else {
            log.error('Gadget URL not spesified');
            return;
        }

        if (this.container) {
            iframeUrl += '&container=' + this.container;
        }

        if (this.country) {
            iframeUrl += '&country=' + this.country;
        }

        if (this.language) {
            iframeUrl += '&lang=' + this.language();
        }

        if (this.view) {
            iframeUrl += '&view=' + this.view();
        }

        if (this.specVersion) {
            iframeUrl += '&v=' + this.specVersion;
        }

        if (this.id) {
            iframeUrl += '&mid=' + this.id;
        }

        if (this.prefs) {
            var uprefs = this.prefs;
            for (var key in uprefs) {
                if (uprefs.hasOwnProperty(key)) {
                    iframeUrl += '&up_' + key + '=' + uprefs[key];
                }
            }
        }

        //adding a security token
        iframeUrl += '&st=' + encodeURIComponent(generateSecureToken());

        print('<iframe src="' + iframeUrl + '" height="auto" width="auto" scrolling="no" frameborder="0"></iframe>');

    };

    var generateSecureToken = function () {
        for (var i = 0; i < this.url.length; i++) {
            this.appId += this.url.charCodeAt(i);
        }
        var fields = [this.owner, this.viewer, this.appId, 'shindig', this.url, '0', 'default'];
        for (var i = 0; i < fields.length; i++) {
            // escape each field individually, for metachars in URL
            fields[i] = encodeURIComponent(fields[i]);
        }
        return fields.join(':');
    };

})();