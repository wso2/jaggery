package org.wso2.jaggery.integration.tests.wsmock;

import javax.jws.WebService;

@WebService(
        serviceName = "AddService",
        endpointInterface = "org.wso2.jaggery.integration.tests.wsmock.AddService")
public class AddServiceImpl implements AddService {
    @Override
    public int addInt(int i, int j) {
        return i + j;
    }
}
