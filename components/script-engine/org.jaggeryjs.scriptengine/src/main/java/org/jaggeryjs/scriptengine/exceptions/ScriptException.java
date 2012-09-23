package org.jaggeryjs.scriptengine.exceptions;

public class ScriptException extends Exception {

    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(Exception exception) {
        super(exception);
    }

    public ScriptException(String message, Exception exception) {
        super(message, exception);
    }
}
