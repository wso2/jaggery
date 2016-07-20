/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jaggeryjs.jaggery.tomcat.listener;

import org.apache.catalina.*;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The JaggeryAppListener (LifecycleListener) listens to the events of each webapp and check whether
 * the webapp is a jaggery app or not. If it is a jaggery app, it manually load a class to process jaggery.conf
 * file
 */
public class JaggeryAppListener implements LifecycleListener {
    private static Log log = LogFactory.getLog(JaggeryAppListener.class);
    private static final String JAGGERY_CLASSLOAD_NAME = "org.jaggeryjs.jaggery.core.manager.JaggeryDeployerManager";
    private static final String JAGGERY_METHOD_NAME = "processJaggeryApp";
    private static final Path PATH_CATALINA_BASE;
    private static final String JAGGERY_CONF = "jaggery.conf";
    private static final String WAR_EXTENSION = ".war";

    static {
        String catalinaBase = System.getProperty(Globals.CATALINA_BASE_PROP);
        PATH_CATALINA_BASE = Paths.get(catalinaBase);
    }

    public void lifecycleEvent(LifecycleEvent event) {
        if (Lifecycle.CONFIGURE_START_EVENT.equals(event.getType())) {
            Context context = null;
            if (event.getLifecycle() instanceof Context) {
                context = (Context) event.getLifecycle();
            }
            if (!isJaggeryApp(context)) {
                return;
            }
            log.debug(context.getName() + " is identified as a Jagggery App");
            try {
                Class<?> webappLoader = context.getServletContext().getClassLoader().loadClass(JAGGERY_CLASSLOAD_NAME);
                Method methodProcessJaggeryApp = webappLoader.getMethod(JAGGERY_METHOD_NAME, Context.class, Path.class);
                methodProcessJaggeryApp.invoke(webappLoader.newInstance(), context, getAppBase(context));
            } catch (Exception e) {
                log.error("Error occurred while processing the jaggery.conf file", e);
            }
        }
    }

    /**
     * check whether webapp is jaggery app or not
     *
     * @param context Context of the jaggery app
     * @return boolean
     */
    private boolean isJaggeryApp(Context context) {
        Path appBase = getAppBase(context);
        String path;
        if (context.getDocBase().contains(WAR_EXTENSION)) {
            try {
                if (!appBase.endsWith("/")) {
                    path = appBase + File.separator + context.getDocBase();
                } else {
                    path = appBase + context.getDocBase();
                }
                ZipFile zip = new ZipFile(path);
                for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    if (entry.getName().toLowerCase().contains(JAGGERY_CONF)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                log.error("Error in processing the zip file", e);
            }
        } else {
            Path filepath = Paths.get(appBase + context.getPath() + File.separator + JAGGERY_CONF);
            return Files.exists(filepath);
        }
        return false;
    }

    /**
     * get the AppBase path of the web app
     *
     * @param context Context of the webapp
     * @return Path
     */
    public Path getAppBase(Context context) {
        String appBase = null;
        if (context != null) {
            String docBase = context.getDocBase();
            Host host = (Host) context.getParent();
            appBase = host.getAppBase();
        }
        return Paths.get(PATH_CATALINA_BASE.toString(), File.separator, appBase, File.separator);
    }
}
