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


import java.util.HashMap;
import java.util.Map;

public class TenantWrapper {
    private Map<String, ContextWrapper> contexts = new HashMap<String, ContextWrapper>();

    public TenantWrapper() {
    }

    public Map<String, ContextWrapper> getContexts() {
        return contexts;
    }

    public ContextWrapper getContext(String context) {
        return contexts.get(context);
    }

    public ContextWrapper getContext(ScriptCachingContext sctx) {
        return getContext(sctx.getContext());
    }

    public PackageWrapper getPath(String context, String path) {
        ContextWrapper ctxWrapper = getContext(context);
        if (ctxWrapper == null) {
            return null;
        }
        return ctxWrapper.getPackage(path);
    }

    public PackageWrapper getPath(ScriptCachingContext sctx) {
        return getPath(sctx.getContext(), sctx.getPath());
    }

    public CachingContext getCachingContext(String context, String path, String cachingKey) {
        PackageWrapper packageWrapper = getPath(context, path);
        if (packageWrapper == null) {
            return null;
        }
        return packageWrapper.getCachingContext(cachingKey);
    }

    public CachingContext getCachingContext(ScriptCachingContext sctx) {
        return getCachingContext(sctx.getContext(), sctx.getPath(), sctx.getCacheKey());
    }

    public void removeContext(String context) {
        contexts.remove(context);
    }

    public void removePath(String context, String path) {
        ContextWrapper ctxWrapper = getContext(context);
        if (ctxWrapper == null) {
            return;
        }
        ctxWrapper.removePackage(path);
        if (ctxWrapper.getPathCount() == 0) {
            removeContext(context);
        }
    }

    public void removePath(ScriptCachingContext sctx) {
        removePath(sctx.getContext(), sctx.getPath());
    }

    public void removeCachingContext(String context, String path, String cachingKey) {
        ContextWrapper ctxWrapper = getContext(context);
        if (ctxWrapper == null) {
            return;
        }
        ctxWrapper.removeCachingContext(path, cachingKey);
        if (ctxWrapper.getPathCount() == 0) {
            removeContext(context);
        }
    }

    public void removeCachingContext(ScriptCachingContext sctx) {
        removeCachingContext(sctx.getContext(), sctx.getPath(), sctx.getCacheKey());
    }

    public void setContext(String context, ContextWrapper ctxWrapper) {
        contexts.put(context, ctxWrapper);
    }

    public void setContext(ScriptCachingContext sctx, ContextWrapper ctxWrapper) {
        setContext(sctx.getContext(), ctxWrapper);
    }

    public void setPath(String context, String path, PackageWrapper packageWrapper) {
        ContextWrapper ctxWrapper = getContext(context);
        if (ctxWrapper == null) {
            ctxWrapper = createContext(context);
        }
        ctxWrapper.addPackage(path, packageWrapper);
    }

    public void setPath(ScriptCachingContext sctx, PackageWrapper packageWrapper) {
        setPath(sctx.getContext(), sctx.getPath(), packageWrapper);
    }

    public void setCachingContext(String context, String path, String cacheKey, CachingContext ctx) {
        ContextWrapper ctxWrapper = getContext(context);
        if (ctxWrapper == null) {
            ctxWrapper = createContext(context);
        }
        ctxWrapper.addCachingContext(path, cacheKey, ctx);
    }

    public void setCachingContext(CachingContext ctx) {
        setCachingContext(ctx.getContext(), ctx.getPath(), ctx.getCacheKey(), ctx);
    }

    private ContextWrapper createContext(String context) {
        ContextWrapper ctxWrapper = new ContextWrapper(context);
        setContext(context, ctxWrapper);
        return ctxWrapper;
    }
}
