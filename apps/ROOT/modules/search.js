importClass(Packages.org.apache.solr.client.solrj.SolrQuery);
importClass(Packages.org.apache.solr.client.solrj.response.QueryResponse);
importClass(Packages.org.apache.solr.common.SolrDocumentList);
importClass(Packages.org.apache.solr.common.SolrInputDocument);

var ArrayList   = java.util.ArrayList;
var Collection  = java.util.Collection;

var result = function (searchParameter) {

    var solr = server

    var query = new SolrQuery();
    query.setQuery(String(searchParameter));
    //query.addSortField("price", SolrQuery.ORDER.asc);
    //query.setHighlight(true).setHighlightSnippets(1);
    //query.setParam("hl.fl", "document");

    /*auto creation of a query
     * SolrQuery solrQuery = new  SolrQuery().
     setQuery("ipod").
     setFacet(true).
     setFacetMinCount(1).
     setFacetLimit(8).
     addFacetField("category").
     addFacetField("inStock");
     QueryResponse rsp = server.query(solrQuery);*/
    var res = [],
        tmpUrl,
        hasquery;

    var response = solr.query(query);
    var results = response.getResults();

    for (var i = 0; i < results.size(); ++i) {
        var obj = {};
        obj.key = results.get(i).getFieldValue("id");

        tmpUrl = String(results.get(i).getFieldValue("url"));

        hasquery = tmpUrl.indexOf('?');

        if (hasquery == -1) {
            tmpUrl = tmpUrl.concat("?keyword=" + searchParameter);
        } else {
            tmpUrl = tmpUrl.concat("&keyword=" + searchParameter);
        }

        obj.url = tmpUrl;
        obj.overview = String(results.get(i).getFieldValue("overview"));

        res[i] = obj;
    }
    return res;
}

var index = function (contentSet) {
    var csLength = contentSet.length;
    var docs = new ArrayList();
    var content, doc;

    //server.setMaxRetries(1);
    //server.setConnectionTimeout(5000);

    for(var k = 0; k < csLength; k++) {
        content = contentSet[k];

        doc = new SolrInputDocument();
        doc.addField( "id", content.key);
        doc.addField( "content", content.content);
        doc.addField( "url", content.url);
        doc.addField("overview", content.overview);

        docs.add(doc);
    }

    server.add(docs);
    server.commit();
}

