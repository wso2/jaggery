/*
 * Copyright 2007,2008 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jaggeryjs.hostobjects.ws;

import org.apache.ws.security.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public class PasswordCallbackHandler implements CallbackHandler {

    private String userPassword = null  ;
    private String keyPassword = null;

    public PasswordCallbackHandler() {
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                WSPasswordCallback passwordCallback = (WSPasswordCallback) callback;

                switch (passwordCallback.getUsage()) {

                    case WSPasswordCallback.SIGNATURE:
                    case WSPasswordCallback.DECRYPT:
                        passwordCallback.setPassword(keyPassword);
                        break;
                    case WSPasswordCallback.USERNAME_TOKEN:
                        passwordCallback.setPassword(userPassword);
                        break;
                }
            } else {
                throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
            }
        }
    }
}
