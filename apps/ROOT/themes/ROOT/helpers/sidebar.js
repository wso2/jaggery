var log = new Log();
var CONFIG_BASE = '/config/apis/';

var ii = 0;

var groupCategories = function(srcArr, destArr){
	
	
	for(var i in srcArr){
		
		var _obj = {};
			
		log.info("---------------COUNTER:" + ii++);	

		_obj.category = i;
			
		var _subcats = [];
			
		if(typeof srcArr[i].subcategories != 'undefined'){
			log.info("RECURSIVE START***************");
			groupCategories(srcArr[i].subcategories, _subcats);
			
			_obj.subcategories = _subcats;
			
		} else {
			
			_obj.apis = srcArr[i];
			
			
			log.info("//////destArr");
			log.info(destArr);
			log.info("-----destArr");
		}
		
		destArr.push(_obj);
	}
}

var populateSidebar = function() {
	var output = [];

	var categories = [];
	
	var categoriesGrouped = [];
	
	//var subcategories = [];

	var file = new File(CONFIG_BASE);
	var files = file.listFiles();

log.info("FILE COUNT: " + files.length);
	for (var i in files) {
		
		//var path = files[i].getPath();
		var path = CONFIG_BASE + files[i].getName();

		var f = new File(path);
		f.open("r");

		var obj = parse(f.readAll());

		
		if (obj.hasOwnProperty('category2')) {


			categories[obj.category1] = categories[obj.category1] || [];

			if ( typeof categories[obj.category1].subcategAPICategoriesories == 'undefined') {
				categories[obj.category1].category1 = obj.category1;
				categories[obj.category1].subcategories = [];
				
				
				//log.info(obj);
			} 
			categories[obj.category1].subcategories[obj.category2] = categories[obj.category1].subcategories[obj.category2] || [];
			categories[obj.category1].subcategories[obj.category2].push(obj);

		} else {
			categories[obj.category1] = categories[obj.category1] || [];
			categories[obj.category1].push(obj);
			//log.info(obj);
		}

		f.close();
	}
	
	
	/*
	log.info(">>>>>>>>>>>>>>>");
	
		for (var j in categories) {
	
		log.info(categories[j]);
	
			if (categories[j].hasOwnProperty('subcategories')) {
				var _obj = {};
				_obj.category = categories[j].category1;
				_obj.subcategories = [];
	
				for (var k in categories[j].subcategories ) {
	
					var __obj = {};
					__obj.category = "";
					__obj.apis = [];
	
					__obj.category = categories[j].subcategories[k][0].category2;
	
					__obj.apis = categories[j].subcategories[k];
	
					_obj.subcategories.push(__obj);
				}
	
				output.push(_obj);
	
			} else {
				output.push({
					category : categories[j][0].category1,
					apis : categories[j]
				});
			}
		}*/
	groupCategories(categories, categoriesGrouped);

	return {
		categories : categoriesGrouped
	};
}
