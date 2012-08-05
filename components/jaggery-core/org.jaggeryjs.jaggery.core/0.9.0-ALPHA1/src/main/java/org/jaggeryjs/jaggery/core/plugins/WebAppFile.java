package org.jaggeryjs.jaggery.core.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.JavaScriptFile;
import org.jaggeryjs.hostobjects.file.JavaScriptFileManager;
import org.jaggeryjs.jaggery.core.manager.CommonManager;
import org.jaggeryjs.jaggery.core.manager.WebAppContext;
import org.jaggeryjs.jaggery.core.manager.WebAppManager;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import javax.activation.FileTypeMap;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.Stack;

public class WebAppFile implements JavaScriptFile {

    private static final Log log = LogFactory.getLog(WebAppFile.class);

    private ServletContext context = null;

    private RandomAccessFile file = null;
    private String path = null;
    private boolean opened = false;

    private JavaScriptFileManager fileManager = null;

    private boolean readable = false;
    private boolean writable = false;

    public WebAppFile(String path, ServletContext context) throws ScriptException {
        this.path = context.getRealPath(getFilePath(path));
        this.context = context;
    }

    @Override
    public void construct() throws ScriptException {

    }

    private String getFilePath(String fileURL) throws ScriptException {
        WebAppContext webAppContext = (WebAppContext) CommonManager.getJaggeryContext();
        Stack<String> includesCallstack = CommonManager.getJaggeryContext().getIncludesCallstack();
        ServletContext context = webAppContext.getServletConext();
        String parent = includesCallstack.lastElement();
        try {
            String keys[] = WebAppManager.getKeys(context.getContextPath(), parent, fileURL);
            fileURL = "/".equals(keys[1]) ? keys[2] : keys[1] + keys[2];
        } catch (NullPointerException ne) {
            throw new ScriptException("Invalid file path : " + fileURL, ne);
        }
        return fileURL;
    }

    @Override
    public void open(String mode) throws ScriptException {
        if ("r".equals(mode)) {
            try {
                file = new RandomAccessFile(path, "r");
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
            readable = true;
        } else if ("r+".equals(mode)) {
            try {
                file = new RandomAccessFile(path, "rw");
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
                file = new RandomAccessFile(path, "rw");
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
                file = new RandomAccessFile(path, "rw");
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
                file = new RandomAccessFile(path, "rw");
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
                file = new RandomAccessFile(path, "rw");
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
            String msg = "Invalid or unsupported file mode, path : " + path + ", mode : " + mode;
            log.error(msg);
            throw new ScriptException(msg);
        }
        opened = true;
    }

    @Override
    public void close() throws ScriptException {
        if(!opened) {
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
        if(!opened) {
            log.warn("You need to open the file for reading");
            return null;
        }
        if(!readable) {
            log.warn("File has not opened in a readable mode.");
            return null;
        }
        try {
            StringBuffer buffer = new StringBuffer();
            long length = file.length();
            for (long i = 0; (i < count) && (i < length); i++) {
                buffer.append((char) file.readByte());
            }
            return buffer.toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public void write(String data) throws ScriptException {
        if(!opened) {
            log.warn("You need to open the file for writing");
            return;
        }
        if(!writable) {
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
    public String readAll() throws ScriptException {
        if(!opened) {
            log.warn("You need to open the file for reading");
            return null;
        }
        if(!readable) {
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
        if(opened) {
            log.warn("Please close the file before moving");
            return false;
        }
        return new File(path).renameTo(fileManager.getFile(path));
    }

    @Override
    public boolean del() throws ScriptException {
        if(opened) {
            log.warn("Please close the file before deleting");
            return false;
        }
        return new File(path).delete();
    }

    @Override
    public long getLength() throws ScriptException {
        try {
            return file.length();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public long getLastModified() throws ScriptException {
        return new File(path).lastModified();
    }

    @Override
    public String getName() throws ScriptException {
        return new File(path).getName();
    }

    @Override
    public boolean isExist() throws ScriptException {
        return new File(path).exists();
    }

    @Override
    public InputStream getInputStream() throws ScriptException {
        try {
            return new FileInputStream(file.getFD());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws ScriptException {
        try {
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

    public void setFileManager(JavaScriptFileManager fileManager) {
        this.fileManager = fileManager;
    }
}
