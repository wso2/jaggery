/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
