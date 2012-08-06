package org.jaggeryjs.scriptengine.engine;

import org.mozilla.javascript.Script;

public class JavaScriptScript {
    private String name = null;
    private Script script = null;

    public JavaScriptScript(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
