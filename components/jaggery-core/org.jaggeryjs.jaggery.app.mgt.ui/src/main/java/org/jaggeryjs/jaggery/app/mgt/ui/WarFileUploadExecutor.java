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

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;
import org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The FileUploadExecutor which handles uploading of webapps
 */
public class WarFileUploadExecutor extends AbstractFileUploadExecutor {

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".war", ".zip"};

    public boolean execute(HttpServletRequest request,
                           HttpServletResponse response) throws CarbonException, IOException {

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "Jaggery application uploading failed. No file specified.";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request, response,
                                                "../" + webContext + "/jaggeryapp-mgt/uploadjaggeryapp.jsp");
            return false;
        }

        JaggeryAdminClient client =
                new JaggeryAdminClient(cookie, serverURL, configurationContext, request.getLocale());
        String msg;

        List<FileItemData> tempDataList = fileItemsMap.get("warFileName");
        if (tempDataList == null ){
        	return false;
        }
        List<WebappUploadData> webappUploadDataList = new ArrayList<WebappUploadData>();

        try {
            for (FileItemData filedata : tempDataList) {
                WebappUploadData tempData = new WebappUploadData();
                checkServiceFileExtensionValidity(getFileName(filedata.getFileItem().getName()), ALLOWED_FILE_EXTENSIONS);
                tempData.setFileName(getFileName(filedata.getFileItem().getName()));
                tempData.setDataHandler(filedata.getDataHandler());
                webappUploadDataList.add(tempData);
            }

            client.uploadWebapp(webappUploadDataList.toArray(new WebappUploadData[webappUploadDataList.size()]));

            response.setContentType("text/html; charset=utf-8");
            
            if(tempDataList.size() > 1) {
                msg = "Jaggery applications have been uploaded "
                        + "successfully. Please refresh this page in a while to see "
                        + "the status of the running Jaggery apps.";
            }else {
                msg = "Jaggery application has been uploaded "
                        + "successfully. Please refresh this page in a while to see "
                        + "the status of the running Jaggery apps.";
            }

            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request, response,
                                                "../" + webContext + "/webapp-list/index.jsp");
            return true;
        } catch (Exception e) {
            msg = "Jaggery application upload failed. " + e.getMessage();
            log.error(msg, e);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request, response,
                                                "../" + webContext + "/jaggeryapp-mgt/uploadjaggeryapp.jsp");
        }
        return false;
    }
}
