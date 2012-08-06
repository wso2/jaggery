package org.jaggeryjs.hostobjects.oauth;

public class ProviderConfig {

    private Float oauth_version;
    private String authorization_url;
    private String access_token_url;
    private String request_token_url;
    private String api_key;
    private String api_secret;

    public ProviderConfig() {
    }

    public Float getOAuth_version() {
        return oauth_version;
    }

    public void setOAuth_version(Float oauth_version) {
        this.oauth_version = oauth_version;
    }

    public String getAuthorization_url() {
        return authorization_url;
    }

    public void setAuthorization_url(String authorization_url) {
        this.authorization_url = authorization_url;
    }

    public String getAccess_token_url() {
        return access_token_url;
    }

    public void setAccess_token_url(String access_token_url) {
        this.access_token_url = access_token_url;
    }

    public String getRequest_token_url() {
        return request_token_url;
    }

    public void setRequest_token_url(String request_token_url) {
        this.request_token_url = request_token_url;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getApi_secret() {
        return api_secret;
    }

    public void setApi_secret(String api_secret) {
        this.api_secret = api_secret;
    }
}
