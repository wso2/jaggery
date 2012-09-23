package org.jaggeryjs.jaggery.core.manager;

import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.security.RhinoSecurityDomain;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;

public class JaggerySecurityDomain implements RhinoSecurityDomain {

    private String scriptPath;
    private CodeSource codeSource;
    private ServletContext servletContext;

    public JaggerySecurityDomain(String scriptPath, ServletContext servletContext) {
        this.scriptPath = scriptPath;
        this.servletContext = servletContext;
    }

    public CodeSource getCodeSource() throws ScriptException {
        if (codeSource != null) {
            return codeSource;
        }
        try {
            String contextPath = servletContext.getRealPath("/");
            if (!contextPath.endsWith(File.separator)) {
                contextPath += File.separator;
            }
            URL url = new File(contextPath + scriptPath).getCanonicalFile().toURI().toURL();
            codeSource = new CodeSource(url, (Certificate[]) null);
            return codeSource;
        } catch (MalformedURLException e) {
            throw new ScriptException(e);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}
