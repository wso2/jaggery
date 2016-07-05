package org.jaggeryjs.hostobjects.file;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
            String uri = (String) object;
            return new JavaScriptFileImpl(uri, getFile(uri).getAbsolutePath());
        } else {
            String msg = "Unsupported parameter to the File constructor : " + object.getClass();
            log.error(msg);
            throw new ScriptException(msg);
        }
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    @Override
    public File getFile(String uri) throws ScriptException {
        File file;
        if (uri.startsWith("file://")) {
            try {
                file = FileUtils.toFile(new URL(uri));
            } catch (MalformedURLException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        } else {
            String oldPath = uri;
            uri = FilenameUtils.normalizeNoEndSeparator(uri);
            if (uri == null) {
                String msg = "Invalid file URI : " + oldPath;
                log.error(msg);
                throw new ScriptException(msg);
            }
            file = new File(uri);
        }

        return file;
    }

    @Override
    public String getDirectoryPath(String path) throws ScriptException {
        return getFile(path).getAbsolutePath();
    }
}
