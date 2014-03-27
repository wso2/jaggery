var render = function (theme, data, meta, require) {
    theme('2-columns', {
        title: [
			{ partial:'title', context: data.title}
		],
		nav: [
			{ partial:'nav', context: require('/helpers/nav.js').currentPage(data.nav)}
		],
		sidebar: [
			{ 
				partial:'sidebar-samples', context: data.sidebar}
		],
		body: [
			{ partial:'samples'}
		]
    });
};

