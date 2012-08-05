package org.jaggeryjs.hostobjects.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class JavaScriptFileManagerImpl implements JavaScriptFileManager {

    private static final Log log = LogFactory.getLog(JavaScriptFileManagerImpl.class);

    @Override
    public JavaScriptFile getJavaScriptFile(Object object) throws ScriptException {
        if (object instanceof String) {
            return new JavaScriptFileImpl(getFile((String) object).getAbsolutePath());
        } else {
            String msg = "Unsupported parameter to the File constructor : " + object.getClass();
            log.error(msg);
            throw new ScriptException(msg);
        }
    }

    @Override
    public File getFile(String path) throws ScriptException {
        File file;
        if (path.startsWith("file://")) {
            try {
                file = FileUtils.toFile(new URL(path));
            } catch (MalformedURLException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        } else {
            String oldPath = path;
            path = FilenameUtils.normalizeNoEndSeparator(path);
            if (path == null) {
                String msg = "Invalid file path : " + oldPath;
                log.error(msg);
                throw new ScriptException(msg);
            }
            file = new File(path);
        }
        if (file.isDirectory()) {
            String msg = "File hostobject doesn't handle directories. Specified path contains a directory : " + path;
            log.error(msg);
            throw new ScriptException(msg);
        }
        return file;
    }
}
