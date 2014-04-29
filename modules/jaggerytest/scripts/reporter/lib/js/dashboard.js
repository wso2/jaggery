TestApp = new function () {
    //name of test framework will be top in here rather 'TestApp'

    testCount = null;
    selectedCount = 0;
    runningCount = 0;
    passCount = 0;
    failCount = 0;
    failArray = [];

    /**
     * function is will be called to run the test
     */
    this.runTest = function () {
        this.countReset();
        for (var i = 0; i < testCount; i++) {
            testID = i;
            $('#err' + testID + '').html('');

            if ($('#chk' + testID + '').is(':checked')) {
                ++runningCount;
                this.makeCallToTest(testID);
            } else {
                $('#res' + testID + '').html('<div  class="alert alert-info">Not Selected</div>');
            }
        }
        this.displaySummaryReport();
    },

    /**
     * function will selectAll depending on parameter
     * @param call {boolean} true for all and false for none
     */
    this.selectAll = function (call) {
        var i = 0;
        for (; i < testCount; i++) {
            $('#chk' + i + '').prop('checked', call);
        }
        if (call) {
            selectedCount = i;
        } else {
            selectedCount = 0;

        }
        this.displayInfoUpdate();
    },

    /**
     * function will pick all failed test after test are execution
     */
    this.selectFail = function () {
        selectedCount = 0;
        this.selectAll(false);
        var i = 0;
        for (; i < failArray.length; i++) {
            $('#chk' + failArray[i] + '').prop('checked', true);
            selectedCount++;
        }

        this.displayInfoUpdate();
    },

    /**
     * function will pick all test suit for test specification
     * @param testSpec - name of testSpec
     */
    this.selectSpec = function (testSpec) {
        selectedCount = 0;
        this.selectAll(false);
        var i = 0;
        for (; i < testCount; i++) {

            if ($('#chk' + i).attr('parent').indexOf(testSpec) == 0) {
                $('#chk' + i + '').prop('checked', true);
                selectedCount++;
            }
        }

        this.displayInfoUpdate();
    },


    /**
     * function will handle test list picker
     * @param checkBox
     */
    this.handleClick = function (checkBox) {
        if (checkBox.checked) {
            selectedCount++;
        } else {
            selectedCount--;
        }
        this.displayInfoUpdate();
    },

    /**
     * function will make AJAX call for test
     * @param testId - test suite ID in dashboard
     */
    this.makeCallToTest = function (testID) {
        $('#basicInfor').html('Test count is ' + testCount + ' running ' + selectedCount + '/' + testCount);


        TestAppUtil.makeJsonRequest(document.location.pathname + $('#chk' + testID + '').val(), null, function (html) {
            if (!html.error) {
                if (html.suites[0].itemResult == "Passed.") {
                    TestApp.testPass(testID, html);
                } else {
                    TestApp.testFail(testID, html);
                }
                TestApp.displayInfoUpdate();
            } else {

                TestApp.testCallError(testID, html);
            }
        });


    },


    /**
     * will update UI for select count for test suites
     */
    this.displayInfoUpdate = function () {
        $('#basicInfor').html(selectedCount + ' selected out of ' + testCount);
    },

    /**
     * function testPass will handle action after test suite is passed
     */
    this.testPass = function (testID, data) {
        $('#err' + testID + '').html('');
        if (data.suites[0].items[0]) {
            passCount++;
            $('#res' + testID + '').html('<div class="alert alert-success">Passed</div>');
        } else {
            $('#res' + testID + '').html('<div class="alert alert-info">No Test found</div>');
        }
    },

    /**
     * function testPass will handle action after test suite is fails
     */
    this.testFail = function (testID, data) {
        failCount++;
        failArray.push(testID);
        var errMsgList = '';
        $('#res' + testID + '').html('<div class="alert alert-danger"> Failed</div>');
        for (var j = 0; l = data.suites[0].itemCount, j < l; j++) {
            if (data.suites[0].items[j].message != 'Passed.') {
                errMsgList += '['+ (j+1) +' assets] '+data.suites[0].items[j].message+' <br>';

            }
        }
        $('#err' + testID + '').html('<div><code>' + errMsgList + '</code></div>');
    },

    /**
     * function testPass will handle action after test suite meeting error in calling
     */
    this.testCallError = function (testID, data) {
        $('#res' + testID + '').html('<div class="alert alert-danger">Fail to Call the Test</div>');
        $('#err' + testID + '').html('<div><code>' + data.message + '</code></div>');
    },

    /**
     * function will display summary report of test suites
     */
    this.displaySummaryReport = function () {
        $('#summaryReport').html(
            '<p>Pass Count out of Run count: <b>' + passCount + '/' + runningCount + '</b><br>Fail Count out of Run count: <b>' + failCount + '/' + runningCount + '</b><br>Run Count out of Selected Test: ' + runningCount + '/' + selectedCount + '</p>');
    },

    /**
     * function will display display error message
     */
    this.displayErrorMessage = function (message) {
        $('#summaryReport').html('<p>Error!</p>');
        $('#controllers').html('');
        $('#sampleLoc').html('<div class="alert alert-danger">Error! ' + message + '</div>');
    },

    /**
     * function will reset count in test
     */
    this.countReset = function () {
        runningCount = 0;
        passCount = 0;
        failCount = 0;
        failArray = [];
    },

    /**
     * function will load test suite List
     */
    this.loadSuiteList = function () {
        //in here we will get warning on (that to be fixed jquery http://bugs.jquery.com/ticket/14320)
        TestAppUtil.makeJsonRequest(document.location.pathname, {
                action: 'listsuits'
            },
            function (html) {
                if (!html.error) {

                    //template for suites
                    $('#basicInfor').html('Test count is' + html.specsCount);
                    testCount = html.specsCount;
                    if (html.specs) {
                        var template = '<table class="table table-hover">{{#.}}<tr><td><label> <input type="checkbox" id="chk{{id}}" value="{{url}}" parent="{{parentName}}" onclick="TestApp.handleClick(this)" checked>  {{name}} <b>[{{parentName}}]</b><div id="err{{id}}"></div> <label> </td><td><div id="res{{id}}"></div></td> </tr>{{/.}}<table>';
                        var htmlx = Mustache.to_html(template, html.specs);
                        $('#sampleLoc').html(htmlx);

                    } else {
                        $('#sampleLoc').html('No test Suite is found still');
                    }
                } else {
                    TestApp.displayErrorMessage(html.message);

                }

            });
        selectedCount = testCount;
        this.loadSpecList();
        this.displayInfoUpdate();
    };

    /**
     * function will load test Specification List
     */
    this.loadSpecList = function () {

        TestAppUtil.makeJsonRequest(document.location.pathname, {
                action: 'listspecs'
            },
            function (html) {
                if (!html.error) {
                    $('#dropdown-menu-button').append('<li class="divider"></li>');
                    var SpecTemplate = '{{#.}}<li><a onclick="TestApp.selectSpec(\'{{name}}\')">{{name}}</a></li>{{/.}}';
                    var htmlSpec = Mustache.to_html(SpecTemplate, html.specs);
                    $('#dropdown-menu-button').append(htmlSpec);
                } else {
                    TestApp.displayErrorMessage(html.message);

                }

            });

    };



};