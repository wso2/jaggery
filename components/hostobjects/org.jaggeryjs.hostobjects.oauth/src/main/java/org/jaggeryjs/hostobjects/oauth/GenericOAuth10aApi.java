package org.jaggeryjs.hostobjects.oauth;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;


public class GenericOAuth10aApi extends DefaultApi10a {

    private String AUTHORIZE_URL;
    private String REQUEST_TOKEN_RESOURCE;
    private String ACCESS_TOKEN_RESOURCE;

    @Override
    public String getRequestTokenEndpoint() {
        return this.REQUEST_TOKEN_RESOURCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return this.ACCESS_TOKEN_RESOURCE;
    }

    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return this.AUTHORIZE_URL + "?oauth_token=" + requestToken.getToken();
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.AUTHORIZE_URL = authorizationUrl;
    }

    public void setRequestTokenEndpoint(String requestTokenEndpoint) {
        this.REQUEST_TOKEN_RESOURCE = requestTokenEndpoint;
    }

    public void setAccessTokenEndpoint(String accessTokenEndpoint) {
        this.ACCESS_TOKEN_RESOURCE = accessTokenEndpoint;
    }
}
