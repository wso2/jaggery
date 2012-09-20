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

import java.io.PrintStream;

import org.apache.commons.cli.*;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public final class CommandLineClient {

    private static final String CONSTANT_HELP = "help";
    private static final String CONSTANT_SHELL = "shell";
    private static final String CONSTANT_VERSION = "version";
    private static final String CONSTANT_QUIT = "quit";
    
    private static PrintStream out = System.out;
    
    private CommandLineClient() {
        //disable external instantiation
    }

    static {
        final Logger rootLogger = Logger.getRootLogger();
        if (!rootLogger.getAllAppenders().hasMoreElements()) {
            rootLogger.setLevel(Level.OFF);
            rootLogger.addAppender(new ConsoleAppender(
                    new PatternLayout("%-5p [%t]: %m%n")));
        }
    }

    public static void main(final String[] args) {

        if (args == null || args.length == 0) {
            out.println("\nEmpty command. Please execute -help for instructions\n");
            return;
        }


        final CommandLineParser commandLineParser = new GnuParser();

        final Options options = new Options();
        options.addOption(CONSTANT_HELP, false, "Display Jaggery help messages");
        options.addOption(CONSTANT_SHELL, false,
                "Enter into the Jaggery shell to execute Jaggery expressions");
        options.addOption(CONSTANT_VERSION, false, "Get Jaggery version");
        options.addOption(CONSTANT_QUIT, false,
                "Quit Jaggery command line client (for shell mode)");

        try {
            final CommandLine line = commandLineParser.parse(options, args);

            if (line.hasOption(CONSTANT_HELP)) {
                out.println("\n");
                help();
                out.println("\n");
            } else if (line.hasOption(CONSTANT_VERSION)) {
                out.println("\n");
                out.println(JaggeryShell.version());
                out.println("\n");
            } else if (line.hasOption(CONSTANT_SHELL)) {
                JaggeryShell.enterContext(new String[0]);
            } else if (line.hasOption(CONSTANT_QUIT)) {
                out.println("\nThis command only supports in shell mode\n");
            } else if (args[0] != null && args[0].contains(".jag")) {
                CommandLineExecutor.parseJaggeryScript(args[0]);
            } else {
                out.println("\nInvalid command. Please execute -help for instructions\n");
            }
        } catch (ParseException exp) {
            out.println("Unexpected exception:" + exp.getMessage());
        }
    }

    public static void help() {
        out.println("");
        out.println("Command                 Description");
        out.println("=======                 ===========");
        out.println("<script.jag>            Execute Jaggery file");
        out.println("<filepath/script.jag>   Execute Jaggery file in the file path");
        out.println("-help                	Display Jaggery help messages");
        out.println("-quit	                Quit Jaggery command line client (for shell mode)");
        out.println("-shell	                Enter into the Jaggery shell to execute Jaggery expressions");
        out.println("-version		Get Jaggery version");
        out.println("");
    }
}


