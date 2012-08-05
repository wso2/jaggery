package org.jaggeryjs.scriptengine.engine;

import org.mozilla.javascript.ScriptableObject;

public class JavaScriptHostObject {

    private String name = null;
    private Class clazz = null;
    private int attribute = ScriptableObject.PERMANENT;

    public JavaScriptHostObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public int getAttribute() {
        return attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }
}
