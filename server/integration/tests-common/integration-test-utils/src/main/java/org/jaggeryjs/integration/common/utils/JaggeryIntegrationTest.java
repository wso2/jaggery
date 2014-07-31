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
*"Jaggery IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.jaggeryjs.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.integration.common.clients.AARServiceUploaderClient;
import org.jaggeryjs.integration.common.clients.ServiceAdminClient;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.admin.client.SecurityAdminServiceClient;
import org.wso2.carbon.integration.common.admin.client.ServerAdminClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

import javax.xml.xpath.XPathExpressionException;

import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class JaggeryIntegrationTest {
    private static final Log log = LogFactory.getLog(JaggeryIntegrationTest.class);
    protected AutomationContext asServer;
    protected String sessionCookie;
    protected String backendURL;
    protected String webAppURL;
    protected SecurityAdminServiceClient securityAdminServiceClient;

    protected void init() throws Exception {
        asServer = new AutomationContext("Jaggery", TestUserMode.SUPER_TENANT_ADMIN);
        asServer.getDefaultInstance().getHosts();
        asServer.getContextTenant();
        //   sessionCookie = asServer.login();
        sessionCookie = new LoginLogoutClient(asServer).login();
        backendURL = asServer.getContextUrls().getBackEndUrl();
        webAppURL = asServer.getContextUrls().getWebAppURL();
    }

    protected void init(TestUserMode testUserMode) throws Exception {
        asServer = new AutomationContext("Jaggery", testUserMode);
        //  sessionCookie = asServer.login();
        backendURL = asServer.getContextUrls().getBackEndUrl();
        webAppURL = asServer.getContextUrls().getWebAppURL();
    }

    protected void init(String domainKey, String userKey) throws Exception {
        asServer = new AutomationContext("Jaggery", "appServerInstance0001", domainKey, userKey);
        //   sessionCookie = asServer.login();
        backendURL = asServer.getContextUrls().getBackEndUrl();
        webAppURL = asServer.getContextUrls().getWebAppURL();
    }

    protected String getServiceUrl(String serviceName) throws XPathExpressionException {
        return asServer.getContextUrls().getServiceUrl() + "/" + serviceName;
    }

    protected String getServiceUrlHttps(String serviceName) throws XPathExpressionException {
        return asServer.getContextUrls().getSecureServiceUrl() + "/" + serviceName;
    }

    protected void deployAarService(String serviceName, String fileNameWithExtension,
                                    String filePath, String serviceHierarchy)
            throws Exception {
        AARServiceUploaderClient aarServiceUploaderClient =
                new AARServiceUploaderClient(backendURL, sessionCookie);
        aarServiceUploaderClient.uploadAARFile(fileNameWithExtension, filePath, serviceHierarchy);
        ServiceDeploymentUtil.isServiceDeployed(backendURL, sessionCookie, serviceName);
        assertTrue(ServiceDeploymentUtil.
                        isServiceDeployed(backendURL, sessionCookie, serviceName),
                "Service file uploading failed withing given deployment time"
        );
    }

    protected void deleteService(String serviceName) throws RemoteException {
        ServiceAdminClient adminServiceService =
                new ServiceAdminClient(backendURL, sessionCookie);
        if (ServiceDeploymentUtil.isFaultyService(backendURL,
                sessionCookie, serviceName)) {
            adminServiceService.deleteFaultyServiceByServiceName(serviceName);
        } else if (ServiceDeploymentUtil.isServiceExist(backendURL,
                sessionCookie, serviceName)) {
            adminServiceService.deleteService(new String[]{adminServiceService.getServiceGroup(serviceName)});
        }
        ServiceDeploymentUtil.isServiceDeleted(backendURL, sessionCookie, serviceName);
    }

    protected void gracefullyRestartServer() throws Exception {
        //regenerate the context with super tenant domain
        AutomationContext adminContext = new AutomationContext("Jaggery", TestUserMode.SUPER_TENANT_ADMIN);
        ServerAdminClient serverAdminClient = new ServerAdminClient(backendURL,
                adminContext.getSuperTenant().getTenantAdmin().getUserName(),
                adminContext.getSuperTenant().getTenantAdmin().getPassword());
        serverAdminClient.restartGracefully();
    }

    protected boolean isServiceDeployed(String serviceName) throws RemoteException {
        return ServiceDeploymentUtil.isServiceDeployed(backendURL,
                sessionCookie, serviceName);
    }

    protected boolean isServiceFaulty(String serviceName) throws RemoteException {
        return ServiceDeploymentUtil.isServiceFaulty(backendURL,
                sessionCookie, serviceName);
    }

    protected String getSecuredServiceEndpoint(String serviceName) throws XPathExpressionException {
        return asServer.getContextUrls().getSecureServiceUrl() + "/" + serviceName;
    }

    protected void applySecurity(String scenarioNumber, String serviceName, String userGroup)
            throws RemoteException, InterruptedException, XPathExpressionException,
            SecurityAdminServiceSecurityConfigExceptionException {

        securityAdminServiceClient =
                new SecurityAdminServiceClient(backendURL, sessionCookie);

        String keyStorePath = FrameworkPathUtil.getSystemResourceLocation()
                + asServer.getConfigurationValue("//keystore/fileName/text()");

        String keyStoreName = new File(keyStorePath).getName();

        securityAdminServiceClient.applySecurity(serviceName, scenarioNumber, new String[]{userGroup},
                new String[]{keyStoreName}, keyStoreName);

        Thread.sleep(2000);//wait for security application
    }

    protected void cleanup() {
        asServer = null;
    }

    public static void main(String[] args) {
    }
}

