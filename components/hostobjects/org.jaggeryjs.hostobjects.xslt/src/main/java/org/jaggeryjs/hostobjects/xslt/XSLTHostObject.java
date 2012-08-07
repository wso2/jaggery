package org.jaggeryjs.hostobjects.xslt;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.javascript.xmlimpl.XML;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * var xslt = new XSLT(xslt);
 */
public class XSLTHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(XSLTHostObject.class);

    private TransformerFactory factory = null;
    private Transformer transformer = null;
    private StringReader xslt = null;

    private static final String hostObjectName = "XSLT";

    /**
     * XSLT(xslt)
     * XSLT(xslt, paramMap)
     * XSLT(xslt, uriResolver)
     * XSLT(xslt, paramMap, uriResolver)
     */
    public static Scriptable jsConstructor(final Context cx, Object[] args, final Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        int argsCount = args.length;
        if (argsCount < 1 || argsCount > 3) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }

        Function uriResolver = null;
        NativeObject paramMap = null;

        XSLTHostObject xho = new XSLTHostObject();
        if (args[0] instanceof String) {
            xho.xslt = new StringReader((String) args[0]);
        } else if (args[0] instanceof XML) {
            xho.xslt = new StringReader(((XML) args[0]).getAxiomFromXML().toString());
        } else {
            HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "1", "string | xml", args[0], true);
        }

        if (argsCount == 2) {
            if (args[1] instanceof NativeObject) {
                paramMap = (NativeObject) args[1];
            } else if (args[1] instanceof Function) {
                uriResolver = (Function) args[1];
            } else {
                HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "2", "string", args[1], true);
            }
        } else if (argsCount == 3) {
            if (args[1] instanceof NativeObject) {
                paramMap = (NativeObject) args[1];
            } else {
                HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "2", "string", args[1], true);
            }

            if (args[2] instanceof Function) {
                uriResolver = (Function) args[2];
            } else {
                HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "3", "string", args[1], true);
            }
        }

        xho.factory = TransformerFactory.newInstance();
        if (uriResolver != null) {
            xho.factory.setURIResolver(getUriResolver(cx, ctorObj, uriResolver));
        }
        return xho;
    }

    private static URIResolver getUriResolver(final Context cx, final Scriptable scope, final Function uriResolver) {
        return new URIResolver() {
            @Override
            public Source resolve(String href, String base) throws TransformerException {
                Object obj = uriResolver.call(cx, scope, scope, new Object[]{base, href});
                if (obj instanceof XML) {
                    return new StreamSource(new StringReader(((XML) obj).getAxiomFromXML().toString()));
                } else if (obj instanceof String) {
                    return new StreamSource(new StringReader((String) obj));
                } else {
                    throw new TransformerException("Error while resolving the content for " + href);
                }
            }
        };
    }

    private static Transformer getTransformer(Context cx, Scriptable scope, XSLTHostObject xho, NativeObject paramMap, Function uriResolver) throws ScriptException {
        Transformer transformer;
        try {
            transformer = xho.factory.newTransformer(new StreamSource(xho.xslt));
            if (paramMap != null) {
                setParams(transformer, paramMap);
            }
            if (uriResolver != null) {
                transformer.setURIResolver(getUriResolver(cx, scope, uriResolver));
            }
            return transformer;
        } catch (TransformerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    public String getClassName() {
        return hostObjectName;
    }

    /**
     *  transform(xml)
     *  transform(xml, callback)
     *  transform(xml, paramMap)
     *  transform(xml, paramMap, callback)
     *  trasformer(xml, callback, uriResolver)
     *  trasformer(xml, paramMap, callback, uriResolver)
     */
    public static String jsFunction_transform(final Context cx, final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "transform";
        int argsCount = args.length;
        if (argsCount > 4) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        StringReader xml = null;
        Function uriResolver = null;
        NativeObject paramMap = null;
        Function callback = null;
        if (args[0] instanceof String) {
            xml = new StringReader((String) args[0]);
        } else if (args[0] instanceof XML) {
            xml = new StringReader(((XML) args[0]).getAxiomFromXML().toString());
        } else {
            HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "1", "string | xml", args[0], true);
        }

        if (argsCount == 2) {
            if (args[1] instanceof NativeObject) {
                paramMap = (NativeObject) args[1];
            } else if (args[1] instanceof Function) {
                callback = (Function) args[1];
            } else {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "object | function", args[1], false);
            }
        } else if (argsCount == 3) {
            if (args[1] instanceof NativeObject) {
                paramMap = (NativeObject) args[1];
                if (args[2] instanceof Function) {
                    callback = (Function) args[2];
                } else {
                    HostObjectUtil.invalidArgsError(hostObjectName, functionName, "3", "function", args[2], false);
                }
            } else if (args[1] instanceof Function) {
                callback = (Function) args[1];
                if (args[2] instanceof Function) {
                    uriResolver = (Function) args[2];
                } else {
                    HostObjectUtil.invalidArgsError(hostObjectName, functionName, "3", "function", args[2], false);
                }
            } else {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "object", args[1], false);
            }
        } else if (argsCount == 4) {
            if (args[1] instanceof NativeObject) {
                paramMap = (NativeObject) args[1];
            } else {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "object", args[1], false);
            }

            if (args[2] instanceof Function) {
                callback = (Function) args[2];
            } else {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "3", "function", args[2], false);
            }

            if (args[3] instanceof Function) {
                uriResolver = (Function) args[3];
            } else {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "4", "function", args[3], false);
            }
        }

        final XSLTHostObject xho = (XSLTHostObject) thisObj;
        try {
            final StringWriter result = new StringWriter();
            if (callback == null) {
                if (xho.transformer == null) {
                    xho.transformer = getTransformer(cx, thisObj, xho, paramMap, uriResolver);
                } else {
                    if (paramMap != null) {
                        setParams(xho.transformer, paramMap);
                    }
                    if (uriResolver != null) {
                        xho.transformer.setURIResolver(getUriResolver(cx, funObj, uriResolver));
                    }
                }
                xho.transformer.transform(new StreamSource(xml), new StreamResult(result));
                return result.toString();
            }
            final StringReader finalXml = xml;
            final Function finalCallback = callback;
            final Function finalUriResolver = uriResolver;
            final NativeObject finalParamMap = paramMap;
            final ContextFactory factory = cx.getFactory();
            final ExecutorService es = Executors.newCachedThreadPool();
            es.submit(new Callable() {
                public Object call() throws Exception {
                    RhinoEngine.enterContext(factory);
                    try {
                        getTransformer(cx, thisObj, xho, finalParamMap, finalUriResolver).transform(
                                new StreamSource(finalXml), new StreamResult(result));
                        finalCallback.call(cx, xho, xho, new Object[]{result.toString()});
                    } catch (TransformerException e) {
                        log.warn(e);
                    } finally {
                        es.shutdown();
                        RhinoEngine.exitContext();
                    }
                    return null;
                }
            });
            return null;
        } catch (TransformerException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    private static void setParams(Transformer transformer, NativeObject paramMap) {
        transformer.clearParameters();
        Object[] ids = paramMap.getIds();
        for (Object id : ids) {
            String key = (String) id;
            Object obj = paramMap.get(key, paramMap);
            transformer.setParameter(key, HostObjectUtil.serializeObject(obj));
        }
    }
}
