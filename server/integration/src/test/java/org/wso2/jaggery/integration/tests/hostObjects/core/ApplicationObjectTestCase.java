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
public class ApplicationObjectTestCase {

    @Test(groups = {"jaggery"},
          description = "Test application object")
    public void testApplication() {
        ClientConnectionUtil.waitForPort(9763);
        
        String finalOutput = null;
        
        try {
        	URL jaggeryURL = new URL("http://localhost:9763/testapp/application.jag");
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
            description = "Test application operations")
    public void testApplicationOperations() {
        ClientConnectionUtil.waitForPort(9763);
        
        String finalOutput = null;
        
        try {
        	URL jaggeryURL = new URL("http://localhost:9763/testapp/application.jag");
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
			assertEquals(finalOutput, "test jaggery application value");
		}
        
    }
    
    @Test(groups = {"jaggery"},
            description = "Test application object serve function")
      public void testApplicationServe() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/");
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
            description = "Test application object serve function Qury")
      public void testApplicationServeQury() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/tests/test1/house/?action=url");
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
            description = "Test application object serve function URL mapping")
      public void testApplicationServeRoute() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/tests/test1/house/?action=url");
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
  			assertEquals(finalOutput, "http://localhost:9763/testapp2/tests/test1/house/");
  		}
          
      }
    
    @Test(groups = {"jaggery"},
            description = "Test application object - Request Query")
      public void testApplicationQuery() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/?action=query");
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
  			assertEquals(finalOutput, "action=query");
  		}
          
      }
    
    @Test(groups = {"jaggery"},
            description = "Test application request url")
      public void testApplicationRequestURL() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/?action=url");
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
  			assertEquals(finalOutput, "http://localhost:9763/testapp2/");
  		}
          
      }
    
    @Test(groups = {"jaggery"},
            description = "Test application request secure")
      public void testApplicationRequestSecure() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/?action=secure");
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
            description = "Test application request port")
      public void testApplicationRequestPort() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/?action=port");
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
  			assertEquals(finalOutput, "9763");
  		}
          
      }
    
    @Test(groups = {"jaggery"},
            description = "Test application object response")
      public void testApplicationResponse() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/?action=response");
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
  			assertEquals(finalOutput, "My response content");
  		}
          
      }
    
    @Test(groups = {"jaggery"},
            description = "Test application object session put")
      public void testSessionPut() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/?action=sessionset");
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
  			assertEquals(finalOutput, "session testing.");
  		}
          
      }
    
    @Test(groups = {"jaggery"},
            description = "Test application object Session get")
      public void testSessionGet() {
          ClientConnectionUtil.waitForPort(9763);
          
          String finalOutput = null;
          
          try {
          	URL jaggeryURL = new URL("http://localhost:9763/testapp2/?action=sessionget");
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
  			assertEquals(finalOutput, "test me");
  		}
          
      }

}
