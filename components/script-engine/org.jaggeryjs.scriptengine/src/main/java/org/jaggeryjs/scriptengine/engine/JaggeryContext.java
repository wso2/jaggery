package org.jaggeryjs.scriptengine.engine;

import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;
import java.util.Map;

public class JaggeryContext {

    private String tenantDomain = null;
    private RhinoEngine engine = null;
    private ScriptableObject scope = null;

    private Map<String, Object> properties = new HashMap<String, Object>();

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
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

    public void addProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

}
