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
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;
import org.wso2.carbon.registry.api.*;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.ArrayList;
import java.util.List;

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
        if (args.length > 2 && args.length != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }

        RegistryHostObject rho = new RegistryHostObject();
        if (args.length == 2) {
            rho.registry = getRegistry((String) args[0], (String) args[1]);
        } else {
            rho.registry = getRegistry((String) args[0]);
        }
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

    public static void jsFunction_createLink(Context cx, Scriptable thisObj,
                                             Object[] arguments,
                                             Function funObj) throws ScriptException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 2) {
            if (arguments[0] instanceof String && arguments[1] instanceof String) {
                try {
                    registryHostObject.registry.createLink((String) arguments[0], (String) arguments[1]);
                } catch (RegistryException e) {
                    throw new ScriptException("Error occurred while creating a Link", e);
                }
            } else {
                throw new ScriptException("Invalid argument types for createLink() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments");
        }
    }

    public static void jsFunction_addRating(Context cx, Scriptable thisObj,
                                            Object[] args,
                                            Function funObj) throws ScriptException {
        String functionName = "addRating";
        int argsCount = args.length;
        if (argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        if (!(args[1] instanceof Number)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "number", args[1], false);
        }
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        try {
            registryHostObject.registry.rateResource((String) args[0], ((Number) args[1]).intValue());
        } catch (RegistryException e) {
            throw new ScriptException(e);
        }
    }

    public static void jsFunction_addComment(Context cx, Scriptable thisObj,
                                             Object[] args,
                                             Function funObj) throws ScriptException {
        String functionName = "addComment";
        int argsCount = args.length;
        if (argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        if (!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "string", args[1], false);
        }
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        try {
            registryHostObject.registry.addComment((String) args[0],
                    new org.wso2.carbon.registry.core.Comment((String) args[1]));
        } catch (RegistryException e) {
            throw new ScriptException(e);
        }
    }


    public static Number jsFunction_getRating(Context cx, Scriptable thisObj,
                                              Object[] args,
                                              Function funObj) throws ScriptException {
        String functionName = "getRating";
        int argsCount = args.length;
        if (argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        if (!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "string", args[1], false);
        }
        try {
            return registryHostObject.registry.getRating((String) args[0], (String) args[1]);
        } catch (RegistryException e) {
            throw new ScriptException(e);
        }
    }

    public static Number jsFunction_getAvgRating(Context cx, Scriptable thisObj,
                                                 Object[] args,
                                                 Function funObj) throws ScriptException {
        String functionName = "getAvgRating";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        try {
            return registryHostObject.registry.getAverageRating((String) args[0]);
        } catch (RegistryException e) {
            throw new ScriptException(e);
        }
    }


    public static NativeArray jsFunction_getComments(Context cx, Scriptable thisObj,
                                                     Object[] args,
                                                     Function funObj) throws ScriptException {
        String functionName = "getComments";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        try {
            List<NativeObject> commentsArray = new ArrayList<NativeObject>();
            RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
            Comment[] comments = registryHostObject.registry.getComments((String) args[0]);
            for (Comment comment : comments) {
                NativeObject commentObj = new NativeObject();
                commentObj.put("cid", commentObj, comment.getCommentID());
                commentObj.put("author", commentObj, comment.getUser());
                commentObj.put("content", commentObj, comment.getText());
                commentObj.put("created", commentObj, comment.getCreatedTime().getTime());
                commentsArray.add(commentObj);
            }
            return new NativeArray(commentsArray.toArray());
        } catch (RegistryException e) {
            throw new ScriptException(e);
        }
    }

    public static void jsFunction_copy(Context cx, Scriptable thisObj,
                                       Object[] arguments,
                                       Function funObj) throws ScriptException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 2) {
            if (arguments[0] instanceof String && arguments[1] instanceof String) {
                try {
                    registryHostObject.registry.copy((String) arguments[0], (String) arguments[1]);
                } catch (RegistryException e) {
                    throw new ScriptException("Error occurred while coping the resource", e);
                }
            } else {
                throw new ScriptException("Invalid argument types for copy() method");
            }
        } else {
            throw new ScriptException("Invalid no. of arguments");
        }
    }

    private static Registry getRegistry(String username, String password) throws ScriptException {
        Registry registry;
        RegistryService registryService = RegistryHostObjectContext.getRegistryService();

        String tDomain;
        try {
            tDomain = username.split("@")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            //if tenant domain is not present in the username we treat is as a super user
            tDomain = "carbon.super";
        }

        try {
            int tId = RegistryHostObjectContext.getRealmService().getTenantManager().getTenantId(tDomain);
            registry = registryService.getGovernanceUserRegistry(username, password, tId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
        if (registry == null) {
            String msg = "User governance registry cannot be retrieved";
            throw new ScriptException(msg);
        }
        return registry;
    }

    private static Registry getRegistry(String username) throws ScriptException {
        Registry registry;
        RegistryService registryService = RegistryHostObjectContext.getRegistryService();

        String tDomain;
        try {
            tDomain = username.split("@")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            //if tenant domain is not present in the username we treat is as a super user
            tDomain = "carbon.super";
        }

        try {
            int tId = RegistryHostObjectContext.getRealmService().getTenantManager().getTenantId(tDomain);
            registry = registryService.getGovernanceUserRegistry(username, tId);
        } catch (Exception e) {
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
