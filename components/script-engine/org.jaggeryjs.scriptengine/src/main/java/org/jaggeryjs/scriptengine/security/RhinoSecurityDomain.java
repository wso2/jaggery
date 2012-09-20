package org.jaggeryjs.scriptengine.security;

import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import java.security.CodeSource;

public interface RhinoSecurityDomain {

    public CodeSource getCodeSource() throws ScriptException;

}
