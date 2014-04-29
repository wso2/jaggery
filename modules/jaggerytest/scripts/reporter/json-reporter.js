jasmine.JSONReporter = function () {

    var startingTime = (new Date()).getTime(),
        log = new Log('jsonReporter');

    this.exports = {
        elapsedTime: null,
        specsCount: 0,
        specsPassed: 0,
        specsFailed: 0,
        specsResult: 'Passed',
        suites: []
    },

    this.listexports = {
        specsCount: 0,
        specs: []
    },

    /**
     * reportRunnerResults will called finally for to get results
     */
    this.reportRunnerResults = function () {
        this.exports.elapsedTime = parseInt((new Date()).getTime() - startingTime, 10) + "ms";
        var outJson = JSON.stringify(this.exports);
        log.debug(outJson);
        if (!test.toListSuites() && !test.toListSpecs()) {

            if (this.exports.specsCount - this.exports.specsPassed !== 0) {
                this.loged('Exiting with errors');
            } else {
                this.loged('Exiting with  ' + this.exports.specsCount + ' test suite with passing ');
            }
            print(outJson);
        }


    },

    /**
     * jasmine API for reporting will be called report initialization
     *
     * @param jasmine
     *                runner
     *
     */
    this.reportRunnerStarting = function (runner) {
        log.debug('reportRunnerStarting with Jasmine --- version' + runner.env.versionString());
        if (test.toListSuites()) {
            this.listAllSuites(runner);
        } else if (test.toListSpecs()) {
            this.listAllSpecs(runner);
        }
    },

    /**
     * Function will list All test Suite (simple values of string 'it()' in test class)
     * @param jasmine runner (ENV details)
     */
    this.listAllSuites = function (runner) {
        var specs = runner.specs() || [],
            specLength = specs.length,
            listEndPoints = [];
        for (var i = 0; i < specLength; i++) {
            suit = {
                id: null,
                name: null,
                url: null
            };

            currentSite = specs[i];
            fullName = currentSite.getFullName();
            name = currentSite.description;
            parentName = fullName.substr(0, fullName.indexOf(name));

            //adding element to test suit
            suit.id = currentSite.id;
            suit.name = name;
            suit.fullname = fullName;
            suit.parentName = parentName;
            suit.url = encodeURIComponent(suit.fullname);
            listEndPoints[i] = suit;

        }
        this.listexports.specsCount = specLength;
        this.listexports.specs = listEndPoints;
        var outJson = JSON.stringify(this.listexports);
        print(outJson);

    },

    /**
     * Function will list All test specification
     * @param jasmine runner (ENV details)
     *
     */
    this.listAllSpecs = function (runner) {
        var suites = runner.suites();
        log.debug('listAllSpecs - suites.length-------' + suites.length);
        var listEndPoints = [];

        for (var i = 0; i < suites.length; i++) {
            spec = {
                name: null,
                url: null,
                id: null,
                parentID: null
            };
            var specx = suites[i];
            spec.name = specx.getFullName();
            spec.id = specx.id;
            spec.url = encodeURIComponent(spec.name);
            if (specx.parentSuite) {
                log.debug('parent ID is ' + specx.parentSuite.id + ' of Siute ID ' + specx.id);
                spec.parentID = specx.parentSuite.id;
                // log.debug('suite Source ::'+suite.toSource());
            }
            listEndPoints[i] = spec;

            log.debug(i + ' Siute ID ' + specx.id + ', suite describtion ' + specx.description);



        }
        this.listexports.specs = listEndPoints;
        var outJson = JSON.stringify(this.listexports);
        print(outJson);


    },
    /**
     * function is result building for each specification
     * Need to check validating for reporting as it all come from jasmine api
     * @param spec is testSpec
     */
    this.reportSpecResults = function (spec) {
        if (test.getSpecification() == spec.getFullName() || test.getSpecification() == null) {
            this.exports.specsCount += 1;

            if (spec.results().passed()) {
                this.exports.specsPassed++;
            } else {
                this.exports.specsFailed++;
                this.exports.specsResult = 'Failed';
            }
            this.builderMessage(spec);
        }
    },
    /**
     * will build message for one suite that will be one element in 'suites'
     * @param Spec - testSpc will be passing
     */
    this.builderMessage = function (spec) {


        var l = spec.results().items_.length,
            items = new Array(),
            itemResult = 'Passed.';
        for (var i = 0; i < l; i++) {
            if (spec.results().items_[i].message != 'Passed.') {
                itemResult = 'Failed';
            }
            log.debug(spec.results().items_[i]);
            items[i] = {
                type: spec.results().items_[i].type,
                message: spec.results().items_[i].message

            };

        }

        // log.debug(spec.results());
        // log.debug(spec.suite.toSource());
        this.exports.suites.push({
            suite: spec.suite.description,
            spec: spec.description,
            passed: spec.results().passed(),
            itemCount: spec.results().totalCount,
            passedCount: spec.results().passedCount,
            failedCount: spec.results().failedCount,
            skipped: spec.results().skipped,
            itemResult: itemResult,
            items: items

        });

    },

    /**
     * Will be used to logged in finally/ make this info to get log for JSON report finally.
     * @param arguments String message to passing for log
     */
    this.loged = function (arguments) {
        log.info(arguments);
    };
};