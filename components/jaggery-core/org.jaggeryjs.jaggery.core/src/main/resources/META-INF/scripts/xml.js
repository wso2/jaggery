(function () {
    var XMLObj = XML,
        filter = function(data) {
            return (data instanceof String || typeof data === 'string') ?
                data.replace(/<\?xml.*?\?>/, "").replace(/<!--[\s\S]*?-->/g, "") : data;
        };
    this.XML = function (data) {
        return new XMLObj(filter(data));
    };

    var XMLListObj = XMLList;
    this.XMLList = function (data) {
        return new XMLListObj(filter(data));
    };
}());