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
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.TagCount;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.registry.search.beans.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.services.utils.AdvancedSearchResultsBeanPopulator;
import org.wso2.carbon.registry.search.services.utils.SearchUtils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p/>
 * This is a JavaScript Rhino host object aimed to provide a set of registry
 * specific utility functions to the javascript service developers.
 * </p>
 */
public class RegistryHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(RegistryHostObject.class);

    private static final String hostObjectName = "MetadataStore";

    private UserRegistry registry = null;

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

    public static Scriptable jsFunction_getComments(Context cx, Scriptable thisObj,
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
            List<ScriptableObject> commentsArray = new ArrayList<ScriptableObject>();
            RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
            Comment[] comments = registryHostObject.registry.getComments((String) args[0]);
            for (Comment comment : comments) {
                ScriptableObject commentObj = (ScriptableObject) cx.newObject(thisObj);
                commentObj.put("cid", commentObj, comment.getCommentID());
                commentObj.put("author", commentObj, comment.getUser());
                commentObj.put("content", commentObj, comment.getText());
                commentObj.put("created", commentObj, comment.getCreatedTime().getTime());
                commentsArray.add(commentObj);
            }
            return cx.newArray(thisObj, commentsArray.toArray());
        } catch (RegistryException e) {
            throw new ScriptException(e);
        }
    }

    public static Scriptable jsFunction_search(Context cx, Scriptable thisObj,
                                               Object[] args,
                                               Function funObj) throws ScriptException {
        String functionName = "search";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof NativeObject)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "json", args[0], false);
        }
        NativeObject options = (NativeObject) args[0];
        CustomSearchParameterBean parameters = new CustomSearchParameterBean();
        String path = null;
        List<String[]> values = new ArrayList<String[]>();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String val;
        for (Object idObj : options.getIds()) {
            String id = (String) idObj;
            Object value = options.get(id, options);
            if (value == null || value instanceof Undefined) {
                continue;
            }
            if ("path".equals(id)) {
                path = (String) value;
                continue;
            }
            if ("createdBefore".equals(id) || "createdAfter".equals(id) ||
                    "updatedBefore".equals(id) || "updatedAfter".equals(id)) {
                long t;
                if (value instanceof Number) {
                    t = ((Number) value).longValue();
                } else {
                    t = Long.parseLong(HostObjectUtil.serializeObject(value));
                }
                val = new String(dateFormat.format(new Date(t)).getBytes());
            } else {
                val = HostObjectUtil.serializeObject(value);
            }
            values.add(new String[]{id, val});
        }
        parameters.setParameterValues(values.toArray(new String[0][0]));
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            UserRegistry userRegistry = RegistryHostObjectContext.getRegistryService().getRegistry(
                    registryHostObject.registry.getUserName(), tenantId);
            Registry configRegistry = RegistryHostObjectContext.getRegistryService().getConfigSystemRegistry(tenantId);
            AdvancedSearchResultsBean resultsBean = registryHostObject.search(configRegistry, userRegistry, parameters);
            if (resultsBean.getResourceDataList() == null) {
                ScriptableObject error = (ScriptableObject) cx.newObject(thisObj);
                error.put("error", error, true);
                error.put("description", error, resultsBean.getErrorMessage());
                return error;
            }
            List<ScriptableObject> results = new ArrayList<ScriptableObject>();
            for (ResourceData resourceData : resultsBean.getResourceDataList()) {
                String resourcePath = resourceData.getResourcePath();
                if (path != null && !resourcePath.startsWith(path)) {
                    continue;
                }
                ScriptableObject result = (ScriptableObject) cx.newObject(thisObj);
                result.put("author", result, resourceData.getAuthorUserName());
                result.put("rating", result, resourceData.getAverageRating());
                result.put("created", result, resourceData.getCreatedOn().getTime().getTime());
                result.put("description", result, resourceData.getDescription());
                result.put("name", result, resourceData.getName());
                result.put("path", result, resourceData.getResourcePath());
                List<ScriptableObject> tags = new ArrayList<ScriptableObject>();
                if (resourceData.getTagCounts() != null) {
                    for (TagCount tagCount : resourceData.getTagCounts()) {
                        ScriptableObject tag = (ScriptableObject) cx.newObject(thisObj);
                        tag.put("name", tag, tagCount.getKey());
                        tag.put("count", tag, tagCount.getValue());
                        tags.add(tag);
                    }
                }
                result.put("tags", result, cx.newArray(thisObj, tags.toArray()));
                results.add(result);
            }
            return cx.newArray(thisObj, results.toArray());
        } catch (RegistryException e) {
            throw new ScriptException(e);
        } catch (CarbonException e) {
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

    private AdvancedSearchResultsBean search(Registry configSystemRegistry, UserRegistry registry,
                                             CustomSearchParameterBean parameters) throws CarbonException {
        RegistryUtils.recordStatistics(parameters);
        AdvancedSearchResultsBean metaDataSearchResultsBean;
        ResourceData[] contentSearchResourceData;
        String[][] tempParameterValues = parameters.getParameterValues();

        //        Doing a validation of all the values sent
        boolean allEmpty = true;
        for (String[] tempParameterValue : tempParameterValues) {
            if (tempParameterValue[1] != null & tempParameterValue[1].trim().length() > 0) {
                allEmpty = false;
                //                Validating all the dates
                if (tempParameterValue[0].equals("createdAfter") || tempParameterValue[0].equals("createdBefore") ||
                        tempParameterValue[0].equals("updatedAfter") || tempParameterValue[0].equals("updatedBefore")) {
                    if (!SearchUtils.validateDateInput(tempParameterValue[1])) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                } else if (tempParameterValue[0].equals("mediaType")) {
                    if (SearchUtils.validateMediaTypeInput(tempParameterValue[1])) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                } else if (tempParameterValue[0].equals("content")) {
                    if (SearchUtils.validateContentInput(tempParameterValue[1])) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                } else if (tempParameterValue[0].equals("tags")) {
                    boolean containsTag = false;
                    for (String str : tempParameterValue[1].split(",")) {
                        if (str.trim().length() > 0) {
                            containsTag = true;
                            break;
                        }
                    }
                    if (!containsTag) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                    if (SearchUtils.validateTagsInput(tempParameterValue[1])) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                } else {
                    if (SearchUtils.validatePathInput(tempParameterValue[1])) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                }
            }
        }

        if (allEmpty) {
            return SearchUtils.getEmptyResultBeanWithErrorMsg("At least one field must be filled");
        }

        boolean onlyContent = true;
        for (String[] tempParameterValue : tempParameterValues) {
            if (!tempParameterValue[0].equals("content") && !tempParameterValue[0].equals("leftOp") &&
                    !tempParameterValue[0].equals("rightOp") && tempParameterValue[1] != null &&
                    tempParameterValue[1].length() > 0) {
                onlyContent = false;
                break;
            }
        }

        for (String[] tempParameterValue : tempParameterValues) {
            if (tempParameterValue[0].equals("content") && tempParameterValue[1] != null &&
                    tempParameterValue[1].length() > 0) {
                try {
                    contentSearchResourceData = search(registry, tempParameterValue[1]);
                } catch (Exception e) {
                    metaDataSearchResultsBean = new AdvancedSearchResultsBean();
                    metaDataSearchResultsBean.setErrorMessage(e.getMessage());
                    return metaDataSearchResultsBean;
                }

                //                If there are no resource paths returned from content, then there is no point of searching for more
                if (contentSearchResourceData != null && contentSearchResourceData.length > 0) {
                    //                    Map<String, ResourceData> resourceDataMap = new HashMap<String, ResourceData>();
                    Map<String, ResourceData> aggregatedMap = new HashMap<String, ResourceData>();

                    for (ResourceData resourceData : contentSearchResourceData) {
                        aggregatedMap.put(resourceData.getResourcePath(), resourceData);
                    }

                    metaDataSearchResultsBean = AdvancedSearchResultsBeanPopulator.populate(configSystemRegistry,
                            registry, parameters);

                    if (metaDataSearchResultsBean != null) {
                        ResourceData[] metaDataResourceData = metaDataSearchResultsBean.getResourceDataList();
                        if (metaDataResourceData != null && metaDataResourceData.length > 0) {

                            List<String> invalidKeys = new ArrayList<String>();
                            for (String key : aggregatedMap.keySet()) {
                                boolean keyFound = false;
                                for (ResourceData resourceData : metaDataResourceData) {
                                    if (resourceData.getResourcePath().equals(key)) {
                                        keyFound = true;
                                        break;
                                    }
                                }
                                if (!keyFound) {
                                    invalidKeys.add(key);
                                }
                            }
                            for (String invalidKey : invalidKeys) {
                                aggregatedMap.remove(invalidKey);
                            }
                        } else if (!onlyContent) {
                            aggregatedMap.clear();
                        }
                    }

                    ArrayList<ResourceData> sortedList = new ArrayList<ResourceData>(aggregatedMap.values());
                    SearchUtils.sortResourceDataList(sortedList);

                    metaDataSearchResultsBean = new AdvancedSearchResultsBean();
                    metaDataSearchResultsBean.setResourceDataList(sortedList.toArray(new ResourceData[sortedList.size()]));
                    return metaDataSearchResultsBean;
                } else {
                    metaDataSearchResultsBean = new AdvancedSearchResultsBean();
                    metaDataSearchResultsBean.setResourceDataList(contentSearchResourceData);
                    return metaDataSearchResultsBean;
                }
            }
        }
        return AdvancedSearchResultsBeanPopulator.populate(configSystemRegistry, registry, parameters);
    }

    private ResourceData[] search(UserRegistry registry, String searchQuery) throws IndexerException, RegistryException {
        SolrClient client = SolrClient.getInstance();
        SolrDocumentList results = client.query(searchQuery, registry.getTenantId());

        if (log.isDebugEnabled()) log.debug("result received " + results);

        List<ResourceData> filteredResults = new ArrayList<ResourceData>();
        for (int i = 0; i < results.getNumFound(); i++) {
            SolrDocument solrDocument = results.get(i);
            String path = getPathFromId((String) solrDocument.getFirstValue("id"));
            //if (AuthorizationUtils.authorize(path, ActionConstants.GET)){
            if ((registry.resourceExists(path)) && (isAuthorized(registry, path, ActionConstants.GET))) {
                filteredResults.add(loadResourceByPath(registry, path));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("filtered results " + filteredResults + " for user " + registry.getUserName());
        }
        return filteredResults.toArray(new ResourceData[0]);
    }

    private String getPathFromId(String id) {
        return id.substring(0, id.lastIndexOf("tenantId"));
    }

    private ResourceData loadResourceByPath(UserRegistry registry, String path) throws RegistryException {
        ResourceData resourceData = new ResourceData();
        resourceData.setResourcePath(path);

        if (path != null) {
            if (RegistryConstants.ROOT_PATH.equals(path)) {
                resourceData.setName("root");
            } else {
                String[] parts = path.split(RegistryConstants.PATH_SEPARATOR);
                resourceData.setName(parts[parts.length - 1]);
            }
        }

        Resource child = registry.get(path);

        resourceData.setResourceType(child instanceof Collection ? "collection"
                : "resource");
        resourceData.setAuthorUserName(child.getAuthorUserName());
        resourceData.setDescription(child.getDescription());
        resourceData.setAverageRating(registry
                .getAverageRating(child.getPath()));
        Calendar createdDateTime = Calendar.getInstance();
        createdDateTime.setTime(child.getCreatedTime());
        resourceData.setCreatedOn(createdDateTime);
        CommonUtil.populateAverageStars(resourceData);

        child.discard();

        return resourceData;
    }

    private static UserRegistry getRegistry(String username, String password) throws ScriptException {
        UserRegistry registry;
        RegistryService registryService = RegistryHostObjectContext.getRegistryService();

        String tDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(false);

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

    private static UserRegistry getRegistry(String username) throws ScriptException {
        UserRegistry registry;
        RegistryService registryService = RegistryHostObjectContext.getRegistryService();

        String tDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(false);

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

    private boolean isAuthorized(UserRegistry registry, String resourcePath, String action) throws RegistryException {
        UserRealm userRealm = registry.getUserRealm();
        String userName = registry.getUserName();

        try {
            if (!userRealm.getAuthorizationManager().isUserAuthorized(userName,
                    resourcePath, action)) {
                return false;
            }
        } catch (UserStoreException e) {
            throw new org.wso2.carbon.registry.core.exceptions.RegistryException("Error at Authorizing " + resourcePath
                    + " with user " + userName + ":" + e.getMessage(), e);
        }

        return true;
    }
}
