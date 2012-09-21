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
import org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData;

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


    public void uploadWebapp(WebappUploadData[] webappUploadDataList) throws AxisFault {
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
}
