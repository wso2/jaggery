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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test cases for Entry Host Object
 */
public class EntryHostObjectTestCase {


    private static final Log log = LogFactory.getLog(EntryHostObjectTestCase.class);


    @Test(groups = {"jaggery"},
            description = "Test entry hostobject")
    public void testFeed() {
        log.info("*****Inside Entry object test in jaggery Test*****");
        ClientConnectionUtil.waitForPort(9763);

        String finalOutput = null;

        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/entry.jag");
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
            description = "Test Entry hostobject toString")
    public void testFeedString() throws IOException, SAXException {
        ClientConnectionUtil.waitForPort(9763);

        String finalOutput = "";

        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/entry.jag");
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
            assertEquals(finalOutput.contains("<name>nuwan</name>"), true);
        }

    }


    @Test(groups = {"jaggery"},
            description = "Test Entry hostobject toXML")
    public void testFeedXML() throws IOException, SAXException {
        ClientConnectionUtil.waitForPort(9763);

        String finalOutput = "";

        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/entry.jag?action=xml");
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
            assertEquals(finalOutput.contains("<name>nuwan</name>"), true);
        }

    }

}
