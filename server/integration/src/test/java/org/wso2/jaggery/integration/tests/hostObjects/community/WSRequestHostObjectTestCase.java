/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.jaggery.integration.tests.hostObjects.community;

import org.custommonkey.xmlunit.XMLAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.jaggery.integration.tests.wsmock.MockServiceImpl;
import org.xml.sax.SAXException;

import javax.xml.ws.Endpoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Test cases for Database Host Object
 */
public class WSRequestHostObjectTestCase {

    private Endpoint ep;

    @BeforeClass
    public void startService(){
        ep = Endpoint.publish("http://localhost:9960/ws/mock", new MockServiceImpl());
    }

    @AfterClass
    public void endService(){
        if (ep != null && ep.isPublished()){
            ep.stop();
        }
    }

    @Test(groups = {"jaggery"},
            description = "Test for WSRequest host object")
    public void testWSRequestExist() {
        ClientConnectionUtil.waitForPort(9763);

        String finalOutput = null;

        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/wsrequest.jag");
            URLConnection jaggeryServerConnection = jaggeryURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    jaggeryServerConnection.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                finalOutput = inputLine;
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assertNotNull(finalOutput, "Result cannot be null");
        }

    }

    @Test(groups = {"jaggery"},
            description = "Test for WSRequest host object")
    public void testWSRequest() throws IOException, SAXException {
        ClientConnectionUtil.waitForPort(9763);

        String finalOutput = "";

        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/wsrequest.jag");
            URLConnection jaggeryServerConnection = jaggeryURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    jaggeryServerConnection.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                finalOutput += inputLine;
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            XMLAssert.assertXMLEqual(finalOutput, "<ns2:addIntResponse xmlns:ns2=\"http://wsmock.tests.integration.jaggery.wso2.org/\">  <return>4</return></ns2:addIntResponse>");
        }

    }

    @Test(groups = {"jaggery"},
            description = "Test WSRequest status")
    public void testWSRequestOperations() {
        ClientConnectionUtil.waitForPort(9763);

        String finalOutput = "";

        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/wsrequest.jag?action=state");
            URLConnection jaggeryServerConnection = jaggeryURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    jaggeryServerConnection.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                finalOutput += inputLine;
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assertEquals(finalOutput, "014 success");
        }

    }

    @Test(groups = {"jaggery"}, description = "Test WSRequest Parallel Invocations")
    public void testWSRequestParallel() throws InterruptedException {
        ClientConnectionUtil.waitForPort(9763);

        final Map<String, String> results = new HashMap<String, String>();
        int threads = 10;
        for (int i = 0; i < threads; i++) {
            final int finalI = i;
            results.put("status" + finalI, "active");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String finalOutput = "";
                    try {
                        URL jaggeryURL = new URL("http://localhost:9763/testapp/wsrequest.jag?action=state");
                        URLConnection jaggeryServerConnection = jaggeryURL.openConnection();
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                jaggeryServerConnection.getInputStream()));

                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            finalOutput += inputLine;
                        }

                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        results.put("status" + finalI, "error");
                    } finally {
                        results.put("status" + finalI, finalOutput.contains("014 success") ? "success" : "error");
                    }
                }
            }).start();
        }
        int sleepInterval = 1000;
        int totalTime = 30 * sleepInterval;
        int time = 0;
        L1:
        while (true) {
            Thread.sleep(sleepInterval);
            time += sleepInterval;
            for (String value : results.values()) {
                if ("active".equals(value)) {
                    continue L1;
                }
                if ("error".equals(value) || time > totalTime) {
                    fail("Error while parallel service invocation via WSRequest()");
                    break L1;
                }
            }
            break;
        }
    }

    @Test(groups = {"jaggery"},
            description = "Test for in-only WSRequest")
    public void testWSRequestInOnly() {
        ClientConnectionUtil.waitForPort(9763);

        String finalOutput = null;

        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/wsrequest.jag?action=in-only");
            URLConnection jaggeryServerConnection = jaggeryURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    jaggeryServerConnection.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                finalOutput = inputLine;
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assertEquals(finalOutput,"null","In only message's return should always should be 'null'");
        }

    }

    @Test(groups = {"jaggery"},
            description = "Test for in-only WSRequest")
    public void testWSRequestInOnlyRobust() {
        ClientConnectionUtil.waitForPort(9763);

        String finalOutput = null;

        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/wsrequest.jag?action=robust-in-only");
            URLConnection jaggeryServerConnection = jaggeryURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    jaggeryServerConnection.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                finalOutput = inputLine;
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assertEquals(finalOutput, "org.apache.axis2.AxisFault: Mock error",
                    "Robust invocation should throw an error");
        }

    }

}
