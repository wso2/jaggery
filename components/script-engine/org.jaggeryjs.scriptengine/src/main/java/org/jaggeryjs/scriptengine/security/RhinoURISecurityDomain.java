/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
