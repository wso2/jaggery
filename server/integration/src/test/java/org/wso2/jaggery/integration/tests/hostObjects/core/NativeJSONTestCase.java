/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test cases for Request Object
 */
public class NativeJSONTestCase {

	@Test(groups = { "jaggery" }, description = "Native JSON Test")
	public void testRequest() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL("http://localhost:9763/testapp/nativejson.jag");
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

	@Test(groups = { "jaggery" }, description = "Test JSON resource file")
	public void testReadRequestParams() {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/nativejson.jag?action=testing");
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
			assertEquals(finalOutput, "print testing");
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Testing jsonObject")
	public void testJSONObject() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/nativejson.jag?action=jsonObject");
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
			JSONObject objout = new JSONObject();
			objout.put("jsonrpc", "2.0");

			JSONObject finalobj = new JSONObject(finalOutput);			
			JSONAssert.assertEquals(finalobj, objout, true);
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Testing json Number Object")
	public void testJSONNumberObject() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/nativejson.jag?action=jsonNumberObject");
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
			JSONObject objout = new JSONObject();
			objout.put("id",1 );
			String expected = "{id:1,count:5.3}";
			//objout.put("count", "5.3");
		
			JSONObject finalobj = new JSONObject(finalOutput);			
			JSONAssert.assertEquals(expected, finalOutput, true);
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Testing json Array")
	public void testJSONArray() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/nativejson.jag?action=jsonArray");
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
			JSONObject objout = new JSONObject();
			JSONArray objArr = new JSONArray();
			objArr.put(42);
			objArr.put(23);
			objout.put("params", objArr);

			JSONObject finalobj = new JSONObject(finalOutput);			
			JSONAssert.assertEquals(finalobj, objout, true);
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Testing json Object Array")
	public void testJSONObjectArray() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/nativejson.jag?action=jsonObjectArray");
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
			
			JSONObject objout = new JSONObject();
			objout.put("jsonrpc", "2.0");
			JSONObject objout1 = new JSONObject();
			objout1.put("jsonrpcOld", "1.2");
			JSONArray objArr = new JSONArray();
			objArr.put(objout);
			objArr.put(objout1);
			//objout.put("params", objArr);

			JSONArray finalobj = new JSONArray(finalOutput);			
			JSONAssert.assertEquals(finalobj, objArr, false);
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Testing jsonObjectNumberArray")
	public void testJSONObjectNumberArray() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/nativejson.jag?action=jsonObjectNumberArray");
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
			JSONObject objout = new JSONObject();
			objout.put("jsonrpc", 2.0);
			JSONObject objout1 = new JSONObject();
			objout1.put("jsonrpcOld", 1.2);
			JSONArray objArr = new JSONArray();
			objArr.put(objout);
			objArr.put(objout1);
			//objout.put("params", objArr);
			System.out.println(finalOutput);
			JSONArray finalobj = new JSONArray(finalOutput);			
			JSONAssert.assertEquals(finalobj, objArr, false);
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Testing json Object spec01")
	public void testJSONspec01() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/nativejson.jag?action=spec01");
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
			JSONObject objout = new JSONObject();
			JSONArray objArr = new JSONArray();
			objArr.put(42);
			objArr.put(23);
			objout.put("jsonrpc", "2.0");
			objout.put("method", "subtract");
			objout.put("id", 1);
			objout.put("params",objArr);

			JSONObject finalobj = new JSONObject(finalOutput);			
			JSONAssert.assertEquals(finalobj, objout, true);
		}

	}
	
	@Test(groups = { "jaggery" }, description = "Testing json Object spec05")
	public void testJSONspec05() throws JSONException {
		ClientConnectionUtil.waitForPort(9763);

		String finalOutput = null;

		try {
			URL jaggeryURL = new URL(
					"http://localhost:9763/testapp/nativejson.jag?action=spec05");
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
			JSONObject objout = new JSONObject();
			JSONObject objout2 = new JSONObject();
			JSONArray objArr = new JSONArray();
			
			objout.put("precision", "zip");
			objout.put("Latitude", 37.7668);
			objout.put("Longitude", -122.3959);
			objout.put("Address", "");
			objout.put("City", "SAN FRANCISCO");
			objout.put("State","CA");
			objout.put("Zip", "94107");
			objout.put("Country","US");
			
			objout2.put("precision", "zip");
			objout2.put("Latitude", 37.371991);
			objout2.put("Longitude", -122.02602);
			objout2.put("Address", "");
			objout2.put("City", "SUNNYVALE");
			objout2.put("State","CA");
			objout2.put("Zip", "94085");
			objout2.put("Country","US");
			
			objArr.put(objout);
			objArr.put(objout2);	

			JSONArray finalobj = new JSONArray(finalOutput);			
			JSONAssert.assertEquals(finalobj, objArr, true);
		}

	}
}
