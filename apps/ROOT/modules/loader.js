var log = new Log();
var CONFIG_APIS = '/config/apis/';
var CONFIG_PROPS = '/config/properties/';
var CONFIG_EXAMPLES = '/config/examples/';

var groupCategories = function(srcArr, destArr) {

	for (var i in srcArr) {

		var _obj = {};

		_obj.category = i;

		var _subcats = [];

		if ( typeof srcArr[i].subcategories != 'undefined') {

			groupCategories(srcArr[i].subcategories, _subcats);

			_obj.subcategories = _subcats;

		} else {

			_obj.apis = srcArr[i];

		}

		destArr.push(_obj);

	}
}
var loadSidebar = function() {
	var output = [];

	var categories = [];

	var categoriesGrouped = [];

	var file = new File(CONFIG_APIS);
	var files = file.listFiles();

	for (var i in files) {

		var path = CONFIG_APIS + files[i].getName();

		var f = new File(path);
		f.open("r");

		var obj = parse(f.readAll());

		if (obj.hasOwnProperty('category2')) {
			categories[obj.category1] = categories[obj.category1] || [];

			if ( typeof categories[obj.category1].subcategories == 'undefined') {
				categories[obj.category1].category1 = obj.category1;
				categories[obj.category1].subcategories = [];

			}
			categories[obj.category1].subcategories[obj.category2] = categories[obj.category1].subcategories[obj.category2] || [];
			categories[obj.category1].subcategories[obj.category2].push(obj);

		} else {
			categories[obj.category1] = categories[obj.category1] || [];
			categories[obj.category1].push(obj);

		}
		f.close();
	}

	groupCategories(categories, categoriesGrouped);

	return categoriesGrouped;

}
var loadSections = function(api) {
	var path = CONFIG_PROPS + api + '.json';
	var file = new File(path);
	file.open("r");
	
	var json = parse(file.readAll());
	
	
	return json.sections;
	
}

var loadExamples = function(api) {
	var path = CONFIG_EXAMPLES + api + '.html';
	var file = new File(path);
	file.open("r");
	
	var html = file.readAll();
	
	
	return html;
	
}