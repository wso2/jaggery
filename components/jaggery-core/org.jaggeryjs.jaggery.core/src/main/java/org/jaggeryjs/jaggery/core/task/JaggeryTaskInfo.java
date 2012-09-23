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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents Jaggery scheduled task information.
 */
public class JaggeryTaskInfo {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 5003413793187436757L;
	
	private String name;
	
	private Calendar startTime;
	
	private Calendar endTime;
	
	private int taskCount;
	
	private long taskInterval;
	
	private String cronExpression;
	
	private Map<String, String> taskProperties;
	
	public JaggeryTaskInfo() {
		taskProperties = new HashMap<String, String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}

	public long getTaskInterval() {
		return taskInterval;
	}

	public void setTaskInterval(long taskInterval) {
		this.taskInterval = taskInterval;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public Map<String, String> getTaskProperties() {
		return taskProperties;
	}

	public void setTaskProperties(Map<String, String> taskProperties) {
		this.taskProperties = taskProperties;
	}

}
