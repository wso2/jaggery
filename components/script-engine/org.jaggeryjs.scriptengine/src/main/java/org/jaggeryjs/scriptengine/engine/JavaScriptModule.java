package org.jaggeryjs.scriptengine.engine;

import java.util.ArrayList;
import java.util.List;

public class JavaScriptModule {

    private String name = null;
    private String namespace = null;
    private boolean expose = false;

    private final List<JavaScriptHostObject> hostObjects = new ArrayList<JavaScriptHostObject>();
    private final List<JavaScriptMethod> methods = new ArrayList<JavaScriptMethod>();
    private final List<JavaScriptProperty> properties = new ArrayList<JavaScriptProperty>();
    private final List<JavaScriptScript> scripts = new ArrayList<JavaScriptScript>();

    public JavaScriptModule(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isExpose() {
        return expose;
    }

    public void setExpose(boolean expose) {
        this.expose = expose;
    }

    public void addHostObject(JavaScriptHostObject hostObject) {
        this.hostObjects.add(hostObject);
    }

    public void addMethod(JavaScriptMethod method) {
        this.methods.add(method);
    }

    public void addProperty(JavaScriptProperty property) {
        this.properties.add(property);
    }

    public void addScript(JavaScriptScript script) {
        this.scripts.add(script);
    }

    public List<JavaScriptHostObject> getHostObjects() {
        return hostObjects;
    }

    public List<JavaScriptMethod> getMethods() {
        return methods;
    }

    public List<JavaScriptProperty> getProperties() {
        return properties;
    }

    public List<JavaScriptScript> getScripts() {
        return scripts;
    }
}
