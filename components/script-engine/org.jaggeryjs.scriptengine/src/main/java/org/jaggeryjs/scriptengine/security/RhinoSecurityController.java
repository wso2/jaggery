package org.jaggeryjs.scriptengine.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.GeneratedClassLoader;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.SecurityController;

import java.security.*;

public class RhinoSecurityController extends SecurityController {

    private static final Log log = LogFactory.getLog(RhinoSecurityController.class);

    private static final boolean IS_SECURITY_ENABLED = (System.getSecurityManager() != null);

    public static boolean isSecurityEnabled() {
        return IS_SECURITY_ENABLED;
    }

    public Class getStaticSecurityDomainClassInternal() {
        return RhinoSecurityDomain.class;
    }

    public GeneratedClassLoader createClassLoader(final ClassLoader parentLoader, Object protectionDomain) {
        final ProtectionDomain domain = (ProtectionDomain) protectionDomain;
        return AccessController.doPrivileged(new PrivilegedAction<GeneratedClassLoader>() {
            @Override
            public GeneratedClassLoader run() {
                return new Loader(parentLoader, domain);
            }
        });
    }

    public ProtectionDomain getDynamicSecurityDomain(Object staticDomain) {
        if (staticDomain == null) {
            return null;
        }
        RhinoSecurityDomain securityDomain = (RhinoSecurityDomain) staticDomain;
        Policy policy = AccessController.doPrivileged(new PrivilegedAction<Policy>() {
            @Override
            public Policy run() {
                return Policy.getPolicy();
            }
        });
        if (policy == null) {
            return null;
        }

        CodeSource codeSource;
        try {
            codeSource = securityDomain.getCodeSource();
        } catch (ScriptException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        if (codeSource == null) {
            return null;
        }

        PermissionCollection permissions = policy.getPermissions(codeSource);
        //getClassLoader permission is needed for E4X feature
        permissions.add(new RuntimePermission("getClassLoader"));
        updatePermissions(permissions, securityDomain);
        return new ProtectionDomain(codeSource, permissions);
    }

    protected void updatePermissions(PermissionCollection permissions, RhinoSecurityDomain securityDomain) {

    }

    public Object callWithDomain(Object securityDomain,
                                 final Context cx,
                                 final Callable callable,
                                 final Scriptable scope,
                                 final Scriptable thisObj,
                                 final Object[] args) {
        ProtectionDomain staticDomain = (ProtectionDomain) securityDomain;
        // There is no direct way in Java to intersect permitions according
        // stack context with additional domain.
        // The following implementation first constructs ProtectionDomain
        // that allows actions only allowed by both staticDomain and current
        // stack context, and then constructs AccessController for this dynamic
        // domain.
        // If this is too slow, alternative solution would be to generate
        // class per domain with a proxy method to call to infect
        // java stack.
        // Another optimization in case of scripts coming from "world" domain,
        // that is having minimal default privileges is to construct
        // one AccessControlContext based on ProtectionDomain
        // with least possible privileges and simply call
        // AccessController.doPrivileged with this untrusted context

        ProtectionDomain dynamicDomain = getDynamicSecurityDomain(staticDomain);
        ProtectionDomain[] tmp = {dynamicDomain};
        AccessControlContext restricted = new AccessControlContext(tmp);

        PrivilegedAction action = new PrivilegedAction() {
            public Object run() {
                return callable.call(cx, scope, thisObj, args);
            }
        };

        return AccessController.doPrivileged(action, restricted);
    }

    private static class Loader extends SecureClassLoader implements GeneratedClassLoader {

        private ProtectionDomain domain;

        Loader(ClassLoader parent, ProtectionDomain domain) {
            super(parent != null ? parent : getSystemClassLoader());
            this.domain = domain;
        }

        public Class defineClass(String name, byte[] data) {
            return super.defineClass(name, data, 0, data.length, domain);
        }

        public void linkClass(Class cl) {
            resolveClass(cl);
        }
    }


}
