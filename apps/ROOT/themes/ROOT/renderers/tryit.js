var render = function (theme, data, meta, require) {
    theme('1-column', {
        title: [
			{ partial:'title', context: data.title}
		],
		nav: [
			{ partial:'nav', context: require('/helpers/nav.js').currentPage(data.nav)}
		],
		body: [
			{ partial:'tryit'}
		]
    });
};

