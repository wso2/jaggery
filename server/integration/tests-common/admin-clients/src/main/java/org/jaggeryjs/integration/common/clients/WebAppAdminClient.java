/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.jaggeryjs.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.webapp.mgt.stub.WebappAdminStub;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.VersionedWebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappsWrapper;

import javax.activation.DataHandler;
import java.io.File;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class WebAppAdminClient {

    private final Log log = LogFactory.getLog(WebAppAdminClient.class);

    private WebappAdminStub webappAdminStub;

    public WebAppAdminClient(String backendUrl, String sessionCookie) throws AxisFault {
        String serviceName = "WebappAdmin";
        String endPoint = backendUrl + serviceName;
        webappAdminStub = new WebappAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, webappAdminStub);
    }

    public void warFileUplaoder(String filePath) throws RemoteException {
        File file = new File(filePath);
        String fileName = file.getName();
        URL url = null;
        try {
            url = new URL("file://" + filePath);
        } catch (MalformedURLException e) {
            log.error("Malformed URL " + e);
        }
        DataHandler dh = new DataHandler(url);
        WebappUploadData webApp;
        webApp = new WebappUploadData();
        webApp.setFileName(fileName);
        webApp.setDataHandler(dh);

        try {
            assert webappAdminStub.uploadWebapp(new WebappUploadData[]{webApp}) : "webapp upload unsuccessful";
        } catch (RemoteException e) {
            log.error("Fail to upload webapp file :" + e);
            throw new RemoteException("Fail to upload webapp file :" + e);
        }
    }

    public void deleteWebAppFile(String fileName, String hostName) throws RemoteException {
        webappAdminStub.deleteStartedWebapps(new String[]{hostName+":"+fileName});
    }

    public void deleteFaultyWebAppFile(String fileName, String hostName) throws RemoteException {
        webappAdminStub.deleteFaultyWebapps(new String[]{hostName+":"+fileName});
    }

    public void deleteStoppedWebapps(String fileName, String hostName) throws RemoteException {

        webappAdminStub.deleteStoppedWebapps(new String[]{hostName+":"+fileName});
    }

    public void deleteFaultyWebApps(String fileName, String hostName) throws RemoteException {
        try {
            webappAdminStub.deleteFaultyWebapps(new String[]{hostName+":"+fileName});
        } catch (RemoteException e) {
            throw new RemoteException("Faulty webApp deletion fail", e);
        }
    }

    public void stopWebapps(String fileName, String hostName) throws RemoteException {
        webappAdminStub.stopAllWebapps();
        WebappMetadata webappMetadata = webappAdminStub.getStoppedWebapp(fileName, hostName);
    }

    public boolean stopWebApp(String fileName, String hostName) throws RemoteException {
        webappAdminStub.stopWebapps(new String[]{fileName});
        WebappMetadata webappMetadata = webappAdminStub.getStoppedWebapp(fileName, hostName);
        if (webappMetadata.getWebappFile().equals(fileName)) {
            return true;
        }
        return false;
    }

    public boolean startWebApp(String fileName, String hostName) throws RemoteException {
        webappAdminStub.startWebapps(new String[]{fileName});
        WebappMetadata webappMetadata = webappAdminStub.getStartedWebapp(fileName, hostName);
        if (webappMetadata.getWebappFile().equals(fileName)) {
            return true;
        }
        return false;
    }

    public WebappMetadata getWebAppInfo(String webAppName) throws RemoteException {
        WebappsWrapper wrapper = getPagedWebappsSummary(webAppName, "ALL", "ALL", 0);
        VersionedWebappMetadata[] webappGroups = wrapper.getWebapps();
        if (webappGroups == null || webappGroups.length == 0) {
            throw new RemoteException("No Web Application Found with given name " + webAppName);
        }
        if (webappGroups.length > 1) {
            // this is happened there are more service available with the given web app name prefix
            throw new RemoteException("More than one service found with the given name");
        }

        WebappMetadata[] webappMetadatas = webappGroups[0].getVersionGroups();
        return webappMetadatas[0];

    }

    public WebappsWrapper getPagedWebappsSummary(String searchString, String webAppType,
                                                 String webAppState, int pageNo)
            throws RemoteException {
        return webappAdminStub.getPagedWebappsSummary(searchString, webAppType, webAppState, pageNo);
    }

    public List<String> getWebApplist(String webAppNameSearchString) throws RemoteException {
        List<String> list = new ArrayList<String>();
        WebappsWrapper wrapper = getPagedWebappsSummary(webAppNameSearchString, "ALL", "ALL", 0);
        VersionedWebappMetadata[] webappGroups = wrapper.getWebapps();

        if (webappGroups != null) {
            for (VersionedWebappMetadata webappGroup : webappGroups) {
                for (WebappMetadata metaData : webappGroup.getVersionGroups()) {
                    list.add(metaData.getWebappFile());
                }
            }
        }
        return list;
    }

    public WebappsWrapper getPagedFaultyWebappsSummary(String searchString, String webAppType,
                                                       int pageNo)
            throws RemoteException {
        return webappAdminStub.getPagedFaultyWebappsSummary(searchString, webAppType, pageNo);
    }

    public List<String> getFaultyWebAppList(String webAppNameSearchString) throws RemoteException {
        List<String> list = new ArrayList<String>();
        WebappsWrapper wrapper = getPagedFaultyWebappsSummary(webAppNameSearchString, "ALL", 0);
        VersionedWebappMetadata[] webappGroups = wrapper.getWebapps();

        if (webappGroups != null && webappGroups[0].getVersionGroups() != null) {
            for (WebappMetadata metaData : webappGroups[0].getVersionGroups()) {
                list.add(metaData.getWebappFile());
            }
        }
        return list;
    }

    public void reloadWebApp(String webAppFileName, String hostName) throws RemoteException {
        webappAdminStub.reloadWebapps(new String[]{hostName+":"+webAppFileName});
    }
}

