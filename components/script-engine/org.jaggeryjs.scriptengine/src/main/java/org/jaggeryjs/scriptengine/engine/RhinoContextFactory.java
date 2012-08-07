package org.jaggeryjs.scriptengine.engine;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.xml.XMLLib;

public class CarbonContextFactory extends ContextFactory {
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
        //cx.setClassShutter(new CarbonClassShutter());
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
