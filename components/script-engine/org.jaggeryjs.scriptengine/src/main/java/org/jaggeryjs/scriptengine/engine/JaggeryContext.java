package org.jaggeryjs.scriptengine.engine;

import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;
import java.util.Map;

public class JaggeryContext {

    private String tenantId = null;
    private RhinoEngine engine = null;
    private ScriptableObject scope = null;

    private Map<String, Object> properties = new HashMap<String, Object>();

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
