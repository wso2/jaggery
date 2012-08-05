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


    public WebappsWrapper getPagedWebappsSummary(String webappSearchString,
                                                 String webappState,
                                                 int pageNumber) throws AxisFault {
        return super.getPagedWebappsSummary(webappSearchString, webappState, pageNumber);
    }

    /**
     * Get the details of a deplyed webapp
     *
     * @param webappFileName
     * @return
     */
    public WebappMetadata getStartedWebapp(String webappFileName) {
        return super.getStartedWebapp(webappFileName);
    }

    /**
     * Get the details of an stopped webapp
     *
     * @param webappFileName
     * @return
     */
    public WebappMetadata getStoppedWebapp(String webappFileName) {
        return super.getStoppedWebapp(webappFileName);
    }

    /**
     * @param webappSearchString
     * @param pageNumber
     * @return
     * @throws AxisFault
     */
    public WebappsWrapper getPagedFaultyWebappsSummary(String webappSearchString,
                                                       int pageNumber) throws AxisFault {
        return super.getPagedFaultyWebappsSummary(webappSearchString, pageNumber);
    }

    /**
     * Delete a set of started webapps
     *
     * @param webappFileNames The names of the webapp files to be deleted
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteStartedWebapps(String[] webappFileNames) throws AxisFault {
        super.deleteStartedWebapps(webappFileNames);
    }

    /**
     * Delete a set of stopped webapps
     *
     * @param webappFileNames The names of the webapp files to be deleted
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteStoppedWebapps(String[] webappFileNames) throws AxisFault {
        super.deleteStoppedWebapps(webappFileNames);
    }

    /**
     * Delete a set of faulty webapps
     *
     * @param webappFileNames The names of the webapp files to be deleted
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteFaultyWebapps(String[] webappFileNames) throws AxisFault {
        super.deleteFaultyWebapps(webappFileNames);
    }

    /**
     * Delete a single webapp which can be in any state; started, stopped or faulty. This method
     * will search the webapp in all lists and delete it if found.
     *
     * @param webappFileName - name of the file to be deleted
     * @throws AxisFault - If an error occurs while deleting the webapp
     */
    public void deleteWebapp(String webappFileName) throws AxisFault {
        super.deleteWebapp(webappFileName);
    }

    /**
     * Delete all started webapps
     *
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllStartedWebapps() throws AxisFault {
        super.deleteAllStartedWebapps();
    }

    /**
     * Delete all stopped webapps
     *
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllStoppedWebapps() throws AxisFault {
        super.deleteAllStoppedWebapps();
    }

    /**
     * Delete all faulty webapps
     *
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllFaultyWebapps() throws AxisFault {
        super.deleteAllFaultyWebapps();
    }

    /**
     * Reload all webapps
     */
    public void reloadAllWebapps() {
        super.reloadAllWebapps();
    }

    /**
     * Reload a set of webapps
     *
     * @param webappFileNames The file names of the webapps to be reloaded
     */
    public void reloadWebapps(String[] webappFileNames) {
        super.reloadWebapps(webappFileNames);
    }

    /**
     * Undeploy all webapps
     *
     * @throws AxisFault If an error occurs while undeploying
     */
    public void stopAllWebapps() throws AxisFault {
        super.stopAllWebapps();
    }

    /**
     * Undeploy a set of webapps
     *
     * @param webappFileNames The file names of the webapps to be stopped
     * @throws AxisFault If an error occurs while undeploying
     */
    public void stopWebapps(String[] webappFileNames) throws AxisFault {
        super.stopWebapps(webappFileNames);
    }

    /**
     * Redeploy all webapps
     *
     * @throws org.apache.axis2.AxisFault If an error occurs while restarting webapps
     */
    public void startAllWebapps() throws AxisFault {
        super.startAllWebapps();
    }

    /**
     * Redeploy a set of webapps
     *
     * @param webappFileNames The file names of the webapps to be restarted
     * @throws org.apache.axis2.AxisFault If a deployment error occurs
     */
    public void startWebapps(String[] webappFileNames) throws AxisFault {
        super.startWebapps(webappFileNames);
    }


    /**
     * Get all active sessions of a webapp
     *
     * @param webappFileName The names of the webapp file
     * @param pageNumber     The number of the page to fetch, starting with 0
     * @return The session array
     */
    public SessionsWrapper getActiveSessions(String webappFileName, int pageNumber) {
        return super.getActiveSessions(webappFileName, pageNumber);
    }

    /**
     * Expire all sessions in all webapps
     */
    public void expireSessionsInAllWebapps() {
        super.expireSessionsInAllWebapps();
    }

    /**
     * Expire all sessions in specified webapps
     *
     * @param webappFileNames The file names of the webapps whose sessions should be expired
     */
    public void expireSessionsInWebapps(String[] webappFileNames) {
        super.expireSessionsInWebapps(webappFileNames);
    }

    /**
     * Expire all sessions in the specified webapp which has a
     * lifetime >= <code>maxSessionLifetimeMillis</code>
     *
     * @param webappFileName           The file name of the webapp whose sessions should be expired
     * @param maxSessionLifetimeMillis The max allowed lifetime for the sessions
     */
    public void expireSessionsInWebapp(String webappFileName, long maxSessionLifetimeMillis) {
        super.expireSessionsInWebapp(webappFileName, maxSessionLifetimeMillis);
    }

    /**
     * Expire a given session in a webapp
     *
     * @param webappFileName The file name of the webapp whose sessions should be expired
     * @param sessionIDs     Array of session IDs
     * @throws org.apache.axis2.AxisFault If an error occurs while retrieving sessions
     */
    public void expireSessions(String webappFileName, String[] sessionIDs) throws AxisFault {
        super.expireSessions(webappFileName, sessionIDs);
    }

    /**
     * Expire a given session in a webapp
     *
     * @param webappFileName The file name of the webapp whose sessions should be expired
     */
    public void expireAllSessions(String webappFileName) {
        super.expireAllSessions(webappFileName);
    }

    /**
     * check if unpack wars enabled
     *
     * @return true if enabled.
     */
    public boolean isUnpackWARs() {
        return super.isUnpackWARs();
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

    /**
     * Downloads the webapp archive (.war) file
     *
     * @param fileName name of the .war that needs to be downloaded
     * @return the corresponding data handler of the .war that needs to be downloaded
     */
    public DataHandler downloadWarFileHandler(String fileName) {

        String repoPath = getAxisConfig().getRepository().getPath();
        String jaggeryAppsPath = getWebappDeploymentDirPath() + File.separator + fileName;

        File webAppFile = new File(jaggeryAppsPath);
        DataHandler handler = null;

        if (webAppFile.isDirectory()) {
            String zipTo = "tmp" + File.separator + fileName + ".zip";
            File fDownload = new File(zipTo);
            ArchiveManipulator archiveManipulator = new ArchiveManipulator();
            synchronized (this) {
                try {
                    archiveManipulator.archiveDir(zipTo, webAppFile.getAbsolutePath());
                    FileDataSource datasource = new FileDataSource(fDownload);
                    handler = new DataHandler(datasource);
                } catch (IOException e) {
                    log.error("Error downloading WAR file.", e);
                }
            }
        } else {
            FileDataSource datasource = new FileDataSource(new File(repoPath));
            handler = new DataHandler(datasource);
        }
        return handler;
    }

}