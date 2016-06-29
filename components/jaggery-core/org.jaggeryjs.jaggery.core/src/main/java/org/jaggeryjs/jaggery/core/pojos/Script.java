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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

/**
 * POJO class to represent script in module.xml
 */
public class Script {
    private String name;
    private String path;

    private Script(){}

    public Script(String name, String path){
        super();
        this.name = name;
        this.path = path;
    }

    @XmlElement (name = "path")
    public void setPath(String path){
        this.path = path;
    }

    public  String getPath(){
        return path;
    }

    @XmlElement (name = "name")
    public void setName(String name){
        this.name = name;
    }

    public  String getName(){
        return name;
    }
}
