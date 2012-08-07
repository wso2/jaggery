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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.webapp"
                       resourceBundle="org.jaggeryjs.jaggery.app.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <script type="text/javascript">
        function validate() {

            if (document.webappUploadForm.warFileName.value != null) {
                var jarinput = document.webappUploadForm.warFileName.value;
                if (jarinput == '') {
                    CARBON.showWarningDialog('<fmt:message key="select.webapp.file"/>');
                } else if (jarinput.lastIndexOf(".zip") == -1) {
                    CARBON.showWarningDialog('<fmt:message key="invalid.webapp.file"/>');
                } else {
                    document.webappUploadForm.submit();
                }
            } else if (document.webappUploadForm.warFileName[0].value != null) {
                var validFileNames = true;

                for (var i=0; i<document.webappUploadForm.warFileName.length; i++) {
                    var jarinput = document.webappUploadForm.warFileName[i].value;
                    if (jarinput == '') {
                        CARBON.showWarningDialog('<fmt:message key="select.webapp.file"/>');
                        validFileNames = false; break;
                    } else if (jarinput.lastIndexOf(".zip") == -1) {
                        CARBON.showWarningDialog('<fmt:message key="invalid.webapp.file"/>');
                        validFileNames = false; break;
                    }
                }

                if(validFileNames) {
                    document.webappUploadForm.submit();
                } else {
                    return;
                }

            }

        }

        var rows = 1;
        //add a new row to the table
        function addRow() {
            rows++;

            //add a row to the rows collection and get a reference to the newly added row
            var newRow = document.getElementById("webappTbl").insertRow(-1);
            newRow.id = 'file' + rows;

            var oCell = newRow.insertCell(-1);
            oCell.innerHTML = '<label><fmt:message key="webapp.archive"/> (.zip)<font color="red">*</font></label>';
            oCell.className = "formRow";

            oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<input type='file' name='warFileName' size='50'/>&nbsp;&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file"+ rows +"');\" />";
            oCell.className = "formRow";

            alternateTableRows('webappTbl', 'tableEvenRow', 'tableOddRow');
        }

        function deleteRow(rowId) {
            var tableRow = document.getElementById(rowId);
            tableRow.parentNode.deleteRow(tableRow.rowIndex);
            alternateTableRows('webappTbl', 'tableEvenRow', 'tableOddRow');
        }


    </script>

    <div id="middle">
        <h2><fmt:message key="upload.web.application"/></h2>

        <div id="workArea">
            <form method="post" name="webappUploadForm" action="../../fileupload/jaggeryapp"
                  enctype="multipart/form-data" target="_self">
                <input type="hidden" name="errorRedirectionPage"
                            value="../carbon/jaggeryapp-mgt/uploadjaggeryapp.jsp?region=region1&item=webapps_add_menu"/>
                <label style="font-weight:bold;">&nbsp;<fmt:message key="upload.new.webapp"/> (.zip)</label>
                <br/><br/>

                <table class="styledLeft" id="webappTbl">
                    <tr>
                        <td class="formRow">
                            <label><fmt:message key="webapp.archive"/> (.zip)<font color="red">*</font></label>
                        </td>
                        <td class="formRow">
                            <input type="file" name="warFileName" size="50"/>&nbsp;
                            <input type="button"  width='20px' class="button" onclick="addRow();" value=" + "/>
                        </td>
                    </tr>
                </table>

                <table class="styledLeft">
                    <tr>
                        <td class="buttonRow">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="upload"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button"
                                   onclick="location.href='../webapp-list/index.jsp'"
                                   value=" <fmt:message key="cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('webappTbl', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
