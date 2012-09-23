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
package org.jaggeryjs.jaggery.app.mgt;

import org.apache.catalina.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.webapp.mgt.TomcatGenericWebappsDeployer;
import org.wso2.carbon.webapp.mgt.WebApplication;

import java.io.File;
import java.util.List;

/**
 * Represents a Tomcat Web Application
 */
@SuppressWarnings("unused")
public class JaggeryApplication extends WebApplication {
    private static final Log log = LogFactory.getLog(JaggeryApplication.class);

    private long configDirLastModifiedTime;
    private List<ServletParameter> servletParameters;
    private List<ServletMappingParameter> servletMappingParameters;

    public JaggeryApplication(TomcatGenericWebappsDeployer tomcatGenericWebappsDeployer, Context context,
                              File webappFile) {
        super(tomcatGenericWebappsDeployer, context, webappFile);
        setWebappFile(webappFile);
        setLastModifiedTime(webappFile.lastModified());
        if (JaggeryDeploymentUtil.getConfig(webappFile) != null) {
            setConfigDirLastModifiedTime(JaggeryDeploymentUtil.getConfig(webappFile).lastModified());
        }
    }

    public long getConfigDirLastModifiedTime() {
        return configDirLastModifiedTime;
    }

    public void setConfigDirLastModifiedTime(long configDirLastModifiedTime) {
        this.configDirLastModifiedTime = configDirLastModifiedTime;
    }

    public List<ServletParameter> getServletParameters() {
        return servletParameters;
    }

    public void setServletParameters(List<ServletParameter> servletParameters) {
        this.servletParameters = servletParameters;
    }

    public List<ServletMappingParameter> getServletMappingParameters() {
        return servletMappingParameters;
    }

    public void setServletMappingParameters(
            List<ServletMappingParameter> servletMappingParameters) {
        this.servletMappingParameters = servletMappingParameters;
    }
}
