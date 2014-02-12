var render = function (theme, data, meta, require) {
    theme('1-column-simple', {
        title: [
			{ partial:'title', context: data.title}
		],
		nav: [
			{ partial:'nav', context: data.nav}
		],
		body: [
			{ partial:'body', context: data.body}
		]
    });
};

