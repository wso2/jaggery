package org.jaggeryjs.hostobjects.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

public class RegistryHostObjectContext {

    private static Log log = LogFactory.getLog(RegistryHostObjectContext.class);

    private static RegistryService registryService;
    private static RealmService realmService;

    public static void setRegistryService(RegistryService registryService) {
        RegistryHostObjectContext.registryService = registryService;
    }

    public static Registry getUserRegistry(String mashupAuthor, int tenantId) throws CarbonException {
        if (registryService == null) {
            throw new CarbonException("Registry is null");
        }
        try {
            if(registryService.getUserRealm(tenantId).getUserStoreManager().isExistingUser(mashupAuthor)) {
                return registryService.getGovernanceUserRegistry(mashupAuthor, tenantId);
            } else {
                throw new CarbonException("Unable to access Registry, mashup author is not an active user");
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        }catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        }
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRealmService(RealmService rs) throws CarbonException {
        RegistryHostObjectContext.realmService = rs;
    }

    public static RealmService getRealmService() throws CarbonException {
        if (realmService == null) {
            String msg = "System has not been started properly. Realm Service is null.";
            log.error(msg);
            throw new CarbonException(msg);
        }
        return realmService;
    }
}
