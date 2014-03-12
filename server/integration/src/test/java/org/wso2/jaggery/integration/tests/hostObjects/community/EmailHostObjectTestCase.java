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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import javax.mail.MessagingException;

/**
 * Test cases for Email Host Object
 */
public class EmailHostObjectTestCase {

    @Test(groups = {"jaggery"},
            description = "Test a sample request and a response for E-mail host object")
    public void testEmail() {
        ClientConnectionUtil.waitForPort(9763);

        GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();

        String finalOutput = null;

        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/email.jag");
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
            greenMail.stop();
        }

    }

    @Test(groups = {"jaggery"},
            description = "Test a sample request and a response for E-mail host object")
    public void testSendEmail() {
        ClientConnectionUtil.waitForPort(9763);

        GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();

        String finalOutput = null;
        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/email.jag");
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
            assertEquals(finalOutput, "email successfully sent");
            greenMail.stop();
        }

    }

    @Test(groups = {"jaggery"},
            description = "Test a sample request and a subject of the response for E-mail host object")
    public void testSendEmailSubject() {
        ClientConnectionUtil.waitForPort(9763);

        GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();

        String subject = null;
        try {
            URL jaggeryURL = new URL("http://localhost:9763/testapp/email.jag");
            URLConnection jaggeryServerConnection = jaggeryURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    jaggeryServerConnection.getInputStream()));
            in.close();
            subject = greenMail.getReceivedMessages()[0].getSubject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {
            assertEquals(subject, "Test Subject");
            greenMail.stop();
        }

    }

}
