/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jaggeryjs.jaggery.core;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * POJO class to represent the module in module.xml
 */
@XmlRootElement(name = "module", namespace = "http://wso2.org/projects/jaggery/module.xml") public class Module {
    List<Method> methods;
    List<HostObject> hostObjects;
    List<Script> scripts;
    String name;
    String namespace;
    String expose;
    String xmlns;

    public Module(){}

    public Module(String name, String namespace, String expose, String xmlns, List<Method> methods,
            List<HostObject> hostObjects, List<Script> scripts) {
        super();
        this.name = name;
        this.namespace = namespace;
        this.expose = expose;
        this.xmlns = xmlns;
        this.methods = methods;
        this.hostObjects = hostObjects;
        this.scripts = scripts;

    }

    @XmlAttribute(name = "name") public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "namespace") public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    @XmlAttribute(name = "expose") public void setExpose(String expose) {
        this.expose = expose;
    }

    public String getExpose() {
        return expose;
    }

    @XmlAttribute   (name = "xmlns") public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    public String getXmlns() {
        return xmlns;
    }

    @XmlElement(name = "method") public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public List<Method> getMethods() {
        return methods;
    }

    @XmlElement(name = "hostObject") public void setHostObjects(List<HostObject> hostObjects) {
        this.hostObjects = hostObjects;
    }

    public List<HostObject> getHostObjects() {
        return hostObjects;
    }

    @XmlElement(name = "script") public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }

    public List<Script> getScripts() {
        return scripts;
    }
}
