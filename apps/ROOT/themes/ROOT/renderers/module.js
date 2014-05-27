var render = function(theme, data, meta, require) {

    var log = new Log();

    if (data.nav.currentPage == 'module') {
        theme('2-columns', {
            title : [{
                partial : 'title',
                context : data.title
            }],
            nav : [{
                partial : 'nav',
                context : require('/helpers/nav.js').currentPage(data.nav)
            }],
            sidebar : [{
                partial : 'sidebar-documentation',
                context : data.sidebar
            }],
            body : [{
                partial : 'module',
                context : data.body
            }]
        });
    } else if (data.nav.currentPage == 'documentation') {
        theme('2-columns', {
            title : [{
                partial : 'title',
                context : data.title
            }],
            nav : [{
                partial : 'nav',
                context : require('/helpers/nav.js').currentPage(data.nav)
            }],
            sidebar : [{
                partial : 'sidebar-documentation',
                context : data.sidebar
            }],
            body : [{
                partial : 'documentation',
                context : data.body
            }]
        });
    }

};

