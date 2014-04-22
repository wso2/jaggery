package org.wso2.jaggery.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.jarservices.stub.DuplicateServiceExceptionException;
import org.wso2.carbon.jarservices.stub.DuplicateServiceGroupExceptionException;
import org.wso2.carbon.jarservices.stub.JarServiceCreatorAdminStub;
import org.wso2.carbon.jarservices.stub.JarUploadExceptionException;
import org.wso2.carbon.jarservices.stub.types.Resource;
import org.wso2.carbon.jarservices.stub.types.Service;
import org.wso2.carbon.jarservices.stub.types.UploadArtifactsResponse;

import javax.activation.DataHandler;
import java.rmi.RemoteException;
import java.util.List;

public class JARServiceUploaderClient {

    private static final Log log = LogFactory.getLog(JARServiceUploaderClient.class);

    private JarServiceCreatorAdminStub jarServiceCreatorAdminStub;
    private final String serviceName = "JarServiceCreatorAdmin";

    public JARServiceUploaderClient(String backEndUrl, String sessionCookie) throws AxisFault {

        String endPoint = backEndUrl + serviceName;
        jarServiceCreatorAdminStub = new JarServiceCreatorAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, jarServiceCreatorAdminStub);
    }

    public JARServiceUploaderClient(String backEndUrl, String userName, String password) throws AxisFault {

        String endPoint = backEndUrl + serviceName;
        jarServiceCreatorAdminStub = new JarServiceCreatorAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, jarServiceCreatorAdminStub);
    }

    public void uploadJARServiceFile(String serviceGroup,
                                     List<DataHandler> dhJarList, DataHandler dhWsdl)
            throws JarUploadExceptionException, RemoteException,
            DuplicateServiceGroupExceptionException,
            DuplicateServiceExceptionException {

        Resource resourceData;
        Resource resourceDataWsdl;
        UploadArtifactsResponse uploadArtifactsResponse;


        Resource[] resourceDataArray = new Resource[dhJarList.size()];

        assert dhJarList.size() != 0;
        for (int i = 0; dhJarList.size() > i; i++) {
            resourceData = new Resource();
            resourceData.setFileName(dhJarList.get(i).getName().substring(dhJarList.get(i).getName().lastIndexOf('/') + 1));
            resourceData.setDataHandler(dhJarList.get(i));
            resourceDataArray[i] = resourceData;
        }

        if (dhWsdl != null) {
            resourceDataWsdl = new Resource();
            resourceDataWsdl.setFileName(dhWsdl.getName().substring(dhWsdl.getName().lastIndexOf('/') + 1));
            resourceDataWsdl.setDataHandler(dhWsdl);
        } else {
            resourceDataWsdl = null;
        }

        uploadArtifactsResponse = jarServiceCreatorAdminStub.upload(serviceGroup, resourceDataWsdl,
                resourceDataArray);

        Service[] service = uploadArtifactsResponse.getServices();
        for (Service temp : service) {
            temp.setUseOriginalWsdl(false);
            temp.setDeploymentScope("request");
        }

        jarServiceCreatorAdminStub.createAndDeployService(uploadArtifactsResponse.getResourcesDirPath(), "", serviceGroup, service);
        log.info("Artifact uploaded");

    }
}
