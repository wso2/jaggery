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
package org.jaggeryjs.jaggery.deployer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.app.mgt.TomcatJaggeryWebappsDeployer;
import org.wso2.carbon.webapp.mgt.AbstractWebappDeployer;
import org.wso2.carbon.webapp.mgt.TomcatGenericWebappsDeployer;
import org.wso2.carbon.webapp.mgt.WebappsConstants;

import java.io.File;

/**
 * Axis2 deployer for deploying Web applications
 */
public class JaggeryDeployer extends AbstractWebappDeployer {

    private static final Log log = LogFactory.getLog(JaggeryDeployer.class);

    @Override
    protected TomcatGenericWebappsDeployer createTomcatGenericWebappDeployer(
            String webContextPrefix, int tenantId, String tenantDomain) {
        return new TomcatJaggeryWebappsDeployer(webContextPrefix, tenantId, tenantDomain, webApplicationsHolderMap, configContext);
    }

    @Override
    protected String getType() {
        return WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP;
    }

    @Override
    public void setDirectory(String directory) {
        this.webappsDir = directory;
    }

    @Override
    public void setExtension(String extension) {

    }

    @Override
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        super.deploy(deploymentFileData);
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    @Override
    public void undeploy(String fileName) throws DeploymentException {
        if (!new File(fileName).exists()) {
            super.undeploy(fileName);
        }
    }

}
