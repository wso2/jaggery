var log = new Log();
log.info("Initializing Jaggery test app2");
application.serve(function test(request, respond, session) {
    var action = request.getParameter("action");
    log.debug("jaggery Application Serve is ping");

    switch(action) {
        case "query":
            print(request.getQueryString());
            break;

        case "url" :
            print(request.getQueryString());
            break;

        case "secure" :
            print(request.isSecure());
            break;

        case "port" :
            print(request.getLocalPort());
            break;

        case "response" :
            response.content = "My response content";
            break;

        case "responseJson" :
            response.content = {
                products : ["Jaggery", "ESB"]
            };
            break;

        case "sessionset" :
            session.set("wso2", "testing");
            print("session testing. ");
            break;

        case "sessionget" :
            var name = session.get("wso2");
            print(name);
            break;
            
        //checking session     
        case "session-set" :
            Session["wso2"] = "testing";
            print("session testing. ");
            break;

        case "session-get" :
            var name = Session["wso2"];
            print(name);
            break;

        default:
            print('app serve testing');
    }

});
