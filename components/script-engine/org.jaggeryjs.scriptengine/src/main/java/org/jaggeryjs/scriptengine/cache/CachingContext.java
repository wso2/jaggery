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

package org.jaggeryjs.scriptengine.cache;

import org.mozilla.javascript.Script;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

public class CachingContext {
    private String tenantId = null;
    private String context = null;
    private String path = null;
    private String cacheKey = null;
    private long lastUsageTime = 0L;
    private long lastModificationTest = 0L;
    private long cacheUpdatedTime = 0L;
    private long sourceModifiedTime = 0L;
    private String className = null;
    private Script script = null;

    public CachingContext(String context, String path, String cacheKey) {
        this.context = context;
        this.path = path;
        this.cacheKey = cacheKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getSourceModifiedTime() {
        return sourceModifiedTime;
    }

    public void setSourceModifiedTime(long sourceModifiedTime) {
        this.sourceModifiedTime = sourceModifiedTime;
    }

    public long getLastUsageTime() {
        return lastUsageTime;
    }

    public void setLastUsageTime(long lastUsageTime) {
        this.lastUsageTime = lastUsageTime;
    }

    public long getLastModificationTest() {
        return lastModificationTest;
    }

    public void setLastModificationTest(long lastModificationTest) {
        this.lastModificationTest = lastModificationTest;
    }

    public long getCacheUpdatedTime() {
        return cacheUpdatedTime;
    }

    public void setCacheUpdatedTime(long cacheUpdatedTime) {
        this.cacheUpdatedTime = cacheUpdatedTime;
    }

    public Script getScript() throws ScriptException {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
