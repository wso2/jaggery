package org.jaggeryjs.hostobjects.registry.internal;
/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.registry.RegistryHostObjectContext;
import org.jaggeryjs.hostobjects.registry.RegistryHostObjectContext;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * @scr.component name="mashup.javascript.hostobjects.registry.dscomponent"
 *                immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 */
public class RegistryHostObjectServiceComponent {

    private static final Log log = LogFactory.getLog(RegistryHostObjectServiceComponent.class);

	protected void setRegistryService(RegistryService registryService) {
		if (log.isDebugEnabled()) {
			log.info("Setting the Registry Service");
		}
		RegistryHostObjectContext.setRegistryService(registryService);
	}

	protected void unsetRegistryService(RegistryService registryService) {
		if (log.isDebugEnabled()) {
			log.info("Unsetting the Registry Service");
		}
		RegistryHostObjectContext.setRegistryService(null);
	}
}
