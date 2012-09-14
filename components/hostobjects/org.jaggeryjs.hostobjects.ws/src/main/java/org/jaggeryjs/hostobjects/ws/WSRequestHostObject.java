/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
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
package org.jaggeryjs.hostobjects.ws;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBindingMessage;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.http.CommonsTransportHeaders;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.transport.http.HttpTransportProperties.Authenticator;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.KerberosConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.jaggeryjs.hostobjects.ws.internal.WSRequestServiceComponent;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.*;
import org.w3c.dom.Document;
import org.wso2.javascript.xmlimpl.XML;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.*;

/**
 * <p/> Mozilla Rhino host Object Implementation of the <a href="http://www.wso2.org/wiki/display/wsfajax/wsrequest_specification">
 * WSRequest Specification</a>. </p> <p/> See the <a href="http://www.wso2.org/wiki/display/mashup/WSRequest+Host+Object">
 * WSRequest Host Object reference guide</a> for more information. </p>
 */
public class WSRequestHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(WSRequestHostObject.class);

    private static final long serialVersionUID = -4540679471306518117L;
    private static final String IN_OUT = "in-out";
    private static final String IN_ONLY = "in-only";
    private static final String CLIENT_REPOSITORY_LOCATION = "Axis2Config.ClientRepositoryLocation";
    private static final String CLIENT_AXIS2_XML_LOCATION = "Axis2Config.clientAxis2XmlLocation";

    private static final String RAMPART = "rampart";
    private static final String ADDRESSING = "addressing";

    String responseText = null;

    Scriptable responseXML = null;

    int readyState = 0;

    // Call back javascript function
    Function onReadyStateChangeFunction;

    //Rhino context.
    Context context;

    private boolean async = true;

    private ServiceClient sender = null;

    //WebServiceError object in case the invocation result in an error
    WebServiceErrorHostObject error = null;

    private boolean wsdlMode = false;

    private String targetNamespace;

    private String mep = IN_OUT;

    private NativeArray soapHeaders = null;

    private NativeArray httpHeaders = null;

    private NativeObject rampartConfig = null;

    private XML policy = null;

    private CommonsTransportHeaders transportHeaders = null;

    /**
     * Constructor for the use by Rhino
     */
    public WSRequestHostObject() {
    }

    /**
     * Constructor the user will be using inside javaScript
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        WSRequestHostObject wsRequest = new WSRequestHostObject();
        wsRequest.context = cx;
        return wsRequest;
    }

    /**
     * Returns the name to be used for this JavaScript Object.
     */
    public String getClassName() {
        return "WSRequest";
    }

    /**
     * <p/> This method prepares the WSRequest object to invoke a Web service. This method
     * corresponds to the following function of the WSRequest java script object. </p>
     * <p/>
     * <pre>
     *   void open ( in object options | in String method, in String url [, in boolean async [, in
     * String user [, in String password]]]);
     * </pre>
     * <p/>
     * See <a href="http://www.wso2.org/wiki/display/mashup/WSRequest+Host+Object">WSRequest host
     * object reference</a> & <a href="http://www.wso2.org/wiki/display/wsfajax/wsrequest_specification">WSRequest
     * specification</a> for more details.
     */
    public static void jsFunction_open(Context cx, Scriptable thisObj, Object[] arguments,
                                       Function funObj) throws ScriptException {

        WSRequestHostObject wsRequest = checkInstance(thisObj);
        if (wsRequest.readyState > 0 && wsRequest.readyState < 4) {
            throw new ScriptException("Invalid readyState for WSRequest Hostobject : " + wsRequest.readyState);
        } else if (wsRequest.readyState == 4) {
            // reset private variables if readyState equals 4
            // readyState equals 4 means this object has been used earlier for an invocation.
            wsRequest.reset();
        }
        try {
            //ConfigurationContext configurationContext = ConfigurationContextFactory.
            //createBasicConfigurationContext("META-INF/axis2_client.xml");
            wsRequest.sender = new ServiceClient(WSRequestServiceComponent.getConfigurationContext(), null);
        } catch (Exception axisFault) {
            log.error("Error creating ServiceClient for WSRequest Hostobject", axisFault);
            throw new ScriptException(axisFault);
        }
        // Setting the cookie policy here
        setCommonProperties(cx, wsRequest, arguments, setOptionsOpen(wsRequest, arguments));
    }

    /**
     * This function enables you to give a WSDL and get WSRequest configured. You dont have to
     * configure it your self using an options object.
     */
    public static void jsFunction_openWSDL(Context cx, Scriptable thisObj, Object[] arguments,
                                           Function funObj) throws ScriptException {
        WSRequestHostObject wsRequest = checkInstance(thisObj);
        if (wsRequest.readyState > 0 && wsRequest.readyState < 4) {
            throw new ScriptException("Invalid readyState for WSRequest Hostobject : " + wsRequest.readyState);
        } else if (wsRequest.readyState == 4) {
            // reset private variables if readyState equals 4
            // readyState equals 4 means this object has been used earlier for an invocation.
            wsRequest.reset();
        }

        wsRequest.wsdlMode = true;
        setCommonProperties(cx, wsRequest, arguments, setOptionsOpenWSDL(wsRequest, arguments));
    }

    private static String getBaseURI(String currentURI) {
        try {
            File file = new File(currentURI);
            if (file.exists()) {
                return file.getCanonicalFile().getParentFile().toURI().toString();
            }
            String uriFragment = currentURI.substring(0, currentURI.lastIndexOf("/"));
            return uriFragment + (uriFragment.endsWith("/") ? "" : "/");
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * <p/> This method invokes the Web service with the requested payload. </p>
     * <p/>
     * <pre>
     *   void send ( [in Document payload | in XMLString payload | in XMLString payload ]);
     * </pre>
     * <p/>
     * See <a href="http://www.wso2.org/wiki/display/mashup/WSRequest+Host+Object">WSRequest host
     * object reference</a> & <a href="http://www.wso2.org/wiki/display/wsfajax/wsrequest_specification">WSRequest
     * specification</a> for more details.
     */
    public static void jsFunction_send(Context cx, Scriptable thisObj, Object[] arguments,
                                       Function funObj) throws ScriptException, AxisFault {
        WSRequestHostObject wsRequest = (WSRequestHostObject) thisObj;
        Object payload;
        QName operationName = ServiceClient.ANON_OUT_IN_OP;
        if (wsRequest.wsdlMode && arguments.length != 2) {
            throw new ScriptException("When the openWSDL method of WSRequest is used the send " +
                    "function should be called with 2 parameters. The operation to invoke and " +
                    "the payload");
        }
        if (arguments.length == 1) {
            payload = arguments[0];
        } else if (arguments.length == 2) {
            if (arguments[0] instanceof org.wso2.javascript.xmlimpl.QName) {
                org.wso2.javascript.xmlimpl.QName qName =
                        (org.wso2.javascript.xmlimpl.QName) arguments[0];
                String uri = (String) qName.get("uri", qName);
                String localName = (String) qName.get("localName", qName);
                operationName = new QName(uri, localName);
            } else if (arguments[0] instanceof String) {
                if (wsRequest.targetNamespace == null) {
                    throw new ScriptException("The targetNamespace of the service is null, please specify a " +
                            "QName for the operation name");
                }
                String localName = (String) arguments[0];
                operationName = new QName(wsRequest.targetNamespace, localName);
            } else {
                throw new ScriptException("Invalid parameter type for the WSRequest.send() method");
            }
            payload = arguments[1];
        } else {
            throw new ScriptException("Invalid no. of parameters for the WSRequest.send() method");
        }

        OMElement payloadElement = null;
        if (wsRequest.readyState != 1) {
            throw new ScriptException("Invalid readyState for the WSRequest Hostobject : " + wsRequest.readyState);
        }
        if (payload instanceof String) {
            try {
                XMLStreamReader parser = XMLInputFactory.newInstance()
                        .createXMLStreamReader(new StringReader((String) payload));
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                payloadElement = builder.getDocumentElement();
            } catch (Exception e) {
                String message = "Invalid input for the payload in WSRequest Hostobject : " + payload;
                log.error(message, e);
                throw new ScriptException(message, e);
            }
        } else if (payload instanceof XML) {
            try {
                OMNode node = ((XML) payload).getAxiomFromXML();
                if (node instanceof OMElement) {
                    payloadElement = (OMElement) node;
                } else {
                    throw new ScriptException("Invalid input for the payload in WSRequest Hostobject : " + payload);
                }
            } catch (Exception e) {
                String message = "Invalid input for the payload in WSRequest Hostobject : " + payload;
                log.error(message, e);
                throw new ScriptException(message, e);
            }
        }
        // else if (typeof(payload) == "object") {
        // // set DOOMRequired to true
        // DocumentBuilderFactoryImpl.setDOOMRequired(true);
        // try {
        // payload = payload.getFirstChild();
        // } catch(Exception e) {
        // throw new Error("INVALID_INPUT_EXCEPTION");
        // }
        // }
        // else if (payload == undefined) {
        // payload = null;
        // }

        try {
            if (wsRequest.async) { // asynchronous call to send()
                AxisCallback callback =
                        new WSRequestCallback(wsRequest);
                setRampartConfigs(wsRequest, operationName);
                if (wsRequest.wsdlMode) {
                    //                    setSSLProperties(wsRequest);
                    if (IN_ONLY.equalsIgnoreCase(wsRequest.mep)) {
                        wsRequest.sender.fireAndForget(operationName, payloadElement);
                        wsRequest.readyState = 4;
                    } else {
                        wsRequest.sender
                                .sendReceiveNonBlocking(operationName, payloadElement, callback);
                        wsRequest.readyState = 2;
                    }
                } else {
                    //                    setSSLProperties(wsRequest);
                    if (IN_ONLY.equalsIgnoreCase(wsRequest.mep)) {
                        wsRequest.sender.fireAndForget(payloadElement);
                        wsRequest.readyState = 4;
                    } else {
                        wsRequest.sender.sendReceiveNonBlocking(payloadElement, callback);
                        wsRequest.readyState = 2;
                    }
                }
            } else { // synchronous call to send()
                wsRequest.readyState = 2;
                // TODO do we need to call onreadystatechange here too
                setRampartConfigs(wsRequest, operationName);
                if (wsRequest.wsdlMode) {
                    //                    setSSLProperties(wsRequest);
                    if (IN_ONLY.equalsIgnoreCase(wsRequest.mep)) {
                        wsRequest.sender.fireAndForget(operationName, payloadElement);
                    } else {
                        wsRequest.updateResponse(
                                wsRequest.sender.sendReceive(operationName, payloadElement));
                    }
                    wsRequest.readyState = 4;
                } else {
                    //                    setSSLProperties(wsRequest);
                    if (IN_ONLY.equalsIgnoreCase(wsRequest.mep)) {
                        wsRequest.sender.fireAndForget(payloadElement);
                    } else {
                        wsRequest.updateResponse(
                                wsRequest.sender.sendReceive(operationName, payloadElement));
                        wsRequest.transportHeaders = (CommonsTransportHeaders) wsRequest.sender.getLastOperationContext().getMessageContext("In").getProperty(MessageContext.TRANSPORT_HEADERS);

                    }


                    wsRequest.readyState = 4;
                }
            }
            // Calling onreadystatechange function
            if (wsRequest.onReadyStateChangeFunction != null) {
                wsRequest.onReadyStateChangeFunction.call(cx, wsRequest, wsRequest, new Object[0]);
            }
        } catch (AxisFault e) {
            wsRequest.error = new WebServiceErrorHostObject();
            OMElement detail = e.getDetail();
            if (detail != null) {
                wsRequest.error.jsSet_detail(detail.toString());
            }
            QName faultCode = e.getFaultCode();
            if (faultCode != null) {
                wsRequest.error.jsSet_code(faultCode.toString());
            }
            wsRequest.error.jsSet_reason(e.getReason());
            String message = "Error occured while invoking the service";
            log.error(message, e);
            throw new ScriptException(message, e);
        } catch (Exception e) {
            wsRequest.error = new WebServiceErrorHostObject();
            wsRequest.error.jsSet_detail(e.getMessage());
            String message = "Error occured while invoking the service";
            log.error(message, e);
            throw new ScriptException(message, e);
        } finally {
            wsRequest.sender.cleanupTransport();
        }
    }

    /**
     * Getter for the responseText property. The raw text representing the XML (or non-XML)
     * response. If the responseXML property is empty, you can check the responseText property to
     * see if a non-XML response was received.
     */
    public String jsGet_responseText() {
        return responseText;
    }

    protected void updateResponse(OMElement response) {
        if (response instanceof OMSourcedElementImpl) {
            OMSourcedElementImpl sourcedElement = (OMSourcedElementImpl) response;
            setJSONAsXML(sourcedElement);
        } else if (response != null) {
            Object[] objects = {response};
            responseXML = context.newObject(this, "XML", objects);
            responseText = response.toString();
        }
    }

    public static String jsFunction_getResponseHeader(Context cx, Scriptable thisObj, Object[] arguments,
                                                      Function funObj) throws ScriptException {
        WSRequestHostObject wsRequest = (WSRequestHostObject) thisObj;
        if (arguments.length != 1) {
            throw new ScriptException("invalid number of arguments");
        }
        return (String) wsRequest.transportHeaders.get((String) arguments[0]);

    }

    private void setJSONAsXML(OMSourcedElementImpl response) {
        if (response.getDataSource() != null) {
            try {
                String jsonString = response.toStringWithConsume();
                responseText = jsonString;
                while (jsonString.indexOf("<?") == 0) {
                    jsonString = jsonString.substring(jsonString.indexOf("?>") + 2);
                }
                Object[] objects = {jsonString};
                responseXML = context.newObject(this, "XML", objects);
            } catch (XMLStreamException e) {
                String message = "Error while converting JSON into XML";
                log.error(message, e);
            }
        }
    }

    /**
     * Getter for the responseE4X property. The parsed E4X XML message representing the response
     * from the service.
     */
    public Scriptable jsGet_responseE4X() throws ScriptException {
        return responseXML;
    }

    /**
     * Getter for the responseXML property. The parsed XML message representing the response from
     * the service. Currently we return an E4X object. But ideally this needs to be a DOM.
     */
    public Scriptable jsGet_responseXML() throws ScriptException {
        return responseXML;
    }

    /**
     * Getter for the 'onreadystatechange' javascript function. This property can be set to a
     * javascript function object, which is invoked when the state of an asynchronous request
     * changes (e.g. the request completes).
     */
    public Scriptable jsGet_onreadystatechange() {
        return onReadyStateChangeFunction;
    }

    /**
     * Setter for the 'onreadystatechange' javascript function. This property can be set to a
     * javascript function object, which is invoked when the state of an asynchronous request
     * changes (e.g. the request completes).
     */
    public void jsSet_onreadystatechange(Function function) {
        onReadyStateChangeFunction = function;
    }

    /**
     * <p/> Getter for the readyState property </p> <p/> readyState property represents current
     * state of the object, which can be one of the following values: </p> <p/> <li>0: The object
     * has not been initialized by calling the open() method.</li> <li>1: The object has been
     * initialized successfully, but the send() method has not been called.</li> <li>2: The request
     * is pending</li> <li>3: The request is partially complete (some data has been received, and
     * may be available in the responseText property.</li> <li>4: The request is complete, all data
     * has been received.</li> <p/> Of these, typically only the last (readyState == 4) is used.
     * </p>
     */
    public int jsGet_readyState() {
        return readyState;
    }

    /**
     * <p/> Getter for the WebServiceError object </p> <p/> The WebServiceError object encapsulates
     * information about the cause of a failed Web service invocation. When an service invocation
     * failed to complete successfully (including internal errors, or protocol errors such as SOAP
     * faults) this error property can be used to find the cause. </p>
     * <p/>
     * WebServiceError object contains 3 member properties - code, reason & details. Refer to <a
     * href="http://www.wso2.org/wiki/display/mashup/WebServiceError">WebServiceError</a> for more
     * details.
     */
    public Scriptable jsGet_error() {
        if (error != null) {
            Object[] objects = {error};
            return context.newObject(this, "WebServiceError", objects);
        }
        return null;
    }

    /*
    * resets private variables
    */

    private void reset() {
        async = true;
        sender = null;
        this.readyState = 0;
    }

    private static WSRequestHostObject checkInstance(Scriptable obj) {
        if (obj == null || !(obj instanceof WSRequestHostObject)) {
            throw Context.reportRuntimeError("called on incompatible object");
        }
        return (WSRequestHostObject) obj;
    }

    private static void setCommonProperties(Context cx, WSRequestHostObject wsRequest,
                                            Object[] args, NativeArray optionsArray)
            throws ScriptException {
        wsRequest.responseText = null;
        wsRequest.responseXML = null;
        wsRequest.error = null;
        wsRequest.readyState = 1;
        // Calling onreadystatechange function
        if (wsRequest.onReadyStateChangeFunction != null) {
            wsRequest.onReadyStateChangeFunction.call(cx, wsRequest, wsRequest, new Object[0]);
        }

        Options options = wsRequest.sender.getOptions();
        if (options == null) {
            options = new Options();
        }
        int timeout = 60000;

        if (optionsArray != null) {
            Object mepObject = optionsArray.get("mep", optionsArray);
            if (mepObject != null && !(mepObject instanceof Undefined) &&
                    !(mepObject instanceof UniqueTag)) {
                String mepValue = mepObject.toString();
                if (IN_OUT.equalsIgnoreCase(mepValue) || IN_ONLY.equalsIgnoreCase(mepValue)) {
                    wsRequest.mep = mepValue;
                } else {
                    throw new ScriptException("Invalid value for mep. Supported values are in-out and in-only");
                }
            }

            Object timeoutObject = optionsArray.get("timeout", optionsArray);
            if (timeoutObject != null && !(timeoutObject instanceof Undefined) &&
                    !(timeoutObject instanceof UniqueTag)) {
                timeout = Integer.parseInt(timeoutObject.toString());
            }

            Object soapHeadersObject = optionsArray.get("SOAPHeaders", optionsArray);
            if (soapHeadersObject != null && !(soapHeadersObject instanceof Undefined) &&
                    !(soapHeadersObject instanceof UniqueTag) &&
                    soapHeadersObject instanceof NativeArray) {
                wsRequest.soapHeaders = (NativeArray) soapHeadersObject;
            }

            Object httpHeadersObject = optionsArray.get("HTTPHeaders", optionsArray);
            if (httpHeadersObject != null && !(httpHeadersObject instanceof Undefined) &&
                    !(httpHeadersObject instanceof UniqueTag) &&
                    httpHeadersObject instanceof NativeArray) {
                wsRequest.httpHeaders = (NativeArray) httpHeadersObject;
            }

            Object rampartConfigObject = optionsArray.get("rampart", optionsArray);
            if (rampartConfigObject != null && !(rampartConfigObject instanceof Undefined) &&
                    !(rampartConfigObject instanceof UniqueTag) && rampartConfigObject instanceof NativeObject) {
                wsRequest.rampartConfig = (NativeObject) rampartConfigObject;
            }

            Object policyObject = optionsArray.get("policy", optionsArray);
            if (policyObject != null && !(policyObject instanceof Undefined) &&
                    !(policyObject instanceof UniqueTag) && policyObject instanceof XML) {
                wsRequest.policy = (XML) policyObject;
            }
        }

        options.setProperty(HTTPConstants.SO_TIMEOUT, timeout);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, timeout);

        if (wsRequest.httpHeaders != null) {
            //Sets HTTPHeaders specified in options object an array of name-value json objects
            List<Header> httpHeaders = new ArrayList<Header>();
            String msg = "Invalid declaration for HTTPHeaders property";

            for (int i = 0; i < wsRequest.httpHeaders.getLength(); i++) {
                if (wsRequest.httpHeaders.get(i, wsRequest.httpHeaders) instanceof NativeObject) {
                    NativeObject headerObject =
                            (NativeObject) wsRequest.httpHeaders.get(i, wsRequest.httpHeaders);
                    if (headerObject.get("name", headerObject) instanceof String &&
                            headerObject.get("value", headerObject) instanceof String) {
                        httpHeaders.add(new Header((String) headerObject.get("name", headerObject),
                                (String) headerObject.get("value", headerObject)));
                    } else {
                        log.error(msg);
                        throw new ScriptException(msg);
                    }
                } else {
                    log.error(msg);
                    throw new ScriptException(msg);
                }
            }
            options.setProperty(HTTPConstants.HTTP_HEADERS, httpHeaders);
        }

        if (wsRequest.soapHeaders != null) {
            //Sets SOAPHeaders speficed in options object an array of name-value json objects
            Object soapHeaderObject;
            for (int i = 0; i < wsRequest.soapHeaders.getLength(); i++) {
                soapHeaderObject = wsRequest.soapHeaders.get(i, wsRequest.soapHeaders);
                if (soapHeaderObject instanceof String) {
                    String header = (String) soapHeaderObject;
                    try {
                        OMElement soapHeaderOM = AXIOMUtil.stringToOM(header);
                        wsRequest.sender.addHeader(soapHeaderOM);
                    } catch (XMLStreamException e) {
                        String message = "Error creating XML from the soap header : " + header;
                        log.error(message, e);
                        throw new ScriptException(message, e);
                    }
                } else if (soapHeaderObject instanceof XML) {
                    wsRequest.sender.addHeader(((OMElement) ((XML) soapHeaderObject).getAxiomFromXML()));
                } else if (soapHeaderObject instanceof NativeObject) {
                    NativeObject soapHeader = (NativeObject) soapHeaderObject;
                    String uri;
                    String localName;
                    if (soapHeader.get("qName",
                            soapHeader) instanceof org.wso2.javascript.xmlimpl.QName) {
                        org.wso2.javascript.xmlimpl.QName qName =
                                (org.wso2.javascript.xmlimpl.QName) soapHeader
                                        .get("qName", soapHeader);
                        uri = (String) qName.get("uri", qName);
                        localName = (String) qName.get("localName", qName);
                    } else {
                        throw new ScriptException("No qName property found for the soap headers");
                    }
                    if (soapHeader.get("value", soapHeader) instanceof String) {
                        try {
                            wsRequest.sender.addStringHeader(new QName(uri, localName),
                                    (String) soapHeader.get("value", soapHeader));
                        } catch (AxisFault e) {
                            log.error(e.getMessage(), e);
                            throw new ScriptException(e);
                        }
                    } else if (soapHeader.get("value", soapHeader) instanceof XML) {
                        OMNamespace omNamespace = OMAbstractFactory.getOMFactory().createOMNamespace(uri, null);
                        SOAPHeaderBlock headerBlock = OMAbstractFactory.getSOAP12Factory().
                                createSOAPHeaderBlock(localName, omNamespace);
                        headerBlock.addChild(((XML) soapHeader.get("value", soapHeader)).getAxiomFromXML());
                        wsRequest.sender.addHeader(headerBlock);
                    } else {
                        throw new ScriptException("Invalid property found for the soap headers");
                    }
                }
            }
        }
    }

    private static NativeArray setOptionsOpen(WSRequestHostObject wsRequest, Object[] args) throws ScriptException {
        NativeArray optionsArray = null;
        Options options = wsRequest.sender.getOptions();
        String httpMethod = "post";
        // set true by default to use SOAP 1.2
        String useSOAP = "true";
        String useWSA = null;
        String action = null;
        String username = null;
        String password = null;
        String url;
        String httpLocation = null;
        String httpLocationIgnoreUncited = null;
        String httpQueryParameterSeparator = "&";
        String httpInputSerialization = null;
        String httpContentEncoding = null;
        switch (args.length) {
            case 0:
                throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
            case 1:
                throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
            case 2:
                break;
            case 3:
                if (args[2] instanceof Boolean) {
                    wsRequest.async = (Boolean) args[2];
                } else if (args[2] instanceof String) {
                    username = (String) args[2];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                break;
            case 4:
                if (args[2] instanceof String) {
                    username = (String) args[2];
                    if (args[3] instanceof String) {
                        password = (String) args[3];
                    } else {
                        throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                    }
                } else if (args[2] instanceof Boolean) {
                    wsRequest.async = (Boolean) args[2];
                    if (args[3] instanceof String) {
                        username = (String) args[3];
                    } else {
                        throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                    }
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                break;
            case 5:
                if (args[2] instanceof Boolean) {
                    wsRequest.async = (Boolean) args[2];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[3] instanceof String) {
                    username = (String) args[3];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[4] instanceof String) {
                    password = (String) args[4];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                break;
            default:
                throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
        }

        if (args[1] instanceof String) {
            url = (String) args[1];
        } else {
            throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
        }

        if (args[0] instanceof String) {
            httpMethod = (String) args[0];
            useSOAP = "false";
        } else if (args[0] instanceof NativeArray) {
            optionsArray = (NativeArray) args[0];

            Object useSOAPObject = optionsArray.get("useSOAP", optionsArray);
            if (useSOAPObject != null && !(useSOAPObject instanceof Undefined) &&
                    !(useSOAPObject instanceof UniqueTag)) {
                useSOAP = useSOAPObject.toString();
            }

            Object useWSAObject = optionsArray.get("useWSA", optionsArray);
            if (useWSAObject != null && !(useWSAObject instanceof Undefined) &&
                    !(useWSAObject instanceof UniqueTag)) {
                useWSA = useWSAObject.toString();
            }

            Object hTTPMethodObject = optionsArray.get("HTTPMethod", optionsArray);
            if (hTTPMethodObject != null && !(hTTPMethodObject instanceof Undefined) &&
                    !(hTTPMethodObject instanceof UniqueTag)) {
                httpMethod = hTTPMethodObject.toString();
            }

            Object actionObject = optionsArray.get("action", optionsArray);
            if (actionObject != null && !(actionObject instanceof Undefined) &&
                    !(actionObject instanceof UniqueTag)) {
                action = actionObject.toString();
            }

            Object httpLocationObject = optionsArray.get("HTTPLocation", optionsArray);
            if (httpLocationObject != null && !(httpLocationObject instanceof Undefined) &&
                    !(httpLocationObject instanceof UniqueTag)) {
                httpLocation = httpLocationObject.toString();
            }

            Object httpLocationIgnoreUncitedObject =
                    optionsArray.get("HTTPLocationIgnoreUncited", optionsArray);
            if (httpLocationIgnoreUncitedObject != null &&
                    !(httpLocationIgnoreUncitedObject instanceof Undefined) &&
                    !(httpLocationIgnoreUncitedObject instanceof UniqueTag)) {
                httpLocationIgnoreUncited = httpLocationIgnoreUncitedObject.toString();
            }

            Object httpQueryParameterSeparatorObject =
                    optionsArray.get("HTTPQueryParameterSeparator", optionsArray);
            if (httpQueryParameterSeparatorObject != null &&
                    !(httpQueryParameterSeparatorObject instanceof Undefined) &&
                    !(httpQueryParameterSeparatorObject instanceof UniqueTag)) {
                httpQueryParameterSeparator = httpQueryParameterSeparatorObject.toString();
            }

            Object httpInputSerializationObject =
                    optionsArray.get("HTTPInputSerialization", optionsArray);
            if (httpInputSerializationObject != null &&
                    !(httpInputSerializationObject instanceof Undefined) &&
                    !(httpInputSerializationObject instanceof UniqueTag)) {
                httpInputSerialization = httpInputSerializationObject.toString();
            }

            Object HTTPContentEncodingObject =
                    optionsArray.get("HTTPContentEncoding", optionsArray);
            if (HTTPContentEncodingObject != null &&
                    !(HTTPContentEncodingObject instanceof Undefined) &&
                    !(HTTPContentEncodingObject instanceof UniqueTag)) {
                httpContentEncoding = HTTPContentEncodingObject.toString();
            }
        }

        options.setProperty(HTTPConstants.CHUNKED, Boolean.FALSE);
        options.setProperty(HTTPConstants.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
        EndpointReference targetEPR = new EndpointReference(url);
        options.setTo(targetEPR);

        if (username != null) {
            // handle basic authentication
            // set username if not null
            Authenticator authenticator = new HttpTransportProperties.Authenticator();
            authenticator.setUsername(username);
            if (password != null) { // set password if present
                authenticator.setPassword(password);
            }
            authenticator.setPreemptiveAuthentication(true);
            options.setProperty(HTTPConstants.AUTHENTICATE, authenticator);
        }

        // handle useSOAP option
        if (useSOAP.equalsIgnoreCase("1.1")) {
            options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        } else if ((useSOAP.equalsIgnoreCase("1.2")) || (useSOAP.equalsIgnoreCase("true"))) {
            options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        } else if (useSOAP.equalsIgnoreCase("false")) {
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
        } else {
            throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
        }

        if (httpMethod != null) {
            if (httpMethod.equalsIgnoreCase(Constants.Configuration.HTTP_METHOD_GET)) {
                // If useSOAP is not false then the HTTPMethod must be POST
                if (!useSOAP.equalsIgnoreCase("false")) {
                    throw Context.reportRuntimeError(
                            "INVALID_SYNTAX_EXCEPTION. Cannot have the value of useSOAP true, when the HTTPMethod is 'GET'");
                }
                options.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_GET);
                options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            } else if (httpMethod.equalsIgnoreCase(Constants.Configuration.HTTP_METHOD_POST)) {
                options.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_POST);
            } else if (httpMethod.equalsIgnoreCase(Constants.Configuration.HTTP_METHOD_DELETE)) {
                // If useSOAP is not false then the HTTPMethod must be POST
                if (!useSOAP.equalsIgnoreCase("false")) {
                    throw Context.reportRuntimeError(
                            "INVALID_SYNTAX_EXCEPTION. Cannot have the value of useSOAP true, when the HTTPMethod is 'DELETE'");
                }
                options.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_DELETE);
                options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            } else if (httpMethod.equalsIgnoreCase(Constants.Configuration.HTTP_METHOD_PUT)) {
                // If useSOAP is not false then the HTTPMethod must be POST
                if (!useSOAP.equalsIgnoreCase("false")) {
                    throw Context.reportRuntimeError(
                            "INVALID_SYNTAX_EXCEPTION. Cannot have the value of useSOAP true, when the HTTPMethod is 'PUT'");
                }
                options.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_PUT);
                options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            } else {
                throw Context
                        .reportRuntimeError("INVALID_SYNTAX_EXCEPTION. Unsupported HTTP method.");
            }
        }

        if (httpLocation != null) {
            options.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);
        }

        if (httpLocationIgnoreUncited != null) {
            options.setProperty(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED,
                    JavaUtils.isTrueExplicitly(httpLocationIgnoreUncited));
        }

        if (httpQueryParameterSeparator != null) {
            options.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                    httpQueryParameterSeparator);
        }

        if (httpInputSerialization != null) {
            options.setProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION,
                    httpInputSerialization);
            options.setProperty(Constants.Configuration.MESSAGE_TYPE, httpInputSerialization);
        }

        if (httpContentEncoding != null) {
            if ("gzip".equals(httpContentEncoding) ||
                    "compress".equals(httpContentEncoding)) {
                options.setProperty("HTTPConstants.MC_GZIP_REQUEST", "true");
            }
        }

        if ((useWSA != null) &&
                (useWSA.equalsIgnoreCase("1.0") || useWSA.equalsIgnoreCase("true") ||
                        useWSA.equalsIgnoreCase("submission"))) {
            if (useWSA.equalsIgnoreCase("submission")) { // set addressing to
                // WSA submission
                // version
                options.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                        AddressingConstants.Submission.WSA_NAMESPACE);
            } else { // set addressing to WSA 1.0 version
                options.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                        AddressingConstants.Final.WSA_NAMESPACE);
            }
            if (action != null) {
                try {
                    wsRequest.sender.engageModule(Constants.MODULE_ADDRESSING);
                } catch (AxisFault axisFault) {
                    throw new ScriptException(axisFault);
                }
                options.setAction(action);
            } else {
                throw Context.reportRuntimeError(
                        "INVALID_SYNTAX_EXCEPTION. Action is NULL when useWSA is true.");
            }
            if (optionsArray != null) {
                Object fromObject = optionsArray.get("from", optionsArray);
                if (fromObject != null && !(fromObject instanceof Undefined) &&
                        !(fromObject instanceof UniqueTag)) {
                    options.setFrom(new EndpointReference(fromObject.toString()));
                }
                Object replyToObject = optionsArray.get("replyTo", optionsArray);
                if (replyToObject != null && !(replyToObject instanceof Undefined) &&
                        !(replyToObject instanceof UniqueTag)) {
                    options.setReplyTo(new EndpointReference(replyToObject.toString()));
                }
                Object faultToObject = optionsArray.get("faultTo", optionsArray);
                if (faultToObject != null && !(faultToObject instanceof Undefined) &&
                        !(faultToObject instanceof UniqueTag)) {
                    options.setFaultTo(new EndpointReference(faultToObject.toString()));
                }
            }
        } else {
            wsRequest.sender.disengageModule(Constants.MODULE_ADDRESSING);
            if (action != null) {
                options.setProperty(Constants.Configuration.DISABLE_SOAP_ACTION, "false");
                options.setAction(action);
            }
        }
        return optionsArray;
    }

    private static NativeArray setOptionsOpenWSDL(WSRequestHostObject wsRequest, Object[] args) throws ScriptException {
        NativeArray optionsArray = null;
        String wsdlURL;
        QName serviceQName = null;
        String endpointName = null;
        switch (args.length) {
            case 0:
                throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
            case 1:
                throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
            case 2:
                if (args[0] instanceof String) {
                    wsdlURL = (String) args[0];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[1] instanceof Boolean) {
                    wsRequest.async = (Boolean) args[1];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                break;
            case 3:
                if (args[0] instanceof String) {
                    wsdlURL = (String) args[0];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[1] instanceof Boolean) {
                    wsRequest.async = (Boolean) args[1];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[2] instanceof NativeArray) {
                    optionsArray = (NativeArray) args[2];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                break;
            case 5:
                if (args[0] instanceof String) {
                    wsdlURL = (String) args[0];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[1] instanceof Boolean) {
                    wsRequest.async = (Boolean) args[1];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[2] instanceof NativeArray) {
                    optionsArray = (NativeArray) args[2];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[3] instanceof org.wso2.javascript.xmlimpl.QName) {
                    org.wso2.javascript.xmlimpl.QName qName =
                            (org.wso2.javascript.xmlimpl.QName) args[3];
                    String uri = (String) qName.get("uri", qName);
                    String localName = (String) qName.get("localName", qName);
                    serviceQName = new QName(uri, localName);
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }
                if (args[4] instanceof String) {
                    endpointName = (String) args[4];
                } else {
                    throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
                }

                wsRequest.wsdlMode = true;
                break;
            default:
                throw Context.reportRuntimeError("INVALID_SYNTAX_EXCEPTION");
        }

        HttpMethod method = new GetMethod(wsdlURL);
        try {
            int statusCode = HostObjectUtil.getURL(wsdlURL, null, null);
            if (statusCode != HttpStatus.SC_OK) {
                throw new ScriptException(
                        "An error occured while getting the resource at " + wsdlURL + ". Reason :" +
                                method.getStatusLine());
            }
            Document doc = XMLUtils.newDocument(method.getResponseBodyAsStream());
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            reader.setFeature("javax.wsdl.importDocuments", true);
            Definition definition = reader.readWSDL(getBaseURI(wsdlURL), doc);
            wsRequest.targetNamespace = definition.getTargetNamespace();
            Service service;
            Port returnPort;
            if (serviceQName == null) {
                Map services = definition.getServices();
                service = null;
                for (Object o : services.values()) {
                    service = (Service) o;
                    if (service.getPorts().size() > 0) {
                        //i.e we have found a service with ports
                        break;
                    }
                }
                if (service == null) {
                    throw Context.reportRuntimeError(
                            "The WSDL given does not contain any services " + "that has ports");
                }
                Map ports = service.getPorts();
                Port port;
                returnPort = null;
                for (Iterator portsIterator = ports.values().iterator();
                     (portsIterator.hasNext() && returnPort == null); ) {
                    port = (Port) portsIterator.next();
                    List extensibilityElements = port.getExtensibilityElements();
                    for (Object extElement : extensibilityElements) {
                        if (extElement instanceof SOAPAddress) {
                            // SOAP 1.1 address found - keep this and loop until http address is found
                            returnPort = port;
                            String location = ((SOAPAddress) extElement).getLocationURI().trim();
                            if ((location != null) && location.startsWith("http:")) {
                                // i.e we have found an http port so return from here
                                break;
                            }
                        }
                    }
                }

                if (returnPort == null) {
                    for (Object o : ports.values()) {
                        port = (Port) o;
                        List extensibilityElements = port.getExtensibilityElements();
                        for (Object extElement : extensibilityElements) {
                            if (extElement instanceof SOAP12Address) {
                                // SOAP 1.2 address found - keep this and loop until http address is found
                                returnPort = port;
                                String location =
                                        ((SOAP12Address) extElement).getLocationURI().trim();
                                if ((location != null) && location.startsWith("http:")) {
                                    // i.e we have found an http port so return from here
                                    break;
                                }
                            }
                        }
                    }

                    if (returnPort == null) {
                        for (Object o : ports.values()) {
                            port = (Port) o;
                            List extensibilityElements = port.getExtensibilityElements();
                            for (Object extElement : extensibilityElements) {
                                if (extElement instanceof HTTPAddress) {
                                    // HTTP address found - keep this and loop until http address is found
                                    returnPort = port;
                                    String location =
                                            ((HTTPAddress) extElement).getLocationURI().trim();
                                    if ((location != null) && location.startsWith("http:")) {
                                        // i.e we have found an http port so return from here
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (returnPort == null) {
                    throw Context.reportRuntimeError("The WSDL given does not contain any ports " +
                            "that use the http transport");
                } else {
                    serviceQName = service.getQName();
                    endpointName = returnPort.getName();

                }
            }
            wsRequest.sender = new ServiceClient(null, definition, serviceQName, endpointName);
        } catch (MalformedURLException e) {
            String message = "Malformed URL : " + wsdlURL;
            log.error(message, e);
            throw new ScriptException(message, e);
        } catch (Exception e) {
            String message = "Error occurred while reading the WSDL content from the URL : " + wsdlURL;
            log.error(message, e);
            throw new ScriptException(message, e);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return optionsArray;
    }

    private static String getObjectProperty(NativeObject object, String property) {
        if (object.get(property, object) instanceof String) {
            return (String) object.get(property, object);
        } else {
            return null;
        }
    }

    private static CryptoConfig getCryptoConfig(WSRequestHostObject wsRequest, NativeObject crypto) throws ScriptException {
        Properties merlinProp = new Properties();
        File file = new File(FilenameUtils.normalizeNoEndSeparator(getObjectProperty(crypto, "file")));
        merlinProp.put("org.apache.ws.security.crypto.merlin.file", file.getAbsolutePath());
        merlinProp.put("org.apache.ws.security.crypto.merlin.keystore.type", getObjectProperty(crypto, "type"));
        merlinProp.put("org.apache.ws.security.crypto.merlin.keystore.password", getObjectProperty(crypto, "password"));
        CryptoConfig cryptoConfig = new CryptoConfig();
        cryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");
        cryptoConfig.setProp(merlinProp);
        Object property = crypto.get(CryptoConfig.CACHE_ENABLED, crypto);
        if ((property instanceof Boolean && (Boolean) property) ||
                (property instanceof String && Boolean.parseBoolean((String) property))) {
            cryptoConfig.setCacheEnabled(true);
            cryptoConfig.setCryptoKey("org.apache.ws.security.crypto.merlin.file");
        } else if (property != null && !(property instanceof Undefined) &&
                !(property instanceof UniqueTag)) {
            throw new ScriptException("Invalid value for property '" + CryptoConfig.CACHE_ENABLED +
                    "' in rampart configuration");
        }

        property = crypto.get(CryptoConfig.CACHE_REFRESH_INTVL, crypto);
        if (property instanceof Integer) {
            cryptoConfig.setCacheRefreshInterval(Integer.toString((Integer) property));
        } else if (property instanceof String) {
            cryptoConfig.setCacheRefreshInterval((String) property);
        } else if (property != null && !(property instanceof Undefined) &&
                !(property instanceof UniqueTag)) {
            throw new ScriptException("Invalid value for property '" + CryptoConfig.CACHE_REFRESH_INTVL +
                    "' in rampart configuration");
        }
        return cryptoConfig;

    }

    private static KerberosConfig getKerberosConfigs(WSRequestHostObject wsRequest, NativeObject configs)
            throws ScriptException {
        KerberosConfig kerberosConfig = new KerberosConfig();
        Properties properties = new Properties();
        Object[] objects = NativeObject.getPropertyIds(configs);
        for (Object object : objects) {
            if (object instanceof String) {
                String property = (String) object;
                Object value = configs.get(property, configs);
                if (value instanceof String) {
                    properties.setProperty(property, (String) value);
                } else {
                    throw new ScriptException("Invalid config value for the property : " + property +
                            " in Kerberos Config");
                }
            } else {
                throw new ScriptException("Invalid property in Kerberos Config");
            }
        }
        kerberosConfig.setProp(properties);
        return kerberosConfig;

    }

    private static void setRampartConfigs(WSRequestHostObject wsRequest, QName operationName)
            throws AxisFault, ScriptException {
        RampartConfig rampartConfig = null;
        boolean useUT = false;
        Policy policy = null;
        if (wsRequest.policy != null) {
            //user has specified a policy, use that one
            OMElement policyElement;
            OMNode node = wsRequest.policy.getAxiomFromXML();
            if (node instanceof OMElement) {
                policyElement = (OMElement) node;
            } else {
                throw new Error("INVALID_INPUT_EXCEPTION. Invalid input was : " + wsRequest.policy);
            }
            policy = PolicyEngine.getPolicy(policyElement);
            List list = (List) policy.getAlternatives().next();
            for (Object o : list) {
                if (o instanceof RampartConfig) {
                    rampartConfig = (RampartConfig) o;
                    filterRampartConfig(wsRequest, rampartConfig);
                    break;
                }
            }
        }
        if (wsRequest.rampartConfig != null) {
            if (rampartConfig == null) {
                rampartConfig = new RampartConfig();
                String property = getObjectProperty(wsRequest.rampartConfig, RampartConfig.USER_CERT_ALIAS_LN);
                if (property != null) {
                    rampartConfig.setUserCertAlias(property);
                }
                property = getObjectProperty(wsRequest.rampartConfig, RampartConfig.STS_ALIAS_LN);
                if (property != null) {
                    rampartConfig.setStsAlias(property);
                }
                property = getObjectProperty(wsRequest.rampartConfig, RampartConfig.ENCRYPTION_USER_LN);
                if (property != null) {
                    rampartConfig.setEncryptionUser(property);
                }

                Object obj = wsRequest.rampartConfig.get(RampartConfig.TS_TTL_LN, wsRequest.rampartConfig);
                if (obj instanceof Integer) {
                    rampartConfig.setTimestampTTL(Integer.toString((Integer) obj));
                } else if (obj instanceof String) {
                    rampartConfig.setTimestampTTL((String) obj);
                } else if (obj != null && !(obj instanceof Undefined) &&
                        !(obj instanceof UniqueTag)) {
                    throw new ScriptException("Invalid value for property '" + RampartConfig.TS_TTL_LN +
                            "' in rampart configuration");
                }

                obj = wsRequest.rampartConfig.get(RampartConfig.TS_MAX_SKEW_LN, wsRequest.rampartConfig);
                if (obj instanceof Integer) {
                    rampartConfig.setTimestampMaxSkew(Integer.toString((Integer) obj));
                } else if (obj instanceof String) {
                    rampartConfig.setTimestampMaxSkew((String) obj);
                } else if (obj != null && !(obj instanceof Undefined) &&
                        !(obj instanceof UniqueTag)) {
                    throw new ScriptException("Invalid value for property '" + RampartConfig.TS_MAX_SKEW_LN +
                            "' in rampart configuration");
                }

                obj = wsRequest.rampartConfig.get(RampartConfig.TS_PRECISION_IN_MS_LN, wsRequest.rampartConfig);
                if (obj instanceof Integer) {
                    rampartConfig.setTimestampPrecisionInMilliseconds(Integer.toString((Integer) obj));
                } else if (obj instanceof String) {
                    rampartConfig.setTimestampPrecisionInMilliseconds((String) obj);
                } else if (obj != null && !(obj instanceof Undefined) &&
                        !(obj instanceof UniqueTag)) {
                    throw new ScriptException("Invalid value for property '" + RampartConfig.TS_PRECISION_IN_MS_LN +
                            "' in rampart configuration");
                }

                //sets crypto configs
                Object cryptoObject = wsRequest.rampartConfig.get(RampartConfig.SIG_CRYPTO_LN, wsRequest.rampartConfig);
                if (cryptoObject instanceof NativeObject) {
                    rampartConfig.setSigCryptoConfig(getCryptoConfig(wsRequest, (NativeObject) cryptoObject));
                }
                cryptoObject = wsRequest.rampartConfig.get(RampartConfig.ENCR_CRYPTO_LN, wsRequest.rampartConfig);
                if (cryptoObject instanceof NativeObject) {
                    rampartConfig.setEncrCryptoConfig(getCryptoConfig(wsRequest, (NativeObject) cryptoObject));
                }
                cryptoObject = wsRequest.rampartConfig.get(RampartConfig.DEC_CRYPTO_LN, wsRequest.rampartConfig);
                if (cryptoObject instanceof NativeObject) {
                    rampartConfig.setEncrCryptoConfig(getCryptoConfig(wsRequest, (NativeObject) cryptoObject));
                }
                cryptoObject = wsRequest.rampartConfig.get(RampartConfig.STS_CRYPTO_LN, wsRequest.rampartConfig);
                if (cryptoObject instanceof NativeObject) {
                    rampartConfig.setSigCryptoConfig(getCryptoConfig(wsRequest, (NativeObject) cryptoObject));
                }

                Object kerberosConfig = wsRequest.rampartConfig.get(RampartConfig.KERBEROS_CONFIG, wsRequest.rampartConfig);
                if (kerberosConfig instanceof NativeObject) {
                    rampartConfig.setKerberosConfig(getKerberosConfigs(wsRequest, (NativeObject) kerberosConfig));
                }
            }

            PasswordCallbackHandler passwordCallbackHandler = new PasswordCallbackHandler();
            wsRequest.sender.getAxisService().addParameter(WSHandlerConstants.PW_CALLBACK_REF,
                    passwordCallbackHandler);
            String property = getObjectProperty(wsRequest.rampartConfig, RampartConfig.USER_LN);
            if (property != null) {
                rampartConfig.setUser(property);
                useUT = true;
            }
            property = getObjectProperty(wsRequest.rampartConfig, "userPassword");
            if (property != null) {
                passwordCallbackHandler.setUserPassword(property);
            }
            property = getObjectProperty(wsRequest.rampartConfig, "keyPassword");
            if (property != null) {
                passwordCallbackHandler.setKeyPassword(property);
            }
        }

        if (policy == null) {
            if (wsRequest.wsdlMode) {
                //get the policy from the wsdl
                AxisService axisService = wsRequest.sender.getAxisService();
                if (axisService.getChild(operationName) == null) {
                    throw new ScriptException("No operation with the name " + operationName.getLocalPart() +
                            " found in the service been called");
                }
                AxisEndpoint axisEndpoint = axisService.getEndpoint(axisService.getEndpointName());
                AxisBindingMessage axisBindingMessage =
                        (AxisBindingMessage) axisEndpoint.getBinding().getChild(operationName)
                                .getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                policy = axisBindingMessage.getEffectivePolicy();
            } else if (useUT) {
                String xmlPath = "scenarios/scenario1-policy.xml";
                try {
                    InputStream policyXMLStream = WSRequestHostObject.class.
                            getClassLoader().getResourceAsStream(xmlPath);
                    StAXOMBuilder builder = new StAXOMBuilder(policyXMLStream);
                    policy = PolicyEngine.getPolicy(builder.getDocumentElement());
                } catch (XMLStreamException e) {
                    String message = "Error loading/parsing default UT policy from the server";
                    log.error(message, e);
                    throw new ScriptException(message, e);
                }
            }
        }
        if (policy != null) {
            if (rampartConfig != null) {
                policy.addAssertion(rampartConfig);
            } else {
                throw new ScriptException("A policy has been specified either in your mashup or in the WSDL. " +
                        "But the Rampart Configuration cannot be found.");
            }
            wsRequest.sender.getOptions().setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            wsRequest.sender.engageModule(RAMPART);
            wsRequest.sender.engageModule(ADDRESSING);
        }
    }

    private static void filterRampartConfig(WSRequestHostObject wsRequest, RampartConfig config) {

        CryptoConfig crypto = config.getSigCryptoConfig();
        if (crypto != null) {
            filterCryptoConfig(wsRequest, crypto);
        }
        crypto = config.getEncrCryptoConfig();
        if (crypto != null) {
            filterCryptoConfig(wsRequest, crypto);
        }
        crypto = config.getDecCryptoConfig();
        if (crypto != null) {
            filterCryptoConfig(wsRequest, crypto);
        }
        crypto = config.getStsCryptoConfig();
        if (crypto != null) {
            filterCryptoConfig(wsRequest, crypto);
        }

        KerberosConfig kerberosConfig = config.getKerberosConfig();
        if (kerberosConfig != null) {
            Properties properties = kerberosConfig.getProp();
            for (String key : properties.stringPropertyNames()) {
                properties.setProperty(key, properties.getProperty(key));
            }
        }
    }

    private static void filterCryptoConfig(WSRequestHostObject wsRequest, CryptoConfig config) {
        Properties properties = config.getProp();
        properties.setProperty("org.apache.ws.security.crypto.merlin.file",
                properties.getProperty("org.apache.ws.security.crypto.merlin.file"));
    }


}
