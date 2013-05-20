/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jaggeryjs.hostobjects.registry;

import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.*;
import org.mozilla.javascript.xml.XMLObject;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Resource;

import java.util.*;

/**
 * <p/>
 * This is a JavaScript Rhino host object aimed to provide a javascript mapping
 * for a registry Resource object to the javascript service developers.
 * </p>
 */
public class ResourceHostObject extends ScriptableObject {

    protected Resource resource;
    protected Context context;

    protected ResourceHostObject(Resource resource, Context cx) {
        this.resource = resource;
        this.context = cx;
    }

    public ResourceHostObject() {
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        if (args.length == 1) {
            if (args[0] instanceof Resource && !(args[0] instanceof Scriptable)) {
                return new ResourceHostObject((Resource) args[0], cx);
            } else if (args[0] instanceof Scriptable) {
                throw new ScriptException("Resource object cannot be initialized directly, " +
                        "use registry.newResource() instead");
            } else {
                throw new ScriptException("Invalid argument type for Resource constructor");
            }
        } else {
            throw new ScriptException("Resource object cannot be initialized directly, use " +
                    "registry.newResource() instead");
        }
    }

    /**
     * Type to be used for this object inside the javascript.
     */
    public String getClassName() {
        return "Resource";
    }

    public static void jsFunction_addProperty(Context cx, Scriptable thisObj, Object[] arguments,
                                              Function funObj) throws ScriptException {
        ResourceHostObject resourceHostObject = (ResourceHostObject) thisObj;
        if (arguments.length == 2) {
            if (arguments[0] instanceof String && arguments[1] instanceof String) {
                resourceHostObject.resource.addProperty((String) arguments[0], (String) arguments[1]);
            } else {
                throw new ScriptException("Invalid argument types for addProperty() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for addProperty() method");
        }
    }

    public static String jsFunction_getProperty(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws ScriptException {
        ResourceHostObject resourceHostObject = (ResourceHostObject) thisObj;
        if (arguments.length == 1) {
            if (arguments[0] instanceof String) {
                return resourceHostObject.resource.getProperty((String) arguments[0]);
            } else {
                throw new ScriptException("Invalid argument type for getProperty() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for getProperty() method");
        }
    }

    public static Scriptable jsFunction_getPropertyValues(Context cx, Scriptable thisObj, Object[] arguments,
                                                          Function funObj) throws ScriptException {
        ResourceHostObject resourceHostObject = (ResourceHostObject) thisObj;
        if (arguments.length == 1) {
            if (arguments[0] instanceof String) {
                return cx.newArray(thisObj, resourceHostObject.resource.getPropertyValues((String) arguments[0]).toArray());
            } else {
                throw new ScriptException("Invalid argument type for getProperty() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for getProperty() method");
        }
    }

    public static Scriptable jsFunction_getProperties(Context cx, Scriptable thisObj,
                                                      Object[] arguments,
                                                      Function funObj) throws ScriptException {
        ResourceHostObject resourceHostObject = (ResourceHostObject) thisObj;
        if (arguments.length == 0) {
            List<ScriptableObject> props = new ArrayList<ScriptableObject>();
            Properties properties = resourceHostObject.resource.getProperties();
            Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                ScriptableObject property = (ScriptableObject) cx.newObject(thisObj);
                String key = (String) propertyNames.nextElement();
                property.put("name", property, key);
                property.put("value", property, properties.get(key));
                props.add(property);
            }
            return cx.newArray(thisObj, props.toArray());
        } else {
            throw new ScriptException("getProperties() method doesn't accept arguments");
        }
    }

    public static void jsFunction_editPropertyValue(Context cx, Scriptable thisObj, Object[] arguments,
                                                    Function funObj) throws ScriptException {
        ResourceHostObject resourceHostObject = (ResourceHostObject) thisObj;
        if (arguments.length == 3) {
            if (arguments[0] instanceof String && arguments[1] instanceof String &&
                    arguments[2] instanceof String) {

                resourceHostObject.resource.editPropertyValue((String) arguments[0], (String) arguments[1],
                        (String) arguments[2]);
            } else {
                throw new ScriptException("Invalid argument types for editProperty() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for editProperty() method");
        }
    }

    public static void jsFunction_removeProperty(Context cx, Scriptable thisObj, Object[] arguments,
                                                 Function funObj) throws ScriptException {
        ResourceHostObject resourceHostObject = (ResourceHostObject) thisObj;
        if (arguments.length == 1) {
            if (arguments[0] instanceof String) {
                resourceHostObject.resource.removeProperty((String) arguments[0]);
            } else {
                throw new ScriptException("Invalid argument type for removeProperty() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for removeProperty() method");
        }
    }

    public static void jsFunction_removePropertyValue(Context cx, Scriptable thisObj,
                                                      Object[] arguments,
                                                      Function funObj) throws ScriptException {
        ResourceHostObject resourceHostObject = (ResourceHostObject) thisObj;
        if (arguments.length == 2) {
            if (arguments[0] instanceof String && arguments[1] instanceof String) {
                resourceHostObject.resource.removePropertyValue((String) arguments[0], (String) arguments[1]);
            } else {
                throw new ScriptException("Invalid argument types for removePropertyValue() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for removePropertyValue() method");
        }
    }

    public static void jsFunction_setProperty(Context cx, Scriptable thisObj, Object[] arguments,
                                              Function funObj) throws ScriptException {
        ResourceHostObject resourceHostObject = (ResourceHostObject) thisObj;
        if (arguments.length == 2) {
            if (arguments[0] instanceof String && arguments[1] instanceof String) {
                resourceHostObject.resource.setProperty((String) arguments[0], (String) arguments[1]);
            } else if (arguments[0] instanceof String && arguments[1] instanceof NativeArray) {
                resourceHostObject.resource.setProperty((String) arguments[0], (List) Context.jsToJava(
                        arguments[1], List.class));
            } else {
                throw new ScriptException("Invalid argument types for setProperty() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for setProperty() method");
        }
    }

    public String jsGet_author() {
        return this.resource.getAuthorUserName();
    }

    public String jsGet_lastUpdatedUser() {
        return this.resource.getLastUpdaterUserName();
    }

    public Scriptable jsGet_createdTime() {
        return Context.toObject(this.resource.getCreatedTime(), this);
    }

    public Scriptable jsGet_lastUpdatedTime() {
        return Context.toObject(this.resource.getLastModified(), this);
    }

    public String jsGet_id() {
        return this.resource.getId();
    }

    public String jsGet_parentPath() {
        return this.resource.getParentPath();
    }

    public String jsGet_path() {
        return this.resource.getPath();
    }

    public String jsGet_permanentPath() {
        return this.resource.getPermanentPath();
    }

    public String jsGet_UUID() {
        return this.resource.getUUID();
    }

    public int jsGet_state() {
        return this.resource.getState();
    }

    public String jsGet_mediaType() {
        return this.resource.getMediaType();
    }

    public void jsSet_mediaType(Object mediaType) throws ScriptException {
        if (mediaType instanceof String) {
            this.resource.setMediaType((String) mediaType);
        } else {
            throw new ScriptException("Invalid property type for mediaType");
        }
    }

    public Object jsGet_content() throws ScriptException {
        try {
            Object result = this.resource.getContent();
            String mediaType = this.resource.getMediaType();
            if (result instanceof byte[]) {
                //if mediaType is xml related one, we return an e4x xml object
                if (mediaType != null) {
                    if (mediaType.matches(".*[\\/].*[xX][mM][lL].*")) {
                        return context.newObject(this, "XML", new Object[]{new String((byte[]) result)});
                    }
                }
                return new String((byte[]) result);
            } else if (result instanceof String[]) {
                String[] content = (String[]) result;
                return context.newArray(this, Arrays.copyOf(content, content.length, Object[].class));
            } else {
                return Context.toObject(result, this);
            }
        } catch (RegistryException e) {
            throw new ScriptException("Registry Exception while reading content property", e);
        }
    }

    public void jsSet_content(Object content) throws ScriptException {
        if (content instanceof String) {
            try {
                this.resource.setContent((String) content);
            } catch (RegistryException e) {
                throw new ScriptException("Registry Exception while setting content property", e);
            }
        } else if (content instanceof XMLObject) {
            try {
                this.resource.setContent(AXIOMUtil.stringToOM(content.toString()));
            } catch (Exception e) {
                throw new ScriptException(e);
            }
        } else {
            throw new ScriptException("Invalid property type for content");
        }
    }

    public String jsGet_description() {
        return this.resource.getDescription();
    }

    public void jsSet_description(Object description) throws ScriptException {
        if (description instanceof String) {
            this.resource.setDescription((String) description);
        } else {
            throw new ScriptException("Invalid property type for description");
        }
    }

    protected Resource getResource() {
        return this.resource;
    }
}
