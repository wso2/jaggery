package org.jaggeryjs.scriptengine.cache;

import org.jaggeryjs.scriptengine.security.RhinoSecurityDomain;

public class ScriptCachingContext {
    private String tenantDomain = null;
    private String context = null;
    private String path = null;
    private String cacheKey = null;
    private RhinoSecurityDomain securityDomain = null;
    private volatile long sourceModifiedTime = 0L;
    private static final String TENANT_DOMAIN = "tenantDomain";
    private static final String DEFAULT_TENANT_DOMAIN = "carbon.super";

    public ScriptCachingContext(String tenantDomain, String context, String path, String cacheKey) {
        if (tenantDomain == null) {
            tenantDomain = System.getProperty(TENANT_DOMAIN);
            if (tenantDomain == null) {
                tenantDomain = DEFAULT_TENANT_DOMAIN;
            }
        }
        this.tenantDomain = tenantDomain;
        this.context = context;
        this.path = path;
        this.cacheKey = cacheKey;
    }

    public String getTenantDomain() {
        return tenantDomain;
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
