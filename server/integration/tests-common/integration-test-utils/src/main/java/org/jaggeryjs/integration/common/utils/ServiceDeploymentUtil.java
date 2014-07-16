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

import org.jaggeryjs.integration.common.clients.AARServiceUploaderClient;
import org.jaggeryjs.integration.common.clients.ServiceAdminClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.aarservices.stub.ExceptionException;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;

public class ServiceDeploymentUtil {
    private static int SERVICE_DEPLOYMENT_DELAY = 90 * 1000;
    private static Log log = LogFactory.getLog(ServiceDeploymentUtil.class);

    public static void deployArrService(String backEndUrl, String sessionCookie, String serviceName,
                                        String serviceFilePath, int deploymentDelay)
            throws RemoteException, MalformedURLException, LoginAuthenticationExceptionException,
            ExceptionException {
        AARServiceUploaderClient adminServiceAARServiceUploader =
                new AARServiceUploaderClient(backEndUrl, sessionCookie);
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        if (adminServiceService.isServiceExists(serviceName)) {
            adminServiceService.deleteService(new String[]{serviceName});
            isServiceUnDeployed(backEndUrl, sessionCookie, serviceName, deploymentDelay);
        }
        adminServiceAARServiceUploader.uploadAARFile(serviceName + ".aar", serviceFilePath, "");
        Assert.assertTrue(isServiceDeployed(backEndUrl, sessionCookie, serviceName)
                , serviceName + " deployment failed in Application Server");
    }

    public static void unDeployArrService(String backEndUrl, String sessionCookie,
                                          String serviceName,
                                          int deploymentDelay)
            throws RemoteException, MalformedURLException, LoginAuthenticationExceptionException,
            ExceptionException {
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        if (adminServiceService.isServiceExists(serviceName)) {
            adminServiceService.deleteService(new String[]{serviceName});
            isServiceUnDeployed(backEndUrl, sessionCookie, serviceName, deploymentDelay);
        }
    }

    public static boolean isServiceDeployed(String backEndUrl, String sessionCookie,
                                            String serviceName)
            throws RemoteException {
        log.info("waiting " + SERVICE_DEPLOYMENT_DELAY + " millis for Service deployment " + serviceName);
        boolean isServiceDeployed = false;
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < SERVICE_DEPLOYMENT_DELAY) {
            if (adminServiceService.isServiceExists(serviceName)) {
                isServiceDeployed = true;
                log.info(serviceName + " Service Deployed in " + time + " millis");
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        return isServiceDeployed;
    }

    public static boolean isServiceUnDeployed(String backEndUrl, String sessionCookie,
                                              String serviceName,
                                              int deploymentDelay)
            throws RemoteException {
        log.info("waiting " + deploymentDelay + " millis for Service undeployment");
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        boolean isServiceDeleted = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < deploymentDelay) {
            if (!adminServiceService.isServiceExists(serviceName)) {
                isServiceDeleted = true;
                log.info(serviceName + " Service undeployed in " + time + " millis");
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        return isServiceDeleted;
    }

    public static boolean isServiceWSDlExist(String serviceUrl, long synchronizingDelay)
            throws Exception {
        log.info("waiting " + synchronizingDelay + " millis for Proxy deployment in worker");
        boolean isServiceDeployed = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < synchronizingDelay) {
            if (isWSDLAvailable(serviceUrl)) {
                isServiceDeployed = true;
                log.info("Proxy Deployed in " + time + " millis in worker");
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        return isServiceDeployed;
    }

    public static boolean isServiceWSDlNotExist(String serviceUrl, long synchronizingDelay)
            throws Exception {
        log.info("waiting " + synchronizingDelay + " millis for Proxy undeployment in worker");
        boolean isServiceUnDeployed = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < synchronizingDelay) {
            if (!isWSDLAvailable(serviceUrl)) {
                isServiceUnDeployed = true;
                log.info("Proxy UnDeployed in " + time + " millis in worker");
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        return isServiceUnDeployed;
    }

    public static boolean isWSDLAvailable(String serviceEndpoint) throws IOException {
        URL url = new URL(serviceEndpoint + "?wsdl");
        boolean isWsdlExist = false;
        BufferedReader rd = null;
        HttpURLConnection conn;
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setReadTimeout(6000);
        try {
            conn.connect();
            // Get the response
            StringBuilder sb = new StringBuilder();
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
                if (sb.toString().contains("wsdl:definitions")) {
                    isWsdlExist = true;
                    break;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                    //ignored
                }
            }
            conn.disconnect();
        }
        return isWsdlExist;
    }

    public static boolean isServiceFaulty(String backEndUrl, String sessionCookie, String serviceName)
            throws RemoteException {
        boolean isServiceDeployed = false;
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        Calendar startTime = Calendar.getInstance();
        long time = startTime.getTimeInMillis() + SERVICE_DEPLOYMENT_DELAY;
        while (System.currentTimeMillis() < time) {
            if (adminServiceService.isServiceFaulty(serviceName)) {
                isServiceDeployed = true;
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        }
        return isServiceDeployed;
    }

    public static boolean isServiceExist(String backEndUrl, String sessionCookie, String serviceName)
            throws RemoteException {
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        return adminServiceService.isServiceExists(serviceName);
    }

    public static boolean isFaultyService(String backEndUrl, String sessionCookie, String serviceName)
            throws RemoteException {
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        return adminServiceService.isServiceFaulty(serviceName);
    }

    public static void deleteService(String backEndUrl, String sessionCookie, String serviceName)
            throws RemoteException {
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        if (isFaultyService(backEndUrl, sessionCookie, serviceName)) {
            adminServiceService.deleteFaultyServiceByServiceName(serviceName);
        } else if (isServiceExist(backEndUrl, sessionCookie, serviceName)) {
            adminServiceService.deleteService(new String[]{adminServiceService.getServiceGroup(serviceName)});
        }
    }

    public static boolean isServiceDeleted(String backEndUrl, String sessionCookie, String serviceName)
            throws RemoteException {
        log.info("waiting " + SERVICE_DEPLOYMENT_DELAY + " millis for service un-deployment");
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        boolean isServiceDeleted = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < SERVICE_DEPLOYMENT_DELAY) {
            if (!adminServiceService.isServiceExists(serviceName)) {
                isServiceDeleted = true;
                log.info(serviceName + " Service un-deployed in " + time + " millis");
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        }
        return isServiceDeleted;
    }
}


