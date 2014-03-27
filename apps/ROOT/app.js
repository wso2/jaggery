var caramel = require('caramel');

caramel.configs({
    context: '',
    cache: true,
    negotiation: true,
    themer: function () {
        return 'ROOT';
    }
});
