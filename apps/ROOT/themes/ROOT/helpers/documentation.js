var log = new Log();

var resources = function (page, meta) {
    return {
        js: ['google-code-prettify/prettify.js', 'documentation.js'],
        css: ['prettify.css'],
        code: []
    };
};



var generateAPIDoc = function(api){
	log.info("---------------" + api);	
}
