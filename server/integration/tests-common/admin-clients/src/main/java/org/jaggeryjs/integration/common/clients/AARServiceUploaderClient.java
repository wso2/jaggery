package org.jaggeryjs.integration.common.clients;

import javax.activation.DataHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.aarservices.stub.ExceptionException;
import org.wso2.carbon.aarservices.stub.ServiceUploaderStub;
import org.wso2.carbon.aarservices.stub.types.carbon.AARServiceData;

public class AARServiceUploaderClient {
    private static final Log log = LogFactory.getLog(AARServiceUploaderClient.class);

    private ServiceUploaderStub serviceUploaderStub;
    private final String serviceName = "ServiceUploader";

    public AARServiceUploaderClient(String backEndUrl, String sessionCookie) throws AxisFault {

        String endPoint = backEndUrl + serviceName;
        try {
            serviceUploaderStub = new ServiceUploaderStub(endPoint);
            AuthenticateStubUtil.authenticateStub(sessionCookie, serviceUploaderStub);
        } catch (AxisFault axisFault) {
            log.error("ServiceUploaderStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("ServiceUploaderStub Initialization fail " + axisFault.getMessage());
        }
    }

    public AARServiceUploaderClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        try {
            serviceUploaderStub = new ServiceUploaderStub(endPoint);
            AuthenticateStubUtil.authenticateStub(userName, password, serviceUploaderStub);
        } catch (AxisFault axisFault) {
            log.error("ServiceUploaderStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("ServiceUploaderStub Initialization fail " + axisFault.getMessage());
        }
    }

    public void uploadAARFile(String fileName, String filePath,
                              String serviceHierarchy)
            throws ExceptionException, RemoteException, MalformedURLException {
        AARServiceData aarServiceData;

        aarServiceData = new AARServiceData();
        aarServiceData.setFileName(fileName);
        aarServiceData.setDataHandler(createDataHandler(filePath));
        aarServiceData.setServiceHierarchy(serviceHierarchy);
        serviceUploaderStub.uploadService(new AARServiceData[]{aarServiceData});
    }

    private DataHandler createDataHandler(String filePath) throws MalformedURLException {
        URL url = null;
        try {
            url = new URL("file://" + filePath);
        } catch (MalformedURLException e) {
            log.error("File path URL is invalid" + e);
            throw new MalformedURLException("File path URL is invalid" + e);
        }
        DataHandler dh = new DataHandler(url);
        return dh;
    }
}
