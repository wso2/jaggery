package org.jaggeryjs.hostobjects.file;

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
import org.mozilla.javascript.annotations.JSConstructor;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.FileTypeMap;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * @param cx
     * @param thisObj
     * @param args    - Argument should be absolute path of unzipping folder
     * @param funObj
     * @throws ScriptException
     */
    public static boolean jsFunction_unZip(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "unZip";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;

        byte[] buffer = new byte[1024];

        try {
            if (fho.file.isExist()) {
                log.info(fho.getJavaScriptFile().getContentType());
                File folder = new File(args[0].toString());
                if (!folder.exists()) {
                    folder.mkdir();
                }
                //get the zip file content
                ZipInputStream zis = new ZipInputStream(fho.getInputStream());
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();

                while (ze != null) {
                    String fileName = ze.getName();
                    File newFile = new File(args[0].toString() + File.separator + fileName);

                    //create all non exists folders
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    ze = zis.getNextEntry();
                }
                zis.closeEntry();
                zis.close();

                return true;
            } else {
                log.error("Zip File not exists");
            }
        } catch (IOException ex) {
            log.error("Cannot unzip the file " + ex);
        }
        return false;
    }

    /**
     * To zip a folder
     *
     * @param cx
     * @param thisObj
     * @param args    - Argument should be absolute path of the zip file to be created
     * @param funObj
     * @throws ScriptException
     */
    public static void jsFunction_zip(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "zip";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        ArrayList<String> fileList = fho.file.listFiles();
        byte[] buffer = new byte[1024];

        String zipFile = args[0].toString();

        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            ArrayList<FileHostObject> fhol = new ArrayList<>();
            for (String jsf : fileList) {
                fhol.add((FileHostObject) fho.context.newObject(thisObj, "File", new Object[] { jsf }));
            }
            for (FileHostObject fhoTemp : fhol) {
                String path = fhoTemp.file.getPath().substring(fho.file.getName().length() + 1);
                ZipEntry ze = new ZipEntry(path);
                zos.putNextEntry(ze);

                InputStream in = fhoTemp.getInputStream();
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
            }
            zos.closeEntry();
            zos.close();
        } catch (IOException ex) {
            log.error("Cannot zip the file" + ex);
        }
    }
}