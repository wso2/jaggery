package org.jaggeryjs.hostobjects.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;

import java.io.InputStream;
import java.io.OutputStream;

public class FileHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(FileHostObject.class);

    private static final String hostObjectName = "File";

    public static final String JAVASCRIPT_FILE_MANAGER = "hostobjects.file.filemanager";

    private JavaScriptFile file = null;
    private JavaScriptFileManager manager = null;
    private Context context = null;

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }

        FileHostObject fho = new FileHostObject();
        Object obj = cx.getThreadLocal(JAVASCRIPT_FILE_MANAGER);
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
        String data = HostObjectUtil.serializeObject(args[0]);
        FileHostObject fho = (FileHostObject) thisObj;
        fho.file.write(data);
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
        return fho.file.move((String)args[0]);
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
        return fho.file.saveAs((String)args[0]);
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
        return fho.file.getContentType();
    }

    public static Scriptable jsFunction_getStream(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getStream";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        FileHostObject fho = (FileHostObject) thisObj;
        return fho.context.newObject(thisObj, "Stream", new Object[]{ fho.file.getInputStream() });
    }

    public InputStream getInputStream() throws ScriptException {
        return file.getInputStream();
    }

    public OutputStream getOutputStream() throws ScriptException {
        return file.getOutputStream();
    }

    public String getName() throws ScriptException {
        return file.getName();
    }

    public JavaScriptFile getJavaScriptFile() throws ScriptException {
        return file;
    }

}