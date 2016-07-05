package org.jaggeryjs.jaggery.core.plugins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.JavaScriptFile;
import org.jaggeryjs.hostobjects.file.JavaScriptFileManager;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import javax.activation.FileTypeMap;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.ArrayList;

public class UploadedFile implements JavaScriptFile {

    private static final Log log = LogFactory.getLog(WebAppFile.class);

    private ServletContext context = null;

    private FileItem fileItem = null;
    private InputStream stream = null;
    private String path = null;
    private boolean opened = false;

    private JavaScriptFileManager fileManager = null;

    private boolean readable = false;
    private boolean writable = false;

    public UploadedFile(FileItem fileItem) {
        //this.path = path.startsWith("/") ? path.substring(1) : path;
        this.fileItem = fileItem;
    }

    @Override
    public void construct() throws ScriptException {

    }

    @Override
    public void open(String mode) throws ScriptException {
        if ("r".equals(mode)) {
            readable = true;
            try {
                stream = fileItem.getInputStream();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        } else {
            String msg = "Invalid or unsupported file mode, path : " + path + ", mode : " + mode;
            log.error(msg);
            throw new ScriptException(msg);
        }
        opened = true;
    }

    @Override
    public void close() throws ScriptException {
        if (!opened) {
            return;
        }
        try {
            stream.close();
            opened = false;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public String read(long count) throws ScriptException {
        if (!opened) {
            log.warn("You need to open the file for reading");
            return null;
        }
        if (!readable) {
            log.warn("File has not opened in a readable mode.");
            return null;
        }
        try {
            BoundedInputStream boundedInputStream = new BoundedInputStream(stream, count);
            return IOUtils.toString(boundedInputStream, "UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public void write(String data) throws ScriptException {
        log.warn("write() method is not available for uploaded files. " +
                "Field name : " + fileItem.getFieldName() + ", File name : " + getName());
    }

    @Override
    public void write(InputStream data) throws ScriptException {
        log.warn("write() method is not available for uploaded files. " +
                "Field name : " + fileItem.getFieldName() + ", File name : " + getName());
    }

    @Override
    public String readAll() throws ScriptException {
        if (!opened) {
            log.warn("You need to open the file for reading");
            return null;
        }
        if (!readable) {
            log.warn("File has not opened in a readable mode.");
            return null;
        }
        try {
            InputStream inputStream = fileItem.getInputStream();
            return IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public boolean move(String dest) throws ScriptException {
        if (opened) {
            log.warn("Please close the file before moving");
            return false;
        }

        try {
            InputStream inputStream = fileItem.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(fileManager.getFile(dest));
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public boolean del() throws ScriptException {
        return false;
    }

    @Override
    public long getLength() throws ScriptException {
        return fileItem.getSize();
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    @Override
    public long getLastModified() throws ScriptException {
        return new File(path).lastModified();
    }

    @SuppressFBWarnings("FILE_UPLOAD_FILENAME")
    @Override
    public String getName() throws ScriptException {
        return fileItem.getName();
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    @Override
    public boolean isExist() throws ScriptException {
        return new File(path).exists();
    }

    @Override
    public InputStream getInputStream() throws ScriptException {
        open("r");
        return stream;
    }

    @Override
    public OutputStream getOutputStream() throws ScriptException {
        return null;
    }

    @Override
    public String getContentType() throws ScriptException {
        return FileTypeMap.getDefaultFileTypeMap().getContentType(getName());
    }

    @Override
    public boolean saveAs(String dest) throws ScriptException {
        return move(dest);
    }

    @Override
    public boolean isDirectory() throws ScriptException {
        return false;
    }

    @Override
    public String getPath() throws ScriptException {
        return ((DiskFileItem)fileItem).getStoreLocation().getPath();
    }

    @Override
    public String getURI() throws ScriptException {
        String filePath = ((DiskFileItem)fileItem).getStoreLocation().toURI().toString();
        return filePath.replaceFirst("file:/", "file:///");
    }

    @Override
    public boolean mkdir() throws ScriptException {
        return false;
    }

    @Override
    public ArrayList<String> listFiles() throws ScriptException {
        return null;
    }

    public void setFileManager(JavaScriptFileManager fileManager) {
        this.fileManager = fileManager;
    }
}
