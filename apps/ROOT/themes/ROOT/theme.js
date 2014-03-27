var cache = false;
var CONFIG_PROPS = '/config/properties/';
var log = new Log();

var engine = caramel.engine('handlebars', ( function() {
		return {
			partials : function(Handlebars) {
				var theme = caramel.theme();
				var partials = function(file) {
					(function register(prefix, file) {
						var i, length, name, files;
						if (file.isDirectory()) {
							files = file.listFiles();
							length = files.length;
							for ( i = 0; i < length; i++) {
								file = files[i];
								register( prefix ? prefix + '.' + file.getName() : file.getName(), file);
							}
						} else {
							name = file.getName();
							if (name.substring(name.length - 4) !== '.hbs') {
								return;
							}
							file.open('r');
							Handlebars.registerPartial(prefix.substring(0, prefix.length - 4), file.readAll());
							file.close();
						}
					})('', file);
				};
				//TODO : we don't need to register all partials in the themes dir.
				//Rather register only not overridden partials
				partials(new File(theme.__proto__.resolve.call(theme, 'partials')));
				partials(new File(theme.resolve('partials')));

				Handlebars.registerHelper('render', function(row) {
					var api = request.getParameter('api');
					var firstCol;
					var i = 0;
					var html = "";

					for (var key in row) {
						var col = "";

						col += "<td>";

						// get Memeber/Operation name
						if (i == 0) {
							firstCol = row[key].trim().replace(/[^\w]+/g, '');
							col += "<code>" + row[key] + "</code>";
						} else if (i == (Object.keys(row).length -1)) {
							var path = CONFIG_PROPS + api + '/' + firstCol + '.hbs';
							var file = new File(path);
							// if an HBS isn't found for the particular Memeber/Operation'
							if (!file.isExists()) {
								col += row[key];
							} else {
								file.open("r");
								col += file.readAll();
							}
						} else {
							col += row[key];
						}
						col += "</td>";
						html += col;
						i++;
					}
					return html;
				});
			}
		};
	}()));
