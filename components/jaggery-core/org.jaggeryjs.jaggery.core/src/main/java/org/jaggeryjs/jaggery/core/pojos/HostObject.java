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
package org.jaggeryjs.jaggery.core.pojos;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO class to represent Hostobject in module.xml
 */
public class HostObject {
    private String className;
    private String name;
    private String readOnly;

    public HostObject(){}

    public HostObject(String className, String name){
        super();
        this.className = className;
        this.name = name;
        this.readOnly = readOnly;
    }

    @XmlAttribute (name = "readOnly")
    public void setReadOnly(String readOnly){
        this.readOnly = readOnly;
    }

    public  String getReadOnly(){
        return readOnly;
    }

    @javax.xml.bind.annotation.XmlElement (name = "className")
    public void setClassName(String className){
        this.className = className;
    }

    public  String getClassName(){
        return className;
    }

    @javax.xml.bind.annotation.XmlElement (name = "name")
    public void setName(String name){
        this.name = name;
    }

    public  String getName(){
        return name;
    }
}
