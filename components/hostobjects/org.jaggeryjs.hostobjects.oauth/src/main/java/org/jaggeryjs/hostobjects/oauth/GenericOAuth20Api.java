package org.jaggeryjs.hostobjects.oauth;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.utils.Preconditions;

import org.scribe.utils.*;


public class GenericOAuth20Api extends DefaultApi20 {
    private String AUTHORIZE_URL;
    private String ACCESS_TOKEN_EP;

    public void setAuthorizeUrl(String authorizeUrl) {
        this.AUTHORIZE_URL = authorizeUrl;
    }

    public void setAccessTokenEP(String accessTokenEP) {
        this.ACCESS_TOKEN_EP = accessTokenEP;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return this.ACCESS_TOKEN_EP;
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback. Live does not support OOB");

        // Append scope if present
        if (config.hasScope()) {
            return AUTHORIZE_URL
                    + "?client_id=" + config.getApiKey()
                    + "&redirect_uri=" + OAuthEncoder.encode(config.getCallback())
                    + "&scope=" + OAuthEncoder.encode(config.getScope());
        } else {
            return AUTHORIZE_URL
                    + "?client_id=" + config.getApiKey()
                    + "&redirect_uri=" + OAuthEncoder.encode(config.getCallback());
        }
    }
}
