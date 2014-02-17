var render = function (theme, data, meta, require) {
    theme('modal', {
		body: [
			{ 
				partial:'tryitframe',
				context: data.body
			}
		]
    });
};

