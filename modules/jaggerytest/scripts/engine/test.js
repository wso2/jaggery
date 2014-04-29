var test = (function () {

    // for test specification name identification
    var specifcation = null,
        log = new Log('test - sugarcane'),
        action = null,
        FILELOAD_PATH = 'scripts/reporter/lib/',
        DASHBOARD_PAGE = 'dashboard.html',
        MODULE_PATH = '/modules/sugarcane/';
    //test specification extension
    var TEST_FILE_EXTENSIOIN = 'js',
        LIST_ACTION = {
            listSuits: 'listsuits',
            listSpecs: 'listspecs'
        };


    /**
     *
     * @param func
     * @returns {jasmine.Spec}
     */
    jasmine.Spec.prototype.runs = function (func) {
        var block = new jasmine.Block(this.env, func, this),
            spec = specifcation;
        action = request.getParameter("action");
        log.debug('specifcation is found ' + spec);
        // replace to id for this.env.currentSpec.suite.description

        //checking 'listSpecs' is called then do not added all in to execution list for to skip it.
        if (this.env.currentSpec.suite.queue.env.currentSpec.getFullName() == spec && action != LIST_ACTION.listSuits && action != LIST_ACTION.listSpecs) {
            this.addToQueue(block);
            //  } else if (this.env.currentSpec.suite.queue.env.currentSpec.id == spec) { 1.1V //checking for ID 
        } else if (this.env.currentSpec.suite.queue.env.currentSpec.getFullName() == spec) {
            this.addToQueue(block);

        }

        return this;
    };

    /**
     * overriding next_ in jasmine
     */
    jasmine.Queue.prototype.next_ = function () {
        var self = this;
        var goAgain = true;

        while (goAgain) {
            goAgain = false;

            if (self.index < self.blocks.length && !(this.abort && !this.ensured[self.index])) {
                var calledSynchronously = true;
                var completedSynchronously = false;

                var onComplete = function () {
                    if (jasmine.Queue.LOOP_DONT_RECURSE && calledSynchronously) {
                        completedSynchronously = true;
                        return;
                    }

                    if (self.blocks[self.index].abort) {
                        self.abort = true;
                    }

                    self.offset = 0;
                    self.index++;

                    var now = new Date().getTime();

                    if (self.env.lastUpdate == 0) {

                        self.env.lastUpdate = now;
                    }

                    if (self.env.updateInterval && now - self.env.lastUpdate > self.env.updateInterval) {
                        self.env.lastUpdate = now;
                        self.next_();
                    } else {
                        if (jasmine.Queue.LOOP_DONT_RECURSE && completedSynchronously) {

                            goAgain = true;
                        } else {
                            self.next_();
                        }
                    }
                };
                self.blocks[self.index].execute(onComplete);

                calledSynchronously = false;
                if (completedSynchronously) {
                    onComplete();
                }

            } else {
                self.running = false;
                if (self.onComplete) {
                    self.onComplete();
                }
            }
        }
    };

    /*
     * END of jasmine over overriding.
     */

    /**
     * run is main function that will be called from application test level
     * this function will register the reporter for ENV in jasmine
     *
     */
    var run = function () {
        log.debug(request.getContentType() + 'Called the run for test-------' + request.getHeader("User-Agent") + ':');
        var jasmineEnv = jasmine.getEnv();

        //text/html --> simpleHTMLReporter removed since AJAX dash-board is using
        if (request.getContentType() == 'application/json') {
            var jsonReporter = new jasmine.JSONReporter();
            jasmineEnv.addReporter(jsonReporter);

        } else if (request.getHeader("User-Agent") != null && (request.getRequestURI().indexOf('.js') == -1) && (request.getRequestURI().indexOf('.css') == -1)) {
            loadFileToFront(FILELOAD_PATH + DASHBOARD_PAGE);
        }
        if (urlMapper()) {
            jasmineEnv.execute();
        }
    };

    /**
     * function have URl mapping for pattern (n *m , 0<n,m<N) folder structure with one
     * file inside with specification
     *
     * http://localhost:9763/automobile/test[/{folderName}0-n][/{fileName}0-1][/{
     * testSpecName }0-1] * above URL pattern can be handle
     *
     */
    var urlMapper = function () {
        var uri = request.getRequestURI(),
            pathMatcher1 = null,
            pathMatcher2 = null,
            uriMatcher = new URIMatcher(uri);


        // Provide a pattern to be matched against the URL
        if (uriMatcher.match('/{appname}/{test}/{+path}')) {
            // If pattern matches, elements can be accessed from their keys
            log.debug('uriMatcher.elements().path :: ' + uriMatcher.elements().path);
            var indexU = uriMatcher.elements().path.indexOf('utilities');
            if (indexU != -1) {
                log.debug('css or js file request for page.' + uriMatcher.elements().path.substr(indexU + 9));
                loadFileToFront(FILELOAD_PATH + uriMatcher.elements().path.substr(indexU + 9));
                return false;
            } else {
                pathMatcher1 = '/' + uriMatcher.elements().test + '/' + uriMatcher.elements().path;
                if (pathMatcher1 != null) {
                    pathMatcher2 = pathMatcher1.substr(0, pathMatcher1
                        .lastIndexOf("/"));
                    if (isValidPath(pathMatcher1, pathMatcher2)) {
                        specifcation = pathMatcher1.substr(pathMatcher1
                            .lastIndexOf("/") + 1);
                    }
                }
                log.debug(request.getContentType() + ' path : ' + pathMatcher1 + ',' + pathMatcher2);
                return validatepattern(pathMatcher1, pathMatcher2) && !errorFound;
            }
        }

        if (uriMatcher.match('/{appname}/{test}/')) {
            // root pattern for test Directory
            log.debug('getting all file from Require');
            crawl('/' + uriMatcher.elements().test);
            return !errorFound;
        }

    };

    /**
     * function is validate URL pattern for requiring files
     *
     * @param pathMatcher1
     *            is path of a pattern of path can existing
     * @param pathMatcher2
     *            is path of a pattern of path can existing for second level
     * @returns boolean
     */
    var validatepattern = function (pathMatcher1, pathMatcher2) {
        if (!requireFiles(pathMatcher1)) {
            if (!(requireFiles(pathMatcher2)) && pathMatcher2 && !(isValidPath(pathMatcher1, pathMatcher2)) && request.getContentType()) {
                log.debug("Path not existing");
                print({
                    'error': true,
                    'message': 'path \'' + pathMatcher1 + '\' is not valid'
                });
                return false;
            }
        }
        return true;
    };


    /**
     * function will require all test specification js files/file in path
     *
     * @param path
     *            can be directory or to file path with out extension all test specification is
     *            define as *.js
     * @returns
     */
    var requireFiles = function (path) {
        log.debug('requiring from ' + path);
        isCompleted = false;
        if (isDirectory(path)) {
            isCompleted = true;
            log.debug("is Dir " + path);
            crawl(path);
        } else if (isExists(path + '.' + TEST_FILE_EXTENSIOIN)) {
            isCompleted = true;
            log.debug("is File " + path + '.' + TEST_FILE_EXTENSIOIN);
            require(path + '.' + TEST_FILE_EXTENSIOIN);
        }
        return isCompleted;
    };

    /**
     * function crawl will require all the file in root location
     *
     * @param root
     *            is path of directory to search for test specification files
     */
    var errorFound = false;
    var crawl = function (root) {
        log.debug('crawling on root called ' + root);
        var file = new File(root),
            list = file.listFiles();
        //stop crawling if error found
        if (list == null || errorFound)
            return;
        try {
            for (var i = 0; i < list.length && !errorFound; i++) {
                log.debug(i + ' checking files on ' + root);
                var f = list[i];

                if (f.isDirectory()) {

                    log.debug("Dir:" + f.getName());
                    crawl(root + '/' + f.getName());
                } else {
                    log.debug("File:" + f.getName());
                    if (isTestspecFile(f.getName())) {
                        log.debug("Test File:" + f.getName());
                        require(root + '/' + f.getName());
                    }
                }

            }
        } catch (error) {

            errorFound = true;
            log.debug('error ocuring on crawling. Error is ' + error);
            errorMessagePasser(error.message);

            return;

        }

    };

    /**
     *
     * @param path of the file inside jaggery module
     * @returns file will be return for front end
     */
    var loadFileToFront = function (filePath) {
        var file = new File(absolute(filePath));
        print(file.getStream());
    };

    /**
     * function absolute to get absolute path of file
     * @param path (file path) module located file path
     */
    var absolute = function (path) {
        var systemProcess = require('process');
        var parent = 'file:///' + (systemProcess.getProperty('jaggery.home') || systemProcess.getProperty('carbon.home')).replace(/[\\]/g, '/').replace(/^[\/]/g, '');
        return parent + MODULE_PATH + path;
    };

    /**
     * function errorMessagePasser will builder meaningful error message for the test user/test writter
     * @param message will error message
     */
    var errorMessagePasser = function (message) {
        var errorMessage = null;
        log.debug('errorMessagePasser' + message);
        if ((i = message.indexOf('Requested resource')) != -1) {
            errorMessage = 'Test Spec file is pointing to non exiting files.' + message.substring(i);
            log.debug('Resource missing error' + message);
        } else if ((i = message.indexOf('java.lang.NullPointerException ')) != -1) {
            log.debug('Directory missing error' + i);
            errorMessage = 'Test Spec file is pointing to non existing directory.There is a ' + message.substring(i);
        } else if ((i = message.indexOf('syntax errors')) != -1) {
            log.debug('syntax errors' + message);
            errorMessage = 'Test Spec file is having a ' + message.substring(i);
        } else if ((i = message.indexOf('A module')) != -1) {
            log.debug('jaggery module error' + message);
            errorMessage = 'Test Spec file is try to accessing non existing jaggery module. ' + message.substring(i);

        } else {
            errorMessage = message;
        }
        if (request.getContentType())
            print({
                'error': true,
                'message': errorMessage
            });
    };

    /**
     * checking client request call 'listsuits'
     */
    var toListSuites = function () {
        log.debug('toListSuites - ' + LIST_ACTION.listSuits);
        if (action == LIST_ACTION.listSuits) {
            return true;
        } else {
            return false;
        }
    };

    /**
     * checking client request call 'listspecs'
     */
    var toListSpecs = function () {
        log.debug('toListSpecs - ' + LIST_ACTION.listSpecs);
        if (action == LIST_ACTION.listSpecs) {
            return true;
        } else {
            return false;
        }
    };

    /**
     * checking client request call 'test Specification' in URL
     */
    var getSpecification = function () {
        return specifcation;
    };


    /**
     * function is to checks whether the given path is a directory
     *
     * @param path
     * @returns directory is existing return true
     */
    var isDirectory = function (path) {
        var file = new File(path);
        return file.isDirectory();
    };

    /**
     * function is to checks whether file is test specification file
     *
     * @param filename
     * @returns boolean if files is test specification
     */
    var isTestspecFile = function (file) {
        log.debug('isTestspecFile is called for file ' + file);
        if (getfileExtension(file) == TEST_FILE_EXTENSIOIN) {
            return true;
        } else {
            return false;
        }
    };

    /**
     * function to get file extension
     *
     * @param filename
     * @returns string of the file extension
     */
    var getfileExtension = function (file) {
        var nameComponents = file.split('.');
        if (nameComponents.length < 1) {
            return null;
        }
        return nameComponents[nameComponents.length - 1];
    };

    /**
     * file give the name of the file
     * @param path
     * @returns Name of the file without the path
     */
    var getName = function (path) {
        var file = new File(path);
        return file.getName();
    };

    /**
     *
     * @param path
     * @returns checks whether this file actually exists. Returns true if the
     *          file exists.
     */
    var isExists = function (path) {
        var file = new File(path);
        return file.isExists();
    };

    /**
     * validation check for specification name of test suite
     *
     * @param path1
     * @param path2
     * @returns {Boolean}
     */
    var isValidPath = function (path1, path2) {
        var isValid = false;
        if ((isDirectory(path2) || isExists(path2 + '.' + TEST_FILE_EXTENSIOIN)) && !(isDirectory(path1) || isExists(path1 + '.' + TEST_FILE_EXTENSIOIN))) {
            log.debug('Current URL - path is validated for testspec name.');
            isValid = true;
        }
        return isValid;
    };

    /**
     * Exposing functions
     */
    return {
        run: run,
        loadFileToFront: loadFileToFront,
        toListSuites: toListSuites,
        toListSpecs: toListSpecs,
        getSpecification: getSpecification
    };

}());