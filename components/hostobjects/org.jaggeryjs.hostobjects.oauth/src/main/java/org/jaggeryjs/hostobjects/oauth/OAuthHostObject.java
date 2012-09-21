package org.jaggeryjs.hostobjects.oauth;

import com.google.gson.Gson;
import org.mozilla.javascript.*;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.*;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;

import java.util.Arrays;

public class OAuthHostObject extends ScriptableObject {

    private static final String hostObjectName = "OAuthProvider";

    private String apiKey;
    private String apiSecret;
    private String protectedResource;
    private OAuthService oauthService;
    private Token requestToken;
    private Token accessToken;
    private OAuthRequest oauthRequest;
    private Verifier verifier;
    private Response response;


    @Override
    public String getClassName() {
        return hostObjectName;
    }


    /**
     * var provider = {
     * "oauth_version" : "1",
     * "authorization_url" : "https://www.linkedin.com/uas/oauth/authorize",
     * "access_token_url" : "https://api.linkedin.com/uas/oauth/accessToken",
     * "request_token_url" : "https://api.linkedin.com/uas/oauth/requestToken",
     * "api_key" : "key",
     * "api_secret" : "secret"
     * }
     * new OAuthProvider(provider);
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        OAuthHostObject oauthho = new OAuthHostObject();
        if (args.length == 1) {
            if (!(args[0] == Context.getUndefinedValue()) && args[0] instanceof NativeObject) {
                NativeObject config = (NativeObject) args[0];

                Gson gson = new Gson();
                ProviderConfig providerConfig = gson.fromJson(HostObjectUtil.serializeJSON(config), ProviderConfig.class);

                if (providerConfig.getApi_key() == null
                        || providerConfig.getApi_secret() == null
                        || providerConfig.getAccess_token_url() == null
                        || providerConfig.getAuthorization_url() == null
                        || providerConfig.getOAuth_version() == null
                        || providerConfig.getRequest_token_url() == null) {
                    throw new ScriptException("API configuration not specified");
                }

                oauthho.apiKey = providerConfig.getApi_key();
                oauthho.apiSecret = providerConfig.getApi_secret();

                if (providerConfig.getOAuth_version() == 1.0) {
                    GenericOAuth10aApi oauth10aApi = new GenericOAuth10aApi();
                    oauth10aApi.setAccessTokenEndpoint(providerConfig.getAccess_token_url());
                    oauth10aApi.setAuthorizationUrl(providerConfig.getAuthorization_url());
                    oauth10aApi.setRequestTokenEndpoint(providerConfig.getRequest_token_url());
                    oauthho.oauthService = new ServiceBuilder()
                            .provider(oauth10aApi)
                            .apiKey(oauthho.apiKey)
                            .apiSecret(oauthho.apiSecret)
                            .build();

                } else if (providerConfig.getOAuth_version() == 2.0) {
                    GenericOAuth20Api oauth20Api = new GenericOAuth20Api();
                    oauth20Api.setAccessTokenEP(providerConfig.getAccess_token_url());
                    oauth20Api.setAuthorizeUrl(providerConfig.getAuthorization_url());
                    oauthho.oauthService = new ServiceBuilder()
                            .provider(oauth20Api)
                            .apiKey(oauthho.apiKey)
                            .apiSecret(oauthho.apiSecret)
                            .build();
                }

            }
            return oauthho;
        } else {
            throw new ScriptException("API configuration not specified");
        }
    }

    /**
     * creates an authorization Token
     */
    public static String jsFunction_getAuthorizationUrl(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        OAuthHostObject oauthho = (OAuthHostObject) thisObj;
        oauthho.requestToken = oauthho.oauthService.getRequestToken();
        return oauthho.oauthService.getAuthorizationUrl(oauthho.requestToken);
    }

    /**
     * Send OAuth Request.
     * lik.sendOAuthRequest(accessToken, "GET", "URL");
     */
    public static Response jsFunction_sendOAuthRequest(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        OAuthHostObject oauthho = (OAuthHostObject) thisObj;
        Verb verb = Verb.GET;

        if (args.length >= 3) {
            if (!(args[0] == Context.getUndefinedValue()) && args[0] instanceof NativeJavaObject) {
                oauthho.accessToken = (Token) Context.jsToJava(args[0], Token.class);
            } else {
                throw new ScriptException("Invalid Access Token");
            }

            if (!(args[1] == Context.getUndefinedValue()) && args[1] instanceof String) {
                String inputVerb = (String) args[1];
                if ("GET".equals(inputVerb.toUpperCase())) {
                    verb = Verb.GET;
                } else if ("PUT".equals(inputVerb.toUpperCase())) {
                    verb = Verb.PUT;
                } else if ("POST".equals(inputVerb.toUpperCase())) {
                    verb = Verb.POST;
                } else if ("DELETE".equals(inputVerb.toUpperCase())) {
                    verb = Verb.DELETE;
                }
            } else {
                throw new ScriptException("Invalid Verb");
            }

            if (!(args[2] == Context.getUndefinedValue()) && args[2] instanceof String) {
                oauthho.protectedResource = (String) args[2];
            } else {
                throw new ScriptException("Invalid URL");
            }

            oauthho.oauthRequest = new OAuthRequest(verb, oauthho.protectedResource);

            if ((args.length == 4) && !(args[3] == Context.getUndefinedValue()) && args[3] instanceof Scriptable) {
                Scriptable queryJsonString = (Scriptable) args[3];
                String[] ids = Arrays.copyOf(queryJsonString.getIds(), queryJsonString.getIds().length, String[].class);
                for (String id : ids) {
                    String value = (String) ((Scriptable) args[3]).get(id, cx.initStandardObjects());
                    oauthho.oauthRequest.addQuerystringParameter(id, value);
                }
            }
            oauthho.oauthService.signRequest(oauthho.accessToken, oauthho.oauthRequest);
            oauthho.response = oauthho.oauthRequest.send();
            return oauthho.response;


        } else {
            throw new ScriptException("Required properties not provided, Request cannot be built");
        }

    }

    /**
     * Provides the authCode and creates a AccessToken
     */
    public static Token jsFunction_getAccessToken(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        OAuthHostObject oauthho = (OAuthHostObject) thisObj;
        if ((args.length == 1) && !(args[0] == Context.getUndefinedValue()) && args[0] instanceof String) {
            oauthho.verifier = new Verifier((String) args[0]);
            return oauthho.oauthService.getAccessToken(oauthho.requestToken, oauthho.verifier);
        } else {
            throw new ScriptException("Illegal argument for the verifier : Add the code given from Provider");
        }
    }

}
