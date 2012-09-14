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
package org.jaggeryjs.hostobjects.ws.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.ConfigurationContextService;


/**
 * @scr.component name="mashup.javascript.hostobjects.wsrequest.dscomponent"" immediate="true"
 * @scr.reference name="carbon.core.configurationContextService"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class WSRequestServiceComponent {

    private static final Log log = LogFactory.getLog(WSRequestServiceComponent.class);

    private static ConfigurationContextService configurationContextService = null;

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        WSRequestServiceComponent.configurationContextService = configurationContextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        WSRequestServiceComponent.configurationContextService = null;
    }

    public static ConfigurationContext getConfigurationContext() throws AxisFault {
        if(WSRequestServiceComponent.configurationContextService != null) {
            return configurationContextService.getClientConfigContext();
        } else {
            String msg = "ConfigurationContextService cannot be found";
            log.error(msg);
            throw new AxisFault(msg);
        }
    }

    protected void activate(ComponentContext context) {
    }
}