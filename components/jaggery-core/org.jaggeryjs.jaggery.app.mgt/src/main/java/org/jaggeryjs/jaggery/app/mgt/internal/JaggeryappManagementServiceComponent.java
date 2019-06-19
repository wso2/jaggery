/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.jaggeryjs.jaggery.app.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.webapp.mgt.DataHolder;

// import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
// import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;

@Component(
        name = "org.jaggeryjs.jaggery.app.mgt.internal.JaggeryappManagementServiceComponent",
        immediate = true)
public class JaggeryappManagementServiceComponent {

    private static final Log log = LogFactory.getLog(JaggeryappManagementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {

        try {
            // Register the valves with Tomcat
            // ArrayList<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
            // valves.add(new TenantLazyLoaderValve());
            // valves.add(new GhostWebappDeployerValve());
            // TomcatValveContainer.addValves(valves);
        } catch (Exception e) {
            log.error("Error occurred while activating JaggeryappManagementServiceComponent", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        // TomcatValveContainer.removeValves();
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        DataHolder.setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        DataHolder.setServerConfigContext(null);
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        // keeping the realm service in the DataHolder class
        DataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

    }

    protected void unsetRegistryService(RegistryService registryService) {

    }
}
