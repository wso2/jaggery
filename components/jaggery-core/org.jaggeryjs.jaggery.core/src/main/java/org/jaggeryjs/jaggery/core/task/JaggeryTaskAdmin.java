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

package org.jaggeryjs.jaggery.core.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.internal.JaggeryCoreServiceComponent;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

public class JaggeryTaskAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(JaggeryTaskAdmin.class);

    public String[] getAllTaskNames() throws TaskException {
        try {
            TaskManager taskManager = JaggeryCoreServiceComponent.getTaskService().getTaskManager(
                    JaggeryTaskConstants.JAGGERY_TASK_TYPE);
            List<TaskInfo> taskInfoList = taskManager.getAllTasks();
            List<String> result = new ArrayList<String>();
            for (TaskInfo taskInfo : taskInfoList) {
                result.add(taskInfo.getName());
            }
            return result.toArray(new String[result.size()]);
        } catch (Exception e) {
            log.error(e);
            throw new TaskException("Error in retrieving task names: " + e.getMessage(), Code.CONFIG_ERROR);
        }
    }

    public JaggeryTaskInfo getTaskInfo(String taskName) throws TaskException {
        try {
            TaskManager taskManager = JaggeryCoreServiceComponent.getTaskService().getTaskManager(
                    JaggeryTaskConstants.JAGGERY_TASK_TYPE);
            return JaggeryTaskUtils.convert(taskManager.getTask(taskName));
        } catch (Exception e) {
            log.error(e);
            throw new TaskException("Error getting task info for task: " + taskName, Code.CONFIG_ERROR);
        }
    }

    public void scheduleTask(JaggeryTaskInfo jaggeryTaskInfo) throws TaskException {
        TaskManager taskManager = null;
        try {
            taskManager = JaggeryCoreServiceComponent.getTaskService().getTaskManager(
                    JaggeryTaskConstants.JAGGERY_TASK_TYPE);
            TaskInfo taskInfo = JaggeryTaskUtils.convert(jaggeryTaskInfo);
            taskManager.registerTask(taskInfo);
            taskManager.scheduleTask(taskInfo.getName());
        } catch (Exception e) {
            log.error(e);
            if (taskManager != null) {
                try {
                    taskManager.deleteTask(jaggeryTaskInfo.getName());
                } catch (TaskException e1) {
                    log.error(e1);
                }
            }
            throw new TaskException("Error scheduling task: " + jaggeryTaskInfo.getName(), Code.CONFIG_ERROR);
        }
    }

    public boolean rescheduleTask(JaggeryTaskInfo jaggeryTaskInfo) throws TaskException {
        try {
            TaskManager taskManager = JaggeryCoreServiceComponent.getTaskService().getTaskManager(
                    JaggeryTaskConstants.JAGGERY_TASK_TYPE);
            TaskInfo taskInfo = JaggeryTaskUtils.convert(jaggeryTaskInfo);
            taskManager.registerTask(taskInfo);
            taskManager.rescheduleTask(taskInfo.getName());
        } catch (Exception e) {
            log.error(e);
            throw new TaskException("Error rescheduling task: " + jaggeryTaskInfo.getName(), Code.CONFIG_ERROR);
        }
        return true;
    }

    public void deleteAllTasks() throws TaskException {
    	String allTaks[] = this.getAllTaskNames();
    	
    	for (String taskName : allTaks) {
			deleteTask(taskName);
		}
    }
    
    public void deleteTask(String taskName) throws TaskException {
        try {
            TaskManager taskManager = JaggeryCoreServiceComponent.getTaskService().getTaskManager(
                    JaggeryTaskConstants.JAGGERY_TASK_TYPE);
            taskManager.deleteTask(taskName);
        } catch (Exception e) {
            log.error(e);
            throw new TaskException("Error deleting task: " + taskName, Code.CONFIG_ERROR);
        }
    }

    public boolean isTaskScheduled(String taskName) throws TaskException {
        try {
            TaskManager taskManager = JaggeryCoreServiceComponent.getTaskService().getTaskManager(
                    JaggeryTaskConstants.JAGGERY_TASK_TYPE);
            return taskManager.isTaskScheduled(taskName);
        } catch (Exception e) {
            log.error(e);
            throw new TaskException("Error checking task scheduled status: " + taskName, Code.CONFIG_ERROR);
        }
    }

}
