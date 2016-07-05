package org.jaggeryjs.hostobjects.file;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.stream.StreamHostObject;
import org.jaggeryjs.scriptengine.EngineConstants;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.FileTypeMap;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(FileHostObject.class);

    private static final String hostObjectName = "File";

    public static final String JAVASCRIPT_FILE_MANAGER = "hostobjects.file.filemanager";

    private static final String RESOURCE_MEDIA_TYPE_MAPPINGS_FILE = "mime.types";
    private static boolean mimeMapLoaded = false;

    private JavaScriptFile file = null;

    private JavaScriptFileManager manager = null;
    private Context context = null;
    private String path;

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }

        FileHostObject fho = new FileHostObject();
        JaggeryContext context = (JaggeryContext) RhinoEngine.getContextProperty(EngineConstants.JAGGERY_CONTEXT);
        Object obj = context.getProperty(JAVASCRIPT_FILE_MANAGER);
        if (obj instanceof JavaScriptFileManager) {
            fho.manager = (JavaScriptFileManager) obj;
        } else {
            fho.manager = new JavaScriptFileManagerImpl();
        }
        fho.file = fho.manager.getJavaScriptFile(args[0]);
        fho.file.construct();
        fho.context = cx;
        return fho;
    }

    public String getClassName() {
        return hostObjectName;
    }

    public static void jsFunction_open(Context cx, Scriptable thisObj, Object[] args, Function funObj)//NOPMD
            throws ScriptException {
        String functionName = "open";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        fho.file.open((String) args[0]);
    }

    public static void jsFunction_write(Context cx, Scriptable thisObj, Object[] args, Function funObj)//NOPMD
            throws ScriptException {
        String functionName = "write";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        Object data = args[0];
        FileHostObject fho = (FileHostObject) thisObj;
        if (data instanceof InputStream) {
            fho.file.write((InputStream) data);
        } else if (data instanceof StreamHostObject) {
            fho.file.write(((StreamHostObject) data).getStream());
        } else {
            fho.file.write(HostObjectUtil.serializeObject(args[0]));
        }
    }

    public static String jsFunction_read(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "read";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof Number)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        int count = ((Number) args[0]).intValue();
        return fho.file.read(count);
    }

    public static String jsFunction_readAll(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "readAll";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.file.readAll();
    }

    public static void jsFunction_close(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "close";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        fho.file.close();
    }

    public static boolean jsFunction_move(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "move";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }

        FileHostObject fho = (FileHostObject) thisObj;
        String dest = fho.manager.getJavaScriptFile(args[0]).getURI();
        return fho.file.move(dest);
    }

    public static boolean jsFunction_saveAs(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "saveAs";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }

        FileHostObject fho = (FileHostObject) thisObj;
        String dest = fho.manager.getJavaScriptFile(args[0]).getURI();
        return fho.file.saveAs(dest);
    }

    public static boolean jsFunction_del(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "del";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.file.del();
    }

    public static long jsFunction_getLength(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getLength";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.file.getLength();
    }

    public static long jsFunction_getLastModified(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getLastModified";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.file.getLastModified();
    }

    public static String jsFunction_getName(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getName";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.file.getName();
    }

    public static boolean jsFunction_isExists(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "isExists";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.file.isExist();
    }

    public static String jsFunction_getContentType(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getContentType";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;

        if (!mimeMapLoaded) {
            FileTypeMap.setDefaultFileTypeMap(loadMimeMap());
            mimeMapLoaded = true;
        }

        return fho.file.getContentType();
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    private static FileTypeMap loadMimeMap() throws ScriptException {
        String configDirPath = CarbonUtils.getEtcCarbonConfigDirPath();
        File configFile = new File(configDirPath, RESOURCE_MEDIA_TYPE_MAPPINGS_FILE);
        if (!configFile.exists()) {
            String msg = "Resource media type definitions file (mime.types) file does " +
                    "not exist in the path " + configDirPath;
            log.error(msg);
            throw new ScriptException(msg);
        }


        final Map<String, String> mimeMappings = new HashMap<String, String>();

        final String mappings;
        try {
            mappings = FileUtils.readFileToString(configFile, "UTF-8");
        } catch (IOException e) {
            String msg = "Error opening resource media type definitions file " +
                    "(mime.types) : " + e.getMessage();
            throw new ScriptException(msg, e);
        }
        String[] lines = mappings.split("[\\r\\n]+");
        for (String line : lines) {
            if (!line.startsWith("#")) {
                String[] parts = line.split("\\s+");
                for (int i = 1; i < parts.length; i++) {
                    mimeMappings.put(parts[i], parts[0]);
                }
            }
        }

        return new FileTypeMap() {
            @Override
            public String getContentType(File file) {
                return getContentType(file.getName());
            }

            @Override
            public String getContentType(String fileName) {
                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    String mimeType = mimeMappings.get(fileName.substring(i + 1));
                    if (mimeType != null) {
                        return mimeType;
                    }
                }
                return "application/octet-stream";
            }
        };
    }

    public static Scriptable jsFunction_getStream(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getStream";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.context.newObject(thisObj, "Stream", new Object[]{fho.file.getInputStream()});

    }

    public InputStream getInputStream() throws ScriptException {
        return file.getInputStream();
    }

    public OutputStream getOutputStream() throws ScriptException {
        return file.getOutputStream();
    }

    public String getName() throws ScriptException {
        String fileName = file.getName();
        fileName = FilenameUtils.getName(fileName);
        return fileName;
    }

    public JavaScriptFile getJavaScriptFile() throws ScriptException {
        return file;
    }

    public static boolean jsFunction_isDirectory(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "isDirectory";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.file.isDirectory();
    }

    public static String jsFunction_getPath(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getPath";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.file.getURI();
    }

    public static boolean jsFunction_mkdir(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "mkdir";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.file.mkdir();
    }

    public static Object jsFunction_listFiles(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "listFiles";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;

        ArrayList<String> fpaths = fho.file.listFiles();

        ArrayList<Scriptable> fhol = new ArrayList<Scriptable>();

        for (String jsf : fpaths) {
            fhol.add(fho.context.newObject(thisObj, "File", new Object[]{jsf}));
        }
        return cx.newArray(thisObj, fhol.toArray());
    }

    /**
     * To unzip a zip file
     *
     * @param cx      Context
     * @param thisObj FileHostObject to be unzipped
     * @param args    Path to unzip the zip file
     * @param funObj  Function Object
     * @throws ScriptException
     */
    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN"})
    public static boolean jsFunction_unZip(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, IOException {
        String functionName = "unZip";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        ZipInputStream zin = null;
        BufferedOutputStream out = null;
        if (fho.file.isExist()) {
            JaggeryContext context = (JaggeryContext) RhinoEngine.getContextProperty(EngineConstants.JAGGERY_CONTEXT);
            Object obj = context.getProperty(JAVASCRIPT_FILE_MANAGER);
            if (obj instanceof JavaScriptFileManager) {
                fho.manager = (JavaScriptFileManager) obj;
            } else {
                fho.manager = new JavaScriptFileManagerImpl();
            }
            File zipfile = new File(fho.manager.getFile(fho.file.getPath()).getAbsolutePath());
            File outdir = new File(fho.manager.getDirectoryPath(args[0].toString()));
            if (outdir.getParentFile().exists() || outdir.getParentFile().mkdirs()) {
                if (outdir.exists() || outdir.mkdir()) {
                    try {
                        zin = new ZipInputStream(new FileInputStream(zipfile));
                        ZipEntry entry;
                        String name, dir;
                        byte[] buffer = new byte[1024];
                        while ((entry = zin.getNextEntry()) != null) {
                            name = entry.getName();
                            if (entry.isDirectory()) {
                                mkdirs(outdir, name);
                                continue;
                            }
                            int hasParentDirs = name.lastIndexOf(File.separatorChar);
                            dir = (hasParentDirs == -1) ? null : name.substring(0, hasParentDirs);
                            if (dir != null) {
                                mkdirs(outdir, dir);
                            }
                            try {
                                out = new BufferedOutputStream(new FileOutputStream(new File(outdir, name)));
                                int count;
                                while ((count = zin.read(buffer)) != -1) {
                                    out.write(buffer, 0, count);
                                }
                            } catch (Exception ex){
                                log.error("Unable to perform unZip operation for file : "+ fho.file.getName(), ex);
                                return false;
                            } finally {
                                if (out != null) {
                                    try {
                                        out.close();
                                    } catch (IOException er) {
                                        log.error("Unable to close the output stream " + er);
                                    }
                                }
                            }
                        }
                        return true;
                    } catch (IOException ex) {
                        log.error("Cannot unzip the file " + ex);
                        throw new IOException(ex);
                    } finally {
                        if (zin != null) {
                            try {
                                zin.close();
                            } catch (IOException er) {
                                log.error("Unable to close the zip input stream " + er);
                            }
                        }
                    }
                } else {
                    log.error("Unable to create directories to handle file : "+  fho.file.getName());
                }
            } else {
                log.error("Unable to create directories to handle file : "+  fho.file.getName());
            }
        } else {
            log.error("Zip file not exists");
        }
        return false;
    }

    /**
     * To zip a folder
     *
     * @param cx      Context
     * @param thisObj FileHostObject
     * @param args    Zip file path to zip the folder
     * @param funObj  Function
     * @throws ScriptException
     */
    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_OUT", "PATH_TRAVERSAL_IN"})
    public static boolean jsFunction_zip(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, IOException {
        String functionName = "zip";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        ZipOutputStream zip = null;

        if (fho.file.isExist()) {
            JaggeryContext context = (JaggeryContext) RhinoEngine.getContextProperty(EngineConstants.JAGGERY_CONTEXT);
            Object obj = context.getProperty(JAVASCRIPT_FILE_MANAGER);
            if (obj instanceof JavaScriptFileManager) {
                fho.manager = (JavaScriptFileManager) obj;
            } else {
                fho.manager = new JavaScriptFileManagerImpl();
            }
            String destinationPath = fho.manager.getFile(args[0].toString()).getAbsolutePath();
            String sourcePath = fho.manager.getDirectoryPath(fho.file.getPath());
            File destinationFile = new File(destinationPath);
            if (destinationFile.getParentFile().exists() || destinationFile.getParentFile().mkdirs()) {
                try {
                    zip = new ZipOutputStream(new FileOutputStream(destinationPath));
                    File folder = new File(sourcePath);
                    if (folder.list() != null) {
                        for (String fileName : folder.list()) {
                            addFileToZip("", sourcePath + File.separator + fileName, zip);
                        }
                    }
                    return true;
                } catch (IOException ex) {
                    log.error("Cannot zip the folder. " + ex);
                    throw new IOException(ex);
                } finally {
                    if (zip != null) {
                        try {
                            zip.flush();
                            zip.close();
                        } catch (IOException er) {
                            log.error("Unable to close the zip output stream " + er);
                        }
                    }
                }
            } else {
                log.error("Unable to create the directory path for file : "+ fho.file.getName());
            }
        }else {
            log.error("Zip operation cannot be done. Folder not found");
        }
        return false;
    }

    /**
     * To add a file to zip
     *
     * @param path    Root path name
     * @param srcFile Source File that need to be added to zip
     * @param zip     ZipOutputStream
     * @throws IOException
     */
    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN"})
    private static void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws IOException {
        FileInputStream in = null;
        try {
            File folder = new File(srcFile);
            if (folder.isDirectory()) {
                addFolderToZip(path, srcFile, zip);
            } else {
                byte[] buf = new byte[1024];
                int len;
                in = new FileInputStream(srcFile);
                zip.putNextEntry(new ZipEntry(path + File.separator + folder.getName()));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            }
        } catch (IOException er) {
            log.error("Cannot add file to zip " + er);
            throw new IOException(er);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    log.error("Unable to close file input stream. " + ex);
                }
            }
        }
    }

    /**
     * To add a folder to zip
     *
     * @param path      Path of the file or folder from root directory of zip
     * @param srcFolder Source folder to be made as zip
     * @param zip       ZipOutputStream
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws IOException {
        File folder = new File(srcFolder);
        if (path.isEmpty()) {
            zip.putNextEntry(new ZipEntry(folder.getName() + File.separator));
        } else {
            zip.putNextEntry(new ZipEntry(path + File.separator + folder.getName() + File.separator));
        }
        for (String fileName : folder.list()) {
            if (path.isEmpty()) {
                addFileToZip(folder.getName(), srcFolder + File.separator + fileName, zip);
            } else {
                addFileToZip(path + File.separator + folder.getName(), srcFolder + File.separator + fileName, zip);
            }
        }
    }

    /**
     * To create the recursive directories in a specific path
     *
     * @param parentDirectory Parent of the directory
     * @param path            Path of the the child directory to be created inside
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    private static boolean mkdirs(File parentDirectory, String path) {
        File dir = new File(parentDirectory, path);
        return dir.exists() || dir.mkdirs();
    }
}