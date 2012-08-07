/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jaggeryjs.jaggery.app.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.webapp.mgt.SessionsWrapper;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebappAdmin;
import org.wso2.carbon.webapp.mgt.WebappMetadata;
import org.wso2.carbon.webapp.mgt.WebappUploadData;
import org.wso2.carbon.webapp.mgt.WebappsConstants;
import org.wso2.carbon.webapp.mgt.WebappsWrapper;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.IOException;

/**
 * The Admin service for managing webapps
 */
@SuppressWarnings("unused")
public class JaggeryAppAdmin extends WebappAdmin {

    private static final int BYTE_BUFFER_SIZE = 8192;

    private static final Log log = LogFactory.getLog(
            JaggeryAppAdmin.class);

    public JaggeryAppAdmin() {
    }

    public JaggeryAppAdmin(AxisConfiguration axisConfig) throws Exception {
        super(axisConfig);
    }

    /**
     * This method can be used to check whether the given web app is relevant for this Webapp
     * type. Only generic webapps are relevant for this Admin service.
     *
     * @param webapp - WebApplication instance
     * @return - true if relevant
     */
    protected boolean isWebappRelevant(WebApplication webapp) {
        String filterProp = (String) webapp.getProperty(WebappsConstants.WEBAPP_FILTER);
        // If non of the filters are set, this is a generic webapp, so return true
        return JaggeryConstants.JAGGERY_WEBAPP_FILTER_PROP.equals(filterProp);
    }

    protected String getWebappDeploymentDirPath() {
        String directory = JaggeryConstants.WEBAPP_DEPLOYMENT_FOLDER;
        if (System.getProperty("jaggery.home") != null) {
            directory = JaggeryConstants.WEBAPP_DEPLOYMENT_FOLDER_IN_JAGGERY;
        }
        return getAxisConfig().getRepository().getPath() + File.separator + directory;
    }


    /**
     * Upload a webapp
     *
     * @param webappUploadDataList Array of data representing the webapps that are to be uploaded
     * @return true - if upload was successful
     * @throws org.apache.axis2.AxisFault If an error occurrs while uploading
     */
    public boolean uploadWebapp(WebappUploadData[] webappUploadDataList) throws AxisFault {

        AxisConfiguration axisConfig = getAxisConfig();
        String repoPath = axisConfig.getRepository().getPath();
        String jaggeryAppsPath = getWebappDeploymentDirPath();

        for (WebappUploadData uploadData : webappUploadDataList) {
            String fName = uploadData.getFileName();
            if (fName.contains(".")) {
                fName = fName.split("\\.")[0];
            }

            File webappsDir = new File(jaggeryAppsPath + File.separator + fName);
            File jaggeryAppsFile = new File(jaggeryAppsPath);
            if (webappsDir.exists()) {
                String msg = "Jaggery app with the same name already exists";
                log.error(msg);
                throw new AxisFault(msg);
            } else if (!jaggeryAppsFile.exists()) {
                //if deployment directory is not there we create it
                if (!jaggeryAppsFile.mkdir()) {
                    String error = "Jaggery deployment directory not found and cannot be created when uploading";
                    log.error(error);
                    throw new AxisFault(error);
                }
            }

            ArchiveManipulator archiveManipulator = new ArchiveManipulator();
            try {
                archiveManipulator.extractFromStream(uploadData.getDataHandler().getInputStream(),
                        jaggeryAppsPath + File.separator + fName);
            } catch (IOException e) {
                log.error("Could not unzip the Jaggery App Archive", e);
                throw new AxisFault(e.getMessage(), e);
            }

        }
        return true;
    }
}