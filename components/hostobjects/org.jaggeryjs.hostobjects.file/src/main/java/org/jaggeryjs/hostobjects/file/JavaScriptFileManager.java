package org.jaggeryjs.hostobjects.file;


import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import java.io.File;

public interface JavaScriptFileManager {

    public JavaScriptFile getJavaScriptFile(Object object) throws ScriptException;

    public File getFile(String path) throws ScriptException;

    public String getDirectoryPath(String path) throws ScriptException;

}
