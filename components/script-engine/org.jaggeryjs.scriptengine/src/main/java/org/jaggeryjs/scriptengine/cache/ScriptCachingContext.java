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

    public String getFilteredPath() {
       return CacheManager.getPackage(context, path);
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
