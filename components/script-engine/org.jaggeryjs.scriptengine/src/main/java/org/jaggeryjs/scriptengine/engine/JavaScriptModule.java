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
