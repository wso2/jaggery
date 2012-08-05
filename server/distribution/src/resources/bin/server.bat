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
rem Main Script for Jaggery
rem
rem Environment Variable Prequisites
rem
rem   JAGGERY_HOME   Home of CARBON installation. If not set I will  try
rem                   to figure it out.
rem
rem ---------------------------------------------------------------------------

rem ----- Only set CARBON_HOME if not already set ----------------------------
:checkServer
rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
if "%JAGGERY_HOME%"=="" set JAGGERY_HOME=%~sdp0..
set agra=%1
set agrb=%2
Call %JAGGERY_HOME%\carbon\bin\wso2server.bat %*
