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

import java.io.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

/**
 *
 * Jaggery shell to execute expressions
 */
public class JaggeryShell extends ScriptableObject
{

    /**
	 * serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Log LOG = LogFactory.getLog(JaggeryShell.class);
	
	private static final String CMD_QUIT = "quit";
	private static final String CMD_HELP = "help";
	private static final String CMD_VERSION = "version";
	
	private boolean quitting;

	private static PrintStream out = System.out;
	
	public boolean isQuitting() {
		return quitting;
	}

	public void setQuitting(final boolean quitting) {
		this.quitting = quitting;
	}
	
	@Override
    public String getClassName()
    {
        return "CommandLineClient";
    }

    /**
     * Enter shell context.
     *
     * @param args argument list of parameters
     * Will need to send empty array to execute expressions.
     */
    public static void enterContext(final String args[]) {
        // Associate a new Context with this thread
        final Context context = ContextFactory.getGlobal().enterContext();
        try {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed.
            final JaggeryShell shell = new JaggeryShell();
            context.initStandardObjects(shell);

            // Define some global functions particular to the shell. Note
            // that these functions are not part of ECMA.
            final String[] names = {CMD_QUIT, CMD_VERSION, CMD_HELP};
            shell.defineFunctionProperties(names, JaggeryShell.class,
                                           ScriptableObject.DONTENUM);

            final String [] optionArgs = processOptions(context, args);

            // Set up "arguments" in the global scope to contain the command
            // line arguments after the name of the script to execute
            Object[] array;
            if (optionArgs.length == 0) {
                array = new Object[0];
            } else {
            	final int length = optionArgs.length - 1;
                array = new Object[length];
                System.arraycopy(optionArgs, 1, array, 0, length);
            }
            final Scriptable argsObj = context.newArray(shell, array);
            shell.defineProperty("arguments", argsObj,
                                 ScriptableObject.DONTENUM);

            shell.processSource(context, optionArgs.length == 0 ? null : optionArgs[0]);
        } finally {
            Context.exit();
        }
    }

    /**
     * Parse arguments.
     * @param cx context
     * @param args argument list of parameters
     */
    public static String[] processOptions(final Context context, final String args[]) {
        for (int i=0; i < args.length; i++) {
            final String arg = args[i];
            if (!arg.startsWith("-")) {
                String[] result = new String[args.length - i];
                for (int j=i; j < args.length; j++) {
                    result[j-i] = args[j];
                }
                return result;
            }
            usage(arg);
        }
        return new String[0];
    }

    /**
     * Print a usage message.
     * @param string to print
     */
    private static void usage(String passedString) {
    	printToConsole("Invalid arguments. Cannot recognize \"" + passedString + "\".");
    	help();
    }

    /**
     * Print Jaggery help messages.
     */
    public static void help() {
    	printToConsole("");
    	printToConsole("Command                 Description");
        printToConsole("=======                 ===========");
        printToConsole(CMD_HELP + "                	Display Jaggery help messages");
        printToConsole(CMD_QUIT + "	                Quit Jaggery command line client");
        printToConsole(CMD_VERSION + "		Get Jaggery version");
        printToConsole("");
    }

    /**
     * Print the string values of its arguments.
     *
     * This method is defined as a JavaScript function.
     * Note that its arguments are of the "varargs" form, which
     * allows it to handle an arbitrary number of arguments
     * supplied to the JavaScript function.
     *
     */
    public static void execute(Context context, Scriptable thisObj,
                             Object[] args, Function funObj)
    {
        for (int i=0; i < args.length; i++) {
            if (i > 0) {
                out.print(" ");
            }

            // Convert the arbitrary JavaScript value into a string form.
            String contextString = Context.toString(args[i]);

            out.print(contextString);
        }
        out.println();
    }

    /**
     * Quit the shell.
     *
     * This only affects the interactive mode.
     *
     * This method is defined as a JavaScript function.
     */
    public void quit()
    {
        quitting = true;
    }

    /**
     * Get and set the language version.
     *
     * This method is defined as a JavaScript function.
     */
    public static String version()
    {
        return "Jaggery version is " + JaggeryShell.class.getPackage().getImplementationVersion();
    }

    /**
     * Load and execute a set of JavaScript source files.
     *
     * This method is defined as a JavaScript function.
     *
     */
    public static void load(Context cx, Scriptable thisObj,
                            Object[] args, Function funObj)
    {
        JaggeryShell shell = (JaggeryShell)getTopLevelScope(thisObj);
        for (int i = 0; i < args.length; i++) {
            shell.processSource(cx, Context.toString(args[i]));
        }
    }


    /**
     * Evaluate JavaScript source.
     *
     * @param cx the current context
     * @param filename the name of the file to compile, or null
     *                 for interactive mode.
     */
	@SuppressFBWarnings("PATH_TRAVERSAL_IN")
    @SuppressWarnings("unused")
	private void processSource(Context cx, String filename)
    {
        if (filename == null) {
            processSource(cx);
        } else {

        	final String fileSeparator = System.getProperty("file.separator");
			String fileToProcess = filename.replace("\\", fileSeparator + fileSeparator);
        	fileToProcess = fileToProcess.replace("/", fileSeparator + fileSeparator);
            File file = new File(fileToProcess);
            if (file.exists()) {
                CommandLineExecutor.parseJaggeryScript(fileToProcess);
            }
        }
    }

	private void processSource(Context cx) {
		BufferedReader in = new BufferedReader
		    (new InputStreamReader(System.in));

		boolean hitEOF = false;
		do {

		    out.print("jaggery> ");
		    out.flush();
		    try {
		        
		        StringBuilder sbuilder = new StringBuilder();
		        // Collect lines of source to compile.
		        while(true) {
		            String newline;
		            newline = in.readLine();
		            if (newline == null) {
		                hitEOF = true;
		                break;
		            }
		            
		            sbuilder.append(newline);
		            sbuilder.append('\n');
		            
		            if (cx.stringIsCompilableUnit(sbuilder.toString())) {
		                break;
		            }
		        }
		        
		        String source = sbuilder.toString();
		        
		    	if (CMD_VERSION.equalsIgnoreCase(source.trim())){
		    		out.println("\n" + version() +  "\n");
		    		processSource(cx);
		    		return;
		    	}else if (CMD_HELP.equalsIgnoreCase(source.trim())){
		    		help();
		    		processSource(cx);
		    		return;
		    	}else if (CMD_QUIT.equalsIgnoreCase(source.trim())){
		    		quit();
		    		out.println("\n");
		    	}else if ("".equals(source.trim())){
		    		processSource(cx);
		    		return;
		    	}
		    	
		    	if (quitting) {
			        // The user executed the quit() function.
			        break;
			    }
		    	
		    	out.println("\n");
		    	CommandLineExecutor.parseJaggeryExpression(source);
		    	out.println("\n");

		    }
		    catch (WrappedException we) {
		        // Some form of exception was caught by JavaScript and
		        // propagated up.
		        out.println(we.getWrappedException().toString());
		    }
		    catch (EvaluatorException ee) {
		        // Some form of JavaScript error.
		        out.println("js: " + ee.getMessage());
		    }
		    catch (JavaScriptException jse) {
		        // Some form of JavaScript error.
		        out.println("js: " + jse.getMessage());
		    }
		    catch (IOException ioe) {
		    	// Some form of IO error.
		        out.println(ioe.toString());
		    }
		    
		} while (!hitEOF);
		out.println();
	}

    /**
     * will print to console
     */
    private static void printToConsole(String s) {
        out.println(s);
    }

}

