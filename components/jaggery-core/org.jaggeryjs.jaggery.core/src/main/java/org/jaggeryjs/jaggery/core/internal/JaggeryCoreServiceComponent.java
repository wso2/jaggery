/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.jaggeryjs.jaggery.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.task.JaggeryTaskAdmin;
import org.jaggeryjs.jaggery.core.task.JaggeryTaskConstants;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * @scr.component name="org.jaggeryjs.jaggery.core" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 **/
public class JaggeryCoreServiceComponent {

    private static final Log log = LogFactory.getLog(JaggeryCoreServiceComponent.class);
    
    private static TaskService taskService;
    
    private static Map<String, Object> taskMap;

    protected void activate(ComponentContext context) {

        JaggeryTaskAdmin taskAdmin = new JaggeryTaskAdmin();
        taskMap = new HashMap<String, Object>();
        try {
			taskAdmin.deleteAllTasks();
		} catch (TaskException e) {
			log.error("Unable to delete job : " + e.getMessage());
		}
        
        try {
            /* register the data service task type */
            getTaskService().registerTaskType(JaggeryTaskConstants.JAGGERY_TASK_TYPE);
            if (log.isDebugEnabled()) {
                log.debug("Jaggery task bundle is activated ");
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            /* don't throw exception */
        }
    }

    protected void deactivate(ComponentContext ctxt) {
    }
    
    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
        JaggeryCoreServiceComponent.taskService = taskService;
    }

    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service");
        }
        JaggeryCoreServiceComponent.taskService = null;
    }

    public static TaskService getTaskService() {
        return JaggeryCoreServiceComponent.taskService;
    }

	public static Map<String, Object> getTaskMap() {
		if(taskMap == null) {
			taskMap = new HashMap<String, Object>();
		}
		
		return taskMap;
	}

	public static void setTaskMap(Map<String, Object> taskMap) {
		JaggeryCoreServiceComponent.taskMap = taskMap;
	}

}
