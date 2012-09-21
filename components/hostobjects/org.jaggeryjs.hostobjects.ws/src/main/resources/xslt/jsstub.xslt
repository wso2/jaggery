<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 2008 WSO2, Inc. http://www.wso2.org

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  Created by: Jonathan Marsh <jonathan@wso2.com>

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:import href="/xslt/xml-to-string.xslt"/>
    <xsl:output method="text"/>

    <xsl:param name="service" select="services/service[1]/@name"/>
    <xsl:param name="e4x" select="true()"/>
    <xsl:param name="localhost-endpoints" select="false()"/>

    <xsl:template match="/">
        <xsl:apply-templates select="services/service[@name=$service][1]"/>
    </xsl:template>

    <xsl:template match="service">
        <xsl:variable name="original-service-name">
            <xsl:value-of select="@name"/>
        </xsl:variable>
//  Example stubs for <xsl:value-of select="$original-service-name"/> operations.  This function is not intended
//  to be called, but rather as a source for copy-and-paste development.

//  Note that this stub has been generated for use in <xsl:choose>
        <xsl:when test="$e4x">E4X</xsl:when>
        <xsl:otherwise>DOM</xsl:otherwise>
</xsl:choose> environments.
<xsl:if test="$localhost-endpoints">//  All endpoints have been converted to the "localhost" domain.</xsl:if>

var services = services || {};
services.visible = false;

var WebService = WebService || {};
WebService.visible = false;

(function() {

    var $ = {
        _setOptions : function (details, opName) {
            var options = new Array();

            if (details.type == 'SOAP12') options.useSOAP = 1.2;
            else if (details.type == 'SOAP11') options.useSOAP = 1.1;
            else if (details.type == 'HTTP') options.useSOAP = false;

            if (options.useSOAP != false) {
                if (details.action != null) {
                    options.useWSA = true;
                    options.action = details.action[opName];
                } else if (details.soapaction != null) {
                    options.useWSA = false;
                    options.action = details.soapaction[opName];
                } else {
                    options.useWSA = false;
                    options.action = undefined;
                }
            }

            if (details["httpmethod"] != null) {
                options.HTTPMethod = details.httpmethod[opName];
            } else {
                options.HTTPMethod = null;
            }

            if (details["httpinputSerialization"] != null) {
                options.HTTPInputSerialization = details.httpinputSerialization[opName];
            } else {
                options.HTTPInputSerialization= null;
            }

            if (details["httplocation"] != null) {
                options.HTTPLocation = details.httplocation[opName];
            } else {
                options.HTTPLocation = null;
            }

            if (details["httpignoreUncited"] != null) {
                options.HTTPLocationIgnoreUncited = details.httpignoreUncited[opName];
            } else {
                options.HTTPLocationIgnoreUncited = null;
            }

            if (details["httpqueryParameterSeparator"] != null) {
                options.HTTPQueryParameterSeparator = details.httpqueryParameterSeparator[opName];
            } else {
                options.HTTPQueryParameterSeparator = null;
            }

            if (details["policies"]) {
                var policies = details["policies"][opName];
                for(i=0; i&lt;policies.length; i++) {
                    if(policies[i] == "UTOverTransport") {
                        options.useWSS = true;
                        break;
                    }
                }
            }
            return options;
        }
    };

    var service = new function () {
        this.$ = {};
        this.$.readyState = 0;
        this.$.onreadystatechange = null;
        this.$.scriptInjectionCallback = null;
        this.$.proxyAddress = null;

        //public accessors for manually intervening in setting the address (e.g. supporting tcpmon)
        this.$.getAddress = function (endpointName) {
            return this._endpointDetails[endpointName].address;
        };

        this.$.setAddress = function (endpointName, address) {
            this._endpointDetails[endpointName].address = address;
        };

        // private helper functions
        this.$._getWSRequest = function() {
            var wsrequest;
            try {
                var ws = require('ws');
                wsrequest = new ws.WSRequest();
                // try to set the proxyAddress based on the context of the stub - browser or Mashup Server
                try {
                    wsrequest.proxyEngagedCallback = this.scriptInjectionCallback;
                    wsrequest.proxyAddress = this.proxyAddress;
                } catch (e) {
                    try {
                        wsrequest.proxyEngagedCallback = this.scriptInjectionCallback;
                        wsrequest.proxyAddress = this.proxyAddress;
                    } catch (e) { }
                }
            } catch(e) {
                try {
                    wsrequest = new ActiveXObject("WSRequest");
                } catch(e) {
                    try {
                        wsrequest = new SOAPHttpRequest();
                    } catch (e) {
                        throw new WebServiceError("WSRequest object not defined.", "WebService._getWSRequest() cannot instantiate WSRequest object.");
                    }
                }
            }
            return wsrequest;
        };

        this.$._endpointDetails =
            {<xsl:for-each select="/services/service[@name=$original-service-name]">
                <xsl:sort select="@type = 'SOAP12'" order="descending"/>
                <xsl:sort select="@type = 'SOAP11'" order="descending"/>
                <xsl:sort select="@address" order="ascending"/>
                "<xsl:value-of select="@endpoint"/>": {
                    "type" : "<xsl:value-of select="@type"/>",
                    "address" : "<xsl:call-template name="localnameify-url"><xsl:with-param name="url" select="@address"/></xsl:call-template>"<xsl:if test="operations/operation/binding-details/@wsawaction">,
                    "action" : {<xsl:for-each select="operations/operation[binding-details/@wsawaction]">
                        "<xsl:value-of select="@name"/>" : "<xsl:value-of select="binding-details/@wsawaction"/>"<xsl:if test="position() &lt; last()">,</xsl:if>
                        </xsl:for-each>
                    }</xsl:if>
                    <xsl:if test="operations/operation/binding-details/@soapaction">,
                    "soapaction" : {<xsl:for-each select="operations/operation">
                        "<xsl:value-of select="@name"/>" : "<xsl:value-of select="binding-details/@soapaction"/>"<xsl:if test="position() &lt; last()">,</xsl:if>
                        </xsl:for-each>
                    }</xsl:if><xsl:if test="operations/operation/binding-details/policy">,
                    "policies" : {<xsl:for-each select="operations/operation">
                        "<xsl:value-of select="@name"/>" : [<xsl:for-each select="binding-details/policy">
                        "<xsl:value-of select="@type"/>"<xsl:if test="position() &lt; last()">,</xsl:if></xsl:for-each>]<xsl:if test="position() &lt; last()">,</xsl:if>
                        </xsl:for-each>
                    }</xsl:if><xsl:if test="operations/operation/binding-details/@httplocation">,
                    "httplocation" : {<xsl:for-each select="operations/operation">
                        "<xsl:value-of select="@name"/>" : "<xsl:value-of select="binding-details/@httplocation"/>"<xsl:if test="position() &lt; last()">,</xsl:if>
                        </xsl:for-each>
                    }</xsl:if><xsl:if test="operations/operation/binding-details/@httpignoreUncited">,
                    "httpignoreUncited" : {<xsl:for-each select="operations/operation">
                            "<xsl:value-of select="@name"/>" : "<xsl:value-of select="binding-details/@httpignoreUncited"/>"<xsl:if test="position() &lt; last()">,</xsl:if>
                        </xsl:for-each>
                    }</xsl:if><xsl:if test="operations/operation/binding-details/@httpmethod">,
                    "httpmethod" : {<xsl:for-each select="operations/operation">
                            "<xsl:value-of select="@name"/>" : <xsl:choose>
                                <xsl:when test="binding-details/@httpmethod">"<xsl:value-of select="binding-details/@httpmethod"/>"</xsl:when>
                                <xsl:otherwise>null</xsl:otherwise>
                        </xsl:choose><xsl:if test="position() &lt; last()">,</xsl:if>
                        </xsl:for-each>
                    }</xsl:if><xsl:if test="operations/operation/binding-details/@httpqueryParameterSeparator">,
                    "httpqueryParameterSeparator" : {<xsl:for-each select="operations/operation">
                            "<xsl:value-of select="@name"/>" : "<xsl:value-of select="binding-details/@httpqueryParameterSeparator"/>"<xsl:if test="position() &lt; last()">,</xsl:if>
                        </xsl:for-each>
                    }</xsl:if><xsl:if test="operations/operation/binding-details/@httpinputSerialization">,
                    "httpinputSerialization" : {<xsl:for-each select="operations/operation">
                            "<xsl:value-of select="@name"/>" : "<xsl:value-of select="binding-details/@httpinputSerialization"/>"<xsl:if test="position() &lt; last()">,</xsl:if>
                        </xsl:for-each>
                    }</xsl:if><xsl:if test="@type = 'HTTP'">,
                    "fitsInURLParams" : {<xsl:for-each select="operations/operation">
                            "<xsl:value-of select="@name"/>" : <xsl:value-of select="count(signature/params/param[@simple='no' or ((@type = 'QName' or @type = 'NOTATION' or @type = 'hexBinary' or @type = 'base64Binary') and @type-namespace = 'http://www.w3.org/2001/XMLSchema')]) = 0"/><xsl:if test="position() &lt; last()">,</xsl:if>
                        </xsl:for-each>
                    }</xsl:if>
                }<xsl:if test="position() &lt; last()">,</xsl:if></xsl:for-each>
        };
        this.$.endpoint = "<xsl:for-each select="/services/service[@name=$original-service-name]">
            <xsl:sort select="@type = 'SOAP12'" order="descending"/>
            <xsl:sort select="@type = 'SOAP11'" order="descending"/>
            <xsl:sort select="@address" order="ascending"/>
            <xsl:if test="position() = 1"><xsl:value-of select="@endpoint"/></xsl:if>
        </xsl:for-each>";

        this.$.username = null;
        this.$.password = null;

        this.$._call = function (opName, pattern, reqContent, callback, userdata) {
            var details = this._endpointDetails[this.endpoint];
            this._options = $._setOptions(details, opName);

            var isAsync = (typeof(callback) == 'function');

            var thisRequest = this._getWSRequest();
            thisRequest.pattern = pattern;
            if (isAsync) {
                thisRequest._userdata = userdata;
                thisRequest.onreadystatechange =
                    function() {
                        if (thisRequest.readyState == 4) {
                            callback(thisRequest, userdata);
                        }
                    }
            }

            if (this.username == null)
                thisRequest.open(this._options, details.address, isAsync);
            else
                thisRequest.open(this._options, details.address, isAsync, this.username, this.password);

            thisRequest.send(reqContent);
            if (isAsync) {
                return "";
            } else {
                try {
                    var resultContent = thisRequest.responseText;
                    if (resultContent == "") {
                        throw new WebServiceError("No response", "WebService._call() did not recieve a response to a synchronous request.");
                    }
                    <xsl:choose>
                        <xsl:when test="$e4x">var resultXML = new XML(thisRequest.responseText);</xsl:when>
                        <xsl:otherwise>var resultXML = thisRequest.responseXML;</xsl:otherwise>
                    </xsl:choose>
                } catch (e) {
                    throw new WebServiceError(e);
                }
                return resultXML;
            }
        };
    };
    service.operations = {};
    <xsl:for-each select="operations/operation">
        <xsl:sort select="@name"/>
    service.operations['<xsl:value-of select="@name"/>'] = {};
    service.operations['<xsl:value-of select="@name"/>'].request = function (request) {
        var isAsync, response, resultValue;
        var operation = service.operations['<xsl:value-of select="@name"/>'];
        request = typeof request === "string" ? request : request;
        service.$._options = new Array();
        isAsync = (operation.callback != null &amp;&amp; typeof(operation.callback) == 'function');

        if (isAsync) {
            try {
                service.$._call(
                    "<xsl:value-of select="@name"/>",
                    "<xsl:value-of select="@pattern"/>",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            <xsl:choose>
                                <xsl:when test="$e4x">response = new XML(thisRequest.responseText);</xsl:when>
                                <xsl:otherwise>response = thisRequest.responseXML;</xsl:otherwise>
                            </xsl:choose>
                            callbacks[0](response);
                        }
                    },
                    new Array(operation.callback, operation.onError)
                );
            } catch (e) {
                var error;
                if (WebServiceError.prototype.isPrototypeOf(e)) {
                    error = e;
                } else if (e.name != null) {
                    // Mozilla
                    error = new WebServiceError(e.name, e.message + " (" + e.fileName + "#" + e.lineNumber + ")");
                } else if (e.description != null) {
                    // IE
                    error = new WebServiceError(e.description, e.number, e.number);
                } else {
                    error = new WebServiceError(e, "Internal Error");
                }
                operation.onError(error);
            }
        } else {
            try {
                response = service.$._call("<xsl:value-of select="@name"/>", "<xsl:value-of select="@pattern"/>", request);
                return response;
            } catch (e) {
                if (typeof(e) == "string") throw(e);
                if (e.message) throw(e.message);
                throw (e.reason);
            }
        }
        return null; // Suppress warnings when there is no return.
    };
    <xsl:variable name="payload-xml">
        <xsl:call-template name="payload-xml">
            <xsl:with-param name="params" select="signature/params"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="payload">
        <xsl:choose>
            <xsl:when test="$payload-xml">'<xsl:call-template name="xml-to-string">
                        <xsl:with-param name="node-set" select="$payload-xml"/>
                    </xsl:call-template>'</xsl:when>
            <xsl:otherwise>null</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    service.operations['<xsl:value-of select="@name"/>'].payloadXML = function () {
        return <xsl:value-of select="$payload"/>;
    };
    service.operations['<xsl:value-of select="@name"/>'].payloadXML.visible = false;

    service.operations['<xsl:value-of select="@name"/>'].payloadJSON = function () {
        var payload = this.payloadXML();
        return (payload != null &amp;&amp; payload != "") ? payload : null;
    };
    service.operations['<xsl:value-of select="@name"/>'].payloadJSON.visible = false;

    service.operations['<xsl:value-of select="@name"/>'].callback = null;
    </xsl:for-each>
    /**
     * Utility functions related to JavaScript Stub. These functions will
     * be added in the global scope as WebService.utils.
     **/
    var utils = {
        toXSdate : function (thisDate) {
            var year = thisDate.getUTCFullYear();
            var month = thisDate.getUTCMonth() + 1;
            var day = thisDate.getUTCDate();

            return year + "-" +
                (month &lt; 10 ? "0" : "") + month + "-" +
                (day &lt; 10 ? "0" : "") + day + "Z";
        },

        toXStime : function (thisDate) {
            var hours = thisDate.getUTCHours();
            var minutes = thisDate.getUTCMinutes();
            var seconds = thisDate.getUTCSeconds();
            var milliseconds = thisDate.getUTCMilliseconds();

            return (hours &lt; 10 ? "0" : "") + hours + ":" +
                (minutes &lt; 10 ? "0" : "") + minutes + ":" +
                (seconds &lt; 10 ? "0" : "") + seconds +
                (milliseconds == 0 ? "" : (milliseconds/1000).toString().substring(1)) + "Z";
        },

        toXSdateTime : function (thisDate) {
            var year = thisDate.getUTCFullYear();
            var month = thisDate.getUTCMonth() + 1;
            var day = thisDate.getUTCDate();
            var hours = thisDate.getUTCHours();
            var minutes = thisDate.getUTCMinutes();
            var seconds = thisDate.getUTCSeconds();
            var milliseconds = thisDate.getUTCMilliseconds();

            return year + "-" +
                (month &lt; 10 ? "0" : "") + month + "-" +
                (day &lt; 10 ? "0" : "") + day + "T" +
                (hours &lt; 10 ? "0" : "") + hours + ":" +
                (minutes &lt; 10 ? "0" : "") + minutes + ":" +
                (seconds &lt; 10 ? "0" : "") + seconds +
                (milliseconds == 0 ? "" : (milliseconds/1000).toString().substring(1)) + "Z";
        },

        parseXSdateTime : function (dateTime) {
            var buffer = dateTime.toString();
            var p = 0; // pointer to current parse location in buffer.

            var era, year, month, day, hour, minute, second, millisecond;

            // parse date, if there is one.
            if (buffer.substr(p,1) == '-')
            {
                era = -1;
                p++;
            } else {
                era = 1;
            }

            if (buffer.charAt(p+2) != ':')
            {
                year = era * buffer.substr(p,4);
                p += 5;
                month = buffer.substr(p,2);
                p += 3;
                day = buffer.substr(p,2);
                p += 3;
            } else {
                year = 1970;
                month = 1;
                day = 1;
            }

            // parse time, if there is one
            if (buffer.charAt(p) != '+' &amp;&amp; buffer.charAt(p) != '-')
            {
                hour = buffer.substr(p,2);
                p += 3;
                minute = buffer.substr(p,2);
                p += 3;
                second = buffer.substr(p,2);
                p += 2;
                if (buffer.charAt(p) == '.')
                {
                    millisecond = parseFloat(buffer.substring(p))*1000;
                    // Note that JS fractional seconds are significant to 3 places - xs:time is significant to more -
                    // though implementations are only required to carry 3 places.
                    p++;
                    while (buffer.charCodeAt(p) >= 48 &amp;&amp; buffer.charCodeAt(p) &lt;= 57) p++;
                } else {
                    millisecond = 0;
                }
            } else {
                hour = 0;
                minute = 0;
                second = 0;
                millisecond = 0;
            }

            var tzhour = 0;
            var tzminute = 0;
            // parse time zone
            if (buffer.charAt(p) != 'Z' &amp;&amp; buffer.charAt(p) != '') {
                var sign = (buffer.charAt(p) == '-' ? -1 : +1);
                p++;
                tzhour = sign * buffer.substr(p,2);
                p += 3;
                tzminute = sign * buffer.substr(p,2);
            }

            var thisDate = new Date();
            thisDate.setUTCFullYear(year);
            thisDate.setUTCMonth(month-1);
            thisDate.setUTCDate(day);
            thisDate.setUTCHours(hour);
            thisDate.setUTCMinutes(minute);
            thisDate.setUTCSeconds(second);
            thisDate.setUTCMilliseconds(millisecond);
            thisDate.setUTCHours(thisDate.getUTCHours() - tzhour);
            thisDate.setUTCMinutes(thisDate.getUTCMinutes() - tzminute);
            return thisDate;
        },

        _encodeXML : function (value) {
            var str = value.toString();
            str = str.replace(/&amp;/g, "&amp;amp;");
            str = str.replace(/&lt;/g, "&amp;lt;");
            return(str);
        },

        _nextPrefixNumber : 0,

        _QNameNamespaceDecl : function (qn) {
            if (qn.uri == null) return "";
            var prefix = qn.localName.substring(0, qn.localName.indexOf(":"));
            if (prefix == "") {
                prefix = "n" + ++this._nextPrefixNumber;
            }
            return ' xmlns:' + prefix + '="' + qn.uri + '"';
        },

        _QNameValue : function(qn) {
            if (qn.uri == null) return qn.localName;
            var prefix, localName;
            if (qn.localName.indexOf(":") >= 0) {
                prefix = qn.localName.substring(0, qn.localName.indexOf(":"));
                localName = qn.localName.substring(qn.localName.indexOf(":")+1);
            } else {
                prefix = "n" + this._nextPrefixNumber;
                localName = qn.localName;
            }
            return prefix + ":" + localName;
        },

        scheme : function (url) {
            return url.substring(0, url.indexOf(':'));
        },

        domain : function (url) {
            return url.substring(url.indexOf('://') + 3, url.indexOf('/',url.indexOf('://')+3));
        },

        domainPort : function (url) {
            var d = this.domain(url);
            if (d.indexOf(":") >= 0)
            d = d.substring(d.indexOf(':') +1);
            return d;
        },

        domainNoPort : function (url) {
            var d = this.domain(url);
            if (d.indexOf(":") >= 0)
            d = d.substring(0, d.indexOf(':'));
            return d;
        },

        _serializeAnytype : function (name, value, namespace, optional) {
            // dynamically serialize an anyType value in xml, including setting xsi:type.
            if (optional &amp;&amp; value == null) return "";
            var type = "xs:string";
            if (value == null) {
                value = "";
            } else if (typeof(value) == "number") {
                type = "xs:double";
            <xsl:if test="$e4x">} else if (typeof(value) == "xml") {
                type = "xs:anyType";
                value = value.toXMLString();</xsl:if>
            <xsl:if test="not($e4x)">} else if (typeof(value) == "object" &amp;&amp; value.nodeType != undefined) {
                type = "xs:anyType";
                value = utils.serializeXML(value);</xsl:if>
            } else if (typeof(value) == "boolean") {
                type = "xs:boolean";
            } else if (typeof(value) == "object" &amp;&amp; Date.prototype.isPrototypeOf(value)) {
                type = "xs:dateTime";
                value = utils.toXSdateTime(value);
            } else if (value.match(/^\s*true\s*$/g) != null) {
                type = "xs:boolean";
            } else if (value.match(/^\s*false\s*$/g) != null) {
                type = "xs:boolean";
            } else if (!isNaN(Date.parse(value))) {
                type = "xs:dateTime";
                value = utils.toXSdateTime(new Date(Date.parse(value)));
            } else if (value.match(/^\s*\-?\d*\-\d\d\-\d\dZ?\s*$/g) != null) {
                type = "xs:date";
            } else if (value.match(/^\s*\-?\d*\-\d\d\-\d\d[\+\-]\d\d:\d\d\s*$/g) != null) {
                type = "xs:date";
            } else if (value.match(/^\s*\d\d:\d\d:\d\d\.?\d*Z?\s*$/g) != null) {
                type = "xs:time";
            } else if (value.match(/^\s*\d\d:\d\d:\d\d\.?\d*[\+\-]\d\d:\d\d\s*$/g) != null) {
                type = "xs:time";
            } else if (value.match(/^\s*\-?\d*\-\d\d\-\d\dT\d\d:\d\d:\d\d\.?\d*Z?\s*$/g) != null) {
                type = "xs:dateTime";
            } else if (value.match(/^\s*\-?\d*\-\d\d\-\d\dT\d\d:\d\d:\d\d\.?\d*[\+\-]\d\d:\d\d\s*$/g) != null) {
                type = "xs:dateTime";
            } else if (value.match(/^\s*\d\d*\.?\d*\s*$/g) != null) {
                type = "xs:double";
            } else if (value.match(/^\s*\d*\.?\d\d*\s*$/g) != null) {
                type = "xs:double";
            } else if (value.match(/^\s*\&lt;/g) != null) {
        <xsl:choose>
            <xsl:when test="$e4x">
                try {
                    value = new XML(value).toXMLString();
                    type = "xs:anyType";
                } catch (e) {}
            </xsl:when>
            <xsl:otherwise>
                var browser = WSRequest.util._getBrowser();
                var parseTest;
                if (browser == "ie" || browser == "ie7") {
                    parseTest = new ActiveXObject("Microsoft.XMLDOM");
                    parseTest.loadXML(value);
                    if (parseTest.parseError == 0)
                        type = "xs:anyType";
                } else {
                    var parser = new DOMParser();
                    parseTest = parser.parseFromString(value,"text/xml");
                    if (parseTest.documentElement.nodeName != "parsererror" || parseTest.documentElement.namespaceURI != "http://www.mozilla.org/newlayout/xml/parsererror.xml")
                        type = "xs:anyType";
                }
            </xsl:otherwise>
        </xsl:choose>
            }
            if (type == "xs:string") {
                value = this._encodeXML(value);
            }
            var starttag =   "&lt;" + name +
                         (namespace == "" ? "" : " xmlns='" + namespace + "'") +
                         " xsi:type='" + type + "'" +
                         " xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
                         "&gt;";
            var endtag = "&lt;/" + name + "&gt;";
            return starttag + value + endtag;
        },

        /*
         * serializeAnyting : serialize simple types, XML, or arrays and objects
         *
         *    data: data to serialize - can be of any type.
         *    partOfList: a flag indicating whether this is a top-level invocation or a recursive one.
         *                used for determining whether to quote strings and keep XML un-pretty printed
         *                when serializing arrays and objects.
         */
        serializeAnything : function(data, partOfList) {
            var output = "";
            if (data == null) {
                // not friendly just to output nothing - insert a visible marker representing a null return.
                output += "(null)";
            } else if (typeof(data) == "string") {
                if (partOfList) {
                    output += '"' + this._encodeXML(data) + '"';
                } else {
                    output += this._encodeXML(data);
                }
            <xsl:if test="$e4x">} else if (typeof(data) == "xml") {
                output += serializeXML (payload, true);
                } else if (typeof(data) == "xmlList") {
                    for each (var item in data) {
                        output += serializeXML (item, true);
                    }
            </xsl:if>} else if (typeof(data) == "object") {
                if (Array.prototype.isPrototypeOf(data)) {
                    // this "object" is really an "array" in disguise
                    output += serializeArray(data);
                } else if (Date.prototype.isPrototypeOf(data)) {
                    // this "object" is really a "date" in disguise
                    output += data;
                <xsl:if test="not($e4x)">} else if (data.nodeType != undefined) {
                    // this "object" is really a DOM node in disguise
                    output += serializeXML (data, true);
                } else if (data.length != undefined &amp;&amp; data.item(0) != undefined) {
                    // this "object" is really a DOM node list in disguise
                    for (var i=0; i&lt;data.length; i++) {
                        output += serializeXML (data.item(i), true);
                    }</xsl:if>
                } else {
                    // must be a generic object then...
                    output += serializeObject(data);
                }
            } else {
                output += this._encodeXML(data);
            }
            return output;
        },

        <xsl:if test="not($e4x)">serializeXML : function(payload, encode) {
            var browser = WSRequest.util._getBrowser();
            switch (browser) {
                case "gecko":
                case "safari":
                    var serializer = new XMLSerializer();
                    payload = serializer.serializeToString(payload);
                    break;
                case "ie":
                case "ie7":
                    payload = payload.xml;
                    break;
                case "opera":
                    var xmlSerializer = document.implementation.createLSSerializer();
                    payload = xmlSerializer.writeToString(payload);
                    break;
                case "undefined":
                    throw new WebServiceError("Unknown browser", "WSRequest.util._serializeToString doesn't recognize the browser, to invoke browser-specific serialization code.");
            }
            return encode ? WebService.utils._encodeXML(payload) : payload;
        },

        /*
         * serializeObject : serialize objects in JSON-like notation
         *
         *    obj: object to serialize.
         */
        serializeObject : function(obj) {
            var output = "{";
            for (var i in obj) {
                if (output != '{') output += ", ";
                output += i + " : " + serializeAnything(obj[i], true);
            }
            output += "}";
            return output;
        },

        /*
         * serializeArray : serialize arrays as comma-separated lists.  Can't just use existing
         *                  JS functions since the array might contain xml, objects, or something
         *                  else requiring recursive treatment.
         *
         *     arr: array to serialize.
         */
        serializeArray : function(arr) {
            var output = "";
            for (var i in arr) {
                if (i != 0) output += ",";
                output += serializeAnything(arr[i]);
            }
            return output;
        },
        </xsl:if>
        // library function for dynamically converting an element with js:type annotation to a Javascript type.
        <xsl:choose>
            <xsl:when test="$e4x">_convertJSType : function (element, isWrapped) {
            if (element == null) return "";
            var extractedValue = element.*.toString();
            var resultValue, i;
            var js = new Namespace("http://www.wso2.org/ns/jstype");
            var type = element.@js::type;
            if (type == null) {
                type = "#raw";
            } else {
                type = type.toString();
            }
            switch (type) {
                case "string":
                    return extractedValue;
                    break;
                case "number":
                    return parseFloat(extractedValue);
                    break;
                case "boolean":
                    return extractedValue == "true" || extractedValue == "1";
                    break;
                case "date":
                    return utils.parseXSdateTime(extractedValue);
                    break;
                case "array":
                    resultValue = new Array();
                    for (i=0; i&lt;element.*.length(); i++) {
                        resultValue = resultValue.concat(utils._convertJSType(element[i]));
                    }
                    return(resultValue);
                    break;
                case "object":
                    resultValue = new Object();
                    for (i=0; i&lt;element.*.length(); i++) {
                        resultValue[element[i].name()] = utils._convertJSType(element[i]);
                    }
                    return(resultValue);
                    break;
                case "xmlList":
                    return element.*;
                    break;
                case "xml":
                    return element.*[0];
                    break;
                case "#raw":
                default:
                    if (isWrapped == true)
                        return element.*;
                    else return element;
                    break;
            }
        }</xsl:when>
            <xsl:otherwise>_convertJSType : function (element, isWrapped) {
            if (element == null) return "";
            var extractedValue = WSRequest.util._stringValue(element);
            var resultValue, i;
            var type = element.getAttribute("js:type");
            if (type == null) {
                type = "#raw";
            } else {
                type = type.toString();
            }
            switch (type) {
                case "string":
                    return extractedValue;
                    break;
                case "number":
                    return parseFloat(extractedValue);
                    break;
                case "boolean":
                    return extractedValue == "true" || extractedValue == "1";
                    break;
                case "date":
                    return utils.parseXSdateTime(extractedValue);
                    break;
                case "array":
                    resultValue = new Array();
                    for (i=0; i&lt;element.childNodes.length; i++) {
                        resultValue = resultValue.concat(utils._convertJSType(element.childNodes[i]));
                    }
                    return(resultValue);
                    break;
                case "object":
                    resultValue = new Object();
                    for (i=0; i&lt;element.childNodes.length; i++) {
                        resultValue[element.childNodes[i].tagName] = utils._convertJSType(element.childNodes[i]);
                    }
                    return(resultValue);
                    break;
                case "xmlList":
                    return element.childNodes;
                    break;
                case "xml":
                    return element.firstChild;
                    break;
                case "#raw":
                default:
                    if (isWrapped)
                        return element.firstChild;
                    else return element;
                    break;
            }
        }</xsl:otherwise>
        </xsl:choose>
    };

    services['<xsl:value-of select="$original-service-name"/>'] = service;
    services.$ = $;
    WebService.utils = WebService.utils || utils;
})();

</xsl:template>

<xsl:template name="localnameify-url">
    <xsl:param name="url"/>
    <xsl:variable name="scheme" select="substring-before($url, '://')"/>
    <xsl:choose>
        <xsl:when test="$localhost-endpoints and ($scheme='http' or $scheme='https')">
            <xsl:value-of select="$scheme"/>
            <xsl:text>://localhost</xsl:text>
            <xsl:variable name="remainder" select="substring-after($url, concat($scheme,'://'))"/>
            <xsl:variable name="domain" select="substring-before($remainder,'/')"/>
            <xsl:if test="contains($domain,':')">
                <xsl:text>:</xsl:text>
                <xsl:value-of select="substring-after($domain, ':')"/>
            </xsl:if>
            <xsl:text>/</xsl:text>
            <xsl:value-of select="substring-after($remainder, '/')"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$url"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="payload-xml">
    <xsl:param name="params"/>
    <xsl:choose>
        <xsl:when test="count($params/*) = 1 and count($params/param[@type = 'anyType']) = 1">
        </xsl:when>
        <xsl:when test="count($params/*) > 0">
            <xsl:element name="p:{$params/@wrapper-element}" namespace="{$params/@wrapper-element-ns}">
                <xsl:call-template name="infer-types">
                    <xsl:with-param name="params" select="$params"/>
                </xsl:call-template>
            </xsl:element>
        </xsl:when>
    </xsl:choose>
</xsl:template>

<xsl:template name="infer-types">
        <xsl:param name="params"/>
        <xsl:variable name="attributes" select="$params/child::node()[@attribute = 'yes']"/>
        <xsl:variable name="elements" select="$params/child::node()[not(@attribute) or @attribute != 'yes']"/>
        <xsl:for-each select="$attributes">
            <xsl:choose>
                <xsl:when test="@targetNamespace">
                    <xsl:attribute name="{@name}" namespace="{@targetNamespace}">?</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="{@name}">?</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        <xsl:choose>
            <xsl:when test="count($elements) > 0">
                <xsl:for-each select="$elements">
                    <xsl:variable name="lname">
                        <xsl:choose>
                            <xsl:when test="@targetNamespace and @type-prefix and @type-prefix != ''">
                                <xsl:value-of select="concat(@type-prefix, ':', @name)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="@name"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="local-name() = 'param'">
                            <xsl:choose>
                                <xsl:when test="@recursive = 'yes'">
                                    <xsl:call-template name="occurrence-comment">
                                        <xsl:with-param name="maxOccurs" select="@maxOccurs"/>
                                        <xsl:with-param name="minOccurs" select="@minOccurs"/>
                                    </xsl:call-template>
                                    <xsl:element name="{$lname}" namespace="{@targetNamespace}">?</xsl:element>
                                </xsl:when>
                                <xsl:when test="@token = '#in'">
                                    <xsl:choose>
                                        <xsl:when test="@simple = 'yes'">
                                            <xsl:call-template name="occurrence-comment">
                                                <xsl:with-param name="maxOccurs" select="@maxOccurs"/>
                                                <xsl:with-param name="minOccurs" select="@minOccurs"/>
                                            </xsl:call-template>
                                            <xsl:if test="@targetNamespace = ''">
                                                <xsl:element name="{$lname}">?</xsl:element>
                                            </xsl:if>
                                            <xsl:if test="@targetNamespace != ''">
                                                <xsl:element name="{$lname}" namespace="{@targetNamespace}">?</xsl:element>
                                            </xsl:if>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:call-template name="occurrence-comment">
                                                <xsl:with-param name="maxOccurs" select="@maxOccurs"/>
                                                <xsl:with-param name="minOccurs" select="@minOccurs"/>
                                            </xsl:call-template>
                                            <xsl:element name="{$lname}" namespace="{@targetNamespace}">
                                                <xsl:call-template name="infer-types">
                                                    <xsl:with-param name="params" select="."/>
                                                </xsl:call-template>
                                            </xsl:element>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:when test="@token = '#any'">
                                    <xsl:comment>You may enter ANY elements at this point</xsl:comment>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:message>TODO:
                                        <xsl:value-of select="@token"/>
                                    </xsl:message>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="local-name() = 'inherited-content'">
                            <xsl:choose>
                                <xsl:when test="@recursive = 'yes'">
                                    <xsl:comment>Content of type "<xsl:value-of select="@extension"/>" which has a recursive type definition goes here</xsl:comment>

                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="infer-types">
                                        <xsl:with-param name="params" select="."/>
                                    </xsl:call-template>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>?</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="occurrence-comment">
        <xsl:param name="maxOccurs"/>
        <xsl:param name="minOccurs"/>
        <xsl:choose>
            <xsl:when test="$minOccurs = 'unbounded' and $maxOccurs = 'unbounded'">
                <xsl:comment>0 or more occurrences</xsl:comment>
            </xsl:when>
            <xsl:when test="$maxOccurs = 'unbounded'">
                <xsl:comment><xsl:value-of select="$minOccurs"/> or more occurrences</xsl:comment>
            </xsl:when>
            <xsl:when test="$minOccurs = $maxOccurs">
                <xsl:comment>Exactly <xsl:value-of select="$minOccurs"/> occurrence<xsl:if test="$minOccurs != '1'">s</xsl:if></xsl:comment>
            </xsl:when>
            <xsl:otherwise>
                <xsl:comment><xsl:value-of select="$minOccurs"/> to <xsl:value-of select="$maxOccurs"/> occurrence<xsl:if test="$maxOccurs != '1'">s</xsl:if></xsl:comment>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
