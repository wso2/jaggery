package org.jaggeryjs.hostobjects.file;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import javax.activation.FileTypeMap;
import java.io.*;
import java.util.ArrayList;

import static java.lang.Math.min;

public class JavaScriptFileImpl implements JavaScriptFile {

    private static final Log log = LogFactory.getLog(JavaScriptFileImpl.class);
    private RandomAccessFile file = null;
    private File f = null;
    private String path = null;
    private String uri = null;
    private boolean opened = false;

    private boolean readable = false;
    private boolean writable = false;

    private JavaScriptFileManager fileManager = new JavaScriptFileManagerImpl();

    public JavaScriptFileImpl(String uri, String path) {
        this.uri = uri;
        this.path = path;
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    @Override
    public void construct() throws ScriptException {
        f = new File(path);
    }

    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN",
            "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN"})
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
            String msg = "Invalid file mode, path : " + path + ", mode : " + mode;
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
        return f.renameTo(this.fileManager.getFile(dest));
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

    public boolean isDirectory() throws ScriptException {
        return f.isDirectory();
    }

    public String getPath() throws ScriptException {
        return path;
    }

    public String getURI() throws ScriptException {
        return uri;
    }

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
}
