package org.jaggeryjs.jaggery.core.manager;

import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class JaggeryContext {
    private String tenantId = null;
    private RhinoEngine engine = null;
    private ScriptableObject scope = null;
    private OutputStream outputStream = null;
    private CommonManager manager = null;
    private Stack<String> includesCallstack = new Stack<String>();
    private Map<String, Boolean> includedScripts = new HashMap<String, Boolean>();

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public RhinoEngine getEngine() {
        return engine;
    }

    public void setEngine(RhinoEngine engine) {
        this.engine = engine;
    }

    public ScriptableObject getScope() {
        return scope;
    }

    public void setScope(ScriptableObject scope) {
        this.scope = scope;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public CommonManager getManager() {
        return manager;
    }

    public void setManager(CommonManager manager) {
        this.manager = manager;
    }

    public Stack<String> getIncludesCallstack() {
        return includesCallstack;
    }

    public Map<String, Boolean> getIncludedScripts() {
        return includedScripts;
    }
}
