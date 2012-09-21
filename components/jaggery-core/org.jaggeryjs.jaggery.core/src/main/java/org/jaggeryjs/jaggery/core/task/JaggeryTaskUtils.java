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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskInfo.TriggerInfo;

/**
 * This class represents a utility class for scheduled tasks.  
 */
public class JaggeryTaskUtils {

	public static JaggeryTaskInfo convert(TaskInfo taskInfo) {
		JaggeryTaskInfo jaggeryTaskInfo = new JaggeryTaskInfo();
		jaggeryTaskInfo.setName(taskInfo.getName());
		TriggerInfo triggerInfo = taskInfo.getTriggerInfo();
		jaggeryTaskInfo.setCronExpression(triggerInfo.getCronExpression());
		jaggeryTaskInfo.setStartTime(dateToCal(triggerInfo.getStartTime()));
		jaggeryTaskInfo.setEndTime(dateToCal(triggerInfo.getEndTime()));
		jaggeryTaskInfo.setTaskCount(triggerInfo.getRepeatCount());
		jaggeryTaskInfo.setTaskInterval(triggerInfo.getIntervalMillis());
		return jaggeryTaskInfo;
	}
	
	public static TaskInfo convert(JaggeryTaskInfo jaggeryTaskInfo) {
		TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.setCronExpression(jaggeryTaskInfo.getCronExpression());
		if (jaggeryTaskInfo.getStartTime() != null) {
		    triggerInfo.setStartTime(jaggeryTaskInfo.getStartTime().getTime());
		}
		if (jaggeryTaskInfo.getEndTime() != null) {
		    triggerInfo.setEndTime(jaggeryTaskInfo.getEndTime().getTime());
		}
		triggerInfo.setIntervalMillis((int)jaggeryTaskInfo.getTaskInterval());
		triggerInfo.setRepeatCount(jaggeryTaskInfo.getTaskCount());
		return new TaskInfo(jaggeryTaskInfo.getName(), JaggeryTask.class.getName(), 
				jaggeryTaskInfo.getTaskProperties(), triggerInfo);
	}

	
	public static Calendar dateToCal(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	public static synchronized Object fromString(String encodedString) 
			throws IOException , ClassNotFoundException {
        byte [] data = Base64.decode(encodedString);
        ObjectInputStream objectInputStream = new ObjectInputStream( 
                                        new ByteArrayInputStream(data));
        Object readObject  = objectInputStream.readObject();
        objectInputStream.close();
        return readObject;
    }

	public static synchronized String toString(Serializable object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return new String(Base64.encode(byteArrayOutputStream.toByteArray()));
    }

    public static String getTenantDomainFromId(int tid) {
        SuperTenantCarbonContext.startTenantFlow();
        SuperTenantCarbonContext.getCurrentContext().setTenantId(tid);
        String tenantDomain = SuperTenantCarbonContext.getCurrentContext().getTenantDomain();
        SuperTenantCarbonContext.endTenantFlow();
        return tenantDomain;
    }

}
