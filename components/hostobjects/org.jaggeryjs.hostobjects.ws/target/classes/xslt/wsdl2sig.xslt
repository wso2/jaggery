<?xml version="1.0" encoding="UTF-8"?>
<!--
    /*
    * Copyright 2007 WSO2, Inc. http://www.wso2.org
    *
    * Licensed under the Apache License, Version 2.0 (the "License");
    * you may not use this file except in compliance with the License.
    * You may obtain a copy of the License at
    *
    * http://www.apache.org/licenses/LICENSE-2.0
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
    */

    This stylesheet does some predigestion of WSDL 2.0 documents to make it easier to locate
    binding and message type information for each operation in a service.  The type information
    is further digested to indicate a mapping into simple function signatures.

    Created by Jonathan Marsh, jonathan@wso2.com
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:wsdl="http://www.w3.org/ns/wsdl" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:wrpc="http://www.w3.org/ns/wsdl/rpc" xmlns:wsoap="http://www.w3.org/ns/wsdl/soap" xmlns:whttp="http://www.w3.org/ns/wsdl/http"
    xmlns:wsdlx="http://www.w3.org/ns/wsdl-extensions" xmlns:wsaw="http://www.w3.org/2006/05/addressing/wsdl"
    xmlns:wsp12="http://schemas.xmlsoap.org/ws/2004/09/policy" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
    xmlns:sp="http://schemas.xmlsoap.org/ws/2005/07/securitypolicy"
    xmlns:exslt="http://exslt.org/common"
    exclude-result-prefixes="wsdl wrpc wsoap whttp wsdlx wsaw">
    <xsl:output method="xml" indent="yes"/>
    <!-- copy and paste from http://dev.w3.org/2002/ws/desc/test-suite/results/wsdl-xslt/wsdl-component-model.xslt
         defining collections (nodesets) representing all the top-level types whereever we can find them (through)
         imports, includes, etc.)

         copy from here:***************** -->
    <xsl:variable name="root" select="/wsdl:description"/>
    <xsl:variable name="imported-wsdl"
        select="document($root[wsdl:include]/wsdl:include/@location)/wsdl:description |
        document($root[wsdl:import]/wsdl:import/@location)/wsdl:description"/>
    <!-- global (including imports/includes) collections of types that may be referred to later -->
    <xsl:variable name="all-interfaces" select="$root/wsdl:interface |
        $imported-wsdl/wsdl:interface"/>
    <xsl:variable name="all-operations" select="$all-interfaces/wsdl:operation"/>
    <xsl:variable name="all-faults" select="$all-interfaces/wsdl:fault"/>
    <xsl:variable name="all-bindings" select="$root/wsdl:binding | $imported-wsdl/wsdl:binding"/>
    <xsl:variable name="all-services" select="$root/wsdl:service| $imported-wsdl/wsdl:service"/>
    <!--
        Note - limited levels of Schema import/include functionality:
        Supported scenarios:
        - Embedded schemas (multiple schemas OK)
        - Schema directly pointed to by wsdl:types/xs:import/@schemaLocation.
        - Schema directly pointed to by wsdl:types/xs:schema/xs:import/@schemaLocation.
        - Above scenarios, when resulting from direct import/include of WSDL.
        - Chameleon includes (namespace specified on the include, not in the included schema.)
        Unsupported scenarios:
        - Schema imported or included indirectly (except as above).
        - Schema imported without @schemaLocation attribute (no catalog support).
    -->
    <xsl:param name="base-uri" select="''"/>

    <xsl:variable name="imported-schema"
                  select="document($root/wsdl:types[xs:import]/xs:import[@schemaLocation and
        not(starts-with(@schemaLocation,'#'))]/@schemaLocation)/xs:schema |
        document($imported-wsdl/wsdl:types[xs:import]/xs:import[@schemaLocation and
        not(starts-with(@schemaLocation,'#'))]/@schemaLocation)/xs:schema |
        $imported-wsdl/wsdl:types/xs:schema"/>

    <xsl:variable name="schemas">
        <xsl:call-template name="process-schemas">
            <xsl:with-param name="inline-schemas" select="$root/wsdl:types"/>
            <xsl:with-param name="imported-schemas" select="$imported-schema"/>
        </xsl:call-template>
    </xsl:variable>
    <!-- global (including imports/includes) collections of types that may be referred to later -->
    <xsl:variable name="all-elements" select="$schemas/xs:schema/xs:element"/>
    <xsl:variable name="all-types" select="$schemas/xs:schema/xs:simpleType | $schemas/xs:schema/xs:complexType"/>
    <xsl:variable name="all-attributeGroups" select="$schemas/xs:schema/xs:attributeGroup"/>
    <xsl:variable name="all-attributes" select="$schemas/xs:schema/xs:attribute"/>
    <xsl:variable name="all-groups" select="$schemas/xs:schema/xs:group"/>

    <!-- limit for recursive elements and type refs -->
    <xsl:variable name="recursive-limit" select="50"/>
    <!-- to here.***************** -->
    <xsl:template match="wsdl:description">

        <!-- <services> : container for all the services represented in this signature file. -->
        <services>
            <!-- Hoist from endpoints, through bindings, through interfaces, to message schemas.
                 Most of the data in each service will be duplicated but having it in predigested
                 format is more valuable than duplication. -->
            <xsl:for-each select="$all-services/wsdl:endpoint">
                <xsl:variable name="interfaceLocalName" select="substring-after(../@interface,':')"/>
                <!-- <service> : representing all the capabilities exposed by a single endpoint.
                     Generate one per endpoint so a downstream processor can choose which
                     service they'd like to process (possibly dynamically).
                       @name : name of the service
                       @endpoint : name of the endpoint
                       @address : endopint address (url)
                       @type : canonical token representing the type of binding
                     -->
                <service name="{ancestor::wsdl:service/@name}" endpoint="{@name}"
                    address="{@address}">
                    <!-- resolve indirection to a wsdl:binding -->
                    <xsl:variable name="binding" select="substring-after(@binding,':')"/>
                    <xsl:variable name="thisBinding" select="$all-bindings[@name = $binding]"/>
                    <xsl:for-each select="$thisBinding">
                        <!-- Only the WSDL 2.0 standard bindings are supported. -->
                        <xsl:choose>
                            <xsl:when test="@type='http://www.w3.org/ns/wsdl/soap' and
                                (not(@wsoap:version) or @wsoap:version='1.2')">
                                <xsl:attribute name="type">SOAP12</xsl:attribute>
                            </xsl:when>
                            <xsl:when test="@type='http://www.w3.org/ns/wsdl/http'">
                                <xsl:attribute name="type">HTTP</xsl:attribute>
                            </xsl:when>
                            <xsl:when test="@type='http://www.w3.org/ns/wsdl/soap' and
                                @wsoap:version='1.1'">
                                <xsl:attribute name="type">SOAP11</xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:comment>Unknown binding type: </xsl:comment>
                                <xsl:copy-of select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                    <documentation>
                        <xsl:copy-of select="/wsdl:description/wsdl:documentation/node()"/>
                    </documentation>
                    <!-- <operations> : container for operations exposed by the interface for this endpoint.
                            @name : interface name
                    -->
                    <operations name="{$interfaceLocalName}">
                        <xsl:for-each select="$all-interfaces[@name = $interfaceLocalName]">
                            <xsl:sort select="@name"/>
                            <!-- returns a list of all the interface names, including extended interfaces, in the form
                                 '[interfacename1][interfacename2]...'.  Workaround for the lack of sets of strings in XSLT.-->
                            <xsl:variable name="interfaces-extended-by-this-one">
                                <xsl:call-template name="interface-list">
                                    <xsl:with-param name="interface" select="."/>
                                </xsl:call-template>
                            </xsl:variable>
                            <!-- loop through all the operations of this interface, or any interface it extends.  -->
                            <xsl:for-each select="$all-operations[contains($interfaces-extended-by-this-one,
                                concat('[',ancestor::wsdl:interface/@name,']'))]">
                                <xsl:sort select="@name"/>
                                <xsl:variable name="thisInterfaceOperation" select="."/>
                                <!-- <operation> : represents the details for a particular operation.
                                       @name : operation name
                                       @pattern : uri representing the MEP
                                       @safe : canonical value of the safety property
                                -->
                                <operation name="{@name}" pattern="{@pattern}" safe="{@wsdlx:safe = 'true' or @wsdlx:safe = '1'}">
                                    <documentation>
                                        <xsl:copy-of select="wsdl:documentation/node()"/>
                                    </documentation>
                                    <!-- <signature> : capture the details of the schema types relevant to generating a function signature
                                           @method : how the signature was computed ('rpc-signature' hints, or 'inference' from the
                                                     schema structures themselves.)
                                    -->
                                    <signature>
                                        <xsl:choose>
                                            <xsl:when test="@wrpc:signature">
                                                <xsl:attribute name="method">rpc-signature</xsl:attribute>
                                                <xsl:call-template name="rpc-signature">
                                                    <xsl:with-param name="operation" select="."/>
                                                    <xsl:with-param name="types-callstack" select="/.."/>
                                                </xsl:call-template>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:attribute name="method">inference</xsl:attribute>
                                                <xsl:call-template name="infer-params">
                                                    <xsl:with-param name="operation" select="."/>
                                                    <xsl:with-param name="types-callstack" select="/.."/>
                                                </xsl:call-template>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </signature>
                                    <xsl:for-each select="$thisBinding/wsdl:operation[substring-after(@ref, ':') = current()/@name]">
                                        <!-- <binding-details> : record any other information specific to an operation that
                                                                 will enable successful communication with the endpoint.
                                        -->
                                        <binding-details>
                                            <!-- soap-only binding details include:
                                                   @wsawaction : WS-Addressing explicit action value
                                                   @soapaction : explicitly declared soap action value
                                                 soap and http binding details include:
                                                   @method : {http method} property
                                                   @httplocation : {http location} property
                                                   @httignoreUncited : {http location ignore uncited} property
                                                   @httpqueryParameterSeparator : cascading value for the {http query parameter separator} property
                                                 http-only binding details include:
                                                   @httpinputSerialization : {http input serialization} property
                                            -->
                                            <xsl:if test="../@type = 'http://www.w3.org/ns/wsdl/soap'">
                                                <xsl:if test="$thisInterfaceOperation/wsdl:input/@wsaw:Action">
                                                    <xsl:attribute name="wsawaction"><xsl:value-of select="$thisInterfaceOperation/wsdl:input/@wsaw:Action"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@wsoap:action">
                                                    <xsl:attribute name="soapaction"><xsl:value-of select="@wsoap:action"/></xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="@wsoap:soapaction">
                                                    <xsl:attribute name="soapaction"><xsl:value-of select="@wsoap:soapaction"/></xsl:attribute>
                                                </xsl:if>
                                                <!-- method can't be explicitly set for a soap binding, but can be implied by the SOAP 1.2 Response MEP. -->
                                                <xsl:if test="../@wsoap:version='1.2' and ../@wsoap:protocol='http://www.w3.org/2003/05/soap/mep/soap-response/' and (@wsoap:mep='http://www.w3.org/2003/05/soap/mep/soap-response/' or (not(@wsoap:mep) and ../@wsoap:mepDefault='http://www.w3.org/2003/05/soap/mep/soap-response/'))">
                                                    <xsl:attribute name="httpmethod">GET</xsl:attribute>
                                                </xsl:if>
                                            </xsl:if>
                                            <xsl:if test="@whttp:location">
                                                <xsl:attribute name="httplocation"><xsl:value-of select="@whttp:location"/></xsl:attribute>
                                            </xsl:if>
                                            <xsl:if test="@whttp:ignoreUncited">
                                                <xsl:attribute name="httpignoreUncited"><xsl:value-of select="@whttp:ignoreUncited"/></xsl:attribute>
                                            </xsl:if>
                                            <xsl:choose>
                                                <xsl:when test="@whttp:queryParameterSeparator">
                                                    <xsl:attribute name="httpqueryParameterSeparator"><xsl:value-of select="@whttp:queryParameterSeparator"/></xsl:attribute>
                                                </xsl:when>
                                                <xsl:when test="../@whttp:queryParameterSeparatorDefault">
                                                    <xsl:attribute name="httpqueryParameterSeparator"><xsl:value-of select="../@whttp:queryParameterSeparatorDefault"/></xsl:attribute>
                                                </xsl:when>
                                            </xsl:choose>
                                            <xsl:if test="../@type = 'http://www.w3.org/ns/wsdl/http'">
                                                <xsl:if test="@whttp:inputSerialization">
                                                    <xsl:attribute name="httpinputSerialization"><xsl:value-of select="@whttp:inputSerialization"/></xsl:attribute>
                                                </xsl:if>
                                                <!-- HTTP method can be set explicitly, defaulted, or implied by wsdlx:safe -->
                                                <xsl:choose>
                                                    <xsl:when test="@whttp:method">
                                                        <xsl:attribute name="httpmethod"><xsl:value-of select="@whttp:method"/></xsl:attribute>
                                                    </xsl:when>
                                                    <xsl:when test="../@whttp:methodDefault">
                                                        <xsl:attribute name="httpmethod"><xsl:value-of select="../@whttp:methodDefault"/></xsl:attribute>
                                                    </xsl:when>
                                                    <xsl:when test="$thisInterfaceOperation/@wsdlx:safe = 'true' or $thisInterfaceOperation/@wsdlx:safe = '1'">
                                                        <xsl:attribute name="httpmethod">GET</xsl:attribute>
                                                    </xsl:when>
                                                </xsl:choose>
                                            </xsl:if>
                                            <xsl:for-each select="*/wsoap:header">
                                                <soapheader for="{local-name(parent::*)}">
                                                    <xsl:copy-of select="@required | @mustUnderstand"/>
                                                    <xsl:variable name="element">
                                                        <xsl:call-template name="local-name">
                                                            <xsl:with-param name="qname" select="@element"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:variable name="element-prefix">
                                                        <xsl:call-template name="prefix">
                                                            <xsl:with-param name="qname" select="@element"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:variable name="element-namespace">
                                                        <xsl:value-of select="namespace::*[local-name() = $element-prefix]"/>
                                                    </xsl:variable>

                                                    <!-- here type should be corrected to element -->
                                                    <xsl:attribute name="type">
                                                        <xsl:value-of select="$element"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="type-namespace">
                                                        <xsl:value-of select="$element-namespace"/>
                                                    </xsl:attribute>

                                                    <xsl:variable name="element-node"
                                                        select="$all-elements[@name=$element and ancestor::*[local-name()='schema' and @targetNamespace=$element-namespace]][1]"/>
                                                    <xsl:copy-of select="*"/>
                                                    <!-- header schema is parameterized here -->
                                                    <xsl:call-template name="complex-param">
                                                       <xsl:with-param name="this-element" select="$element-node"/>
                                                       <xsl:with-param name="direction" select="local-name(parent::*)"/>
                                                       <xsl:with-param name="recursive-index" select="1"/>
                                                       <xsl:with-param name="types-callstack" select="/.."/>
                                                    </xsl:call-template>
                                                </soapheader>
                                            </xsl:for-each>
                                            <xsl:for-each select="whttp:header">
                                                <httpheader for="{local-name(parent::*)}">
                                                    <xsl:copy-of select="@required"/>
                                                    <xsl:attribute name="type">
                                                        <xsl:call-template name="local-name">
                                                            <xsl:with-param name="qname" select="@element"/>
                                                        </xsl:call-template>
                                                    </xsl:attribute>
                                                    <xsl:variable name="element-prefix">
                                                        <xsl:call-template name="prefix">
                                                            <xsl:with-param name="qname" select="@element"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:variable name="element-namespace">
                                                        <xsl:value-of select="namespace::*[local-name() = $element-prefix]"/>
                                                    </xsl:variable>
                                                       <xsl:attribute name="type-namespace">
                                                           <xsl:if test="@element">
                                                        <xsl:value-of select="$element-namespace"/>
                                                           </xsl:if>
                                                    </xsl:attribute>
                                                    <xsl:copy-of select="*"/>
                                                </httpheader>
                                            </xsl:for-each>
                                            <!-- Note this only looks for a single specific pattern exposed by the Mashup Server to determine whether usernameToken should be engaged. -->
                                            <xsl:for-each select="/wsdl:description/wsp12:Policy[@wsu:Id = substring-after($all-interfaces[@name = $interfaceLocalName]/wsp12:PolicyReference/@URI,'#')] | ../wsp12:Policy">
                                                <policy type="{@wsu:Id}"/>
                                            </xsl:for-each>
                                        </binding-details>
                                    </xsl:for-each>
                                </operation>
                            </xsl:for-each>
                        </xsl:for-each>
                    </operations>
                </service>
            </xsl:for-each>
        </services>
    </xsl:template>

    <!-- A pair of recursive templates to list extended interfaces
         since we operat on flat lists, just having a list of names is sufficient.  An actual nodeset
         would be quite difficult to construct without copying (which loses context and the ability
         to further query the result in a standard way). Result should look like:
            [interfacename1][interfacename2]...
         The square brackets provide convenient delimiters so that we can search the list for accurate
         matches (including delimiters) and not get false positives when one interface name contains
         another.
    -->
    <xsl:template name="interface-list">
        <xsl:param name="interface"/>
        <xsl:text>[</xsl:text>
        <xsl:value-of select="$interface/@name"/>
        <xsl:text>]</xsl:text>
        <!-- append the names of any interfaces that this one extends -->
        <xsl:if test="$interface/@extends">
            <xsl:call-template name="extended-interface-list">
                <xsl:with-param name="interface-names" select="$interface/@extends"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <xsl:template name="extended-interface-list">
        <xsl:param name="interface-names"/>
        <!-- split off the first QName in a space-delimited list -->
        <xsl:variable name="qnamesplit" select="concat(normalize-space($interface-names),' ')"/>
        <xsl:variable name="firstqname" select="substring-before($qnamesplit,' ')"/>
        <xsl:variable name="remainder" select="substring-after($qnamesplit,' ')"/>
        <xsl:variable name="interface-local-name">
            <xsl:call-template name="local-name">
                <xsl:with-param name="qname" select="$firstqname"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- add the first QName to the list (recursively processing any other extends attributes) -->
        <xsl:call-template name="interface-list">
            <xsl:with-param name="interface" select="$all-interfaces[@name=$interface-local-name]"/>
        </xsl:call-template>
        <!-- recursively process the remainder of the list until nothing is left. -->
        <xsl:if test="$remainder != ''">
            <xsl:call-template name="extended-interface-list">
                <xsl:with-param name="interface-names" select="$remainder"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>


    <!-- Template to extract relevant details about the signature from the wrpc:signature extension.
           $signature : string representing the value of the wrpc:signature extension.
           $operation : node representing the operation for which to generate parameters -->
     <xsl:template name="rpc-signature">
        <xsl:param name="operation"/>
        <xsl:param name="types-callstack"/>

        <!-- inputs -->
        <xsl:call-template name="generate-rpc-params">
            <xsl:with-param name="signature" select="@wrpc:signature"/>
            <xsl:with-param name="direction" select="'#in'"/>
            <xsl:with-param name="element-name" select="$operation/wsdl:input/@element"/>
            <xsl:with-param name="element-namespace" select="$operation/wsdl:input/namespace::*[local-name() =
                substring-before($operation/wsdl:input/@element,':')]"/>
            <xsl:with-param name="types-callstack" select="$types-callstack"/>
        </xsl:call-template>

        <!-- outputs -->
        <!-- The following <if> implies we only support the in-out, in-only, and robust-in-only MEPs -->
        <xsl:if test="$operation/@pattern = 'http://www.w3.org/ns/wsdl/in-out'">
            <xsl:call-template name="generate-rpc-params">
                <xsl:with-param name="signature" select="@wrpc:signature"/>
                <xsl:with-param name="direction" select="'#return'"/>
                <xsl:with-param name="element-name" select="$operation/wsdl:output/@element"/>
                <xsl:with-param name="element-namespace" select="$operation/wsdl:output/namespace::*[local-name() =
                    substring-before($operation/wsdl:output/@element,':')]"/>
                <xsl:with-param name="types-callstack" select="$types-callstack"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
     <!-- Helper template for "rpc-signature", generating the <params>/<returns> element
            $signature : rpc-signature string to use
            $direction : '#in' | '#return' signifying input/return parameter types
            $element-name : local name of the element declaration associated with this parameter
            $element-namespace : namespace of the element declaration associated with this parameter.
     -->
     <xsl:template name="generate-rpc-params">
        <xsl:param name="signature"/>
        <xsl:param name="direction"/>
        <xsl:param name="element-name"/>
        <xsl:param name="element-namespace"/>
        <xsl:param name="types-callstack"/>
        <!-- <params>/<returns> : wrapper for input/output parameters
                @wrapper-element : localName of the RPC wrapper element (implied by the WSDL 2.0 RPC style) if there is one
                @wrapper-element-ns : namespace URI of the RPC wrapper element
             <param> : represent a parameter in a function signature
                @name : name of the parameter
                @type : localName of schema type for the parameter
                @type-namespace : namespace URI of schema type for the parameter
                @token : what type of parameter this is (#in, #return, #any wildcard)
                @simple : whether the type is simple or complex
        -->
        <xsl:variable name="containername">
            <xsl:choose>
                <xsl:when test="$direction = '#in'">params</xsl:when>
                <xsl:otherwise>returns</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:element name="{$containername}">
            <!-- Find the element declaration and its type definition. -->
            <xsl:variable name="this-element"
                select="$all-elements[@name=substring-after($element-name,':')][1]"/>
            <xsl:variable name="this-type" select="$this-element[not(@type)]/xs:complexType |
                $all-types[@name=substring-after($this-element/@type,':')][1]"/>

            <xsl:if test="contains($element-name, ':')">
                <xsl:attribute name="wrapper-element">
                    <xsl:value-of select="substring-after($element-name,':')"/>
                </xsl:attribute>
                <xsl:attribute name="wrapper-element-ns">
                    <xsl:value-of select="$element-namespace"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:call-template name="rpc-sig-param">
                <xsl:with-param name="signature" select="$signature"/>
                <xsl:with-param name="direction" select="$direction"/>
                <xsl:with-param name="wrapper-type" select="$this-type"/>
                <xsl:with-param name="types-callstack" select="$types-callstack"/>
            </xsl:call-template>

            <!-- Handle any element wildcards -->
            <xsl:for-each select="$this-type/xs:sequence/xs:any">
                <param token="#any">
                    <xsl:copy-of select="@minOccurs | @maxOccurs"/>
                    <xsl:if test="not(@minOccurs)"><xsl:attribute name="minOccurs">1</xsl:attribute></xsl:if>
                    <xsl:if test="not(@maxOccurs)"><xsl:attribute name="maxOccurs">1</xsl:attribute></xsl:if>
                </param>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <!-- Helper template for "generate-rpc-params", generating the <param/> element representing a complex type
         (possibly recursive)
            $signature : the rpc signature string
            $direction : '#in' | '#return' signifying input/return parameter types
            $this-element : node representing the element delcaration associated with this parameter
            $this-type : node representing the type definition associated with this parameter.
    -->
    <xsl:template name="rpc-sig-param">
        <xsl:param name="signature"/>
        <xsl:param name="direction"/>
        <xsl:param name="wrapper-type"/>
        <xsl:param name="types-callstack"/>

        <!-- strip off the first pair of tokens, process and recurse.  So first, check that we're not done. -->
        <xsl:if test="$signature != ''">
            <!-- qname is the first item, token is the second. -->
            <xsl:variable name="qname" select="substring-before($signature,' ')"/>
            <xsl:variable name="token" select="substring-before(substring-after(concat($signature,' '),' '),' ')"/>

            <xsl:variable name="this-element" select="$wrapper-type/xs:sequence/xs:element[@name=$qname]"/>

            <!-- This list is processed twice, filtered by $direction.  If we're looking for an in and this sig pair
                 is an input, we'll process it.  Or if we're looking for returns and this sig pair represents a
                 return, we'll process it. -->
            <xsl:if test="($direction='#in' and ($token='#in' or $token='#inout')) or ($direction='#return' and ($token='#return' or $token='#out' or $token='#inout'))">
                <xsl:call-template name="complex-param">
                    <xsl:with-param name="direction" select="$direction"/>
                    <xsl:with-param name="this-element" select="$this-element"/>
                    <xsl:with-param name="recursive-index" select="1"/>
                    <xsl:with-param name="types-callstack" select="$types-callstack"/>
                </xsl:call-template>
            </xsl:if>

            <!-- recurse, stripping the sig pair we just processed off the sig. Looping XSLT-style. -->
            <xsl:call-template name="rpc-sig-param">
                <xsl:with-param name="signature"
                    select="substring-after(substring-after($signature,' '),' ')"/>
                <xsl:with-param name="direction" select="$direction"/>
                <xsl:with-param name="wrapper-type" select="$wrapper-type"/>
                <xsl:with-param name="types-callstack" select="$types-callstack"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- Template to extract relevant details about the signature from an rpc-style service, but
         lacking the optional wrpc:signature extension.
           $operation : node representing the operation for which to generate parameters -->
     <xsl:template name="infer-params">
        <xsl:param name="operation"/>
        <xsl:param name="types-callstack"/>

        <!-- inputs -->
        <xsl:call-template name="generate-params">
            <xsl:with-param name="direction" select="'#in'"/>
            <xsl:with-param name="element-name" select="$operation/wsdl:input/@element"/>
            <xsl:with-param name="element-namespace" select="$operation/wsdl:input/namespace::*[local-name() =
                substring-before($operation/wsdl:input/@element,':')]"/>
            <xsl:with-param name="types-callstack" select="$types-callstack"/>
        </xsl:call-template>

        <!-- outputs -->
        <!-- The following <if> implies we only support the in-out, in-only, and robust-in-only MEPs -->
        <xsl:if test="$operation/@pattern = 'http://www.w3.org/ns/wsdl/in-out'">
            <xsl:call-template name="generate-params">
                <xsl:with-param name="direction" select="'#return'"/>
                <xsl:with-param name="element-name" select="$operation/wsdl:output/@element"/>
                <xsl:with-param name="element-namespace" select="$operation/wsdl:output/namespace::*[local-name() =
                    substring-before($operation/wsdl:output/@element,':')]"/>
                <xsl:with-param name="types-callstack" select="$types-callstack"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- Helper template for "infer-params", generating the <params>/<returns> element -->
    <xsl:template name="generate-params">
        <!-- $direction : '#in' | '#return' signifying input/return parameter types
             $element-name : local name of the element declaration associated with this parameter
             $element-namespace : namespace of the element declaration associated with this parameter.
        -->
        <xsl:param name="direction"/>
        <xsl:param name="element-name"/>
        <xsl:param name="element-namespace"/>
        <xsl:param name="types-callstack"/>
        <!-- <params>/<returns> : wrapper for input/output parameters
                @wrapper-element : localName of the RPC wrapper element (implied by the WSDL 2.0 RPC style) if there is one
                @wrapper-element-ns : namespace URI of the RPC wrapper element
             <param> : represent a parameter in a function signature
                @name : name of the parameter
                @type : localName of schema type for the parameter
                @type-namespace : namespace URI of schema type for the parameter
                @token : what type of parameter this is (#in, #return, #any wildcard)
                @simple : whether the type is simple or complex
        -->
        <xsl:variable name="containername">
            <xsl:choose>
                <xsl:when test="$direction = '#in'">params</xsl:when>
                <xsl:otherwise>returns</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:element name="{$containername}">
            <xsl:choose>
                <!-- If there is no expected input/output, don't generate any <param>s.  -->
                <xsl:when test="$element-name = '#none'"/>

                <!-- If any single element is expected, generate a default xs:anyType parameter type. -->
                <xsl:when test="$element-name = '#any'">
                    <param token="{$direction}" type="anyType"
                        type-namespace="http://www.w3.org/2001/XMLSchema" simple="no"/>
                </xsl:when>

                <!-- Otherwise there is a referenced XML Schema type. -->
                <xsl:otherwise>
                    <!-- Find the element declaration and its type definition. -->
                    <xsl:variable name="this-element"
                        select="$all-elements[@name=$element-name or @name=substring-after($element-name,':')][1]"/>

                    <!--required in order to handle the elements correctly -->
                    <xsl:variable name="adjusted-element-namespace" select="$this-element/ancestor::xs:schema/@targetNamespace"/>

                    <!-- the following variables will only be valid for the named types -->
                    <xsl:variable name="element-type-prefix">
                        <xsl:call-template name="prefix">
                            <xsl:with-param name="qname" select="$this-element/@type"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="element-type-name">
                        <xsl:call-template name="local-name">
                            <xsl:with-param name="qname" select="$this-element/@type"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="element-type-namespace">
                        <xsl:value-of select="$this-element/namespace::*[local-name()=$element-type-prefix]"/>
                    </xsl:variable>

                    <!-- the referred type can be either simpleType or complexType-->
                    <xsl:variable name="this-type" select="$this-element[not(@type)]/xs:*[local-name() = 'simpleType' or local-name() = 'complexType'] |
                            $all-types[@name=$element-type-name and ancestor::*[local-name()='schema' and @targetNamespace=$element-type-namespace]][1]"/>

                    <xsl:choose>
                        <!-- If there is a referenced simple type definition -->
                        <xsl:when test="local-name($this-type)='simpleType' or $this-type/xs:simpleContent">
                            <!-- here no wrapper element is needed  -->
                            <param name="{substring-after($element-name,':')}" token="#in"
                                         type="{substring-after($this-element/@type,':')}" type-namespace="http://www.w3.org/2001/XMLSchema">
                                <xsl:attribute name="targetNamespace">
                                    <xsl:value-of select="$this-element/ancestor::xs:schema/@targetNamespace"/>
                                </xsl:attribute>
                                <xsl:call-template name="infer-types">
                                    <xsl:with-param name="direction" select="$direction"/>
                                    <xsl:with-param name="this-type-def" select="$this-type"/>
                                    <xsl:with-param name="recursive-index" select="1"/>
                                    <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                </xsl:call-template>
                            </param>
                        </xsl:when>

                        <!-- If there is a referenced complex type definition -->
                        <!--<xsl:when test="$this-type[not(xs:simpleContent or xs:complexContent or xs:group or xs:all or xs:choice or xs:attribute or xs:attributeGroup or xs:anyAttribute or xs:any[2])][not(xs:any/following-sibling::xs:*)]/xs:sequence[not(xs:element/@ref)]">-->
                        <xsl:when test="$this-type[not(xs:any[2])][not(xs:any/following-sibling::xs:*)]">
                            <!-- See if the complexType conforms to some of the rquirements of the WSDL 2.0 RPC style,
                                 namely, a sequence of local elements, with xs:any at the end if anywhere. -->
                            <xsl:attribute name="wrapper-element">
                                <xsl:call-template name="local-name">
                                    <xsl:with-param name="qname" select="$element-name"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:attribute name="wrapper-element-ns">
                                <xsl:value-of select="$adjusted-element-namespace"/>
                            </xsl:attribute>

                            <!-- Generate the <param> for the type. -->
                            <!-- local element children in a sequence are interpreted as parameters in the WSDL 2.0 RPC style -->

                            <xsl:call-template name="infer-types">
                                <xsl:with-param name="direction" select="$direction"/>
                                <xsl:with-param name="this-type-def" select="$this-type"/>
                                <xsl:with-param name="recursive-index" select="1"/>
                                <xsl:with-param name="types-callstack" select="$types-callstack"/>
                            </xsl:call-template>

                        </xsl:when>
                        <!-- No referenced definition, is the type a built-in schema type other than xs:anyType? -->
                        <xsl:when test="not($this-type) and $this-element/namespace::*[local-name() = substring-before($this-element/@type,':')][. = 'http://www.w3.org/2001/XMLSchema'] and substring-after($this-element/@type,':') != 'anyType'">
                            <param name="{substring-after($element-name,':')}" token="#in"
                                type="{substring-after($this-element/@type,':')}" type-namespace="http://www.w3.org/2001/XMLSchema" simple="yes">
                                <xsl:if test="$this-element/ancestor::xs:schema/@elementFormDefault != 'unqualified'">
                                    <xsl:attribute name="targetNamespace">
                                        <xsl:value-of select="$this-element/ancestor::xs:schema/@targetNamespace"/>
                                    </xsl:attribute>
                                </xsl:if>
                            </param>
                        </xsl:when>

                        <!-- Either xs:anyType or some other complex type that doesn't conform to the RPC style.
                             Fall back to xs:anyType.  -->
                        <xsl:otherwise>
                            <param name="{substring-after($element-name,':')}" token="#in"
                                type="anyType" type-namespace="http://www.w3.org/2001/XMLSchema" simple="no">
                                <xsl:if test="$this-element/ancestor::xs:schema/@elementFormDefault != 'unqualified'">
                                    <xsl:attribute name="targetNamespace">
                                        <xsl:value-of select="$this-element/ancestor::xs:schema/@targetNamespace"/>
                                    </xsl:attribute>
                                </xsl:if>
                            </param>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <!-- Helper template for "infer-params", extract out the basic type information of the types
         (possibly recursive)
            $direction : '#in' | '#return' signifying input/return parameter types
            $this-type-def : node representing the type delcaration -->
    <xsl:template name="infer-types">
        <xsl:param name="direction"/>
        <xsl:param name="this-type-def"/>
        <xsl:param name="recursive-index"/>
        <xsl:param name="within-inherited-content"/>
        <xsl:param name="within-inheriting-type"/>
        <xsl:param name="types-callstack"/>

           <!-- Does the type represent an enumeration of strings (as generated by the Mashup Server?
                If so capture the possible values.  -->

            <xsl:choose>
                <xsl:when test="local-name($this-type-def) = 'simpleType'">

                    <xsl:attribute name="simple">yes</xsl:attribute>

                    <!-- Handles simple type restrictions here -->
                    <xsl:if test="$this-type-def/xs:restriction">

                        <xsl:variable name="type-base">
                            <xsl:value-of select="$this-type-def/xs:restriction/@base"/>
                        </xsl:variable>

                        <xsl:variable name="type-base-prefix">
                            <xsl:call-template name="prefix">
                                <xsl:with-param name="qname" select="$type-base"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:variable name="type-base-name">
                            <xsl:call-template name="local-name">
                                <xsl:with-param name="qname" select="$type-base"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:variable name="type-base-namespace">
                            <xsl:value-of select="$this-type-def/namespace::*[local-name()=$type-base-prefix]"/>
                        </xsl:variable>

                        <xsl:choose>
                            <xsl:when test="$type-base-namespace='http://www.w3.org/2001/XMLSchema'">
                                <!-- if the base type is a builtin type we don't have to worry on this -->
                                <xsl:attribute name="type"><xsl:value-of select="$type-base-name"/></xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <!-- if the base type is not a builtin type we will be catching the referred type too -->
                                <xsl:variable name="type-base-node"
                                              select="$all-types[@name=$type-base-name and ancestor::*[local-name()='schema' and @targetNamespace=$type-base-namespace]][1]"/>
                                <xsl:for-each select="$type-base-node">
                                    <xsl:call-template name="infer-non-recursive-params">
                                        <xsl:with-param name="type-node" select="."/>
                                        <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                        <xsl:with-param name="type-name" select="$type-base-name"/>
                                        <xsl:with-param name="type-namespace" select="$type-base-namespace"/>
                                        <xsl:with-param name="direction" select="$direction"/>
                                        <xsl:with-param name="recursive-index" select="$recursive-index"/>
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!-- type is consideres as the base type-->
                        <!-- loop throught all the facets -->
                        <!--  potential bug: doesn't double-check the xs namespace on this -->
                        <xsl:for-each select="$this-type-def/xs:restriction/xs:*">
                            <xsl:element name="{local-name()}">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="@value"/>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:for-each>
                    </xsl:if> <!-- finished handling simpleType restriction -->

                    <!-- Handles simple type list here -->
                    <xsl:if test="$this-type-def/xs:list">
                        <xsl:attribute name="list">yes</xsl:attribute>

                        <xsl:variable name="type-base">
                            <xsl:value-of select="$this-type-def/xs:list/@itemType"/>
                        </xsl:variable>
                        <!-- follow the same code as simple type restriciton to follow through
                             the type heierachy -->
                        <xsl:variable name="type-base-prefix">
                            <xsl:call-template name="prefix">
                                <xsl:with-param name="qname" select="$type-base"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:variable name="type-base-name">
                            <xsl:call-template name="local-name">
                                <xsl:with-param name="qname" select="$type-base"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:variable name="type-base-namespace">
                            <xsl:value-of select="$this-type-def/namespace::*[local-name()=$type-base-prefix]"/>
                        </xsl:variable>

                        <xsl:choose>
                            <xsl:when test="$type-base-namespace='http://www.w3.org/2001/XMLSchema'">
                                <!-- if the base type is a builtin type we don't have to worry on this -->
                                <xsl:attribute name="type"><xsl:value-of select="$type-base-name"/></xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <!-- if the base type is not a builtin type we will be catching the referred type too -->
                                <xsl:variable name="type-base-node"
                                              select="$all-types[@name=$type-base-name and ancestor::*[local-name()='schema' and @targetNamespace=$type-base-namespace]][1]"/>
                                <xsl:for-each select="$type-base-node">
                                    <xsl:call-template name="infer-non-recursive-params">
                                        <xsl:with-param name="type-node" select="."/>
                                        <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                        <xsl:with-param name="type-name" select="$type-base-name"/>
                                        <xsl:with-param name="type-namespace" select="$type-base-namespace"/>
                                        <xsl:with-param name="direction" select="$direction"/>
                                        <xsl:with-param name="recursive-index" select="$recursive-index"/>
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                    <!-- Handles simple type union here -->
                    <xsl:if test="$this-type-def/xs:union">
                        <xsl:attribute name="union">yes</xsl:attribute>

                        <xsl:call-template name="handle-union">
                            <xsl:with-param name="type-list" select="$this-type-def/xs:union/@memberTypes"/>
                            <xsl:with-param name="this-type-def" select="$this-type-def"/>
                            <xsl:with-param name="direction" select="$direction"/>
                            <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                            <xsl:with-param name="types-callstack" select="$types-callstack"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <!-- not a simple type -->
                    <xsl:attribute name="simple">no</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>

           <!-- If the type itself is complex, try to recurse and infer sub-params, helpful for generating nested forms -->
           <xsl:if test="local-name($this-type-def) = 'complexType'">

                <xsl:for-each select="$this-type-def/*">
                    <xsl:if test="local-name()='all' or local-name()='sequence' or local-name()='choice'">
                        <xsl:attribute name="contentModel"><xsl:value-of select="local-name(.)"/></xsl:attribute>
                        <xsl:call-template name="infer-content-model">
                            <xsl:with-param name="direction" select="$direction"/>
                            <xsl:with-param name="content-container" select="."/>
                            <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                            <xsl:with-param name="types-callstack" select="$types-callstack"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="local-name()='attribute'">
                       <xsl:call-template name="infer-attribs">
                           <xsl:with-param name="this-attrib" select="."/>
                           <xsl:with-param name="direction" select="$direction"/>
                           <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                           <xsl:with-param name="types-callstack" select="$types-callstack"/>
                       </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="local-name()='attributeGroup'">
                       <xsl:variable name="attrib-group-ref">
                           <xsl:value-of select="@ref"/>
                       </xsl:variable>

                       <xsl:variable name="attrib-group-ref-prefix">
                           <xsl:call-template name="prefix">
                               <xsl:with-param name="qname" select="$attrib-group-ref"/>
                           </xsl:call-template>
                       </xsl:variable>
                       <xsl:variable name="attrib-group-ref-name">
                           <xsl:call-template name="local-name">
                               <xsl:with-param name="qname" select="$attrib-group-ref"/>
                           </xsl:call-template>
                       </xsl:variable>
                       <xsl:variable name="attrib-group-ref-namespace">
                           <xsl:value-of select="$this-type-def/namespace::*[local-name()=$attrib-group-ref-prefix]"/>
                       </xsl:variable>

                       <xsl:variable name="attrib-group-ref-node"
                           select="$all-attributeGroups[@name=$attrib-group-ref-name and ancestor::*[local-name()='schema' and @targetNamespace=$attrib-group-ref-namespace]][1]"/>

                       <xsl:for-each select="$attrib-group-ref-node/*">
                           <xsl:call-template name="infer-attribs">
                               <xsl:with-param name="this-attrib" select="."/>
                               <xsl:with-param name="direction" select="$direction"/>
                               <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                               <xsl:with-param name="types-callstack" select="$types-callstack"/>
                           </xsl:call-template>
                        </xsl:for-each>
                    </xsl:if>
                    <xsl:if test="local-name()='anyAttribute'">
                        <param attribute="yes" token="{$direction}" type="anyType" simple="no"/>
                    </xsl:if>
                </xsl:for-each>


               <!-- look into the type and see if we can determine whether it's a complexContent extension/restriction  simpleContent extension-->
               <xsl:if test="$this-type-def/xs:complexContent or $this-type-def/xs:simpleContent">
                   <!-- add extension info for complexContent  or simpleContent-->
                   <xsl:if test="$this-type-def/xs:complexContent/xs:extension or $this-type-def/xs:simpleContent/xs:extension">

                        <xsl:if test="$this-type-def/xs:complexContent">
                            <xsl:for-each select="$this-type-def//xs:extension/*"><!-- works only '*' represent one element -->
                                <xsl:attribute name="contentModel"><xsl:value-of select="local-name(.)"/></xsl:attribute>
                            </xsl:for-each>
                        </xsl:if>

                       <xsl:variable name="basetype">
                           <xsl:value-of select="$this-type-def//xs:extension/@base"/>
                       </xsl:variable>

                       <xsl:variable name="basetype-prefix">
                           <xsl:call-template name="prefix">
                               <xsl:with-param name="qname" select="$basetype"/>
                           </xsl:call-template>
                       </xsl:variable>
                       <xsl:variable name="basetype-name">
                           <xsl:call-template name="local-name">
                               <xsl:with-param name="qname" select="$basetype"/>
                           </xsl:call-template>
                       </xsl:variable>
                       <xsl:variable name="basetype-namespace">
                           <xsl:value-of select="$this-type-def/namespace::*[local-name()=$basetype-prefix]"/>
                       </xsl:variable>

                       <xsl:variable name="basetype-node"
                           select="$all-types[@name=$basetype-name and ancestor::*[local-name()='schema' and @targetNamespace=$basetype-namespace]][1]"/>

                       <xsl:choose>
                           <xsl:when test="$this-type-def/xs:complexContent">
                               <inherited-content>
                                    <xsl:attribute name="contentModel">
                                        <xsl:for-each select="$this-type-def/*">
                                            <xsl:value-of select="local-name()"/>
                                        </xsl:for-each>
                                    </xsl:attribute>
                                     <!-- If the type is a built-in schema type -->
                                    <xsl:if test="$basetype-namespace = 'http://www.w3.org/2001/XMLSchema'">
                                        <xsl:attribute name="simple">
                                            <xsl:choose>
                                                <xsl:when test="$basetype-name = 'anyType'">no</xsl:when>
                                                <xsl:otherwise>yes</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:attribute>
                                    </xsl:if>

                                   <xsl:attribute name="extension">
                                       <xsl:value-of select="$basetype-name"/>
                                   </xsl:attribute>
                                   <xsl:for-each select="$basetype-node">
                                       <xsl:call-template name="infer-non-recursive-params">
                                            <xsl:with-param name="type-node" select="."/>
                                            <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                            <xsl:with-param name="type-name" select="$basetype-name"/>
                                            <xsl:with-param name="type-namespace" select="$basetype-namespace"/>
                                            <xsl:with-param name="direction" select="$direction"/>
                                            <xsl:with-param name="recursive-index" select="$recursive-index"/>
                                        </xsl:call-template>
                                   </xsl:for-each>
                                </inherited-content>
                            </xsl:when>
                            <xsl:otherwise> <!-- this is for the simpleContentExtension -->
                                <!-- we are not using another wraper element for the simple type extension -->
                                <xsl:attribute name="contentModel">
                                    <xsl:for-each select="$this-type-def/*">
                                        <xsl:value-of select="local-name()"/>
                                    </xsl:for-each>
                                </xsl:attribute>
                                 <!-- If the type is a built-in schema type -->
                                <xsl:if test="$basetype-namespace = 'http://www.w3.org/2001/XMLSchema'">
                                    <xsl:attribute name="simple">
                                        <xsl:choose>
                                            <xsl:when test="$basetype-name = 'anyType'">no</xsl:when>
                                            <xsl:otherwise>yes</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:attribute>
                                </xsl:if>

                               <xsl:attribute name="extension">
                                   <xsl:value-of select="$basetype-name"/>
                               </xsl:attribute>
                                <xsl:for-each select="$basetype-node">
                                    <xsl:call-template name="infer-non-recursive-params">
                                        <xsl:with-param name="type-node" select="."/>
                                        <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                        <xsl:with-param name="type-name" select="$basetype-name"/>
                                        <xsl:with-param name="type-namespace" select="$basetype-namespace"/>
                                        <xsl:with-param name="direction" select="$direction"/>
                                        <xsl:with-param name="recursive-index" select="$recursive-index"/>
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:otherwise>
                       </xsl:choose>
                   </xsl:if>

                   <!-- look into the type and see if we can determine whether it's a restriction of another type -->
                   <xsl:if test="$this-type-def/xs:complexContent/xs:restriction">
                       <xsl:for-each select="$this-type-def/xs:complexContent/xs:restriction">
                           <!-- correct when the restriction have only one child -->
                           <xsl:attribute name="contentModel"><xsl:value-of select="local-name(./*)"/></xsl:attribute>
                           <xsl:attribute name="restriction-of"><xsl:value-of select="substring-after(@base,':')"/></xsl:attribute>
                           <xsl:variable name="ns_prefix"><xsl:value-of select="substring-before(current()/@base,':')"/></xsl:variable>
                           <xsl:attribute name="restriction-namespace"><xsl:value-of select="namespace::*[local-name() = $ns_prefix]"/></xsl:attribute>
                       </xsl:for-each>
                   </xsl:if>

                    <!-- xs:extenion/* or xs:restriction/* -->
                    <xsl:for-each select="$this-type-def//*">
                        <xsl:if test="local-name()='all' or local-name()='sequence' or local-name()='choice'">
                            <xsl:call-template name="infer-content-model">
                                <xsl:with-param name="direction" select="$direction"/>
                                <xsl:with-param name="content-container" select="."/>
                                <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                                <xsl:with-param name="types-callstack" select="$types-callstack"/>
                            </xsl:call-template>
                        </xsl:if>
                        <xsl:if test="local-name()='attribute'">
                           <xsl:call-template name="infer-attribs">
                               <xsl:with-param name="this-attrib" select="."/>
                               <xsl:with-param name="direction" select="$direction"/>
                               <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                               <xsl:with-param name="types-callstack" select="$types-callstack"/>
                           </xsl:call-template>
                        </xsl:if>
                        <xsl:if test="local-name()='attributeGroup'">
                           <xsl:variable name="attrib-group-ref">
                               <xsl:value-of select="@ref"/>
                           </xsl:variable>

                           <xsl:variable name="attrib-group-ref-prefix">
                               <xsl:call-template name="prefix">
                                   <xsl:with-param name="qname" select="$attrib-group-ref"/>
                               </xsl:call-template>
                           </xsl:variable>
                           <xsl:variable name="attrib-group-ref-name">
                               <xsl:call-template name="local-name">
                                   <xsl:with-param name="qname" select="$attrib-group-ref"/>
                               </xsl:call-template>
                           </xsl:variable>
                           <xsl:variable name="attrib-group-ref-namespace">
                               <xsl:value-of select="$this-type-def/namespace::*[local-name()=$attrib-group-ref-prefix]"/>
                           </xsl:variable>

                           <xsl:variable name="attrib-group-ref-node"
                               select="$all-types[@name=$attrib-group-ref-name and ancestor::*[local-name()='schema' and @targetNamespace=$attrib-group-ref-namespace]][1]"/>

                           <xsl:for-each select="$attrib-group-ref-node/*">
                               <xsl:call-template name="infer-attribs">
                                   <xsl:with-param name="this-attrib" select="."/>
                                   <xsl:with-param name="direction" select="$direction"/>
                                   <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                                   <xsl:with-param name="types-callstack" select="$types-callstack"/>
                               </xsl:call-template>
                            </xsl:for-each>
                        </xsl:if>
                        <xsl:if test="local-name()='anyAttribute'">
                            <param attribute="yes" token="{$direction}" type="anyType" simple="no"/>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:if>
                <!--<xsl:if test="$within-inherited-content!='yes' and $within-inheriting-type!='yes'">
                    &lt;!&ndash; before finishing up, we have to check whether there are any type that inherits this,
                        yea we are keeping backward references &ndash;&gt;
                    <xsl:call-template name="infer-inheriting-type">
                        <xsl:with-param name="this-type-def" select="$this-type-def"/>
                        <xsl:with-param name="direction" select="$direction"/>
                        <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                        <xsl:with-param name="types-callstack" select="$types-callstack"/>
                    </xsl:call-template>
                </xsl:if>-->
          </xsl:if> <!--<xsl:if test="local-name($this-type-def) = 'complexType'"> -->
    </xsl:template>

    <!-- a perticular types can be inherited by many type, user may need to use the type abstraction
      so whenever there is a super type required he can put a inheriting type, to solve that we keep track
      of inheriting types in the super type with the tag inheriting-type-->
    <xsl:template name="infer-inheriting-type">
        <xsl:param name="this-type-def"/>
        <xsl:param name="direction"/>
        <xsl:param name="recursive-index"/>
        <xsl:param name="types-callstack"/>
        <!--TODO : recheck whether this template is correct-->
        <!-- before finishing up, we have to check whether there are any type that inherits this,
            yea we are keeping backward references -->
        <xsl:variable name="type-name" select="$this-type-def/@name"/>
        <xsl:if test="$type-name">
            <!-- so this has a name, the first requirment to get inherited -->
            <xsl:variable name="inheriting-type"
                select="$all-types[substring-after(xs:complexContent/xs:extension/@base,':')=$type-name or xs:complexContent/xs:extension/@base=$type-name][1]"/>

            <xsl:for-each select="$inheriting-type">
                <inheriting-type xsi-type="{@name}" xsi-type-ns="{ancestor::*[local-name()='schema']/@targetNamespace}">
                    <xsl:call-template name="infer-non-recursive-params">
                        <xsl:with-param name="type-node" select="."/>
                        <xsl:with-param name="types-callstack" select="$types-callstack"/>
                        <xsl:with-param name="type-name" select="$type-name"/>
                        <xsl:with-param name="type-namespace" select="namespace-uri($this-type-def)"/>
                        <xsl:with-param name="direction" select="$direction"/>
                        <xsl:with-param name="recursive-index" select="$recursive-index"/>
                    </xsl:call-template>
                </inheriting-type>
                <!--of course we have to find types inheriting the inheriting types -->
               <xsl:call-template name="infer-inheriting-type">
                   <xsl:with-param name="this-type-def" select="."/>
                   <xsl:with-param name="direction" select="$direction"/>
                   <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                   <xsl:with-param name="types-callstack" select="$types-callstack"/>
               </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template name="infer-content-model">
        <xsl:param name="direction"/>
        <xsl:param name="content-container"/>
        <xsl:param name="recursive-index"/>
        <xsl:param name="types-callstack"/>
            <xsl:if test="local-name($content-container)='all' or local-name($content-container)='choice' or local-name($content-container)">
               <xsl:for-each select="$content-container/*">
                   <xsl:if test="local-name(.)='element'">
                       <xsl:call-template name="complex-param">
                           <xsl:with-param name="this-element" select="."/>
                           <xsl:with-param name="direction" select="$direction"/>
                           <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                           <xsl:with-param name="types-callstack" select="$types-callstack"/>
                       </xsl:call-template>
                   </xsl:if>
                   <!-- Handle any wildcards -->
                   <xsl:if test="local-name(.)='any'">
                       <param token="#any">
                           <xsl:copy-of select="@minOccurs | @maxOccurs"/>
                           <xsl:if test="not(@minOccurs)"><xsl:attribute name="minOccurs">1</xsl:attribute></xsl:if>
                           <xsl:if test="not(@maxOccurs)"><xsl:attribute name="maxOccurs">1</xsl:attribute></xsl:if>
                       </param>
                   </xsl:if>
                   <xsl:if test="local-name(.)='all' or local-name(.)='choice' or local-name(.)='sequence'">
                        <inner-content contentModel="{local-name(.)}">
                           <xsl:call-template name="infer-content-model">
                               <xsl:with-param name="content-container" select="."/>
                               <xsl:with-param name="direction" select="$direction"/>
                               <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                               <xsl:with-param name="types-callstack" select="$types-callstack"/>
                           </xsl:call-template>
                        </inner-content>
                   </xsl:if>

                   <!--handling groups -->
                   <xsl:if test="local-name(.)='group'">
                        <xsl:choose>
                            <!-- if the @ref exists -->
                            <xsl:when test="@ref">
                                   <xsl:variable name="group-ref">
                                       <xsl:value-of select="@ref"/>
                                   </xsl:variable>

                                   <xsl:variable name="group-ref-prefix">
                                       <xsl:call-template name="prefix">
                                           <xsl:with-param name="qname" select="$group-ref"/>
                                       </xsl:call-template>
                                   </xsl:variable>
                                   <xsl:variable name="group-ref-name">
                                       <xsl:call-template name="local-name">
                                           <xsl:with-param name="qname" select="$group-ref"/>
                                       </xsl:call-template>
                                   </xsl:variable>
                                   <xsl:variable name="group-ref-namespace">
                                       <xsl:value-of select="namespace::*[local-name()=$group-ref-prefix]"/>
                                   </xsl:variable>

                                   <xsl:variable name="group-ref-node"
                                       select="$all-groups[@name=$group-ref-name and ancestor::*[local-name()='schema' and @targetNamespace=$group-ref-namespace]][1]"/>

                                   <!-- just apply the infer-content-model template to the referring group childs-->
                                    <xsl:for-each select="$group-ref-node/*">
                                        <inner-content contentModel="{local-name(.)}">
                                           <xsl:call-template name="infer-content-model">
                                               <xsl:with-param name="content-container" select="."/>
                                               <xsl:with-param name="direction" select="$direction"/>
                                               <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                                               <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                           </xsl:call-template>
                                        </inner-content>
                                    </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:for-each select="./*">
                                    <inner-content contentModel="{local-name(.)}">
                                       <xsl:call-template name="infer-content-model">
                                           <xsl:with-param name="content-container" select="."/>
                                           <xsl:with-param name="direction" select="$direction"/>
                                           <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                                           <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                       </xsl:call-template>
                                    </inner-content>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                   </xsl:if>
               </xsl:for-each>
           </xsl:if>
    </xsl:template>

    <!-- Helper template for "infer-params", generating the <param/> element representing a complex type
         (possibly recursive)
            $direction : '#in' | '#return' signifying input/return parameter types
            $this-element : node representing the element delcaration associated with this parameter
    -->
    <xsl:template name="complex-param">
        <xsl:param name="direction"/>
        <xsl:param name="this-element"/>
        <xsl:param name="recursive-index"/>
        <xsl:param name="types-callstack"/>
        <xsl:param name="minOccurs"/>
        <xsl:param name="maxOccurs"/>

        <xsl:choose>
            <!-- first we eliminate infinite recursive with this -->
            <xsl:when test="$recursive-index &gt; $recursive-limit">
            </xsl:when>
            <!-- then we should handle the element ref -->
            <xsl:when test="$this-element/@ref">
                <xsl:variable name="element-ref">
                    <xsl:value-of select="$this-element/@ref"/>
                </xsl:variable>

                <xsl:variable name="element-ref-prefix">
                    <xsl:call-template name="prefix">
                        <xsl:with-param name="qname" select="$element-ref"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="element-ref-name">
                    <xsl:call-template name="local-name">
                       <xsl:with-param name="qname" select="$element-ref"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="element-ref-namespace">
                    <xsl:value-of select="$this-element/namespace::*[local-name()=$element-ref-prefix]"/>
                </xsl:variable>
                <xsl:variable name="element-ref-node"
                    select="$all-elements[@name=$element-ref-name and ancestor::*[local-name()='schema' and @targetNamespace=$element-ref-namespace]][1]"/>
                <xsl:variable name="minOccursOverride" select="$this-element/@minOccurs"/>
                <xsl:variable name="maxOccursOverride" select="$this-element/@maxOccurs"/>

                <xsl:for-each select="$element-ref-node">
                    <xsl:call-template name="complex-param">
                        <xsl:with-param name="this-element" select="."/>
                        <xsl:with-param name="direction" select="$direction"/>
                        <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                        <xsl:with-param name="types-callstack" select="$types-callstack"/>
                        <xsl:with-param name="minOccurs" select="$minOccursOverride"/>
                        <xsl:with-param name="maxOccurs" select="$maxOccursOverride"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <param token="{$direction}">
                    <xsl:choose>
                        <!-- elements can be qualified in several ways -->
                        <xsl:when test="local-name($this-element/..)='schema'">
                            <!-- this is if the element is straigt child of the schema, happens in refs -->
                            <xsl:attribute name="targetNamespace">
                                <xsl:value-of select="$this-element/../@targetNamespace"/>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$this-element/@form">
                            <xsl:if test="$this-element/@form = 'qualified'">
                                <xsl:attribute name="targetNamespace">
                                    <xsl:value-of select="$this-element/ancestor::xs:schema/@targetNamespace"/>
                                </xsl:attribute>
                            </xsl:if>
                        </xsl:when>
                        <xsl:when test="$this-element/ancestor::xs:schema/@elementFormDefault != 'unqualified'">
                            <xsl:attribute name="targetNamespace">
                                <xsl:value-of select="$this-element/ancestor::xs:schema/@targetNamespace"/>
                            </xsl:attribute>
                        </xsl:when>
                    </xsl:choose>
                    <!-- set the context for attribute scraping -->
                    <xsl:for-each select="$this-element">
                        <xsl:choose>
                            <xsl:when test="$maxOccurs">
                                <xsl:attribute name="maxOccurs"><xsl:value-of select="$maxOccurs"/></xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="@maxOccurs"/>
                                <!-- canonical values for maxOccurs -->
                                <xsl:if test="not(@maxOccurs)"><xsl:attribute name="maxOccurs">1</xsl:attribute></xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:choose>
                            <xsl:when test="$minOccurs">
                                <xsl:attribute name="minOccurs"><xsl:value-of select="$minOccurs"/></xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="@minOccurs"/>
                                <!-- canonical values for minOccurs -->
                                <xsl:if test="not(@minOccurs)"><xsl:attribute name="minOccurs">1</xsl:attribute></xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:copy-of select="@nillable"/>
                        <xsl:attribute name="name">
                            <xsl:call-template name="elementname-2-paramname">
                                <xsl:with-param name="elementname" select="@name | @ref"/>
                            </xsl:call-template>
                        </xsl:attribute>

                        <xsl:choose>
                            <!-- This is when elements have named types -->
                            <xsl:when test="@type">
                                <xsl:variable name="typePrefix"><xsl:if test="contains(@type,':')"><xsl:value-of select="substring-before(@type,':')"/></xsl:if></xsl:variable>
                                <xsl:variable name="typeLocalName">
                                    <xsl:call-template name="local-name">
                                        <xsl:with-param name="qname" select="@type"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:variable name="typeNamespace"><xsl:value-of select="namespace::*[local-name() = $typePrefix]"/></xsl:variable>
                                <xsl:attribute name="type"><xsl:value-of select="$typeLocalName"/></xsl:attribute>
                                <xsl:attribute name="type-namespace"><xsl:value-of select="$typeNamespace"/></xsl:attribute>
                                <xsl:attribute name="type-prefix"><xsl:value-of select="$typePrefix"/></xsl:attribute>
                                <xsl:choose>
                                    <!-- If the type is a built-in schema type -->
                                    <xsl:when test="$typeNamespace = 'http://www.w3.org/2001/XMLSchema'">
                                        <xsl:attribute name="simple">
                                            <xsl:choose>
                                                <xsl:when test="$typeLocalName = 'anyType'">no</xsl:when>
                                                <xsl:otherwise>yes</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:attribute>
                                    </xsl:when>

                                    <!-- Not in a built-in type, do some more inference -->
                                    <xsl:otherwise>
                                        <!-- this-type-def will either point to the named type (if there is a @type) or to the child type (if there is a @name) -->

                                       <xsl:variable name="this-type-def"
                                           select="$all-types[@name=$typeLocalName and ancestor::*[local-name()='schema' and @targetNamespace=$typeNamespace]][1] | xs:simpleType | xs:complexType"/>

                                        <!-- <xsl:variable name="this-type-def" select="$all-types[@name=$typeLocalName] | xs:simpleType | xs:complexType"/> -->
                                        <xsl:for-each select="$this-type-def">
                                            <xsl:call-template name="infer-types">
                                                <xsl:with-param name="this-type-def" select="."/>
                                                <xsl:with-param name="direction" select="$direction"/>
                                                <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                                                <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                            </xsl:call-template>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <!-- This is when elements have anonymous types -->
                                <xsl:for-each select="./*">
                                    <xsl:call-template name="infer-types">
                                        <xsl:with-param name="this-type-def" select="."/>
                                        <xsl:with-param name="direction" select="$direction"/>
                                        <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                                        <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </param>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="infer-attribs">
        <xsl:param name="direction"/>
        <xsl:param name="this-attrib"/>
        <xsl:param name="recursive-index"/>
        <xsl:param name="types-callstack"/>
        <xsl:choose>
            <xsl:when test="$this-attrib/@ref">
                <xsl:variable name="attrib-ref">
                    <xsl:value-of select="$this-attrib/@ref"/>
                </xsl:variable>

                <xsl:variable name="attrib-ref-prefix">
                    <xsl:call-template name="prefix">
                        <xsl:with-param name="qname" select="$attrib-ref"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="attrib-ref-name">
                    <xsl:call-template name="local-name">
                        <xsl:with-param name="qname" select="$attrib-ref"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="attrib-ref-namespace">
                    <xsl:value-of select="$this-attrib/namespace::*[local-name()=$attrib-ref-prefix]"/>
                </xsl:variable>

                <xsl:variable name="attrib-ref-node"
                    select="$all-attributes[@name=$attrib-ref-name and ancestor::*[local-name()='schema' and @targetNamespace=$attrib-ref-namespace]][1]"/>

                <xsl:for-each select="$attrib-ref-node">
                    <xsl:call-template name="infer-attribs">
                        <xsl:with-param name="this-attrib" select="."/>
                        <xsl:with-param name="direction" select="$direction"/>
                        <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                        <xsl:with-param name="types-callstack" select="$types-callstack"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <param attribute="yes" token="{$direction}">
                    <xsl:choose>
                        <!-- attributes can be qualified in several ways -->
                        <xsl:when test="local-name($this-attrib/..)='schema'">
                            <!-- this is if the attribute is straigt child of the schema, happens in refs -->
                            <xsl:attribute name="targetNamespace">
                                <xsl:value-of select="$this-attrib/../@targetNamespace"/>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$this-attrib/@form">
                            <xsl:if test="$this-attrib/@form = 'qualified'">
                                <xsl:attribute name="targetNamespace">
                                    <xsl:value-of select="$this-attrib/ancestor::xs:schema/@targetNamespace"/>
                                </xsl:attribute>
                            </xsl:if>
                        </xsl:when>
                        <xsl:when test="$this-attrib/ancestor::xs:schema/@attributeFormDefault = 'qualified'">
                            <xsl:attribute name="targetNamespace">
                                <xsl:value-of select="$this-attrib/ancestor::xs:schema/@targetNamespace"/>
                            </xsl:attribute>
                        </xsl:when>
                    </xsl:choose>

                    <!-- set the context for attribute scraping -->
                    <xsl:for-each select="$this-attrib">
                        <xsl:attribute name="name">
                            <xsl:call-template name="elementname-2-paramname">
                                <xsl:with-param name="elementname" select="@name | @ref"/>
                            </xsl:call-template>
                        </xsl:attribute>

                        <xsl:choose>
                            <!-- This is when attributes have named types -->
                            <xsl:when test="@type">
                                <xsl:variable name="typePrefix"><xsl:if test="contains(@type,':')"><xsl:value-of select="substring-before(@type,':')"/></xsl:if></xsl:variable>
                                <xsl:variable name="typeLocalName">
                                    <xsl:call-template name="local-name">
                                        <xsl:with-param name="qname" select="@type"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:variable name="typeNamespace"><xsl:value-of select="namespace::*[local-name() = $typePrefix]"/></xsl:variable>
                                <xsl:attribute name="type"><xsl:value-of select="$typeLocalName"/></xsl:attribute>
                                <xsl:attribute name="type-namespace"><xsl:value-of select="$typeNamespace"/></xsl:attribute>
                                <xsl:choose>
                                    <!-- If the type is a built-in schema type -->
                                    <xsl:when test="$typeNamespace = 'http://www.w3.org/2001/XMLSchema'">
                                        <xsl:attribute name="simple">
                                            <xsl:choose>
                                                <xsl:when test="$typeLocalName = 'anyType'">no</xsl:when>
                                                <xsl:otherwise>yes</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:attribute>
                                    </xsl:when>

                                    <!-- Not in a built-in type, do some more inference -->
                                    <xsl:otherwise>
                                        <!-- this-type-def will either point to the named type (if there is a @type) or to the child type (if there is a @name) -->

                                       <xsl:variable name="this-type-def"
                                           select="$all-types[@name=$typeLocalName and ancestor::*[local-name()='schema' and @targetNamespace=$typeNamespace]][1] | xs:simpleType | xs:complexType"/>

                                        <!-- <xsl:variable name="this-type-def" select="$all-types[@name=$typeLocalName] | xs:simpleType | xs:complexType"/> -->
                                        <xsl:for-each select="$this-type-def">
                                            <xsl:call-template name="infer-types">
                                                <xsl:with-param name="this-type-def" select="."/>
                                                <xsl:with-param name="direction" select="$direction"/>
                                                <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                                                <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                            </xsl:call-template>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <!-- This is when attributes have anonymous types -->
                                <xsl:for-each select="./*">
                                    <xsl:call-template name="infer-types">
                                        <xsl:with-param name="this-type-def" select="."/>
                                        <xsl:with-param name="direction" select="$direction"/>
                                        <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                                        <xsl:with-param name="types-callstack" select="$types-callstack"/>
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:otherwise>
                         </xsl:choose>
                    </xsl:for-each>
                </param>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- extract union type information -->
    <xsl:template name="handle-union">
        <xsl:param name="type-list"/>
        <xsl:param name="this-type-def"/>
        <xsl:param name="direction"/>
        <xsl:param name="recursive-index"/>
        <xsl:param name="types-callstack"/>

        <xsl:variable name="next-union-type">
            <xsl:choose>
                <xsl:when test="contains($type-list, ' ')">
                    <xsl:value-of select="substring-before($type-list, ' ')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$type-list"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- set the information of the next-union-type -->
        <union>
        <xsl:variable name="type-base">
            <xsl:value-of select="$next-union-type"/>
        </xsl:variable>
        <!-- follow the same code as simple type restriciton to follow through
             the type heierachy -->
        <xsl:variable name="type-base-prefix">
            <xsl:call-template name="prefix">
                <xsl:with-param name="qname" select="$type-base"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="type-base-name">
            <xsl:call-template name="local-name">
                <xsl:with-param name="qname" select="$type-base"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="type-base-namespace">
            <xsl:value-of select="$this-type-def/namespace::*[local-name()=$type-base-prefix]"/>
        </xsl:variable>

        <xsl:choose>
          <xsl:when test="$type-base-namespace='http://www.w3.org/2001/XMLSchema'">
              <!-- if the base type is a builtin type we don't have to worry on this -->
              <xsl:attribute name="type"><xsl:value-of select="$type-base-name"/></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
              <!-- if the base type is not a builtin type we will be catching the referred type too -->
              <xsl:variable name="type-base-node"
                 select="$all-types[@name=$type-base-name and ancestor::*[local-name()='schema' and @targetNamespace=$type-base-namespace]][1]"/>
              <xsl:for-each select="$type-base-node">
                  <xsl:call-template name="infer-types">
                      <xsl:with-param name="this-type-def" select="."/>
                      <xsl:with-param name="direction" select="$direction"/>
                      <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                      <xsl:with-param name="types-callstack" select="$types-callstack"/>
                  </xsl:call-template>
              </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>

        </union>

        <!-- handle the rest of the type -->
        <xsl:if test="contains($type-list, ' ')">
            <xsl:call-template name="handle-union">
                <xsl:with-param name="type-list" select="substring-after($type-list, ' ')"/>
                <xsl:with-param name="this-type-def" select="$this-type-def"/>
                <xsl:with-param name="direction" select="$direction"/>
                <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                <xsl:with-param name="types-callstack" select="$types-callstack"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- extract a localName from a QName -->
    <xsl:template name="local-name">
        <xsl:param name="qname"/>
        <xsl:choose>
            <xsl:when test="contains($qname,':')">
                <xsl:value-of select="substring-after($qname,':')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$qname"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- extract a prefix from a QName -->
    <xsl:template name="prefix">
        <xsl:param name="qname"/>
        <xsl:if test="contains($qname,':')">
            <xsl:value-of select="substring-before($qname,':')"/>
        </xsl:if>
    </xsl:template>

    <!-- perform some XML->Javascript (and other programming languages) mapping -->
    <xsl:template name="elementname-2-paramname">
        <xsl:param name="elementname"/>
        <xsl:value-of select="translate($elementname,':.-','___')"/>
    </xsl:template>

    <xsl:template name="resolve-schemas">
        <xsl:param name="schema"/>
        <xsl:param name="schema-uri"/>
        <xsl:param name="previous-schemas"/>
        <xsl:variable name="current-schemas">
            <xsl:for-each select="$schema/xs:import[@schemaLocation and
                not(starts-with(@schemaLocation,'#'))]">
                <xsl:variable name="uri">
                    <xsl:call-template name="get-schema-url">
                        <xsl:with-param name="node" select="."/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:if test="not($previous-schemas[@uri=$uri])">
                    <xsl:variable name="current-schema">
                        <xsl:call-template name="create-schema-node">
                            <xsl:with-param name="schema-reference" select="."/>
                            <xsl:with-param name="parent-schema-node" select=".."/>
                            <xsl:with-param name="uri" select="$uri"/>
                            <xsl:with-param name="type" select="'import'"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:call-template name="resolve-schemas">
                        <xsl:with-param name="schema" select="document($uri)"/>
                        <xsl:with-param name="schema-uri" select="$uri"/>
                        <xsl:with-param name="previous-schemas"
                                        select="$previous-schemas | $current-schema"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
            <xsl:for-each select="$schema/xs:include[@schemaLocation]">
                <xsl:variable name="uri">
                    <xsl:call-template name="get-schema-url">
                        <xsl:with-param name="node" select="."/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:if test="not($previous-schemas[@uri=$uri])">
                    <xsl:variable name="current-schema">
                        <xsl:call-template name="create-schema-node">
                            <xsl:with-param name="schema-reference" select="."/>
                            <xsl:with-param name="parent-schema-node" select=".."/>
                            <xsl:with-param name="uri" select="$uri"/>
                            <xsl:with-param name="type" select="'include'"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:call-template name="resolve-schemas">
                        <xsl:with-param name="schema" select="document($uri)"/>
                        <xsl:with-param name="schema-uri" select="$uri"/>
                        <xsl:with-param name="previous-schemas"
                                        select="$previous-schemas | $current-schema"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:copy-of select="$current-schemas | $previous-schemas"/>
    </xsl:template>

    <xsl:template name="create-schema-node">
        <xsl:param name="uri"/>
        <xsl:param name="schema-reference"/>
        <xsl:param name="parent-schema-node"/>
        <xsl:param name="type"/>
        <xsl:element name="schema">
            <xsl:attribute name="uri">
                <xsl:value-of select="$uri"/>
            </xsl:attribute>
            <xsl:attribute name="type">
                <xsl:value-of select="$type"/>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="$type = 'import'">
                    <xsl:attribute name="namespace">
                        <xsl:value-of select="$schema-reference/@namespace"/>
                    </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="namespace">
                        <xsl:value-of select="$parent-schema-node/@targetNamespace"/>
                    </xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template name="get-schema-url">
        <xsl:param name="node"/>
        <xsl:choose>
            <xsl:when
                    test="starts-with($node/@schemaLocation, 'http://') or starts-with($node/@schemaLocation, 'https://')">
                <xsl:value-of select="$node/@schemaLocation"/>
            </xsl:when>
            <xsl:when test="@base[namespace-uri() = 'http://www.w3.org/XML/1998/namespace']">
                <xsl:value-of select="concat(@base, $node/@schemaLocation)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat($base-uri, $node/@schemaLocation)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="process-schemas">
        <xsl:param name="inline-schemas"/>
        <xsl:param name="imported-schemas"/>
        <xsl:variable name="empty-schemas" select="/"/>
        <xsl:variable name="inline">
            <xsl:call-template name="resolve-schemas">
                <xsl:with-param name="schema" select="$inline-schemas/xs:schema"/>
                <xsl:with-param name="schema-uri" select="'#'"/>
                <xsl:with-param name="previous-schemas" select="$empty-schemas"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="all-schemas">
            <xsl:call-template name="resolve-schemas">
                <xsl:with-param name="schema" select="$imported-schemas/xs:schema"/>
                <xsl:with-param name="schema-uri" select="'#'"/>
                <xsl:with-param name="previous-schemas" select="$inline"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:copy-of select="$inline-schemas/xs:schema"/>
        <xsl:for-each select="$all-schemas/schema">
            <xsl:variable name="schema" select="document(@uri)"/>
            <xsl:choose>
                <xsl:when test="@type = 'import'">
                    <xsl:variable name="namespace" select="@namespace"/>
                    <xsl:for-each select="$schema/xs:schema">
                        <xsl:copy>
                            <xsl:copy-of select="@*"/>
                            <xsl:attribute name="targetNamespace">
                                <xsl:value-of select="$namespace"/>
                            </xsl:attribute>
                            <xsl:copy-of select="*"/>
                        </xsl:copy>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="namespace" select="@namespace"/>
                    <xsl:for-each select="$schema/xs:schema">
                        <xsl:copy>
                            <xsl:copy-of select="@*"/>
                            <xsl:attribute name="targetNamespace">
                                <xsl:choose>
                                    <xsl:when test="@targetNamespace">
                                        <xsl:value-of select="@targetNamespace"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$namespace"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:copy-of select="*"/>
                        </xsl:copy>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="infer-non-recursive-params">
        <xsl:param name="type-node"/>
        <xsl:param name="types-callstack"/>
        <xsl:param name="type-name"/>
        <xsl:param name="type-namespace"/>
        <xsl:param name="direction"/>
        <xsl:param name="recursive-index"/>
        <xsl:choose>
            <xsl:when
                    test="(count($types-callstack) = 0) or ($types-callstack/type[not(@name = $type-name and @namespace = $type-namespace)])">
                <xsl:variable name="this-type">
                    <xsl:element name="type">
                        <xsl:attribute name="name">
                            <xsl:value-of select="$type-name"/>
                        </xsl:attribute>
                        <xsl:attribute name="namespace">
                            <xsl:value-of select="$type-namespace"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:variable>
                <xsl:call-template name="infer-types">
                    <xsl:with-param name="this-type-def" select="$type-node"/>
                    <xsl:with-param name="direction" select="$direction"/>
                    <xsl:with-param name="recursive-index" select="$recursive-index + 1"/>
                    <xsl:with-param name="types-callstack"
                                    select="$types-callstack | $this-type"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="recursive">yes</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
