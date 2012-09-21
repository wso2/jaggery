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
 * Test cases for URI matcher Object
 */
public class URIObjectTestCase {

    @Test(groups = {"jaggery"},
          description = "Test URI object")
    public void testURI() {
        ClientConnectionUtil.waitForPort(9763);
        
        String finalOutput = null;
        
        try {
        	URL jaggeryURL = new URL("http://localhost:9763/testapp/uri.jag");
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
            description = "Test urlMappings config")
      public void testURIurlMappingsConfig() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp/uri/");
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
            description = "Test URI operations")
    public void testURIOperations() {
        ClientConnectionUtil.waitForPort(9763);
        
        String finalOutput = null;
        
        try {
        	URL jaggeryURL = new URL("http://localhost:9763/testapp/uri.jag");
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
			assertEquals(finalOutput, "dir0 element is : testapp ,page element is : uri.jag");
		}
        
    }
    
    @Test(groups = {"jaggery"},
            description = "Test URI operations for dir 4")
    public void testURIOperationsDir() {
        ClientConnectionUtil.waitForPort(9763);
        
        String finalOutput = null;
        
        try {
        	URL jaggeryURL = new URL("http://localhost:9763/testapp/uri/aa/bb/");
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
			assertEquals(finalOutput, "dir0 element is : testapp "
					+",dir1 element is : uri ,dir2 element is : aa "
					+",dir3 element is : bb");
		}
        
    }
    
    @Test(groups = {"jaggery"},
            description = "Test URI operations for PathInfor in request")
    public void testURIOperationsPathInfor() {
        ClientConnectionUtil.waitForPort(9763);
        
        String finalOutput = null;
        
        try {
        	URL jaggeryURL = new URL("http://localhost:9763/testapp/uri/test");
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
			assertEquals(finalOutput, "request getPathInfo : /test");
		}
        
    }

}
