package org.jaggeryjs.integration.common.clients;

import javax.activation.DataHandler;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.app.mgt.stub.JaggeryAppAdminStub;
import org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData;

public class JaggeryApplicationUploaderClient {
    private static final Log log = LogFactory.getLog(JaggeryApplicationUploaderClient.class);
    private JaggeryAppAdminStub jaggeryAppAdminStub;
    private final String serviceName = "JaggeryAppAdmin";

    public JaggeryApplicationUploaderClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        try {
            jaggeryAppAdminStub = new JaggeryAppAdminStub(endPoint);
            AuthenticateStubUtil.authenticateStub(sessionCookie, jaggeryAppAdminStub);
        } catch (AxisFault axisFault) {
            log.error("JaggeryAppAdminStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("JaggeryAppAdminStub Initialization fail " + axisFault.getMessage());
        }
    }

    public void uploadJaggeryFile(String fileName, String filePath) throws Exception {
        WebappUploadData webappUploadData = new WebappUploadData();
        webappUploadData.setFileName(fileName);
        webappUploadData.setDataHandler(createDataHandler(filePath));
        jaggeryAppAdminStub.uploadWebapp(new WebappUploadData[]{webappUploadData});// uploads to server

    }

    private DataHandler createDataHandler(String filePath) throws MalformedURLException {
        URL url;
        try {
            url = new URL("file://" + filePath);
        } catch (MalformedURLException e) {
            log.error("File path URL is invalid" + e);
            throw new MalformedURLException("File path URL is invalid" + e);
        }
        return new DataHandler(url);
    }

}
