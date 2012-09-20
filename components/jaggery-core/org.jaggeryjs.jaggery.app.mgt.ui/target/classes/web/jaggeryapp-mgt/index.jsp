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
<%@page import="org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappMetadata" %>
<%@page import="org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappsWrapper" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ResourceBundle" %>
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
    WebappsWrapper webappsWrapper;
    WebappMetadata[] webapps;

    String webappSearchString = request.getParameter("webappSearchString");
    if (webappSearchString == null) {
        webappSearchString = "";
    }

    String webappState = request.getParameter("webappState");
    if (webappState == null) {
        webappState = "started";
    }
    try {
        client = new JaggeryAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        webappsWrapper = client.getPagedWebappsSummary(webappSearchString,
                                                       webappState,
                                                       Integer.parseInt(pageNumber));
        numberOfPages = webappsWrapper.getNumberOfPages();
        webapps = webappsWrapper.getWebapps();
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
	return;
    }
    int numOfWebapps = webappsWrapper.getNumberOfCorrectWebapps();
    int numOfFaultyWebapps = webappsWrapper.getNumberOfFaultyWebapps();

    ResourceBundle bundle = ResourceBundle.getBundle(JaggeryAdminClient.BUNDLE, request.getLocale());
%>

<fmt:bundle basename="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources">
<carbon:breadcrumb
        label="webapps"
        resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<jsp:include page="javascript_include.jsp"/>
<script type="text/javascript">
    var allWebappsSelected = false;

    function expireSessions() {
        var selected = isWebappSelected();
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.webapps.for.session.expiry"/>');
            return;
        }
        if (allWebappsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="session.expiry.all.webapps.prompt"><fmt:param value="<%= numOfWebapps%>"/></fmt:message>",
                                          function() {
                                              location.href = 'expire_sessions.jsp?expireAll=true';
                                          }
                    );
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="session.expiry.selected.webapps.prompt"/>",
                                          function() {
                                              document.webappsForm.action = 'expire_sessions.jsp';
                                              document.webappsForm.submit();
                                          }
                    );
        }
    }

    function reloadWebapps() {
        var selected = isWebappSelected();
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.webapps.to.be.reloaded"/>');
            return;
        }
        if (allWebappsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="reload.all.webapps.prompt"><fmt:param value="<%= numOfWebapps%>"/></fmt:message>",
                                          function() {
                                              location.href = 'reload_webapps.jsp?reloadAll=true';
                                          }
                    );
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="reload.selected.webapps.prompt"/>",
                                          function() {
                                              document.webappsForm.action = 'reload_webapps.jsp';
                                              document.webappsForm.submit();
                                          }
                    );
        }
    }

    function stopWebapps() {
        var selected = isWebappSelected();
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.webapps.to.be.stopped"/>');
            return;
        }
        if (allWebappsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="stop.all.webapps.prompt"><fmt:param value="<%= numOfWebapps %>"/></fmt:message>",
                                          function() {
                                              location.href = 'stop_webapps.jsp?reloadAll=true';
                                          }
                    );
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="stop.selected.webapps.prompt"/>",
                                          function() {
                                              document.webappsForm.action = 'stop_webapps.jsp';
                                              document.webappsForm.submit();
                                          }
                    );
        }
    }

    function startWebapps() {
        var selected = isWebappSelected();
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.webapps.to.be.started"/>');
            return;
        }
        if (allWebappsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="start.all.webapps.prompt"><fmt:param value="<%= numOfWebapps%>"/></fmt:message>",
                                          function() {
                                              location.href = 'start_webapps.jsp?reloadAll=true';
                                          }
                    );
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="start.selected.webapps.prompt"/>",
                                          function() {
                                              document.webappsForm.action = 'start_webapps.jsp';
                                              document.webappsForm.submit();
                                          }
                    );
        }
    }

    function isWebappSelected() {
        var selected = false;
        if (document.webappsForm.webappFileName[0] != null) { // there is more than 1
            for (var j = 0; j < document.webappsForm.webappFileName.length; j++) {
                selected = document.webappsForm.webappFileName[j].checked;
                if (selected) break;
            }
        } else if (document.webappsForm.webappFileName != null) { // only 1
            selected = document.webappsForm.webappFileName.checked;
        }
        return selected;
    }

    function deleteWebapps() {
        var selected = isWebappSelected();
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.webapps.to.be.deleted"/>');
            return;
        }
        if (allWebappsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.webapps.prompt"><fmt:param value="<%= numOfWebapps%>"/></fmt:message>",
                                          function() {
                                              location.href = 'delete_webapps.jsp?deleteAllWebapps=true&webappState=<%= webappState%>';
                                          }
                    );
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.webapps.on.page.prompt"/>",
                                          function() {
                                              document.webappsForm.action = 'delete_webapps.jsp';
                                              document.webappsForm.submit();
                                          }
                    );
        }
    }

    function selectAllInThisPage(isSelected) {
        allWebappsSelected = false;
        if (document.webappsForm.webappFileName != null &&
            document.webappsForm.webappFileName[0] != null) { // there is more than 1
            if (isSelected) {
                for (var j = 0; j < document.webappsForm.webappFileName.length; j++) {
                    document.webappsForm.webappFileName[j].checked = true;
                }
            } else {
                for (j = 0; j < document.webappsForm.webappFileName.length; j++) {
                    document.webappsForm.webappFileName[j].checked = false;
                }
            }
        } else if (document.webappsForm.webappFileName != null) { // only 1
            document.webappsForm.webappFileName.checked = isSelected;
        }
        return false;
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allWebappsSelected = true;
        return false;
    }

    function resetVars() {
        allWebappsSelected = false;

        var isSelected = false;
        if (document.webappsForm.webappFileName[0] != null) { // there is more than 1 sg
            for (var j = 0; j < document.webappsForm.webappFileName.length; j++) {
                if (document.webappsForm.webappFileName[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.webappsForm.webappFileName != null) { // only 1 sg
            if (document.webappsForm.webappFileName.checked) {
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
<h2><fmt:message key="running.webapps"/></h2>

<div id="workArea">
<form action="index.jsp" name="searchForm">
    <table class="styledLeft">
        <tr>
            <td style="border:0; !important">
                <nobr>
                    <%= numOfWebapps%> <fmt:message key="running.webapps"/>.&nbsp;
                    <%
                        if (numOfFaultyWebapps > 0) {
                    %>
                    <u>
                        <a href="faulty_webapps.jsp">
                            <font color="red"><%= numOfFaultyWebapps%>
                                <fmt:message key="faulty.webapps"/>
                            </font>
                        </a>
                    </u>
                    <%
                        }
                    %>
                </nobr>
            </td>
        </tr>
        <tr>
            <td style="border:0; !important">&nbsp;</td>
        </tr>
        <tr>
            <td>
                <table style="border:0; !important">
                    <tbody>
                    <tr style="border:0; !important">
                        <td style="border:0; !important">
                            <nobr>
                                <fmt:message key="webapp.state"/>
                                <select name="webappState">
                                    <%
                                        for (String state : new String[]{"Started", "Stopped"}) {
                                            if (webappState.equalsIgnoreCase(state)) {
                                    %>
                                    <option value="<%= state%>" selected="selected">
                                        <%= state%>
                                    </option>
                                    <%
                                    } else {
                                    %>
                                    <option value="<%= state%>"><%= state%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </nobr>
                        </td>
                        <td style="border:0; !important">
                            <nobr>
                                <fmt:message key="search.webapps"/>
                                <input type="text" name="webappSearchString"
                                       value="<%= webappSearchString != null? webappSearchString : ""%>"/>&nbsp;
                            </nobr>
                        </td>
                        <td style="border:0; !important">
                            <a class="icon-link" href="#"
                               style="background-image: url(images/search.gif);"
                               onclick="searchWebapps(); return false;"
                               alt="<fmt:message key="search"/>">
                            </a>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
    </table>
</form>

<p>&nbsp;</p>
<%
    if (webapps != null) {
        String parameters = "webappSearchString=" + webappSearchString;
        String extraHtml;
        if (webappState.equalsIgnoreCase("started")) {
            extraHtml = "<a href='#' onclick='expireSessions()' class='item-selector-link' style='background-image:url(images/expire_session.gif)';>" +
                        bundle.getString("webapps.expire.sessions") + "</a>" +
                        "<a href='#' onclick='reloadWebapps()' class='item-selector-link' style='background-image:url(images/reload.gif)';> " +
                        bundle.getString("webapps.reload") + "</a>" +
                        "<a href='#' onclick='stopWebapps()' class='item-selector-link' style='background-image:url(images/stop.gif)';> " +
                        bundle.getString("webapps.stop") + "</a>";
        } else if (webappState.equalsIgnoreCase("stopped")) {
            extraHtml = "<a href='#' onclick='startWebapps()' class='item-selector-link' style='background-image:url(images/start.gif)';> " +
                        bundle.getString("webapps.start") + "</a>";
        } else {
            throw new RuntimeException("Unknown webapp state: " + webappState);
        }
%>

<carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=parameters%>"/>
<carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                          selectAllFunction="selectAllInAllPages()"
                          selectNoneFunction="selectAllInThisPage(false)"
                          resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveFunction="deleteWebapps()"
                          addRemoveButtonId="delete1"
                          addRemoveKey="delete"
                          numberOfPages="<%=numberOfPages%>"
                          extraHtml="<%= extraHtml%>"/>
<p>&nbsp;</p>
    <%--<table id="deployedServiceReport">
            <thead>
            <tr>
                <%
                    boolean reportingContext = CarbonUIUtil.isContextRegistered(config, "/reporting/");

                    if (reportingContext) {
                %>
                <td>

                    <jsp:include page="../reporting/service_mgt.jsp" flush="true">
                        <jsp:param name="template" value="service_group_data"/>
                        <jsp:param name="component" value="service-mgt"/>
                    </jsp:include>
                </td>
                <%
                    }
                %>
            </tr>
            </thead>
    </table>--%>
<form action="delete_webapps.jsp" name="webappsForm" method="post">
    <input type="hidden" name="pageNumber" value="<%= pageNumber%>"/>
    <input type="hidden" name="webappState" value="<%= webappState %>"/>
    <table class="styledLeft" id="webappsTable" width="100%">
        <thead>
        <tr>
            <th>&nbsp;</th>
            <th><fmt:message key="webapp.context"/></th>
            <th>
                <nobr><fmt:message key="webapp.display.name"/></nobr>
            </th>
            <th>
                <nobr><fmt:message key="webapp.state"/></nobr>
            </th>
            <th width="8%" style="text-align: right; padding-right: 5px; !important"><fmt:message
                    key="webapp.sessions"/></th>
            <th>
                <nobr><fmt:message key="webapp.file"/></nobr>
            </th>
            <th>
                <nobr><fmt:message key="webapp.last.modified"/></nobr>
            </th>
            <% if (webappState.equalsIgnoreCase("started")) { %>
            <th colspan="2"><fmt:message key="webapp.action"/></th>
            <% } else { %>
            <th><fmt:message key="webapp.action"/></th>
            <%  }   %>
        </tr>
        </thead>
        <tbody>

        <%
            int position = 0;
            String urlPrefix = "http://" + webappsWrapper.getHostName() + ":" +
                               webappsWrapper.getHttpPort();
            for (WebappMetadata webapp : webapps) {
                String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                position++;
                String webappURL = urlPrefix + webapp.getContext();
        %>

        <tr bgcolor="<%= bgColor%>">
            <td width="10px" style="text-align:center; !important">
                <input type="checkbox" name="webappFileName"
                       value="<%= webapp.getWebappFile() %>"
                       onclick="resetVars()" class="chkBox"/>
            </td>
            <td>
                <a href="webapp_info.jsp?webappFileName=<%= webapp.getWebappFile()%>&webappState=<%= webappState %>&hostName=<%= webappsWrapper.getHostName()%>&httpPort=<%= webappsWrapper.getHttpPort()%>">
                    <%= webapp.getContext()%>
                </a>
            </td>
            <td><%= (webapp.getDisplayName() != null ? webapp.getDisplayName() : "") %>
            </td>
            <td><%= (webapp.getState() != null ? webapp.getState() : "") %>
            </td>
            <td style="text-align: right; !important">
                <%
                    if (webapp.getStatistics().getActiveSessions() != 0) {
                %>
                <a href="sessions.jsp?webappFileName=<%= webapp.getWebappFile() %>">
                    <%= webapp.getStatistics().getActiveSessions() %>
                </a>
                <%
                } else {
                %>
                <%= webapp.getStatistics().getActiveSessions() %>
                <%
                    }
                %>
            </td>
            <td>
                <%= webapp.getWebappFile()%>
            </td>
            <td>
                <%
                    SimpleDateFormat dateFormatter =
                            new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
                    String lastModified = dateFormatter.format(webapp.getLastModifiedTime());
                %>
                <nobr><%= lastModified %>
                </nobr>
            </td>
            <% if (webappState.equalsIgnoreCase("started")) {
            %>
            <td>
                <a href="<%= webappURL %>" target="_blank" class="icon-link"
                   style='background-image:url(images/goto_url.gif)'>
                    <fmt:message key="go.to.url"/>
                </a>
            </td>
            <% } %>
            <td>
                <a href="download-ajaxprocessor.jsp?name=<%= webapp.getWebappFile()%>" target="_self"
                        style='background-image:url(images/download.gif)' class="icon-link">
                    <fmt:message key="download"/>
                </a>
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
                          resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveFunction="deleteWebapps()"
                          addRemoveButtonId="delete2"
                          addRemoveKey="delete"
                          numberOfPages="<%=numberOfPages%>"
                          extraHtml="<%= extraHtml%>"/>
<carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%= parameters%>"/>
<%
} else {
%>
<b><fmt:message key="no.webapps.found"/></b>
<%
    }
%>
</div>
</div>
</fmt:bundle>
