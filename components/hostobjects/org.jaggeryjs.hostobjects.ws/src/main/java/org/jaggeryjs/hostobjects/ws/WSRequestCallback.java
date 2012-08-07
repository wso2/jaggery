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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;

import javax.xml.namespace.QName;

public class WSRequestCallback implements AxisCallback {

    private WSRequestHostObject wsrequest;

    private static final Log log = LogFactory.getLog(WSRequestCallback.class);

    public WSRequestCallback(WSRequestHostObject wsrequest) {
        super();
        this.wsrequest = wsrequest;
    }

    public void onComplete() {

    }

    public void onError(Exception ex) {
        wsrequest.error = new WebServiceErrorHostObject();
        processError(ex);
    }

    public void onFault(MessageContext messageContext) {
        AxisFault fault = Utils.getInboundFaultFromMessageContext(messageContext);
        processError(fault);
    }

    private void processError(Exception ex) {
        if (ex instanceof AxisFault) {
            AxisFault e = (AxisFault) ex;
            OMElement detail = e.getDetail();
            if (detail != null) {
                wsrequest.error.jsSet_detail(detail.toString());
            }
            QName faultCode = e.getFaultCode();
            if (faultCode != null) {
                wsrequest.error.jsSet_code(faultCode.toString());
            }
            wsrequest.error.jsSet_reason(e.getReason());
        } else {
            Throwable cause = ex.getCause();
            if (cause != null) {
                wsrequest.error.jsSet_detail(cause.toString());
            }
            wsrequest.error.jsSet_code("No SOAP Body.");
            wsrequest.error.jsSet_reason(ex.getMessage());
        }
        this.wsrequest.readyState = 4;
        if (this.wsrequest.onReadyStateChangeFunction != null) {
            Context cx = RhinoEngine.enterContext(this.wsrequest.context.getFactory());
            this.wsrequest.onReadyStateChangeFunction.call(cx, wsrequest, wsrequest, new Object[0]);
            RhinoEngine.exitContext();
        }
    }

    public void onMessage(MessageContext messageContext) {
        this.wsrequest.updateResponse(messageContext.getEnvelope().getBody().getFirstElement());
        this.wsrequest.readyState = 4;
        if (this.wsrequest.onReadyStateChangeFunction != null) {
            Context cx = RhinoEngine.enterContext(this.wsrequest.context.getFactory());
            this.wsrequest.onReadyStateChangeFunction.call(cx, wsrequest, wsrequest, new Object[0]);
            RhinoEngine.exitContext();
        }
    }
}
