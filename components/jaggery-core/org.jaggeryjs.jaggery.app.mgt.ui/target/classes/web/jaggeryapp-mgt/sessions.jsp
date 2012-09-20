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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.jaggeryjs.jaggery.app.mgt.ui.JaggeryAdminClient" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.SessionMetadata" %>
<%@ page import="org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.SessionsWrapper" %>
<%@ page import="java.text.SimpleDateFormat" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
	response.setHeader("Cache-Control", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    JaggeryAdminClient client;

    int numberOfPages;
    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }
    String webappFileName = request.getParameter("webappFileName");
    SessionMetadata[] sessions;

    SessionsWrapper sessionsWrapper;
    try {
        client = new JaggeryAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        sessionsWrapper = client.getActiveSessionsInWebapp(webappFileName, pageNumberInt);
        numberOfPages = sessionsWrapper.getNumberOfPages();
        sessions = sessionsWrapper.getSessions();
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
    int activeSessions = sessionsWrapper.getNumberOfActiveSessions();
%>

<fmt:bundle basename="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources">
<carbon:breadcrumb
        label="webapps"
        resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<jsp:include page="javascript_include.jsp"/>

<script type="text/javascript">
    var allSessionsSelected = false;

    function expireSessions() {
        var selected = isSessionSelected();
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.sessions.to.be.expired"/>');
            return;
        }
        if (allSessionsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="expire.all.sessions.prompt"><fmt:param value="<%= activeSessions%>"/></fmt:message>",
                                          function() {
                                              location.href = 'expire_webapp_sessions.jsp?expireAll=true&webappFileName=<%= webappFileName %>';
                                          }
                    );
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="expire.sessions.on.page.prompt"/>",
                                          function() {
                                              document.sessionsForm.submit();
                                          }
                    );
        }
    }

    function isSessionSelected() {
        var selected = false;
        if (document.sessionsForm.sessionId[0] != null) { // there is more than 1
            for (var j = 0; j < document.sessionsForm.sessionId.length; j++) {
                selected = document.sessionsForm.sessionId[j].checked;
                if (selected) break;
            }
        } else if (document.sessionsForm.sessionId != null) { // only 1
            selected = document.sessionsForm.sessionId.checked;
        }
        return selected;
    }

    function selectAllInThisPage(isSelected) {
        allSessionsSelected = false;
        if (document.sessionsForm.sessionId != null &&
            document.sessionsForm.sessionId[0] != null) { // there is more than 1
            if (isSelected) {
                for (var j = 0; j < document.sessionsForm.sessionId.length; j++) {
                    document.sessionsForm.sessionId[j].checked = true;
                }
            } else {
                for (j = 0; j < document.sessionsForm.sessionId.length; j++) {
                    document.sessionsForm.sessionId[j].checked = false;
                }
            }
        } else if (document.sessionsForm.sessionId != null) { // only 1
            document.sessionsForm.sessionId.checked = isSelected;
        }
        return false;
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allSessionsSelected = true;
        return false;
    }

    function resetVars() {
        allSessionsSelected = false;

        var isSelected = false;
        if (document.sessionsForm.webappFileName[0] != null) { // there is more than 1 sg
            for (var j = 0; j < document.sessionsForm.webappFileName.length; j++) {
                if (document.sessionsForm.webappFileName[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.sessionsForm.webappFileName != null) { // only 1 sg
            if (document.sessionsForm.webappFileName.checked) {
                isSelected = true;
            }
        }
        return false;
    }
</script>

<script type="text/javascript">
    function searchWebapps() {
        document.searchForm.submit();
    }
</script>

<div id="middle">
    <h2>
        <fmt:message key="sessions.in.webapp">
            <fmt:param value="<%= sessionsWrapper.getWebappFileName() %>"/>
        </fmt:message>
    </h2>

    <div id="workArea">
        <form action="index.jsp" name="searchForm">
            <table class="styledLeft">
                <tr>
                    <td style="border:0; !important">
                        <nobr>
                            <%= activeSessions%> <fmt:message key="active.sessions"/>.&nbsp;
                        </nobr>
                    </td>
                </tr>
                <tr>
                    <td style="border:0; !important">&nbsp;</td>
                </tr>
            </table>
        </form>

        <%
            if (sessions != null) {
                String parameters = "webappFileName=" + webappFileName;
        %>

        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                          page="sessions.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%= parameters %>"/>
        <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                  selectAllFunction="selectAllInAllPages()"
                                  selectNoneFunction="selectAllInThisPage(false)"
                                  addRemoveFunction="expireSessions()"
                                  addRemoveButtonId="delete1"
                                  resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                                  selectAllInPageKey="selectAllInPage"
                                  selectAllKey="selectAll"
                                  selectNoneKey="selectNone"
                                  addRemoveKey="webapps.expire.sessions"
                                  numberOfPages="<%=numberOfPages%>"/>
        <p>&nbsp;</p>

        <form action="expire_webapp_sessions.jsp" name="sessionsForm" method="post">
            <input type="hidden" name="pageNumber" value="<%= pageNumber%>"/>
            <input type="hidden" name="webappFileName" value="<%= webappFileName%>"/>
            <table class="styledLeft" id="webappsTable" width="100%">
                <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th><fmt:message key="session.id"/></th>
                    <th><fmt:message key="session.auth.type"/></th>
                    <th><fmt:message key="session.creation.time"/></th>
                    <th><fmt:message key="session.last.accessed.time"/></th>
                    <th><fmt:message key="session.max.inactive.interval"/></th>
                </tr>
                </thead>
                <tbody>

                <%
                    SimpleDateFormat dateFormatter =
                            new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
                    int position = 0;
                    for (SessionMetadata sessionMetadata : sessions) {
                        String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                        position++;
                %>

                <tr bgcolor="<%= bgColor%>">
                    <td width="10px" style="text-align:center; !important">
                        <input type="checkbox" name="sessionId"
                               value="<%= sessionMetadata.getSessionId() %>"
                               onclick="resetVars()" class="chkBox"/>
                    </td>
                    <td>
                        <%= sessionMetadata.getSessionId() %>
                    </td>
                    <td>
                        <% if (sessionMetadata.getAuthType() != null) { %>
                            <%= sessionMetadata.getAuthType() %>
                        <%} %>
                        &nbsp;
                    </td>
                    <td>
                        <%= dateFormatter.format(sessionMetadata.getCreationTime()) %>
                    </td>
                    <td>
                        <%= dateFormatter.format(sessionMetadata.getLastAccessedTime()) %>
                    </td>
                    <td>
                        <%= sessionMetadata.getMaxInactiveInterval() %>&nbsp;ms
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </form>
        <p>&nbsp;</p>
        <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                  selectAllFunction="selectAllInAllPages()"
                                  selectNoneFunction="selectAllInThisPage(false)"
                                  addRemoveFunction="expireSessions()"
                                  addRemoveButtonId="delete2"
                                  resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                                  selectAllInPageKey="selectAllInPage"
                                  selectAllKey="selectAll"
                                  selectNoneKey="selectNone"
                                  addRemoveKey="webapps.expire.sessions"
                                  numberOfPages="<%=numberOfPages%>"/>
        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                          page="sessions.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%= parameters %>"/>
        <%
        } else {
        %>
        <b><fmt:message key="active.sessions.not.found"/></b>
        <%
            }
        %>
    </div>
</div>
</fmt:bundle>
