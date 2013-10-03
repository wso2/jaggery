package org.jaggeryjs.jaggery.core.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.jaggery.core.manager.CommonManager;
import org.jaggeryjs.jaggery.core.manager.WebAppManager;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.JavaScriptProperty;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
public class WebAppSessionListener implements HttpSessionListener {
    private static Log log = LogFactory.getLog(WebAppSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        ServletContext ctx = httpSessionEvent.getSession().getServletContext();

        List<Object> jsListeners = (List<Object>) ctx.getAttribute(JaggeryCoreConstants.JS_CREATED_LISTENERS);
        JaggeryContext clonedContext = WebAppManager.clonedJaggeryContext(ctx);

        RhinoEngine engine = clonedContext.getEngine();
        Context cx = engine.enterContext();


        ScriptableObject clonedScope = clonedContext.getScope();

        JavaScriptProperty session = new JavaScriptProperty("session");
        session.setValue(cx.newObject(clonedScope, "Session", new Object[]{httpSessionEvent.getSession()}));
        session.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(clonedScope, session);

        if (jsListeners != null) {
            for (Object jsListener : jsListeners) {
                CommonManager.getCallstack(clonedContext).push((String) jsListener);
                try {
                    ScriptReader sr = new ScriptReader(ctx.getResourceAsStream((String) jsListener)) {
                        @Override
                        protected void build() throws IOException {
                            try {
                                sourceReader = new StringReader(HostObjectUtil.streamToString(sourceIn));
                            } catch (ScriptException e) {
                                throw new IOException(e);
                            }
                        }
                    };
                    engine.exec(sr, clonedScope, null);
                } catch (ScriptException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        ServletContext ctx = httpSessionEvent.getSession().getServletContext();

        List<Object> jsListeners = (List<Object>) ctx.getAttribute(JaggeryCoreConstants.JS_DESTROYED_LISTENERS);
        JaggeryContext clonedContext = WebAppManager.clonedJaggeryContext(ctx);

        RhinoEngine engine = clonedContext.getEngine();
        Context cx = engine.enterContext();


        ScriptableObject clonedScope = clonedContext.getScope();

        JavaScriptProperty session = new JavaScriptProperty("session");
        session.setValue(cx.newObject(clonedScope, "Session", new Object[]{httpSessionEvent.getSession()}));
        session.setAttribute(ScriptableObject.READONLY);
        RhinoEngine.defineProperty(clonedScope, session);

        if (jsListeners != null) {
            for (Object jsListener : jsListeners) {
                CommonManager.getCallstack(clonedContext).push((String) jsListener);
                try {
                    ScriptReader sr = new ScriptReader(ctx.getResourceAsStream((String) jsListener)) {
                        @Override
                        protected void build() throws IOException {
                            try {
                                sourceReader = new StringReader(HostObjectUtil.streamToString(sourceIn));
                            } catch (ScriptException e) {
                                throw new IOException(e);
                            }
                        }
                    };
                    engine.exec(sr, clonedScope, null);
                } catch (ScriptException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
