var currentPage = function(data) {
	var currentPage = data.currentPage;

	var pages = [{
		page : "About",
		url : "/about.jag"
	}, {
		page : "Quickstart",
		url : "/quickstart.jag"
	}, {
		page : "Documentation",
		url : "/documentation.jag"
	}, {
		page : "Tools",
		url : "/tools.jag"
	}, {
		page : "Samples",
		url : "/samples.jag"
	}, {
		page : "Try it!",
		url : "/tryit.jag"
	}];

	for (i in pages) {

		if (pages[i].page.toLowerCase() == currentPage.toLowerCase()) {
			pages[i].active = true;
			break;
		}
	}

	return {
		pages : pages
	};
}
