/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.jaggeryjs.integration.tests.jaggery.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class JaggeryTestUtil {
    private static final int WAIT_TIME = 500;

    public static URLConnection openConnection(URL url) {
        long timeoutExpiredMs = System.currentTimeMillis() + WAIT_TIME;
        URLConnection jaggeryServerConnection = null;
        try {
            jaggeryServerConnection = url.openConnection();
        } catch (IOException ignored) {
        }
        while ((jaggeryServerConnection == null) && (System.currentTimeMillis() <= timeoutExpiredMs)) {
            try {
                jaggeryServerConnection = url.openConnection();
            } catch (IOException ignored) {
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        return jaggeryServerConnection;
    }

    public static BufferedReader inputReader(URLConnection jaggeryServerConnection) {
        long timeoutExpiredMs = System.currentTimeMillis() + WAIT_TIME;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    jaggeryServerConnection.getInputStream()));
        } catch (IOException ignored) {
        }
        while ((in == null) && (System.currentTimeMillis() <= timeoutExpiredMs)) {
            try {
                in = new BufferedReader(
                        new InputStreamReader(jaggeryServerConnection.getInputStream()));
            } catch (IOException ignored) {
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        return in;
    }
}
