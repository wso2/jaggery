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

import org.jaggeryjs.scriptengine.security.RhinoSecurityDomain;

public class ScriptCachingContext {
    private String tenantId = null;
    private String context = null;
    private String path = null;
    private String cacheKey = null;
    private RhinoSecurityDomain securityDomain = null;
    private volatile long sourceModifiedTime = 0L;

    public ScriptCachingContext(String tenantId, String context, String path, String cacheKey) {
        this.tenantId = tenantId;
        this.context = context;
        this.path = path;
        this.cacheKey = cacheKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getContext() {
        return context;
    }

    public String getPath() {
        return path;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public RhinoSecurityDomain getSecurityDomain() {
        return securityDomain;
    }

    public void setSecurityDomain(RhinoSecurityDomain securityDomain) {
        this.securityDomain = securityDomain;
    }

    public long getSourceModifiedTime() {
        return sourceModifiedTime;
    }

    public void setSourceModifiedTime(long sourceModifiedTime) {
        this.sourceModifiedTime = sourceModifiedTime;
    }

}
