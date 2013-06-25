package org.jaggeryjs.scriptengine.engine;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.SecurityController;

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

}
