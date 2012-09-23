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

import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.internal.JaggeryCoreServiceComponent;
import org.mozilla.javascript.*;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ntask.common.TaskException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * This the method implementations of Jaggery setInterval, 
 * setTimeout, clearInterval and clearTimeout functions.
 * </p>
 */
public class JaggeryTaskManager extends ScriptableObject {

    private static final long serialVersionUID = 5003413793187124449L;

    private static final Log log = LogFactory.getLog(JaggeryTaskManager.class);

    /**
     * Type to be used for this object inside the javascript.
     */
    public String getClassName() {
        return "JaggeryTaskManager";
    }

    /**
     * <p/>
     * This method allows the scheduling of a JavaScript function periodically. There are 2 mandatory parameters.
     * A javascript function (or a javascript expression) and the time interval between two consecutive executions. Optionally one can specify a start time, indicating
     * when to begin the function execution (after given number of milliseconds in the frequency parameter by default). It is also possible to give a start time and an end time.
     * <p/>
     * The method returns a String UUID, which can be used to refer to this function scheduling instance.
     * </p>
     * <p/>
     * <p/>
     * Imagine you have a javascript function in your service as follows
     * <p/>
     * <pre>
     * function myJavaScriptFunction(function-parameter)
     * {
     *      print("The parameter value is " + function-parameter);
     * }
     * </pre>
     * </p>
     * <p/>
     * example 1:
     * <pre>
     *    //Setting up 'myJavaScriptFunction' to be executed in 2000 millisecond intervals, starting now and continuing forever.
     *    var id = setInterval(myJavaScriptFunction, 2000, 'I am a parameter value');
     * </pre>
     * <p/>
     * example 2:
     * <pre>
     *    //Setting up 'myJavaScriptFunction' to be executed in 2000 millisecond intervals, starting now and continuing forever.
     *    //But passing the function as a javascript expression.
     *    var id = setInterval('myJavaScriptFunction("I am a parameter value");', 2000);
     * </pre>
     * <p/>
     * example 3:
     * <pre>
     *    //Setting to start in 2 minutes from now
     *    var startTime = new Date();
     *    startTime.setMinutes(startTime.getMinutes() + 2);
     * <p/>
     *    var id = setInterval(myJavaScriptFunction, 2000, 'I am a parameter value', startTime);
     *    or
     *    var id = setInterval('myJavaScriptFunction("I am a parameter value");', 2000, null, startTime);
     * </pre>
     * <p/>
     * example 4:
     * <pre>
     *    //Setting to start in 2 minutes from now
     *    var startTime = new Date();
     *    startTime.setMinutes(startTime.getMinutes() + 2);
     * <p/>
     *    //Setting to end in 4 minutes after starting
     *    var endtime = new Date();
     *    endtime.setMinutes(startTime.getMinutes() + 4);
     * <p/>
     *    var id = setInterval(myJavaScriptFunction, 2000, 'I am a parameter value', startTime, endtime);
     *    or
     *    var id = setInterval('myJavaScriptFunction("I am a parameter value");', 2000, null, startTime, endtime);
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */

    public static String setInterval(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws CarbonException, TaskException, IOException {

        //Generating UUID + current time for the taskName
        String taskName =
                JaggeryTaskManager.getFormattedCurrentDateTime() + "-" + UIDGenerator.generateUID().substring(9);

        int argCount = arguments.length;
        Object jsFunction = null;
        Object[] functionParams = null;
        long frequency = 0;
        Date startTime = null;
        Date endTime = null;
        final Map<String, Object> resources = new HashMap<String, Object>();
        JaggeryTaskInfo jaggeryTaskInfo = new JaggeryTaskInfo();
        
        switch (argCount) {

            case 2://A javascript function and its execution frequency were passed

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must be " +
                                              "a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException("Invalid parameter. The second parameter " +
                                              "must be the execution frequency in milliseconds.");
                }

                //Creating the trigger. There will be a one-to-one mapping between jobs and triggers in this implementation
                jaggeryTaskInfo.setName(taskName);
                jaggeryTaskInfo.setTaskCount(JaggeryTaskConstants.REPEAT_INDEFINITELY);
                jaggeryTaskInfo.setTaskInterval(frequency);
                break;

            case 3://A javascript function its execution frequency and parameters were passed

                //Extracting the javascript function from the arguments=
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must " +
                                              "be a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the " +
                            "execution frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {

                    if (arguments[2] instanceof NativeArray) {
                        NativeArray nativeArray = (NativeArray) arguments[2];
                        Object[] objects = nativeArray.getIds();
                        ArrayList<Object> tempParamHolder = new ArrayList<Object>();
                        for (Object currObject : objects) {
                            if (currObject instanceof String) {
                                String property = (String) currObject;
                                if ("length".equals(property)) {
                                    continue;
                                }
                                tempParamHolder.add(nativeArray.get(property, nativeArray));
                            } else {
                                Integer property = (Integer) currObject;
                                tempParamHolder
                                        .add(nativeArray.get(property.intValue(), nativeArray));
                            }
                        }
                        //Convert the arraylist to an object array
                        functionParams = new Object[tempParamHolder.size()];
                        tempParamHolder.toArray(functionParams);

                    } else if (arguments[2] instanceof String) {
                        taskName = (String) arguments[2];
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be an Array " +
                                "of parameters to the argument, a string value for the task name or null.");
                    }
                }

                //Creating the trigger. There will be a one-to-one mapping between jobs and triggers in this implementation
                jaggeryTaskInfo.setName(taskName);
                jaggeryTaskInfo.setTaskCount(JaggeryTaskConstants.REPEAT_INDEFINITELY);
                jaggeryTaskInfo.setTaskInterval(frequency);
                break;

            case 4:// A javascript function, its execution frequnecy, function parameters and a start time is passed.

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The first parameter must be a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the execution " +
                            "frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {
                    if (arguments[2] instanceof NativeArray) {
                        NativeArray nativeArray = (NativeArray) arguments[2];
                        Object[] objects = nativeArray.getIds();
                        ArrayList<Object> tempParamHolder = new ArrayList<Object>();
                        for (Object currObject : objects) {
                            if (currObject instanceof String) {
                                String property = (String) currObject;
                                if ("length".equals(property)) {
                                    continue;
                                }
                                tempParamHolder.add(nativeArray.get(property, nativeArray));
                            } else {
                                Integer property = (Integer) currObject;
                                tempParamHolder
                                        .add(nativeArray.get(property.intValue(), nativeArray));
                            }
                        }
                        //Convert the arraylist to an object array
                        functionParams = new Object[tempParamHolder.size()];
                        tempParamHolder.toArray(functionParams);
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be an Array of " +
                                "parameters to the argument or null.");
                    }
                }

                if (arguments[3] != null) {
                    if (arguments[3] instanceof String) {
                        taskName = (String) arguments[3];
                    } else {
                        try {
                            startTime = (Date) Context.jsToJava(arguments[3], Date.class);
                        } catch (EvaluatorException e) {
                            throw new CarbonException(
                                    "Invalid parameter. The fourth parameter must be " +
                                    "the start time in date format or a string value " +
                                    "for the task name.", e);
                        }
                    }
                }

                jaggeryTaskInfo.setName(taskName);
                jaggeryTaskInfo.setTaskCount(JaggeryTaskConstants.REPEAT_INDEFINITELY);
                jaggeryTaskInfo.setTaskInterval(frequency);
                jaggeryTaskInfo.setStartTime(JaggeryTaskUtils.dateToCal(startTime));

                break;

            case 5: // A javascript function, its execution frequnecy, function parameters, start time and an end time is passed.

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must be a " +
                                              "JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the execution " +
                            "frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {
                    if (arguments[2] instanceof NativeArray) {
                        NativeArray nativeArray = (NativeArray) arguments[2];
                        Object[] objects = nativeArray.getIds();
                        ArrayList<Object> tempParamHolder = new ArrayList<Object>();
                        for (Object currObject : objects) {
                            if (currObject instanceof String) {
                                String property = (String) currObject;
                                if ("length".equals(property)) {
                                    continue;
                                }
                                tempParamHolder.add(nativeArray.get(property, nativeArray));
                            } else {
                                Integer property = (Integer) currObject;
                                tempParamHolder
                                        .add(nativeArray.get(property.intValue(), nativeArray));
                            }
                        }
                        //Convert the arraylist to an object array
                        functionParams = new Object[tempParamHolder.size()];
                        tempParamHolder.toArray(functionParams);
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be an Array of " +
                                "parameters to the argument or null.");
                    }
                }

                //Extracting the start time from the arguments
                if (arguments[3] != null) {
                    if (arguments[3] instanceof String) {
                        taskName = (String) arguments[3];
                    } else {
                        try {
                            startTime = (Date) Context.jsToJava(arguments[3], Date.class);
                        } catch (EvaluatorException e) {
                            throw new CarbonException(
                                    "Invalid parameter. The fourth parameter must be " +
                                    "the start time in date format.", e);
                        }
                    }
                }

                //Extracting the end time from the arguments
                if (arguments[4] != null) {
                    if (arguments[4] instanceof String) {
                        taskName = (String) arguments[4];
                    } else {
                        try {
                            endTime = (Date) Context.jsToJava(arguments[4], Date.class);
                        } catch (EvaluatorException e) {
                            throw new CarbonException(
                                    "Invalid parameter. The fifth parameter must be " +
                                    "the end time in date format or a string value " +
                                    "for the task name.", e);
                        }
                    }
                }

                jaggeryTaskInfo.setName(taskName);
                jaggeryTaskInfo.setTaskCount(JaggeryTaskConstants.REPEAT_INDEFINITELY);
                jaggeryTaskInfo.setTaskInterval(frequency);
                jaggeryTaskInfo.setStartTime(JaggeryTaskUtils.dateToCal(startTime));
                jaggeryTaskInfo.setEndTime(JaggeryTaskUtils.dateToCal(endTime));

                break;

            case 6: // A javascript function, its execution frequnecy, function parameters, start time and an end time is passed.

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must be a " +
                                              "JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the execution " +
                            "frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {
                    if (arguments[2] instanceof NativeArray) {
                        NativeArray nativeArray = (NativeArray) arguments[2];
                        Object[] objects = nativeArray.getIds();
                        ArrayList<Object> tempParamHolder = new ArrayList<Object>();
                        for (Object currObject : objects) {
                            if (currObject instanceof String) {
                                String property = (String) currObject;
                                if ("length".equals(property)) {
                                    continue;
                                }
                                tempParamHolder.add(nativeArray.get(property, nativeArray));
                            } else {
                                Integer property = (Integer) currObject;
                                tempParamHolder
                                        .add(nativeArray.get(property.intValue(), nativeArray));
                            }
                        }
                        //Convert the arraylist to an object array
                        functionParams = new Object[tempParamHolder.size()];
                        tempParamHolder.toArray(functionParams);
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be an Array of " +
                                "parameters to the argument or null.");
                    }
                }

                //Extracting the start time from the arguments
                if (arguments[3] != null) {
                    try {
                        startTime = (Date) Context.jsToJava(arguments[3], Date.class);
                    } catch (EvaluatorException e) {
                        throw new CarbonException(
                                "Invalid parameter. The fourth parameter must be " +
                                "the start time in date format.", e);
                    }
                }

                if (arguments[4] != null) {
                    try {
                        endTime = (Date) Context.jsToJava(arguments[4], Date.class);
                    } catch (EvaluatorException e) {
                        throw new CarbonException(
                                "Invalid parameter. The fifth parameter must be " +
                                "the end time in date format.", e);
                    }
                }

                if (arguments[5] != null) {
                    if (arguments[5] instanceof String) {
                        taskName = (String) arguments[5];
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The sixth parameter must be a string value " +
                                "for the task name");
                    }
                }

                jaggeryTaskInfo.setName(taskName);
                jaggeryTaskInfo.setTaskCount(JaggeryTaskConstants.REPEAT_INDEFINITELY);
                jaggeryTaskInfo.setTaskInterval(frequency);
                jaggeryTaskInfo.setStartTime(JaggeryTaskUtils.dateToCal(startTime));
                jaggeryTaskInfo.setEndTime(JaggeryTaskUtils.dateToCal(endTime));

                break;

            default:
                throw new CarbonException("Invalid number of parameters.");
        }

        resources.put(JaggeryTaskConstants.FUNCTION_PARAMETERS, functionParams);
        resources.put(JaggeryTaskConstants.TASK_NAME, taskName);
        resources.put(JaggeryTaskConstants.JAVASCRIPT_FUNCTION, jsFunction);
        resources.put(JaggeryTaskConstants.CONTEXT_FACTORY, cx.getFactory());

        JaggeryCoreServiceComponent.getTaskMap().put(taskName, resources);
        
        final Map<String, String> propertyMap = new HashMap<String, String>();
        propertyMap.put(JaggeryTaskConstants.TASK_NAME, taskName);
        jaggeryTaskInfo.setTaskProperties(propertyMap);
        
        JaggeryTaskAdmin taskAdmin = new JaggeryTaskAdmin();
        
        try {
			taskAdmin.scheduleTask(jaggeryTaskInfo);
		} catch (TaskException e) {
			throw new CarbonException("Unable to create the scheduling task");
		}

        return taskName;
    }

    /**
     * <p/>
     * Removes a JavaScript function scheduled for periodic execution using the job id
     * </p>
     * <p/>
     * <pre>
     *   clearInterval(id);
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */

    public static void clearInterval(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws CarbonException {

        if (arguments[0] instanceof String) {
            deleteJob(arguments);
        } else {
            throw new CarbonException("Invalid parameter");
        }

    }

    private static void deleteJob(Object[] arguments) {
        String taskName = (String) arguments[0];

        JaggeryTaskAdmin taskAdmin = new JaggeryTaskAdmin();
        try {
			taskAdmin.deleteTask(taskName);
		} catch (TaskException e) {
			log.error("Unable to delete job : " + e.getMessage());
		}
    }

    public static boolean isTaskScheduled(Context cx, Scriptable thisObj,
                                                  Object[] arguments, Function funObj)
            throws CarbonException, TaskException {

        if (arguments[0] instanceof String) {
            JaggeryTaskAdmin taskAdmin = new JaggeryTaskAdmin();
            
            return taskAdmin.isTaskScheduled((String) arguments[0]);
        } else {
            return false;
        }
    }

    /**
     * setTimeout() allows you to specify that a piece of JavaScript code (called an expression) will be run a specified number
     * of milliseconds from when the setTimeout() method was called.
     * <p/>
     * <p/>
     * <pre>
     *    ex: setTimeout (expression, timeout);
     * </pre>
     * <p/>
     * <p/>
     * where expression is the JavaScript code to run after timeout milliseconds have elapsed.
     * <p/>
     * setTimeout() also returns a numeric timeout ID that can be used to track the timeout. This is most commonly used with the clearTimeout() method
     *
     * @throws CarbonException Thrown in case any exceptions occur
     * @throws IOException 
     */

    public static String setTimeout(Context cx, Scriptable thisObj, Object[] arguments,
                                               Function funObj) throws CarbonException, IOException {

        //Generating UUID + current time for the taskName
        String taskName =
        		JaggeryTaskManager.getFormattedCurrentDateTime() + "-" + UIDGenerator.generateUID().substring(9);

        int argCount = arguments.length;
        Object jsFunction = null;
        Object[] functionParams = null;
        long timeout = 0;
        Date currentTime = new Date();
        
        final Map<String, Object> resources = new HashMap<String, Object>();
        JaggeryTaskInfo jaggeryTaskInfo = new JaggeryTaskInfo();

        switch (argCount) {

            case 2://A javascript function and its timeout were passed

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must be " +
                                              "a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    timeout = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException("Invalid parameter. The second parameter " +
                                              "must be function starting timeout.");
                }
                
                //Creating the trigger. There will be a one-to-one mapping between jobs and triggers in this implementation
                jaggeryTaskInfo.setName(taskName);
                jaggeryTaskInfo.setTaskCount(0);
                jaggeryTaskInfo.setTaskInterval(0);
                jaggeryTaskInfo.setStartTime(JaggeryTaskUtils.dateToCal(new Date(currentTime.getTime() + timeout)));
                
                break;

            case 3://A javascript function its execution frequency and parameters were passed

                //Extracting the javascript function from the arguments=
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must " +
                                              "be a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    timeout = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the " +
                            "execution frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {

                    if (arguments[2] instanceof String) {
                        taskName = (String) arguments[2];
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be a string " +
                                "value for the  the task name");
                    }
                }
                
                //Creating the trigger. There will be a one-to-one mapping between jobs and triggers in this implementation
                jaggeryTaskInfo.setName(taskName);
                jaggeryTaskInfo.setTaskCount(0);
                jaggeryTaskInfo.setTaskInterval(0);
                jaggeryTaskInfo.setStartTime(JaggeryTaskUtils.dateToCal(new Date(currentTime.getTime() + timeout)));

                break;

            default:
                throw new CarbonException("Invalid number of parameters.");
        }

        resources.put(JaggeryTaskConstants.FUNCTION_PARAMETERS, functionParams);
        resources.put(JaggeryTaskConstants.TASK_NAME, taskName);
        resources.put(JaggeryTaskConstants.JAVASCRIPT_FUNCTION, jsFunction);
        resources.put(JaggeryTaskConstants.CONTEXT_FACTORY, cx.getFactory());
        
        JaggeryCoreServiceComponent.getTaskMap().put(taskName, resources);
        
        final Map<String, String> propertyMap = new HashMap<String, String>();
        propertyMap.put(JaggeryTaskConstants.TASK_NAME, taskName);
        jaggeryTaskInfo.setTaskProperties(propertyMap);
        
        JaggeryTaskAdmin taskAdmin = new JaggeryTaskAdmin();
        
        try {
			taskAdmin.scheduleTask(jaggeryTaskInfo);
		} catch (TaskException e) {
			throw new CarbonException("Unable to create the scheduling task");
		}

        return taskName;

    }


    /**
     * Sometimes it's useful to be able to cancel a timer before it goes off. The clearTimeout() method lets us do exactly that.
     * <p/>
     * <p/>
     * <pre>
     *    ex: clearTimeout ( timeoutId );
     * </pre>
     * <p/>
     * <p/>
     * where timeoutId is the ID of the timeout as returned from the setTimeout() method call.
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public static void clearTimeout(Context cx, Scriptable thisObj, Object[] arguments,
                                               Function funObj) throws CarbonException {

        if (arguments[0] instanceof String) {
            deleteJob(arguments);
        } else {
            throw new CarbonException("Invalid parameter");
        }
    }

    private static String getFormattedCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-z");
        Date date = new Date();
        return dateFormat.format(date);
    }
}