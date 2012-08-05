@echo off

REM ---------------------------------------------------------------------------
REM        Copyright 2005-2012 WSO2, Inc. http://www.wso2.org
REM
REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.

rem ---------------------------------------------------------------------------
rem Main Script for WSO2 Jaggery Command Line Client
rem
rem Environment Variable Prequisites
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem ---------------------------------------------------------------------------

rem ----- if JAVA_HOME is not set we're not happy ------------------------------

:checkJava

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto runcommandlinetool

:noJavaHome
echo "You must set the JAVA_HOME variable before running Jaggery command line client."
goto end

:runcommandlinetool
java -cp "%~dp0../carbon/repository/components/plugins/org.wso2.carbon.jaggery.tools-1.0-SNAPSHOT.jar;%~dp0../carbon/repository/components/plugins/*" org.jaggeryjs.jaggery.tools.CommandLineClient %1

:END
