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

import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test cases for Process Object
 */
public class ProcessObjectTestCase {

	@Test(groups = { "jaggery" }, description = "Test Process object")
	public void testProcess() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/process.jag");
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

	@Test(groups = { "jaggery" }, description = "Test Process object Operation getEnv")
	public void testGetEnvProcessParams() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/process.jag?action=getEnv");
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
			String javaHome = System.getProperty("java.home");
			String pathSeparator = System.getProperty("path.separator");
			String javaHomeStr = finalOutput+""+pathSeparator+"jre";
			assertEquals(javaHomeStr.length(), javaHome.length());
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Test Process object for getEnvs function")
	public void testGetEnvsProcessParams() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/process.jag?action=getEnvs");
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
			assertEquals(finalOutput, "true");
		}

	}

	@Test(groups = { "jaggery" }, description = "Test Process object setProperty for first")
	public void testSetNewPropertyProcess() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/process.jag?action=setProperty");
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
			assertEquals(
					finalOutput,"null");
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Test Process object setProperty2")
	public void testSetPropertyProcess() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/process.jag?action=setProperty2");
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
			assertEquals(
					finalOutput,"testname");
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Test Process object getProperty")
	public void testGetPropertyProcess() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/process.jag?action=getProperty");
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
			assertEquals(
					finalOutput,"null");
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Test Process object getProperties")
	public void testGetPropertiesProcess() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/process.jag?action=getProperties");
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
			assertEquals(
					finalOutput,"portOffset 0");
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Test Process object getProperties2")
	public void testGetPropertiesAllProcess() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/process.jag?action=getProperties2");
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
			assertEquals(
					finalOutput,"true");
		}

	}

}
