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
package org.jaggeryjs.jaggery.app.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.app.mgt.stub.JaggeryAppAdminStub;
import org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.SessionsWrapper;
import org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappMetadata;
import org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData;
import org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappsWrapper;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Client which communicates with the WebappAdmin service
 */
public class JaggeryAdminClient {
    private static final String CANNOT_EXPIRE_ALL_SESSIONS_IN_WEBAPPS = "cannot.expire.all.sessions.in.webapps";
	private static final String CANNOT_DELETE_WEBAPPS = "cannot.delete.webapps";
	public static final String BUNDLE = "org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources";
    private static final Log log = LogFactory.getLog(JaggeryAdminClient.class);
    private ResourceBundle bundle;
    private JaggeryAppAdminStub stub;

    public JaggeryAdminClient(String cookie,
                             String backendServerURL,
                             ConfigurationContext configCtx,
                             Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "JaggeryAppAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new JaggeryAppAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
    }

    public WebappsWrapper getPagedWebappsSummary(String webappSearchString,
                                                 String webappState,
                                                 int pageNumber) throws AxisFault {
        try {
            return stub.getPagedWebappsSummary(webappSearchString, webappState, pageNumber);
        } catch (RemoteException e) {
            handleException("cannot.get.webapp.data", e);
        }
        return null;
    }

    public WebappMetadata getStartedWebapp(String webappFileName) throws AxisFault {
        try {
            return stub.getStartedWebapp(webappFileName);
        } catch (RemoteException e) {
            handleException("cannot.get.started.webapp.data", e);
        }
        return null;
    }

    public WebappMetadata getStoppedWebapp(String webappFileName) throws AxisFault {
        try {
            return stub.getStoppedWebapp(webappFileName);
        } catch (RemoteException e) {
            handleException("cannot.get.stopped.webapp.data", e);
        }
        return null;
    }

    public void deleteAllStartedWebapps() throws AxisFault {
        try {
            stub.deleteAllStartedWebapps();
        } catch (RemoteException e) {
            handleException(CANNOT_DELETE_WEBAPPS, e);
        }
    }

    public void deleteAllStoppedWebapps() throws AxisFault {
        try {
            stub.deleteAllStoppedWebapps();
        } catch (RemoteException e) {
            handleException(CANNOT_DELETE_WEBAPPS, e);
        }
    }

    public void deleteStartedWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.deleteStartedWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException(CANNOT_DELETE_WEBAPPS, e);
        }
    }

    public void deleteStoppedWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.deleteStoppedWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException(CANNOT_DELETE_WEBAPPS, e);
        }
    }

    public WebappsWrapper getPagedFaultyWebappsSummary(String webappSearchString,
                                                       int pageNumber) throws AxisFault {
        try {
            return stub.getPagedFaultyWebappsSummary(webappSearchString, pageNumber);
        } catch (RemoteException e) {
            handleException("cannot.get.webapp.data", e);
        }
        return null;
    }

    public void deleteFaultyWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.deleteFaultyWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException("cannot.delete.all.faulty.webapps", e);
        }
    }

    public void deleteAllFaultyWebapps() throws AxisFault {
        try {
            stub.deleteAllFaultyWebapps();
        } catch (RemoteException e) {
            handleException("cannot.delete.all.faulty.webapps", e);
        }
    }

    public void reloadAllWebapps() throws AxisFault {
        try {
            stub.reloadAllWebapps();
        } catch (RemoteException e) {
            handleException("cannot.reload.webapps", e);
        }
    }

    public void reloadWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.reloadWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException("cannot.reload.webapps", e);
        }
    }

    public void stopAllWebapps() throws AxisFault {
        try {
            stub.stopAllWebapps();
        } catch (RemoteException e) {
            handleException("cannot.stop.webapps", e);
        }
    }

    public void stopWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.stopWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException("cannot.stop.webapps", e);
        }
    }

    public void startAllWebapps() throws AxisFault {
        try {
            stub.startAllWebapps();
        } catch (RemoteException e) {
            handleException("cannot.start.webapps", e);
        }
    }

    public void startWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.startWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException("cannot.start.webapps", e);
        }
    }

    public SessionsWrapper getActiveSessionsInWebapp(String webappFileName,
                                                     int pageNumber) throws AxisFault {
        try {
            return stub.getActiveSessions(webappFileName, pageNumber);
        } catch (RemoteException e) {
            handleException("cannot.get.active.sessions", e);
        }
        return null;
    }

    public void expireSessionsInWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.expireSessionsInWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException(CANNOT_EXPIRE_ALL_SESSIONS_IN_WEBAPPS, e);
        }
    }

    public void expireSessionsInWebapp(String webappFileName,
                                       long maxSessionLifetimeMillis) throws AxisFault {
        try {
            stub.expireSessionsInWebapp(webappFileName, maxSessionLifetimeMillis);
        } catch (RemoteException e) {
            handleException(CANNOT_EXPIRE_ALL_SESSIONS_IN_WEBAPPS, e);
        }
    }

    public void expireSessionsInWebapp(String webappFileName,
                                       String[] sessionIDs) throws AxisFault {
        try {
            stub.expireSessions(webappFileName, sessionIDs);
        } catch (RemoteException e) {
            handleException(CANNOT_EXPIRE_ALL_SESSIONS_IN_WEBAPPS, e);
        }
    }

    public void expireSessionsInAllWebapps() throws AxisFault {
        try {
            stub.expireSessionsInAllWebapps();
        } catch (RemoteException e) {
            handleException(CANNOT_EXPIRE_ALL_SESSIONS_IN_WEBAPPS, e);
        }
    }

    public void expireAllSessionsInWebapp(String webappFileName) throws AxisFault {
        try {
            stub.expireAllSessions(webappFileName);
        } catch (RemoteException e) {
            handleException(CANNOT_EXPIRE_ALL_SESSIONS_IN_WEBAPPS, e);
        }
    }

    public void uploadWebapp(WebappUploadData [] webappUploadDataList) throws AxisFault {
        try {
            stub.uploadWebapp(webappUploadDataList);
        } catch (RemoteException e) {
            handleException("cannot.upload.webapps", e);
        }
    }

    private void handleException(String msgKey, Exception e) throws AxisFault {
        String msg = bundle.getString(msgKey);
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public void downloadWarFileHandler(String fileName, HttpServletResponse response) throws AxisFault {
        try {
            ServletOutputStream out = response.getOutputStream();
            DataHandler handler = stub.downloadWarFileHandler(fileName);
            if (handler != null) {
                response.setHeader("Content-Disposition", "fileName=" + fileName);
                response.setContentType(handler.getContentType());
                InputStream in = handler.getDataSource().getInputStream();
                int nextChar;
                while ((nextChar = in.read()) != -1) {
                    out.write((char) nextChar);
                }
                out.flush();
                in.close();
            } else {
                out.write("The requested Jaggery app was not found on the server".getBytes());
            }
        } catch (RemoteException e) {
            handleException("error.downloading.war", e);
        } catch (IOException e) {
            handleException("error.downloading.war", e);
        }
    }
}
