/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jaggeryjs.hostobjects.oauth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OAuthHostObjectTest {

    private static final Log log = LogFactory.getLog(OAuthHostObject.class);

    public void testOAuthProvider() {
/*        //These are some test keys taken from scribe OAuth library
        NativeObject o = new NativeObject();
        o.put("oauth_version", o, "1");
        o.put("authorization_url", o, "https://api.twitter.com/oauth/authorize");
        o.put("access_token_url", o, "https://api.twitter.com/oauth/access_token");
        o.put("request_token_url", o, "https://api.twitter.com/oauth/request_token");
        o.put("api_key", o, "d0CTc4Zg9pufCnMkteDc7w");
        o.put("api_secret", o, "z4FMZhP87U5QEwycggDe5JN6TDDh7xEyhnAcEpdWk");

        Object[] args = {o};

        try {
            Context cx = RhinoEngine.enterGlobalContext();
            OAuthHostObject oauth = (OAuthHostObject) OAuthHostObject.jsConstructor(cx, args, null, true);
            String authUrl = oauth.jsFunction_getAuthorizationUrl(cx, oauth, null, null);
            assert authUrl.length() != 0;
            log.info("Twitter Authorization URL Created : " + authUrl);
        } catch (ScriptException e) {
            log.error(e);
        }*/
    }
}
