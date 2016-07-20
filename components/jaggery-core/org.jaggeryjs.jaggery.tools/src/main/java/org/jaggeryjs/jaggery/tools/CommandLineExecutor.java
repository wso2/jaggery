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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.jaggery.core.manager.CommandLineManager;
import org.jaggeryjs.jaggery.core.manager.CommonManager;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.mozilla.javascript.ScriptableObject;

import java.io.*;

/**
 * Jaggery CommnadLineExecutor - this will parse file URLs or expressions by
 * runtime Jaggery engine
 */
final class CommandLineExecutor {

    private static PrintStream out = System.out;
    private static String DEFAULT_TENANTDOMAIN = "carbon.super";

    private CommandLineExecutor() {
        //disable external instantiation
    }

    /**
     * Parse Jaggery scripts resides in the file path
     *
     * @param fileURL url of the file
     */

	@SuppressFBWarnings("PATH_TRAVERSAL_IN")
    static void parseJaggeryScript(final String fileURL) {
		FileInputStream fstream = null;
        try{
            //Initialize the Rhino context
            RhinoEngine.enterGlobalContext();
            fstream = new FileInputStream(fileURL);
            final RhinoEngine engine = CommandLineManager.getCommandLineEngine();
            final ScriptableObject scope = engine.getRuntimeScope();
            //initialize JaggeryContext
            final JaggeryContext jaggeryContext = new JaggeryContext();
            jaggeryContext.setTenantDomain(DEFAULT_TENANTDOMAIN);
            jaggeryContext.setEngine(engine);
            jaggeryContext.setScope(scope);
            jaggeryContext.addProperty(CommonManager.JAGGERY_OUTPUT_STREAM, System.out);
            RhinoEngine.putContextProperty("jaggeryContext", jaggeryContext);
            //Parsing the script
            final Reader source = new ScriptReader(new BufferedInputStream(fstream));
            out.println("\n");
            ShellUtilityService.initializeUtilityServices();
            engine.exec(source, scope, null);
            ShellUtilityService.destroyUtilityServices();
            out.flush();
            out.println("\n");
        } catch (Exception e) {
            out.println("\n");
            out.println("Error: " + e.getMessage());
            out.println("\n");
        } finally {
			if (fstream != null){
				try {
					fstream.close();
				} catch (IOException ignored) {
				}
			}
		}

    }

    /**
     * Parse Jaggery expressions
     *
     * @param expression Jaggery expression string
     */
    static void parseJaggeryExpression(final String expression) {
        try {
            //Initialize the Rhino context
            RhinoEngine.enterGlobalContext();
            final RhinoEngine engine = CommandLineManager.getCommandLineEngine();
            final ScriptableObject scope = engine.getRuntimeScope();
            //initialize JaggeryContext
            final JaggeryContext jaggeryContext = new JaggeryContext();
            jaggeryContext.setTenantDomain("ca");
            jaggeryContext.setEngine(engine);
            jaggeryContext.setScope(scope);
            jaggeryContext.addProperty(CommonManager.JAGGERY_OUTPUT_STREAM, out);
            RhinoEngine.putContextProperty("jaggeryContext", jaggeryContext);
            //Parsing the script
            ShellUtilityService.initializeUtilityServices();
            engine.exec(new StringReader(expression), scope, null);
            ShellUtilityService.destroyUtilityServices();
            out.flush();
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }
}
