package org.wso2.jaggery.integration.tests.wsmock;

import javax.jws.WebService;

@WebService(
        serviceName = "MockService",
        endpointInterface = "org.wso2.jaggery.integration.tests.wsmock.MockService")
public class MockServiceImpl implements MockService {

    @Override
    public int addInt(int i, int j) {
        return i + j;
    }

    @Override
    public void error() {
        throw new RuntimeException("Mock error");
    }
}
