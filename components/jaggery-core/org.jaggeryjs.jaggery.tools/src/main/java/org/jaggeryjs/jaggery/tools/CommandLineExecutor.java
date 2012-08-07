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

import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.jaggery.core.manager.CommandLineManager;
import org.jaggeryjs.jaggery.core.manager.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.mozilla.javascript.ScriptableObject;

import java.io.*;

/**
*
* Jaggery CommnadLineExecutor - this will parse file URLs or expressions by 
* runtime Jaggery engine
*/
public final class CommandLineExecutor {

	private static PrintStream out = System.out;
    
	private CommandLineExecutor() {
	    //disable external instantiation
	}
	/**
     * Parse Jaggery scripts resides in the file path
     *
     * @param fileURL url of the file
     */
	public static void parseJaggeryScript(final String fileURL) {

        try{

            //Initialize the Rhino context
            RhinoEngine.enterGlobalContext();
			final FileInputStream fstream = new FileInputStream(fileURL);
            
			final RhinoEngine engine = CommandLineManager.getCommandLineEngine();
			final ScriptableObject scope = engine.getRuntimeScope();
        	
        	//initialize JaggeryContext
			final JaggeryContext jaggeryContext = new JaggeryContext();
        	jaggeryContext.setTenantId("0");
        	jaggeryContext.setOutputStream(System.out);
        	jaggeryContext.setEngine(engine);
        	jaggeryContext.setScope(scope);
        	
        	RhinoEngine.putContextProperty("jaggeryContext", jaggeryContext);

            //Parsing the script
        	final Reader source = new ScriptReader(new BufferedInputStream(fstream));
            out.println("\n");
            ShellUtilityService.initializeUtilityServices();
            engine.exec(source, scope, null);
            ShellUtilityService.destroyUtilityServices();
            out.flush();
            out.println("\n");
		}catch (Exception e){
			out.println("\n");
			out.println("Error: " + e.getMessage());
			out.println("\n");
		}

	}

    /**
     * Parse Jaggery expressions
     *
     * @param expression Jaggery expression string
     */
	public static void parseJaggeryExpression(final String expression) {

        try{

        	//Initialize the Rhino context
            RhinoEngine.enterGlobalContext();

            final RhinoEngine engine = CommandLineManager.getCommandLineEngine();
            final ScriptableObject scope = engine.getRuntimeScope();
        	
        	//initialize JaggeryContext
            final JaggeryContext jaggeryContext = new JaggeryContext();
        	jaggeryContext.setTenantId("0");
        	jaggeryContext.setOutputStream(out);
        	jaggeryContext.setEngine(engine);
        	jaggeryContext.setScope(scope);
        	
        	RhinoEngine.putContextProperty("jaggeryContext", jaggeryContext);

        	//Parsing the script
        	ShellUtilityService.initializeUtilityServices();
            engine.exec(new StringReader(expression), scope, null);
            ShellUtilityService.destroyUtilityServices();
            out.flush();
		}catch (Exception e){
			out.println("Error: " + e.getMessage());
		}

	}

}
