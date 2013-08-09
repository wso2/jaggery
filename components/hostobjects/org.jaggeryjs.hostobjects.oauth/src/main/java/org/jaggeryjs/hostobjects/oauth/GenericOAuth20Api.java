package org.jaggeryjs.hostobjects.oauth;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.utils.Preconditions;

import org.scribe.utils.*;


public class GenericOAuth20Api extends DefaultApi20 {
    private String AUTHORIZE_URL;
    private String ACCESS_TOKEN_EP;
    private String CALLBACK_URL;

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
    
    public String getCallBackUrl() {
		return CALLBACK_URL;
	}

	public void setCallBackUrl(String cALLBACK_URL) {
		CALLBACK_URL = cALLBACK_URL;
	}

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        Preconditions.checkValidUrl(getCallBackUrl(), "Must provide a valid url as callback.");

        // Append scope if present
        if (config.hasScope()) {
            return AUTHORIZE_URL
                    + "?client_id=" + config.getApiKey()
                    + "&response_type=code"
                    + "&redirect_uri=" + OAuthEncoder.encode(getCallBackUrl())
                    + "&scope=" + OAuthEncoder.encode(config.getScope());
        } else {
            return AUTHORIZE_URL
                    + "?client_id=" + config.getApiKey()
                    + "&response_type=code"
                    + "&redirect_uri=" + OAuthEncoder.encode(getCallBackUrl());
        }
    }
}
