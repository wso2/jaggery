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
<%@ page import="org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappMetadata" %>
<%@ page import="org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappsWrapper" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    JaggeryAdminClient client;
    WebappsWrapper faultyWebappsWrapper;
    try {
        client = new JaggeryAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        faultyWebappsWrapper = client.getPagedFaultyWebappsSummary("", pageNumberInt);
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>

<div id="middle">
    <div id="workArea">
        <fmt:bundle basename="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources">
            <carbon:breadcrumb
                    label="faulty.webapps"
                    resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                    topPage="false"
                    request="<%=request%>"/>
            <h2><fmt:message key="faulty.webapps"/></h2>
        </fmt:bundle>
        <%
            int numberOfPages;
            if (faultyWebappsWrapper == null) {
        %>
        <fmt:bundle basename="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources">
            <p><fmt:message key="no.faulty.webapps.found"/></p>
        </fmt:bundle>
        <%
                return;
            }

            WebappMetadata[] faultyWebapps = faultyWebappsWrapper.getWebapps();
            numberOfPages = faultyWebappsWrapper.getNumberOfPages();
        %>

        <% if (faultyWebapps == null || faultyWebapps.length == 0) { %>
        <fmt:bundle basename="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources">
            <p><fmt:message key="no.faulty.webapps.found"/></p>
        </fmt:bundle>
        <%
                return;
            }
        %>
        <fmt:bundle basename="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources">
            <script type="text/javascript">
                var allWebappsSelected = false;
                function showError(divId) {
                    if (document.getElementById(divId).style.visibility == 'visible') {
                        document.getElementById(divId).style.visibility = 'hidden';
                    } else {
                        document.getElementById(divId).style.visibility = 'visible';
                    }
                }

                function deleteFaultyWebapps() {
                    var selected = false;
                    if (document.faultyWebappsForm.webappFileName[0] != null) { // there is more than 1 sg
                        for (var j = 0; j < document.faultyWebappsForm.webappFileName.length; j++) {
                            selected = document.faultyWebappsForm.webappFileName[j].checked;
                            if (selected) break;
                        }
                    } else if (document.faultyWebappsForm.webappFileName != null) { // only 1 sg
                        selected = document.faultyWebappsForm.webappFileName.checked;
                    }
                    if (!selected) {
                        CARBON.showInfoDialog('<fmt:message key="select.webapps.to.be.deleted"/>');
                        return;
                    }
                    if (allWebappsSelected) {
                        CARBON.showConfirmationDialog("<fmt:message key="delete.selected.faulty.webapps.prompt"><fmt:param value="<%= faultyWebappsWrapper.getNumberOfFaultyWebapps() %>"/></fmt:message>",
                                                      function() {
                                                          location.href = 'delete_faulty_webapps.jsp?deleteAllWebapps=true';
                                                      });
                    } else {
                        CARBON.showConfirmationDialog("<fmt:message key="delete.all.faulty.webapps.prompt"/>", function() {
                            document.faultyWebappsForm.submit();
                        });
                    }
                }

                function selectAllInThisPage(isSelected) {
                    allWebappsSelected = false;
                    if (document.faultyWebappsForm.webappFileName[0] != null) { // there is more than 1 sg
                        if (isSelected) {
                            for (var j = 0; j < document.faultyWebappsForm.webappFileName.length; j++) {
                                document.faultyWebappsForm.webappFileName[j].checked = true;
                            }
                        } else {
                            for (j = 0; j < document.faultyWebappsForm.webappFileName.length; j++) {
                                document.faultyWebappsForm.webappFileName[j].checked = false;
                            }
                        }
                    } else if (document.faultyWebappsForm.webappFileName != null) { // only 1 sg
                        document.faultyWebappsForm.webappFileName.checked = isSelected;
                    }
                }

                function selectAllInAllPages() {
                    selectAllInThisPage(true);
                    allWebappsSelected = true;
                }

                function resetVars() {
                    allWebappsSelected = false;

                    var isSelected = false;
                    if (document.faultyWebappsForm.webappFileName[0] != null) { // there is more than 1 sg
                        for (var j = 0; j < document.faultyWebappsForm.webappFileName.length; j++) {
                            if (document.faultyWebappsForm.webappFileName[j].checked) {
                                isSelected = true;
                            }
                        }
                    } else if (document.faultyWebappsForm.webappFileName != null) { // only 1 sg
                        if (document.faultyWebappsForm.webappFileName.checked) {
                            isSelected = true;
                        }
                    }
                }
            </script>
            <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                      selectAllFunction="selectAllInAllPages()"
                                      selectNoneFunction="selectAllInThisPage(false)"
                                      addRemoveFunction="deleteFaultyWebapps()"
                                      addRemoveButtonId="delete1"/>
            <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                              page="faulty_services.jsp" pageNumberParameterName="pageNumber"/>
            <p>&nbsp;</p>

            <form action="delete_faulty_webapps.jsp" name="faultyWebappsForm">
                <table class="styledLeft" id="faultyWebappsTable">
                    <thead>
                    <tr>
                        <th>&nbsp;</th>
                        <th><fmt:message key="faulty.webapp.file"/></th>
                        <th><fmt:message key="fault.reason"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%

                        int count = 0;
                        for (WebappMetadata webapp : faultyWebapps) {
                            if (webapp != null) {
                                count++;
                                String webappFile = webapp.getWebappFile();
                    %>
                    <tr>
                        <td>
                            <input type="checkbox" name="webappFileName"
                                   value="<%= webappFile %>"
                                   onclick="resetVars()"/>
                        </td>
                        <td width="300px">
                            <%=webappFile%>
                        </td>
                        <td>
                            <%= webapp.getFaultException() %>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                    </tbody>
                </table>
            </form>
            <p>&nbsp;</p>
            <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                              page="faulty_services.jsp" pageNumberParameterName="pageNumber"/>
            <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                      selectAllFunction="selectAllInAllPages()"
                                      selectNoneFunction="selectAllInThisPage(false)"
                                      addRemoveFunction="deleteFaultyWebapps()"
                                      addRemoveButtonId="delete2"/>
        </fmt:bundle>
        <script type="text/javascript">
            alternateTableRows('faultyWebappsTable', 'tableEvenRow', 'tableOddRow');
        </script>
    </div>
</div>
