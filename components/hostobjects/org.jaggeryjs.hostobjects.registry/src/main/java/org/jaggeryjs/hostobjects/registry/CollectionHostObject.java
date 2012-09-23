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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.api.RegistryException;

/**
 * <p/>
 * This is a JavaScript Rhino host object aimed to provide a javascript mapping
 * for a registry Collection object to the javascript service developers.
 * </p>
 */
public class CollectionHostObject extends ResourceHostObject {

    protected CollectionHostObject(Resource resource, Context cx) {
        this.resource = resource;
        this.context = cx;
    }

    public CollectionHostObject() {
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        if (args.length == 1) {
            if (args[0] instanceof Collection && !(args[0] instanceof Scriptable)) {
                return new CollectionHostObject((Collection) args[0], cx);
            } else if (args[0] instanceof Scriptable) {
                throw new ScriptException("Collection object cannot be initialized directly, " +
                                          "use registry.newCollection() instead");
            } else {
                throw new ScriptException("Invalid argument type for Collection constructor");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for Collection constructor");
        }
    }

    /**
     * Type to be used for this object inside the javascript.
     */
    public String getClassName() {
        return "Collection";
    }

    public static void jsFunction_addProperty(Context cx, Scriptable thisObj, Object[] arguments,
                                              Function funObj) throws ScriptException {
        ResourceHostObject.jsFunction_addProperty(cx, thisObj, arguments, funObj);
    }

    public static String jsFunction_getProperty(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws ScriptException {
        return ResourceHostObject.jsFunction_getProperty(cx, thisObj, arguments, funObj);
    }

    public static NativeArray jsFunction_getPropertyValues(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws ScriptException {
        return ResourceHostObject.jsFunction_getPropertyValues(cx, thisObj, arguments, funObj);
    }

    public static NativeArray jsFunction_getProperties(Context cx, Scriptable thisObj,
                                                      Object[] arguments,
                                                      Function funObj) throws ScriptException {
        return ResourceHostObject.jsFunction_getProperties(cx, thisObj, arguments, funObj);
    }

    public static void jsFunction_editPropertyValue(Context cx, Scriptable thisObj, Object[] arguments,
                                               Function funObj) throws ScriptException {
        ResourceHostObject.jsFunction_editPropertyValue(cx, thisObj, arguments, funObj);
    }

    public static void jsFunction_removeProperty(Context cx, Scriptable thisObj, Object[] arguments,
                                                 Function funObj) throws ScriptException {
        ResourceHostObject.jsFunction_removeProperty(cx, thisObj, arguments, funObj);
    }

    public static void jsFunction_removePropertyValue(Context cx, Scriptable thisObj,
                                                      Object[] arguments,
                                                      Function funObj) throws ScriptException {
        ResourceHostObject.jsFunction_removePropertyValue(cx, thisObj, arguments, funObj);
    }

    public static void jsFunction_setProperty(Context cx, Scriptable thisObj, Object[] arguments,
                                              Function funObj) throws ScriptException {
        ResourceHostObject.jsFunction_setProperty(cx, thisObj, arguments, funObj);
    }

    public static NativeArray jsFunction_getChildren(Context cx, Scriptable thisObj,
                                                    Object[] arguments,
                                                    Function funObj) throws ScriptException {
        CollectionHostObject collectionHostObject = (CollectionHostObject) thisObj;
        if (arguments.length == 0) {
            try {
                return new NativeArray(((Collection)collectionHostObject.getResource()).getChildren());
            } catch (RegistryException e) {
                throw new ScriptException("Error occurred while creating a new Resource.", e);
            }
        } else if (arguments.length == 2) {
            if (arguments[0] instanceof Number && arguments[1] instanceof Number) {
                try {
                    return new NativeArray(((Collection)collectionHostObject.getResource()).getChildren(
                            ((Number) arguments[0]).intValue(),
                            ((Number) arguments[1]).intValue()));
                } catch (RegistryException e) {
                    throw new ScriptException("Error occurred while creating a new Resource.", e);
                }
            } else {
                throw new ScriptException("Invalid argument types for getChildren() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for getChildren() method");
        }
    }

    public int jsGet_childCount() throws ScriptException {
        try {
            return ((Collection)this.getResource()).getChildCount();
        } catch (RegistryException e) {
            throw new ScriptException("Error occurred while creating a new Resource.", e);
        }
    }

    @Override
    public String jsGet_author() {
        return super.jsGet_author();    
    }

    @Override
    public String jsGet_lastUpdatedUser() {
        return super.jsGet_lastUpdatedUser();    
    }

    @Override
    public Scriptable jsGet_createdTime() {
        return super.jsGet_createdTime();    
    }

    @Override
    public Scriptable jsGet_lastUpdatedTime() {
        return super.jsGet_lastUpdatedTime();    
    }

    @Override
    public String jsGet_id() {
        return super.jsGet_id();    
    }

    @Override
    public String jsGet_parentPath() {
        return super.jsGet_parentPath();    
    }

    @Override
    public String jsGet_path() {
        return super.jsGet_path();    
    }

    @Override
    public String jsGet_permanentPath() {
        return super.jsGet_permanentPath();    
    }

    @Override
    public int jsGet_state() {
        return super.jsGet_state();    
    }

    @Override
    public String jsGet_mediaType() {
        return super.jsGet_mediaType();    
    }

    @Override
    public void jsSet_mediaType(Object mediaType) throws ScriptException {
        super.jsSet_mediaType(mediaType);    
    }

    @Override
    public Object jsGet_content() throws ScriptException {
        return super.jsGet_content();    
    }

    @Override
    public void jsSet_content(Object content) throws ScriptException {
        super.jsSet_content(content);    
    }

    @Override
    public String jsGet_description() {
        return super.jsGet_description();    
    }

    @Override
    public void jsSet_description(Object description) throws ScriptException {
        super.jsSet_description(description);    
    }

    @Override
    protected Resource getResource() {
        return super.getResource();    
    }
}
