package org.jaggeryjs.jaggery.core.plugins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.JavaScriptFile;
import org.jaggeryjs.hostobjects.file.JavaScriptFileManager;
import org.jaggeryjs.hostobjects.web.Constants;
import org.jaggeryjs.jaggery.core.manager.CommonManager;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.jaggery.core.manager.WebAppManager;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import javax.activation.FileTypeMap;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

import static java.lang.Math.min;

public class WebAppFile implements JavaScriptFile {

    private static final Log log = LogFactory.getLog(WebAppFile.class);

    private RandomAccessFile file = null;
    private File f = null;
    private String realPath = null;
    private String path = null;
    private boolean opened = false;

    private JavaScriptFileManager fileManager = null;

    private boolean readable = false;
    private boolean writable = false;

    public WebAppFile(String path, ServletContext context) throws ScriptException {
        this.path = path;
        this.realPath = context.getRealPath(getFilePath(path));
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    @Override
    public void construct() throws ScriptException {
        f = new File(realPath);
    }

    private String getFilePath(String fileURL) throws ScriptException {
        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        Stack<String> includesCallstack = CommonManager.getCallstack(jaggeryContext);
        ServletContext context = (ServletContext) jaggeryContext.getProperty(Constants.SERVLET_CONTEXT);
        String parent = includesCallstack.lastElement();
        try {
            String keys[] = WebAppManager.getKeys(context.getContextPath(), parent, fileURL);
            fileURL = "/".equals(keys[1]) ? keys[2] : keys[1] + keys[2];
        } catch (NullPointerException ne) {
            throw new ScriptException("Invalid file path : " + fileURL, ne);
        }
        return fileURL;
    }

    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN"})
    @Override
    public void open(String mode) throws ScriptException {
        if ("r".equals(mode)) {
            try {
                file = new RandomAccessFile(realPath, "r");
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
            readable = true;
        } else if ("r+".equals(mode)) {
            try {
                file = new RandomAccessFile(realPath, "rw");
                file.seek(0);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
            readable = true;
            writable = true;
        } else if ("w".equals(mode)) {
            try {
                file = new RandomAccessFile(realPath, "rw");
                file.setLength(0);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
            writable = true;
        } else if ("w+".equals(mode)) {
            try {
                file = new RandomAccessFile(realPath, "rw");
                file.setLength(0);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
            readable = true;
            writable = true;
        } else if ("a".equals(mode)) {
            try {
                file = new RandomAccessFile(realPath, "rw");
                file.seek(file.length());
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
            writable = true;
        } else if ("a+".equals(mode)) {
            try {
                file = new RandomAccessFile(realPath, "rw");
                file.seek(file.length());
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
            readable = true;
            writable = true;
        } else {
            String msg = "Invalid or unsupported file mode, path : " + realPath + ", mode : " + mode;
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
            file.close();
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
            byte[] arr = new byte[(int) min(count, file.length())];
            file.readFully(arr);
            return new String(arr, "UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public void write(String data) throws ScriptException {
        if (!opened) {
            log.warn("You need to open the file for writing");
            return;
        }
        if (!writable) {
            log.warn("File has not opened in a writable mode.");
            return;
        }
        try {
            file.writeBytes(data);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public void write(InputStream data) throws ScriptException {
        if (!opened) {
            log.warn("You need to open the file for writing");
            return;
        }
        if (!writable) {
            log.warn("File has not opened in a writable mode.");
            return;
        }
        try {
            IOUtils.copy(data, new FileOutputStream(file.getFD()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
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
            long pointer = file.getFilePointer();
            file.seek(0);
            String data = read(file.length());
            file.seek(pointer);
            return data;
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
        return f.renameTo(fileManager.getFile(dest));
    }

    @Override
    public boolean del() throws ScriptException {
        if (opened) {
            log.warn("Please close the file before deleting");
            return false;
        }
        return FileUtils.deleteQuietly(f);
    }

    @Override
    public long getLength() throws ScriptException {
        return f.length();
    }

    @Override
    public long getLastModified() throws ScriptException {
        return f.lastModified();
    }

    @Override
    public String getName() throws ScriptException {
        return f.getName();
    }

    @Override
    public boolean isExist() throws ScriptException {
        return f.exists();
    }

    @Override
    public InputStream getInputStream() throws ScriptException {
        try {
            open("r");
            return new FileInputStream(file.getFD());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws ScriptException {
        try {
            open("w");
            return new FileOutputStream(file.getFD());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
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
    public boolean mkdir() throws ScriptException {
        return f.mkdir();
    }

    @Override
    public boolean isDirectory() throws ScriptException {
        return f.isDirectory();
    }

    @Override
    public String getPath() throws ScriptException {
        return this.path;
    }

    @Override
    public String getURI() throws ScriptException {
        return this.path;
    }

    @Override
    public ArrayList<String> listFiles() throws ScriptException {
        File[] fileList = f.listFiles();
        ArrayList<String> jsfl = new ArrayList<String>();
        String parentDir = this.getURI();
        if (!parentDir.endsWith("/")) {
            parentDir += "/";
        }
        if (fileList != null) {
            for (File fi : fileList) {
                jsfl.add(parentDir + fi.getName());
            }
        }
        return jsfl;
    }

    public void setFileManager(JavaScriptFileManager fileManager) {
        this.fileManager = fileManager;
    }
}
