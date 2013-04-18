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
        return packages.get(getPackage(context, path));
    }

    public void removePackage(String path) {
        packages.remove(getPackage(context, path));
    }

    public void removeCachingContext(String path, String cacheKey) {
        PackageWrapper packageWrapper = getPackage(path);
        if (packageWrapper == null) {
            return;
        }
        packageWrapper.removeCachingContext(cacheKey);
        if (packageWrapper.getCachingContextCount() == 0) {
            removePackage(path);
        }
    }

    public void addPackage(String path, PackageWrapper packageWrapper) {
        packages.put(getPackage(context, path), packageWrapper);
    }

    public void addCachingContext(String path, String cacheKey, CachingContext ctx) {
        PackageWrapper packageWrapper = getPackage(path);
        if (packageWrapper == null) {
            packageWrapper = new PackageWrapper();
            addPackage(path, packageWrapper);
        }
        packageWrapper.addCachingContext(cacheKey, ctx);
    }

    public static String getPackage(String context, String path) {
        path = normalizeForPackage(normalizePath(path));
        String pack = normalizeForPackage(normalizePath(context));
        if (!path.equals("")) {
            pack += path;
        }
        return pack;
    }

    private static String normalizeForPackage(String path) {
        path = path.replaceAll("[-\\(\\)\\s]", "_")
                .replace("/", ".")
                .replaceAll("(.)([0-9])", "$1_$2");
        return path;
    }

    private static String normalizePath(String path) {
        if (path.equals("") || path.equals("/")) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}
