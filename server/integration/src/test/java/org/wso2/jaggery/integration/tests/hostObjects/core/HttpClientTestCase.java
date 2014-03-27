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

package org.wso2.jaggery.integration.tests.hostObjects.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static org.testng.Assert.assertNotNull;

/**
 * Test cases for HTTP client (GET/PUT/POST/DELETE)
 */
public class HttpClientTestCase {

	// for testing get

	@Test(groups = { "jaggery" }, description = "Test Http Client GET object")
	public void testHttpClientGet() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/get.jag");
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

	@Test(groups = { "jaggery" }, description = "Test Http Client GET operation ")
	public void testHttpClientGetOperations() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/get.jag");
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
			JSONAssert
					.assertEquals(
							"{\"data\" : [{\"type\" : \"GET\", \"name\" : \"Test\"}], \"xhr\" : {}}",
							finalOutput, true);
		}

	}

	@Test(groups = { "jaggery" }, description = "Test Http Client GET operation ")
	public void testHttpClientGetParameters() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/get.jag?action=parameters");
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
			JSONObject obj = new JSONObject();
			JSONObject emptyObj = new JSONObject();
			obj.put("type", "GET");
			obj.put("name", "Test parameters");
			JSONObject objout = new JSONObject();
			objout.put("data", obj);
			objout.put("xhr", emptyObj);
			JSONObject finalobj = new JSONObject(finalOutput);			
			JSONAssert.assertEquals(finalobj, objout, true);
		}

	}

	// end for get;

	// for testing post

	@Test(groups = { "jaggery" }, description = "Test Http Client POST object")
	public void testHttpClientPost() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/post.jag");
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

	@Test(groups = { "jaggery" }, description = "Test Http Client POST operation ")
	public void testHttpClientPostOperations() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/post.jag");
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
			
			JSONAssert.assertEquals("{\"data\" : [{\"type\" : \"POST\", \"name\" : \"Test\"}], \"xhr\" : {}}", finalOutput, true);
		}

	}

	@Test(groups = { "jaggery" }, description = "Test Http Client POST operation with params")
	public void testHttpClientPostParameters() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/post.jag?action=parameters");
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
			JSONAssert
					.assertEquals(
							
									"{\"data\" : {\"type\" : \"POST\", \"name\" : \"Test parameters\"}, \"xhr\" : {}}",
							finalOutput, true);
		}

	}

	// end for the post

	// for testing put

	@Test(groups = { "jaggery" }, description = "Test Http Client PUT object")
	public void testHttpClientPut() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/put.jag");
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

	@Test(groups = { "jaggery" }, description = "Test Http Client PUT operation ")
	public void testHttpClientPutOperations() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/put.jag");
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
			JSONArray obj = new JSONArray();
			JSONObject obj1 = new JSONObject();
			JSONObject emptyObj = new JSONObject();
			obj1.put("type", "PUT");
			obj1.put("name", "Test");
			obj.put(obj1);
			JSONObject objoutx = new JSONObject();
			objoutx.put("data", obj);
			objoutx.put("xhr", emptyObj);
			String jsonFinalOutputString = finalOutput.replace("\\", "");
			jsonFinalOutputString = jsonFinalOutputString.replace("\"[", "[");
			jsonFinalOutputString = jsonFinalOutputString.replace("]\"", "]");			
			JSONObject finalobj = new JSONObject(jsonFinalOutputString);			
			JSONAssert.assertEquals(objoutx, finalobj, true);
		}

	}

	

	@Test(groups = { "jaggery" }, description = "Test Http Client PUT operation with params")
	public void testHttpClientPutParameters() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/put.jag?action=parameters");
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
			JSONArray obj = new JSONArray();
			JSONObject emptyObj = new JSONObject();
			JSONObject obj1 = new JSONObject();
			obj1.put("type", "PUT");
			obj1.put("name", "Test parameters");
			obj.put(obj1);
			JSONObject objout = new JSONObject();
			objout.put("data", obj1);
			objout.put("xhr", emptyObj);
			JSONObject finalobj = new JSONObject(finalOutput);
			JSONAssert.assertEquals(finalobj, objout, true);
		}

	}

	// end for the put

	// for testing delete (del)

	@Test(groups = { "jaggery" }, description = "Test Http Client DEL object")
	public void testHttpClientDel() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/delet.jag");
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

	@Test(groups = { "jaggery" }, description = "Test Http Client Del operation ")
	public void testHttpClientDelOperations() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/delet.jag");
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
			JSONArray obj = new JSONArray();
			JSONObject emptyObj = new JSONObject();
			JSONObject obj1 = new JSONObject();
			obj1.put("type", "DELETE");
			obj1.put("name", "Test");
			obj.put(obj1);
			JSONObject objout = new JSONObject();
			objout.put("data", obj);
			objout.put("xhr", emptyObj);
			String jsonFinalOutputString = finalOutput.replace("\\", "");
			jsonFinalOutputString = jsonFinalOutputString.replace("\"[", "[");
			jsonFinalOutputString = jsonFinalOutputString.replace("]\"", "]");			
			JSONObject finalobj = new JSONObject(jsonFinalOutputString);
			JSONAssert.assertEquals(finalobj, objout, true);

		}

	}

	@Test(groups = { "jaggery" }, description = "Test Http Client DEL operation with params")
	public void testHttpClientDelParameters() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/delet.jag?action=parameters");
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
			JSONAssert
					.assertEquals(
							new JSONObject(
									"{\"data\" : {\"type\" : \"DELETE\", \"name\" : \"Test parameters\"}, \"xhr\" : {}}"),
							new JSONObject(finalOutput), true);
		}

	}

	// end for the DEL

}
