(function () {
    var XMLObj = XML;
    this.XML = function (data) {
        return new XMLObj(data.replace(/<\?xml.*?\?>/, "").replace(/<!--[\s\S]*?-->/g, ""));
    };

    var XMLListObj = XMLList;
    this.XMLList = function (data) {
        return new XMLListObj(data.replace(/<\?xml.*?\?>/, "").replace(/<!--[\s\S]*?-->/g, ""));
    };
}());