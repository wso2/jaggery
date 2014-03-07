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
            print(request.getRequestURL());
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

        case "sessionset" :
            session.put("wso2", "testing");
            print("session testing.");
            break;

        case "sessionget" :
			session.put("me", "test me");
            var name = session.get("me");
            print(name);
            break;            
        
        default:
            print('app serve testing');
    }

});
