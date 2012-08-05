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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.api.Collection;

/**
 * <p/>
 * This is a JavaScript Rhino host object aimed to provide a set of registry
 * specific utility functions to the javascript service developers.
 * </p>
 */
public class RegistryHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(RegistryHostObject.class);

    private static final String hostObjectName = "MetadataStore";

    private Registry registry = null;

    public RegistryHostObject() {
        super();
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        int argsCount = args.length;
        if (args.length != 2 && (args[0] instanceof String) && (args[1] instanceof String)) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }

        RegistryHostObject rho = new RegistryHostObject();
        rho.registry = getRegistry((String) args[0], (String) args[1]);
        return rho;
    }

    /**
     * Type to be used for this object inside the javascript.
     */
    public String getClassName() {
        return hostObjectName;
    }

    public static void jsFunction_remove(Context cx, Scriptable thisObj, Object[] arguments,
                                         Function funObj) throws ScriptException {
        RegistryHostObject rho = (RegistryHostObject) thisObj;
        if (arguments.length == 1) {
            if (arguments[0] instanceof String) {
                try {
                    rho.registry.delete((String) arguments[0]);
                } catch (RegistryException e) {
                    throw new ScriptException("Registry error occurred while executing delete() operation", e);
                }
            } else {
                throw new ScriptException("Path argument of method delete() should be a string");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for delete() method");
        }
    }

    public static Scriptable jsFunction_get(Context cx, Scriptable thisObj, Object[] arguments,
                                            Function funObj) throws ScriptException {
        RegistryHostObject rho = (RegistryHostObject) thisObj;
        if (arguments.length == 1) {
            if (arguments[0] instanceof String) {
                try {
                    Scriptable hostObject;
                    Resource resource = rho.registry.get((String) arguments[0]);
                    if (resource instanceof Collection) {
                        hostObject = cx.newObject(rho, "Collection", new Object[]{resource});
                    } else {
                        hostObject = cx.newObject(rho, "Resource", new Object[]{resource});
                    }
                    return hostObject;
                } catch (RegistryException e) {
                    throw new ScriptException("Registry error occurred while executing get() operation", e);
                }
            } else {
                throw new ScriptException("Path argument of method get() should be a string");
            }
        } else if (arguments.length == 3) {
            if (arguments[0] instanceof String && arguments[1] instanceof Number && arguments[2] instanceof Number) {
                try {
                    Collection collection = rho.registry.get((String) arguments[0],
                            ((Number) arguments[1]).intValue(), ((Number) arguments[2]).intValue());
                    CollectionHostObject cho = (CollectionHostObject) cx.newObject(
                            rho, "Collection", new Object[]{collection});
                    return cho;
                } catch (RegistryException e) {
                    throw new ScriptException("Registry error occurred while executing get() operation", e);
                }

            } else {
                throw new ScriptException("Invalid argument types for get() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for get() method");
        }
    }

    public static String jsFunction_put(Context cx, Scriptable thisObj, Object[] arguments,
                                        Function funObj) throws ScriptException {
        RegistryHostObject rho = (RegistryHostObject) thisObj;
        if (arguments.length == 2) {
            if (arguments[0] instanceof String && arguments[1] instanceof Scriptable) {
                ResourceHostObject reho = (ResourceHostObject) arguments[1];
                try {
                    return rho.registry.put((String) arguments[0], reho.getResource());
                } catch (RegistryException e) {
                    throw new ScriptException("Registry error occurred while executing get() operation", e);
                }
            } else {
                throw new ScriptException("Invalid argument types for put() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments for put() method");
        }
    }

    public static Scriptable jsFunction_newCollection(Context cx, Scriptable thisObj,
                                                      Object[] arguments,
                                                      Function funObj) throws ScriptException {
        RegistryHostObject rho = (RegistryHostObject) thisObj;
        if (arguments.length == 0) {
            if (rho.registry != null) {
                try {
                    Collection collection = rho.registry.newCollection();
                    CollectionHostObject cho = (CollectionHostObject) cx.newObject(
                            rho, "Collection", new Object[]{collection});
                    return cho;
                } catch (RegistryException e) {
                    throw new ScriptException("Error occurred while creating a new Collection", e);
                }
            } else {
                throw new ScriptException("Registry has not initialized");
            }
        } else {
            throw new ScriptException("newCollection() Method doesn't accept arguments");
        }
    }

    public static Scriptable jsFunction_newResource(Context cx, Scriptable thisObj,
                                                    Object[] arguments,
                                                    Function funObj) throws ScriptException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 0) {
            if (registryHostObject.registry != null) {
                try {
                    Resource resource = registryHostObject.registry.newResource();
                    ResourceHostObject rho = (ResourceHostObject) cx.newObject(
                            registryHostObject, "Resource", new Object[]{resource});
                    return rho;
                } catch (RegistryException e) {
                    throw new ScriptException("Error occurred while creating a new Resource", e);
                }
            } else {
                throw new ScriptException("Registry has not initialized");
            }
        } else {
            throw new ScriptException("newResource() Method doesn't accept arguments");
        }
    }

    public static boolean jsFunction_resourceExists(Context cx, Scriptable thisObj,
                                                    Object[] arguments,
                                                    Function funObj) throws ScriptException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 1) {
            if (arguments[0] instanceof String) {
                try {
                    return registryHostObject.registry.resourceExists((String) arguments[0]);
                } catch (RegistryException e) {
                    throw new ScriptException("Error occurred while creating a new Resource", e);
                }
            } else {
                throw new ScriptException("Invalid argument types for resourceExists() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments");
        }
    }

    private static Registry getRegistry(String username, String password) throws ScriptException {
        Registry registry;
        RegistryService registryService = RegistryHostObjectContext.getRegistryService();
        try {
            registry = registryService.getGovernanceUserRegistry(username, password);
        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
        if (registry == null) {
            String msg = "User governance registry cannot be retrieved";
            throw new ScriptException(msg);
        }
        return registry;
    }
}
