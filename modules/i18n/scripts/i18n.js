(function () {
    var localeResourcesBasePath = '',
        localizations = {};
    //initialization with the request and loads the correct localization resources
    this.init = function (req) {
        var locale = req.getLocale();
        try {
            localizations = require(localeResourcesBasePath + 'locale_' + locale + '.json');
        } catch (e) {
            localizations = {};
        }
    };

    var getLocalString = function (key, fallback) {
        if (localizations[key]) {
            return localizations[key]
        } else {
            return  key;
        }

    };

    this.localize = function (key, fallback) {
        var localized = getLocalString(key);
        if (localized !== key) {
            return localized;
        } else {
            return fallback;
        }
    };
})();