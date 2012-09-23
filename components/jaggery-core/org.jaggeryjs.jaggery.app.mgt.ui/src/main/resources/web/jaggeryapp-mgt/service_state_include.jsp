<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources">
    <%
        if (Boolean.valueOf((String) request.getAttribute("isActive"))) {
    %>
    <span class="icon-text" style="background-image:url(images/activate.gif);"><fmt:message
            key="active"/>&nbsp; &nbsp;[</span>
    <a href="#" class="icon-link"
       style="background-image:none !important; margin-left: 2px !important; padding-left: 2px !important;"
       title="<fmt:message key="deactivate.service"/>"
       onclick="changeServiceState(false); return false;"><fmt:message key="deactivate"/></a>
    <span class="icon-text"
          style="background-image:none !important; margin-left: 2px !important; padding-left: 2px !important;">]</span>
    <%} else {%>
    <span class="icon-text" style="background-image:url(images/deactivate.gif);"><fmt:message
            key="inactive"/>&nbsp; &nbsp;[</span><a href="#" class="icon-link"
                                                    style="background-image:none !important; margin-left: 2px !important; padding-left: 2px !important;"
                                                    title="<fmt:message key="activate.service"/>"
                                                    onclick="changeServiceState(true); return false;"><fmt:message
        key="activate"/></a>
    <span class="icon-text"
          style="background-image:none !important; margin-left: 2px !important; padding-left: 2px !important;">]</span>
    <%}%>
</fmt:bundle>
