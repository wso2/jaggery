var log = new Log();
log.info("Initializing Jaggery test app2");
application.serve(function test(request, respond, session) {
    //log.info("jaggery Application Serve is ping");     
    if (action == "query") {
        print(request.getQueryString());
    } else if (action == "url") {
        print(request.getRequestURI());
    } else if (action == "action") {
        print(request.getParameter("action"));
    } else if (action == "session") {
        Session["wso2"] = "testing";
        print("session testing. ");
        var name = Session["wso2"];
        print("Data is " + name);
    } else {
        print("application serve");
    }


});