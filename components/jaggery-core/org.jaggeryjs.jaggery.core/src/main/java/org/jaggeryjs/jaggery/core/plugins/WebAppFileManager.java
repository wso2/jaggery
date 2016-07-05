package org.jaggeryjs.jaggery.core.plugins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.JavaScriptFile;
import org.jaggeryjs.hostobjects.file.JavaScriptFileManager;
import org.jaggeryjs.hostobjects.file.JavaScriptFileManagerImpl;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import javax.servlet.ServletContext;
import java.io.File;

public class WebAppFileManager implements JavaScriptFileManager {

    private static final Log log = LogFactory.getLog(WebAppFileManager.class);

    private ServletContext context;
    private static final String FILE_PATH = "file://";

    public WebAppFileManager(ServletContext context) throws ScriptException {
        this.context = context;
    }

    @Override
    public JavaScriptFile getJavaScriptFile(Object object) throws ScriptException {
        if (object instanceof String) {
            String path = (String) object;
            if (path.startsWith(FILE_PATH)) {
                return new JavaScriptFileManagerImpl().getJavaScriptFile(path);
            }
            WebAppFile webAppFile = new WebAppFile(path, context);
            webAppFile.setFileManager(this);
            return webAppFile;
        } else if (object instanceof FileItem) {
            UploadedFile uploadedFile = new UploadedFile((FileItem) object);
            uploadedFile.setFileManager(this);
            return uploadedFile;
        } else {
            String msg = "Unsupported parameter to the File constructor : " + object.getClass();
            log.error(msg);
            throw new ScriptException(msg);
        }
    }

    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN"})
    @Override
    public File getFile(String path) throws ScriptException {
        if (path.startsWith(FILE_PATH)) {
            return new JavaScriptFileManagerImpl().getFile(path);
        }

        String oldPath = path;
        path = FilenameUtils.normalizeNoEndSeparator(path);
        if (path == null) {
            String msg = "Invalid file path : " + oldPath;
            log.error(msg);
            throw new ScriptException(msg);
        }

        File file = new File(context.getRealPath("/"), path);
        if (file.isDirectory()) {
            String msg = "File hostobject doesn't handle directories. Specified path contains a directory : " + path;
            log.error(msg);
            throw new ScriptException(msg);
        }
        return file;
    }

    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN"})
    @Override
    public String getDirectoryPath(String path) throws ScriptException {
        if (path.startsWith(FILE_PATH)) {
            return new JavaScriptFileManagerImpl().getFile(path).getAbsolutePath();
        }

        String oldPath = path;
        path = FilenameUtils.normalizeNoEndSeparator(path);
        if (path == null) {
            String msg = "Invalid file path : " + oldPath;
            log.error(msg);
            throw new ScriptException(msg);
        }

        File file = new File(context.getRealPath("/"), path);
        return file.getPath();
    }
}
