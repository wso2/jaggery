//importClass(Packages.org.apache.solr.client.solrj.impl.CommonsHttpSolrServer);
importClass(Packages.org.apache.solr.client.solrj.embedded.EmbeddedSolrServer);
importClass(Packages.org.apache.solr.core.CoreContainer);
importClass(Packages.org.apache.solr.core.CoreDescriptor);
importClass(Packages.org.apache.solr.core.SolrCore);
importClass(Packages.org.wso2.carbon.utils.CarbonUtils);

var System = java.lang.System;
var ArrayList = java.util.ArrayList;
var JFile = java.io.File;

(function () {

    //set Solr Home
    var SOLR_HOME = CarbonUtils.getCarbonHome() + JFile.separator + "repository" +
        JFile.separator + "conf" + JFile.separator +"SolrRoot";

    var coreContainer = new CoreContainer();
    var coreDescriptor = new CoreDescriptor(coreContainer, "RootSearch", SOLR_HOME);
    var solrCore = coreContainer.create(coreDescriptor);
    coreContainer.register(solrCore, false);
    server = new EmbeddedSolrServer(coreContainer, "RootSearch");


    //var url = "http://localhost:8983/solr"; //need to change this when we put into server
    //server = new CommonsHttpSolrServer(url);

    var dirApi, dirExamples, dirProperties, dirHbs, hbsFileSet, apiContent,
        exampleContent, propertiesContent, subPropertiesContent, indexer,
        searchTitle;

    var pageSets = [];

    dirApi = "/config/apis";
    dirExamples = "/config/examples";
    dirProperties = "/config/properties";
    dirHbs = "/themes/ROOT/partials/";

    apiContent = {};
    searchTitle = {};
    exampleContent = {};
    propertiesContent = {};
    subPropertiesContent = {};

    var pages = [{
        page : "about.hbs",
        url : "/about.jag"
    }, {
        page : "documentation.hbs",
        url : "/documentation.jag",
        overview : "API documentation for Jaggery.js"
    }, {
        page : "quickstart.hbs",
        url : "/quickstart.jag",
        overview : "Setting up Jaggery"
    }, {
        page : "tools.hbs",
        url : "/tools.jag",
        overview : "A list of tools you can use together with Jaggery"
    }, {
        page : "samples.hbs",
        url : "/samples.jag",
        overview : "A list of sample apps created using Jaggery"
    }, {
        page : "tryit.hbs",
        url : "/tryit.jag",
        overview : "Have a bite of Jaggery now!"
    }];


    var fullContents = {};

    function readHbs () {
        var file, hbsFile, hbsContent, fileName, fn;

        hbsFileSet = [];

        for(var i = 0; i < pages.length; i++) {
            //hbsFileSet.push(hbsFile);
            hbsFile = {};
            fileName = (dirHbs + (pages[i]).page);
            fn = ((pages[i]).page).split(".");

            file = new File(fileName);
            file.open("r");
            hbsContent = file.readAll();
            file.close();

            hbsFile.key = fn[0];
            hbsFile.content = hbsContent;
            hbsFile.url = pages[i].url;
            hbsFile.overview = pages[i].overview;

            hbsFileSet[i] = hbsFile;
        }
        return hbsFileSet;
    }

    function readExamples () {
        var file, fileName;
        var dir = new File(dirExamples);
        if (dir.isDirectory()) {
            var listFiles = dir.listFiles();
            var fileCount = listFiles.length;

            for (var i = 0; i < fileCount; i++) {
                file = listFiles[i];
                file.open("r");
                fileName = (file.getName()).split(".");
                exampleContent[fileName[0]] = file.readAll();
                file.close();
            }
        }
    }

    function readProperties () {
        var file, subDirFile, fileName, subDirFileSet;
        var dir = new File(dirProperties);
        if (dir.isDirectory()) {
            var listFiles = dir.listFiles();
            var fileCount = listFiles.length;

            for (var i = 0; i < fileCount; i++) {
                file = listFiles[i];

                if (file.isDirectory()) {

                    var subListFiles = file.listFiles();
                    var subFileCount = subListFiles.length;
                    subDirFileSet = {};
                    subPropertiesContent[file.getName()] = subDirFileSet;

                    for (var k = 0; k < subFileCount; k++) {
                        subDirFile = subListFiles[k];
                        subDirFile.open("r");
                        fileName = (subDirFile.getName()).split(".");
                        //subPropertiesContent[fileName[0]] = subDirFile.readAll();
                        subDirFileSet[fileName[0]] = subDirFile.readAll();
                        subDirFile.close();
                    }
                } else {
                    file.open("r");
                    fileName = (file.getName()).split(".");
                    propertiesContent[fileName[0]] = file.readAll();
                    file.close();
                }
            }
        }
    }

    function readApis () {
        var file, fileName, fName;
        var dir = new File(dirApi);
        if (dir.isDirectory()) {
            var listFiles = dir.listFiles();
            var fileCount = listFiles.length;

            for (var i = 0; i < fileCount; i++) {
                file = listFiles[i];
                file.open("r");
                var jsonApi = JSON.parse(file.readAll());
                fName = (file.getName()).split(".");
                fileName = fName[0];

                var overview = jsonApi.overview;
                apiContent[fileName] = overview;
                searchTitle[fileName] = overview;
            }
        }
    }

    function hasOwnProperty (obj, prop) {
        return Object.prototype.hasOwnProperty.call(obj, prop);
    }

    readExamples();
    readProperties();
    readApis();

    /**
     * description of searched result.
     * */
    var title;
    for(var t in searchTitle) {
        title = searchTitle[t].replace(/<[^>]*>/g, "");
        searchTitle[t] = title;
    }

    var subContent;
    var apiKeys = Object.keys(apiContent);

    apiKeys.every(function(value) {
        value = value.toString();

        fullContents[value] = apiContent[value];

        if(hasOwnProperty(exampleContent, value)) {
            fullContents[value] = fullContents[value].concat(" ").
            concat(exampleContent[value]);
        }

        if(hasOwnProperty(propertiesContent, value)) {
            fullContents[value] = fullContents[value].concat(" ").
            concat(propertiesContent[value]);
        }

        if(hasOwnProperty(subPropertiesContent, value)) {
            subContent = subPropertiesContent[value];

            for (var key in subContent) {
                fullContents[value] = fullContents[value].concat(" ").
                concat(subContent[key]);
            }
        }
            return true;
    });

    var page, pageContent;

    for(var key in fullContents) {
        page = {};
        page.key = key;
        page.content = fullContents[key];
        page.url = "/documentation.jag?api="+key;
        page.overview = searchTitle[key];

        pageSets.push(page);
    }

    var hbsPageSets = readHbs();
    pageSets = pageSets.concat(hbsPageSets);

    /**
     * removing all <a> tags
     *
     * we cannot use solr.HTMLStripCharFilterFactory in solr.it will remove all the content
     * inside <script></script> tags.
     * some examples provided in jaggery doc includes search keywords inside script tags.
     */

    for (var j = 0; j < pageSets.length; j++) {
        pageContent = pageSets[j].content;
        pageSets[j].content = pageContent.replace(/(<[a|A][^>]*>|)/g, "");
    }

    indexer = require('/modules/search.js');
    indexer.index(pageSets);
})();
