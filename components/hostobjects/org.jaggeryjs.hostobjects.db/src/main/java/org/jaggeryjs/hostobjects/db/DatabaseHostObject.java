package org.jaggeryjs.hostobjects.db;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.stream.StreamHostObject;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSource;

import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceManager;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(DatabaseHostObject.class);

    private static final String hostObjectName = "Database";
    public static final String COM_MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final String ORG_H2_DRIVER = "org.h2.Driver";
    public static final String ORACLE_JDBC_ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
    public static final String MYSQL = "jdbc:mysql";
    public static final String H2 = "jdbc:h2";
    public static final String ORACLE = "jdbc:oracle";

    private boolean autoCommit = true;

    private Context context = null;

    private Connection conn = null;

    static RDBMSDataSource rdbmsDataSource = null;

    private Map<String, Savepoint> savePoints = new HashMap<String, Savepoint>();

    public DatabaseHostObject() {

    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        DatabaseHostObject db = new DatabaseHostObject();

        //args count 1 for dataSource name
        if (argsCount != 1 && argsCount != 3 && argsCount != 4) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }

        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "1", "string", args[0], true);
        }

        if (argsCount == 1) {
            String dataSourceName = (String) args[0];
            DataSourceManager dataSourceManager = new DataSourceManager();
            try {
                CarbonDataSource carbonDataSource = dataSourceManager.getInstance().getDataSourceRepository().getDataSource(dataSourceName);
                DataSource dataSource = (DataSource) carbonDataSource.getDSObject();

                db.conn = dataSource.getConnection();
                db.context = cx;
                return db;
            } catch (DataSourceException e) {
                log.error("Failed to access datasource " + dataSourceName, e);
            } catch (SQLException e) {
                log.error("Failed to get connection", e);
            }
        }

        if (!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "2", "string", args[1], true);
        }

        if (!(args[2] instanceof String) && !(args[2] instanceof Integer)) {
            HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "3", "string", args[2], true);
        }

        NativeObject configs = null;
        if (argsCount == 4) {
            if (!(args[3] instanceof NativeObject)) {
                HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "4", "object", args[3], true);
            }
            configs = (NativeObject) args[3];
        }

        String dbUrl = (String) args[0];
        RDBMSConfiguration rdbmsConfig = new RDBMSConfiguration();
        try {
            if (configs != null) {
                Gson gson = new Gson();
                rdbmsConfig = gson.fromJson(HostObjectUtil.serializeJSON(configs), RDBMSConfiguration.class);
            }

            if (rdbmsConfig.getDriverClassName() == null || rdbmsConfig.getDriverClassName().equals("")) {
                rdbmsConfig.setDriverClassName(getDriverClassName(dbUrl));
            }


            rdbmsConfig.setUsername((String) args[1]);
            rdbmsConfig.setPassword((String) args[2]);
            rdbmsConfig.setUrl(dbUrl);


            try {
                rdbmsDataSource = new RDBMSDataSource(rdbmsConfig);

            } catch (DataSourceException e) {
                throw new ScriptException(e);
            }

            db.conn = rdbmsDataSource.getDataSource().getConnection();
            db.context = cx;
            return db;
        } catch (SQLException e) {
            String msg = "Error connecting to the database : " + dbUrl;
            log.warn(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    private static String getDriverClassName(String dburl) {
        if (dburl.contains(MYSQL)) {
            return COM_MYSQL_JDBC_DRIVER;
        } else if (dburl.contains(H2)) {
            return ORG_H2_DRIVER;
        } else if (dburl.contains(ORACLE)) {
            return ORACLE_JDBC_ORACLE_DRIVER;
        } else {
            return null;
        }
    }

    public boolean jsGet_autoCommit() throws ScriptException {
        return this.autoCommit;
    }

    public void jsSet_autoCommit(Object object) throws ScriptException {
        if (!(object instanceof Boolean)) {
            HostObjectUtil.invalidProperty(hostObjectName, "autoCommit", "boolean", object);
        }
        this.autoCommit = (Boolean) object;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SQL_INJECTION_JDBC", "SQL_INJECTION_JDBC", "SQL_INJECTION_JDBC", "SQL_INJECTION_JDBC"})
    public static Object jsFunction_query(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, SQLException {
        String functionName = "query";
        int argsCount = args.length;
        if (argsCount == 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        DatabaseHostObject db = (DatabaseHostObject) thisObj;
        String query;

        if (argsCount == 1) {
            //query
            Function callback = null;
            if (!(args[0] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
            }
            query = (String) args[0];
            PreparedStatement stmt = db.conn.prepareStatement(query);
            return executeQuery(cx, db, stmt, query, callback, true);
        } else if (argsCount == 2) {
            if (!(args[0] instanceof String)) {
                //batch
                Function callback = null;
                if (!(args[0] instanceof NativeArray)) {
                    HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
                }
                NativeArray queries = (NativeArray) args[0];
                NativeArray values = null;

                if (args[1] instanceof Function) {
                    callback = (Function) args[1];
                } else if (args[1] instanceof NativeArray) {
                    values = (NativeArray) args[1];
                } else {
                    HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "array | function", args[0], false);
                }

                return executeBatch(cx, db, queries, values, callback);

            } else {
                //query
                Function callback = null;
                query = (String) args[0];
                PreparedStatement stmt = db.conn.prepareStatement(query);
                if (args[1] instanceof Function) {
                    callback = (Function) args[1];
                } else {
                    setQueryParams(stmt, args, 1, 1);
                }
                return executeQuery(cx, db, stmt, query, callback, true);
            }
        } else if (argsCount == 3) {
            if (!(args[0] instanceof String)) {
                //batch
                Function callback = null;
                if (!(args[0] instanceof NativeArray)) {
                    HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "array", args[0], false);
                }
                if (!(args[1] instanceof NativeArray)) {
                    HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "array", args[1], false);
                }
                if (!(args[2] instanceof Function)) {
                    HostObjectUtil.invalidArgsError(hostObjectName, functionName, "3", "function", args[2], false);
                }
                NativeArray queries = (NativeArray) args[0];
                NativeArray values = (NativeArray) args[1];
                callback = (Function) args[2];
                return executeBatch(cx, db, queries, values, callback);
            } else {
                //query
                Function callback = null;
                query = (String) args[0];
                PreparedStatement stmt = db.conn.prepareStatement(query);
                if (args[2] instanceof Function) {
                    callback = (Function) args[2];
                    setQueryParams(stmt, args, 1, 1);
                } else {
                    setQueryParams(stmt, args, 1, 2);
                }
                return executeQuery(cx, db, stmt, query, callback, true);
            }
        } else {
            //args count > 3
            if (!(args[0] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
            }
            Function callback = null;
            query = (String) args[0];
            PreparedStatement stmt = db.conn.prepareStatement(query);
            if (args[argsCount - 1] instanceof Function) {
                callback = (Function) args[argsCount - 1];
                setQueryParams(stmt, args, 1, argsCount - 1);
            } else {
                setQueryParams(stmt, args, 1, argsCount - 1);
            }
            return executeQuery(cx, db, stmt, query, callback, true);
        }
    }

    public static String jsFunction_savePoint(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, SQLException {
        String functionName = "savePoint";
        int argsCount = args.length;
        String savePoint;
        if (argsCount == 0) {
            savePoint = UUID.randomUUID().toString();
        } else {
            if (argsCount != 1) {
                HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
            }
            if (!(args[0] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
            }
            savePoint = (String) args[0];
        }
        DatabaseHostObject db = (DatabaseHostObject) thisObj;
        db.savePoints.put(savePoint, db.conn.setSavepoint(savePoint));
        return savePoint;
    }

    public static void jsFunction_releasePoint(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "releasePoint";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        String savePoint = (String) args[0];
        DatabaseHostObject db = (DatabaseHostObject) thisObj;
        try {
            db.conn.releaseSavepoint(db.savePoints.remove(savePoint));
        } catch (SQLException e) {
            String msg = "Error while releasing the savepoint : " + savePoint;
            log.warn(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    public static void jsFunction_rollback(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "rollback";
        int argsCount = args.length;
        if (argsCount > 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        String savePoint = null;
        if (argsCount == 1) {
            if (!(args[0] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
            }
            savePoint = (String) args[0];
        }
        DatabaseHostObject db = (DatabaseHostObject) thisObj;
        if (savePoint != null) {
            try {
                db.conn.rollback(db.savePoints.get(savePoint));
            } catch (SQLException e) {
                String msg = "Error while rolling back the transaction to savepoint : " + savePoint;
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            }
        } else {
            try {
                db.conn.rollback();
            } catch (SQLException e) {
                String msg = "Error while rolling back the transaction";
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            }
        }
    }

    public static void jsFunction_commit(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "commit";
        int argsCount = args.length;
        if (argsCount > 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        DatabaseHostObject db = (DatabaseHostObject) thisObj;
        try {
            db.conn.commit();
        } catch (SQLException e) {
            String msg = "Error while committing the transaction";
            log.warn(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    public static void jsFunction_close(Context cx, Scriptable thisObj, Object[] args,
                                        Function funObj) throws ScriptException {
        String functionName = "c";
        int argsCount = args.length;
        if (argsCount > 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        DatabaseHostObject db = (DatabaseHostObject) thisObj;
        try {
            db.conn.close();
            if (rdbmsDataSource != null) {
                rdbmsDataSource.getDataSource().close();
            }
        } catch (SQLException e) {
            String msg = "Error while closing the Database Connection";
            log.warn(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    private static String replaceWildcards(DatabaseHostObject db, String query, NativeArray params) throws SQLException {
        String openedChar = null;
        String lastChar = null;
        StringBuffer newQuery = new StringBuffer();
        int paramIndex = 0;
        for (int i = 0; i < query.length(); i++) {
            String c = Character.toString(query.charAt(i));
            if (lastChar == null) {
                lastChar = c;
                if (c.equals("'") || c.equals("\"")) {
                    openedChar = c;
                }
                newQuery.append(c);
                continue;
            }
            if (c.equals("'")) {
                if (openedChar == null) {
                    openedChar = c;
                } else if (openedChar.equals(c)) {
                    if (!lastChar.equals("\\")) {
                        //closing reached
                        openedChar = null;
                    }
                }

            } else if (c.equals("\"")) {
                if (openedChar == null) {
                    openedChar = c;
                } else if (openedChar.equals(c)) {
                    if (!lastChar.equals("\\")) {
                        //closing reached
                        openedChar = null;
                    }
                }
            } else if (c.equals("?")) {
                if (openedChar == null) {
                    //replace ?
                    newQuery.append(HostObjectUtil.serializeObject(params.get(paramIndex, db)));
                    paramIndex++;
                    continue;
                } else if (lastChar.equals("'")) {
                    if (openedChar.equals("'")) {
                        String nextChart = Character.toString(query.charAt(i + 1));
                        if (nextChart.equals("'")) {
                            //replace '?'
                            newQuery.append(HostObjectUtil.serializeObject(params.get(paramIndex, db)));
                            continue;
                        }
                    }
                } else if (lastChar.equals("\"")) {
                    if (openedChar.equals("\"")) {
                        String nextChart = Character.toString(query.charAt(i + 1));
                        if (nextChart.equals("\"")) {
                            //replace '?'
                            newQuery.append(HostObjectUtil.serializeObject(params.get(paramIndex, db)));
                            continue;
                        }
                    }
                }
            }
            newQuery.append(c);
            lastChar = c;
        }
        return newQuery.toString();
    }

    private static void setQueryParams(PreparedStatement stmt, Object[] params, int from, int to) throws SQLException {
        for (int i = from; i < to + 1; i++) {
            setQueryParam(stmt, params[i], i);
        }
    }

    private static void setQueryParam(PreparedStatement stmt, Object obj, int index) throws SQLException {
        if (obj instanceof String) {
            stmt.setString(index, (String) obj);
        } else if (obj instanceof Integer) {
            stmt.setInt(index, (Integer) obj);
        } else if (obj instanceof Double) {
            stmt.setDouble(index, (Double) obj);
        }
        //Support for streams in queries
        //Added 25/9/2013
        else if (obj instanceof StreamHostObject) {
            StreamHostObject stream=(StreamHostObject)obj;
            stmt.setBinaryStream(index,stream.getStream());
        }
        else {
            stmt.setString(index, HostObjectUtil.serializeObject(obj));
        }
    }

    private static Object executeQuery(Context cx, final DatabaseHostObject db, final PreparedStatement stmt, String query,
                                       final Function callback, final boolean keyed) throws ScriptException {

        String regex = "^[\\s\\t\\r\\n]*[Ss][Ee][Ll][Ee][Cc][Tt].*";//select
        final boolean isSelect = query.matches(regex);
        if (callback != null) {
            final ContextFactory factory = cx.getFactory();
            final ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(new Callable() {
                public Object call() throws Exception {
                    Context cx = RhinoEngine.enterContext(factory);
                    try {
                        Object result;
                        if (isSelect) {
                            result = processResults(cx, db, db, stmt.executeQuery(), keyed);
                        } else {
                            result = stmt.executeUpdate();                            
                        }
                        stmt.close();
                        callback.call(cx, db, db, new Object[]{result});
                    } catch (SQLException e) {
                        log.warn(e);
                    } finally {
                        es.shutdown();
                        RhinoEngine.exitContext();
                    }
                    return null;
                }
            });
            return null;
        } else {
            try {
            	Object result;
                if (isSelect) {
                	result =  processResults(cx, db, db, stmt.executeQuery(), keyed);
                } else {                	
                	result = stmt.executeUpdate();                	
                }               
                stmt.close();
            	return result;
            } catch (SQLException e) {
                log.warn(e);
                throw new ScriptException(e);
            }
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SQL_INJECTION_JDBC")
    private static Object executeBatch(Context cx, final DatabaseHostObject db, NativeArray queries,
                                       NativeArray params, final Function callback)
            throws ScriptException, SQLException {
        if (params != null && (queries.getLength() != params.getLength())) {
            String msg = "Query array and values array should be in the same size. HostObject : " +
                    hostObjectName + ", Method : query";
            log.warn(msg);
            throw new ScriptException(msg);
        }

        final Statement stmt = db.conn.createStatement();
        for (int index : (Integer[]) queries.getIds()) {
            Object obj = queries.get(index, db);
            if (!(obj instanceof String)) {
                String msg = "Invalid query type : " + obj.toString() + ". Query should be a string";
                log.warn(msg);
                throw new ScriptException(msg);
            }
            String query = (String) obj;
            if (params != null) {
                Object valObj = params.get(index, db);
                if (!(valObj instanceof NativeArray)) {
                    String msg = "Invalid value type : " + obj.toString() + " for the query " + query;
                    log.warn(msg);
                    throw new ScriptException(msg);
                }
                query = replaceWildcards(db, query, (NativeArray) valObj);
            }
            stmt.addBatch(query);
        }

        if (callback != null) {
            final ContextFactory factory = cx.getFactory();
            final ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(new Callable() {
                public Object call() throws Exception {
                    Context ctx = RhinoEngine.enterContext(factory);
                    try {
                        int[] result = stmt.executeBatch();
                        callback.call(ctx, db, db, new Object[]{result});
                    } catch (SQLException e) {
                        log.warn(e);
                    } finally {
                        es.shutdown();
                        RhinoEngine.exitContext();
                    }
                    return null;
                }
            });
            return null;
        } else {
            return stmt.executeBatch();
        }

    }

    private static Scriptable processResults(Context cx, Scriptable scope, DatabaseHostObject db, ResultSet results, boolean keyed) throws SQLException, ScriptException {
        List<ScriptableObject> rows = new ArrayList<ScriptableObject>();
        while (results.next()) {
            ScriptableObject row;
            ResultSetMetaData rsmd = results.getMetaData();
            if (keyed) {
                row = (ScriptableObject) db.context.newObject(db);
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    String columnName = rsmd.getColumnLabel(i + 1);
                    Object columnValue = getValue(db, results, i + 1, rsmd.getColumnType(i + 1));
                    row.put(columnName, row, columnValue);
                }
            } else {
                row = (ScriptableObject) cx.newArray(scope, rsmd.getColumnCount());
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    Object columnValue = getValue(db, results, i + 1, rsmd.getColumnType(i + 1));
                    row.put(i + 1, row, columnValue);
                }
            }
            rows.add(row);
        }
        return db.context.newArray(db, rows.toArray());
    }

    private static Object getValue(DatabaseHostObject db, ResultSet results, int index, int type) throws SQLException, ScriptException {
        Context cx = db.context;
        //TODO : implement for other sql types
        switch (type) {
            case Types.ARRAY:
                return (results.getArray(index) == null) ? null : cx.newArray(db, new Object[]{results.getArray(index)});
            case Types.BIGINT:
                return (results.getBigDecimal(index) == null) ? null : results.getBigDecimal(index).toPlainString();
            case Types.BINARY:
            case Types.LONGVARBINARY:
                return results.getBinaryStream(index) == null ? null : cx.newObject(db, "Stream", new Object[]{results.getBinaryStream(index)});
            case Types.CLOB:
                return results.getClob(index) == null ? null : cx.newObject(db, "Stream", new Object[]{results.getClob(index).getAsciiStream()});
            case Types.BLOB:
                return results.getBlob(index) == null ? null : cx.newObject(db, "Stream", new Object[]{results.getBlob(index).getBinaryStream()});
            case Types.TIMESTAMP:
                Timestamp date = results.getTimestamp(index);
                return date == null ? null : cx.newObject(db, "Date", new Object[] {date.getTime()});
            default:
                return results.getObject(index) == null ? null : results.getObject(index);
        }
    }


}
