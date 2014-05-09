var log = new Log(), 
	CONFIG_APIS = '/config/apis/', 
	CONFIG_PROPS = '/config/properties/',
	CONFIG_EXAMPLES = '/config/examples/', 
	CATEGORIES_SORTED = [],

// Only the APIs listed below are loaded, in the order they are listed
CATEGORIES = [{
	'Basic Syntax' : ['html', 'html2', 'require']
}, {
	'Built-ins' : [{
		'Output' : ['print', 'Log']
	}, {
		'Variables' : ['request', 'response', 'session', 'application', 'webSocket']
	}, {
		'Http Client' : ['get', 'post', 'put', 'del', 'xhr']
	}, {
		'Utils' : ['urimatcher', 'include', 'includeonce']
	}, {
		'Feed' : ['feed', 'entry']
	}, {
		'File System' : ['file']
	}, {
		'Data Storage' : ['rdb', 'metadatastore', 'collection', 'resource']
	}, {
		'Dataformats' : ['parse', 'stringify', 'xml']
	}]
}, {
	'Add-ons' : [{
		'Email' : ['sender']
	}, {
		'Web Services' : ['ws', 'wsstub']
	}, {
		'Authentication' : ['oauth']
	}, {
		'Server' : ['process']
	}, {
		'Internationalization' : ['i18n']
	}, {
		'Carbon' : ['UserManager', 'user', 'registry', 'server', 'server2']
	}]
}, {
	'Configuration File' : [{
		' ' : ['jagconf']
	}]
}];

var loadAPI = function(collection, destArray, currentAPI) {
	for (var item in collection) {
		var f = new File(CONFIG_APIS + collection[item] + '.json');
		f.open("r");

		var fo = parse(f.readAll());

		if (currentAPI == fo.api)
			fo.active = true;
			
		destArray.push(fo);
		f.close();
	}
}
var getCategorizedAPIs = function(currentAPI) {

	for (var i in CATEGORIES) {

		var category = Object.keys(CATEGORIES[i])[0];

		var _obj = {};
		_obj.category = category;

		if ( typeof CATEGORIES[i][category][0] == 'string') {
			_obj.apis = [];

			loadAPI(CATEGORIES[i][category], _obj.apis, currentAPI);

		} else {
			_obj.subcategories = [];

			for (var sc in CATEGORIES[i][category]) {
				var subcat = Object.keys(CATEGORIES[i][category][sc])[0];

				var __obj = {};
				__obj.category = subcat;
				__obj.apis = [];

				loadAPI(CATEGORIES[i][category][sc][subcat], __obj.apis, currentAPI);

				_obj.subcategories.push(__obj);

			}

		}
		CATEGORIES_SORTED.push(_obj);
	}

	return CATEGORIES_SORTED;

}
var loadSections = function(api) {
	var path = CONFIG_PROPS + api + '.json';
	var file = new File(path);

	if (!file.isExists())
		return {};
	file.open("r");

	var json = parse(file.readAll());

	return json.sections;

}
var loadExamples = function(api) {
	var path = CONFIG_EXAMPLES + api + '.jag';
	var file = new File(path);
	file.open("r");

	var html = file.readAll();

	return html;
}
