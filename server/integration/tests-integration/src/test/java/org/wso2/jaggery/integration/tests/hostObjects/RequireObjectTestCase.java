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

package org.wso2.jaggery.integration.tests.hostObjects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

/**
 * Test cases for Application Object
 */
public class RequireObjectTestCase {

    @Test(groups = {"jaggery"},
          description = "Test require object")
    public void testApplication() {
        ClientConnectionUtil.waitForPort(9763);
        
        String finalOutput = null;
        
        try {
        	URL jaggeryURL = new URL("http://localhost:9763/testapp/require.jag");
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
            description = "Test require object operations")
    public void testApplicationOperations() {
        ClientConnectionUtil.waitForPort(9763);
        
        String finalOutput = null;
        
        try {
        	URL jaggeryURL = new URL("http://localhost:9763/testapp/require.jag");
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
			assertEquals(finalOutput, "core require was success ");
		}
        
    }

}
