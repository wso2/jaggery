package org.jaggeryjs.scriptengine.cache;

import java.util.HashMap;
import java.util.Map;

public class PackageWrapper {

    private long classIndex = 0L;
    private Map<String, CachingContext> cachingContexts = new HashMap<String, CachingContext>();

    public PackageWrapper() {
    }

    public long getClassIndex() {
        return classIndex;
    }

    public void setClassIndex(long classIndex) {
        this.classIndex = classIndex;
    }

    public int getCachingContextCount() {
        return cachingContexts.size();
    }

    public Map<String, CachingContext> getCachingContexts() {
        return cachingContexts;
    }

    public CachingContext getCachingContext(String cacheKey) {
        return cachingContexts.get(cacheKey);
    }

    public void removeCachingContext(String cacheKey) {
        cachingContexts.remove(cacheKey);
    }

    public void setCachingContexts(String cacheKey, CachingContext cachingContext) {
        cachingContexts.put(cacheKey, cachingContext);
    }
}
