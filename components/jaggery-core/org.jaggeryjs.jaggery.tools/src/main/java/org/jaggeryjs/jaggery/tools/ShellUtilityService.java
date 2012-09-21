/*
 * Copyright 2012 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jaggeryjs.jaggery.tools;

import java.sql.SQLException;

import org.wso2.carbon.h2.osgi.console.H2DatabaseManager;

/**
*
* This will include the utility services needed for command line client
* 
*/
public final class ShellUtilityService {

	private ShellUtilityService() {
	    //disable external instantiation
	}
	
	/**
     * Initialize utility services for command line client
     * @throws SQLException 
     *
     */
	protected static void initializeUtilityServices () throws SQLException {
		
		//H2 database
		final H2DatabaseManager databaseManager = H2DatabaseManager.getInstance();
		databaseManager.initialize();
	}
	
    /**
     * Destroy utility services for command line client
     *
     */
	protected static void destroyUtilityServices () {
		
		//H2 database
		final H2DatabaseManager databaseManager = H2DatabaseManager.getInstance();
		databaseManager.terminate();
	}
}
