package org.jaggeryjs.scriptengine.security;

import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;

public class RhinoURISecurityDomain implements RhinoSecurityDomain {

    private String scriptURI;
    private CodeSource codeSource = null;

    public RhinoURISecurityDomain(String scriptURI) {
        this.scriptURI = scriptURI;
    }

    public CodeSource getCodeSource() throws ScriptException {
        if(codeSource != null) {
            return codeSource;
        }
        try {
            URL url = new URI(scriptURI).toURL();
            codeSource = new CodeSource(url, (Certificate[])null);
            return codeSource;
        } catch (MalformedURLException e) {
            throw new ScriptException(e);
        } catch (URISyntaxException e) {
            throw new ScriptException(e);
        }
    }

    public String getScriptURI() {
        return scriptURI;
    }
}
