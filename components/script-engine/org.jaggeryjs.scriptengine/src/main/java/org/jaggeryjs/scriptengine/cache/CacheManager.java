package org.jaggeryjs.scriptengine.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.GeneratedClassLoader;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.optimizer.ClassCompiler;
import org.mozilla.javascript.tools.ToolErrorReporter;

import java.io.Reader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CacheManager {

    private static final Log log = LogFactory.getLog(CacheManager.class);

    private static final String PACKAGE_NAME = "org.jaggeryjs.rhino";

    private ClassCompiler compiler;

    private ConcurrentMap<String, TenantWrapper> tenants = new ConcurrentHashMap<String, TenantWrapper>();

    private final Object lock0 = new Object();

    public CacheManager(CompilerEnvirons compilerEnv) {
        if (compilerEnv == null) {
            compilerEnv = new CompilerEnvirons();
            compilerEnv.setErrorReporter(new ToolErrorReporter(true));
        }
        this.compiler = new ClassCompiler(compilerEnv);
    }

    public void invalidateCache(ScriptCachingContext sctx) throws ScriptException {
        synchronized (lock0) {
            CachingContext ctx = getCachingContext(sctx);
            if (ctx == null) {
                return;
            }
            TenantWrapper tenant = tenants.get(ctx.getTenantId());
            tenant.removeCachingContext(sctx);
        }
    }

    public void unloadTenant(String tenantId) {
        if (tenantId == null) {
            return;
        }
        tenants.remove(tenantId);
    }

    public synchronized void cacheScript(Reader scriptReader, ScriptCachingContext sctx) throws ScriptException {
        if (scriptReader == null) {
            String msg = "Unable to find the Reader for script source in CachingContext";
            log.error(msg);
            throw new ScriptException(msg);
        }
        String className;
        TenantWrapper tenant = initContexts(sctx);
        CachingContext ctx = getCachingContext(sctx);
        if (ctx != null) {
            if (sctx.getSourceModifiedTime() <= ctx.getSourceModifiedTime()) {
                return;
            }
            className = ctx.getClassName();
            invalidateCache(sctx);
            ctx.setSourceModifiedTime(0L);
            ctx.setCacheUpdatedTime(0L);
        } else {
            className = getClassName(tenant, sctx);
            ctx = new CachingContext(sctx.getContext(), sctx.getPath(), sctx.getCacheKey());
            ctx.setTenantId(sctx.getTenantDomain());
            ctx.setContext(sctx.getContext());
            ctx.setPath(sctx.getPath());
            ctx.setCacheKey(sctx.getCacheKey());
            ctx.setClassName(className);
        }
        try {
            String scriptPath = sctx.getContext() + sctx.getPath() + sctx.getCacheKey();
            Object[] compiled = compiler
                    .compileToClassFiles(HostObjectUtil.readerToString(scriptReader), scriptPath, 1, className);
            ctx.setScript(getScriptObject(compiled, sctx));
            ctx.setCacheUpdatedTime(System.currentTimeMillis());
            ctx.setSourceModifiedTime(sctx.getSourceModifiedTime());
            tenant.setCachingContext(ctx);
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }

    public Script getScriptObject(Reader scriptReader, ScriptCachingContext sctx) throws ScriptException {
        CachingContext ctx = getCachingContext(sctx);
        if (ctx == null) {
            return null;
        }
        // source file has been modified, so we recreate
        if (sctx.getSourceModifiedTime() > ctx.getSourceModifiedTime()) {
            cacheScript(scriptReader, sctx);
            ctx = getCachingContext(sctx);
        }
        return ctx.getScript();
    }

    public boolean isCached(ScriptCachingContext sctx) {
        return !(getCachingContext(sctx) == null);
    }

    public boolean isOlder(ScriptCachingContext sctx) {
        CachingContext ctx = getCachingContext(sctx);
        return ctx == null || sctx.getSourceModifiedTime() > ctx.getSourceModifiedTime();
    }

    private TenantWrapper initContexts(ScriptCachingContext sctx) {
        TenantWrapper tenant = tenants.get(sctx.getTenantDomain());
        if (tenant == null) {
            tenant = new TenantWrapper();
            tenants.put(sctx.getTenantDomain(), tenant);
        }
        ContextWrapper context = tenant.getContext(sctx);
        if (context == null) {
            context = new ContextWrapper(sctx.getContext());
            tenant.setContext(sctx, context);
        }
        PackageWrapper packageWrapper = context.getPackage(sctx.getPath());
        if (packageWrapper == null) {
            packageWrapper = new PackageWrapper();
            context.addPackage(sctx.getPath(), packageWrapper);
        }
        return tenant;
    }

    private static String getClassName(TenantWrapper tenant, ScriptCachingContext sctx) throws ScriptException {
        String filteredPath = ContextWrapper.getPackage(sctx.getContext(), sctx.getPath());
        PackageWrapper packageWrapper = tenant.getPath(sctx);
        long classIndex = packageWrapper.getClassIndex();
        packageWrapper.setClassIndex(classIndex + 1);
        return PACKAGE_NAME + filteredPath + ".c" + classIndex;
    }

    private CachingContext getCachingContext(ScriptCachingContext sctx) {
        TenantWrapper tenant = tenants.get(sctx.getTenantDomain());
        if (tenant == null) {
            return null;
        }
        return tenant.getCachingContext(sctx);
    }

    private Script getScriptObject(Object[] compiled, ScriptCachingContext sctx) throws ScriptException {
        String className = (String) compiled[0];
        byte[] classBytes = (byte[]) compiled[1];
        ClassLoader rhinoLoader = getClass().getClassLoader();
        GeneratedClassLoader loader;
        try {
            loader = SecurityController.createLoader(rhinoLoader, sctx.getSecurityDomain());
            Class cl = loader.defineClass(className, classBytes);
            loader.linkClass(cl);
            return (Script) cl.newInstance();
        } catch (SecurityException e) {
            throw new ScriptException(e);
        } catch (IllegalArgumentException e) {
            throw new ScriptException(e);
        } catch (InstantiationException e) {
            throw new ScriptException(e);
        } catch (IllegalAccessException e) {
            throw new ScriptException(e);
        }
    }

}
