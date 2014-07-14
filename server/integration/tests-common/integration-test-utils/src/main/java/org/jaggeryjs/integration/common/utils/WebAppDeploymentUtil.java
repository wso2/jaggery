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
package org.jaggeryjs.integration.common.utils;

import org.jaggeryjs.integration.common.clients.WebAppAdminClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

public class WebAppDeploymentUtil {
    private static Log log = LogFactory.getLog(WebAppDeploymentUtil.class);
    private static int WEBAPP_DEPLOYMENT_DELAY = 90 * 1000;

    public static boolean isWebApplicationDeployed(String backEndUrl, String sessionCookie,
                                                   String webAppFileName) throws Exception {
        log.info("waiting " + WEBAPP_DEPLOYMENT_DELAY + " millis for Service deployment " + webAppFileName);
        WebAppAdminClient webAppAdminClient = new WebAppAdminClient(backEndUrl, sessionCookie);
        List<String> webAppList;
        List<String> faultyWebAppList;
        String webAppName = webAppFileName + ".war";

        boolean isWebAppDeployed = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < WEBAPP_DEPLOYMENT_DELAY) {
            webAppList = webAppAdminClient.getWebApplist(webAppFileName);
            faultyWebAppList = webAppAdminClient.getFaultyWebAppList(webAppFileName);

            for (String faultWebAppName : faultyWebAppList) {
                if (webAppName.equalsIgnoreCase(faultWebAppName)) {
                    isWebAppDeployed = false;
                    log.info(webAppFileName + "- Web Application is faulty");
                    return isWebAppDeployed;
                }
            }

            for (String name : webAppList) {
                if (webAppName.equalsIgnoreCase(name)) {
                    isWebAppDeployed = true;
                    log.info(webAppFileName + " Web Application deployed in " + time + " millis");
                    return isWebAppDeployed;
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {

            }
        }
        return isWebAppDeployed;
    }

    public static boolean isWebApplicationUnDeployed(String backEndUrl, String sessionCookie,
                                                     String webAppFileName) throws Exception {
        log.info("waiting " + WEBAPP_DEPLOYMENT_DELAY + " millis for webApp undeployment " + webAppFileName);
        WebAppAdminClient webAppAdminClient = new WebAppAdminClient(backEndUrl, sessionCookie);
        List<String> webAppList;

        boolean isWebAppUnDeployed = false;
        Calendar startTime = Calendar.getInstance();
        while ((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) < WEBAPP_DEPLOYMENT_DELAY) {
            webAppList = webAppAdminClient.getWebApplist(webAppFileName);
            if (webAppList.size() != 0) {
                for (String name : webAppList) {
                    if (webAppFileName.equalsIgnoreCase(name)) {
                        isWebAppUnDeployed = false;
                        log.info(webAppFileName + " -  Web Application not undeployed yet");
                        break;
                    }
                }
            } else {
                return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {

            }
        }
        return isWebAppUnDeployed;
    }

    public static boolean isFaultyWebApplicationUnDeployed(String backEndUrl, String sessionCookie,
                                                           String webAppFileName) throws Exception {
        log.info("waiting " + WEBAPP_DEPLOYMENT_DELAY + " millis for Service undeployment " + webAppFileName);
        WebAppAdminClient webAppAdminClient = new WebAppAdminClient(backEndUrl, sessionCookie);
        List<String> faultyWebAppList;
        String webAppName = webAppFileName + ".war";

        boolean isWebAppDeployed = false;
        Calendar startTime = Calendar.getInstance();
        while ((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) < WEBAPP_DEPLOYMENT_DELAY) {
            faultyWebAppList = webAppAdminClient.getFaultyWebAppList(webAppFileName);
            if (faultyWebAppList.size() != 0) {
                for (String faultWebAppName : faultyWebAppList) {
                    if (webAppName.equalsIgnoreCase(faultWebAppName)) {
                        isWebAppDeployed = false;
                        log.info(webAppFileName + "- Web Application is faulty");
                        break;
                    }
                }
            } else {
                return true;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {

            }
        }
        return isWebAppDeployed;
    }
}