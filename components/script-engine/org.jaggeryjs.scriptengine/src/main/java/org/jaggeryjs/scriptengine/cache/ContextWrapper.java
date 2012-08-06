package org.jaggeryjs.scriptengine.cache;

import java.util.HashMap;
import java.util.Map;

public class ContextWrapper {
    private Map<String, PackageWrapper> packages = new HashMap<String, PackageWrapper>();

    private String context = null;


    public ContextWrapper(String context) {
        this.context = context;
    }

    public int getPathCount() {
        return packages.size();
    }

    public Map<String, PackageWrapper> getPackages() {
        return packages;
    }

    public PackageWrapper getPackage(String path) {
        return packages.get(CacheManager.getPackage(context, path));
    }

    public void removePackage(String path) {
        packages.remove(CacheManager.getPackage(context, path));
    }

    public void removeCachingContext(String path, String cacheKey) {
        PackageWrapper packageWrapper = getPackage(path);
        if(packageWrapper == null) {
            return;
        }
        packageWrapper.removeCachingContext(cacheKey);
        if(packageWrapper.getCachingContextCount() == 0) {
            removePackage(path);
        }
    }

    public void setPackage(String path, PackageWrapper packageWrapper) {
        packages.put(CacheManager.getPackage(context, path), packageWrapper);
    }

    public void setCachingContext(String path, String cacheKey, CachingContext ctx) {
        PackageWrapper packageWrapper = getPackage(path);
        if(packageWrapper == null) {
            packageWrapper = new PackageWrapper();
            setPackage(path, packageWrapper);
        }
        packageWrapper.setCachingContexts(cacheKey, ctx);
    }
}
