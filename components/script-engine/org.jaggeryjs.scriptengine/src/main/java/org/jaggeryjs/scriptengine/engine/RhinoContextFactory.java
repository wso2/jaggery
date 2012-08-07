package org.jaggeryjs.scriptengine.engine;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.xml.XMLLib;

public class RhinoContextFactory extends ContextFactory {

    private SecurityController securityController;

    public RhinoContextFactory(SecurityController securityController) {
        this.securityController = securityController;
    }

    @Override
    protected boolean hasFeature(Context cx, int featureIndex) {
        if (featureIndex == Context.FEATURE_DYNAMIC_SCOPE) {
            return true;
        }
        return super.hasFeature(cx, featureIndex);
    }

    @Override
    protected Context makeContext() {
        Context cx = super.makeContext();
        //cx.setClassShutter(new RhinoClassShutter());
        if(securityController != null) {
            cx.setSecurityController(securityController);
        }
        return cx;
    }

    /**
     * This methods is used to get the E4x implementation factory.
     *
     * @return the factory which is used
     */
    protected XMLLib.Factory getE4xImplementationFactory() {
        return org.mozilla.javascript.xml.XMLLib.Factory.create("org.wso2.javascript.xmlimpl.XMLLibImpl");
    }

}
