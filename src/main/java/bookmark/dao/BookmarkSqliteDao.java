package bookmark.dao;

import bookmark.model.meta.BookmarkConfig;
import bookmark.model.meta.BookmarkMetadata;
import bookmark.model.meta.BookmarkState;
import bookmark.model.meta.ContextMetadata;
import bookmark.model.txn.Bookmark;
import bookmark.model.txn.BookmarkTxns;
import bookmark.model.txn.TxnQuery;
import bookmark.model.value.BookmarkValues;
import bookmark.model.value.ValueQuery;
import ch.qos.logback.core.encoder.EchoEncoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.internal.org.objectweb.asm.TypeReference;
import util.time.model.Time;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class BookmarkSqliteDao implements BookmarkDao{

    String path = "./bookmarks/";
    String valuesTable = "states";
    String metaTable = "meta";
    String defaultContextName = "default";
    Integer batch_size = 200;
    Integer timeoutMillis = 60000;

    ObjectMapper mapper = new ObjectMapper();

    public BookmarkSqliteDao(String path) throws ConfigurationException {
        if (path != null) {
            this.path = path + "/bookmarks/";
        }
        try {
            if (!Files.exists(Paths.get(this.path))) {
                Files.createDirectory(Paths.get(this.path));
            }
            if (!Files.isWritable(Paths.get(this.path))) {
                throw new ConfigurationException("Initial configuration failed... bookmark path is not writable");
            }
        } catch (Exception e) {
            throw new ConfigurationException("Initial configuration failed... Check configuration. Caused by:" + e);
        }
    }

    @Override
    public Boolean createBookmark(String bookmarkName, BookmarkMetadata bookmarkMetadata) throws Exception{
        //make sure the bookmark name is valid & bookmark does not already exists
        if (!validateBookmarkName(bookmarkName) || bookmarkExists(bookmarkName)) {
            return false;
        }
        //reject bookmark if there is invalid context name in metadata
        if (bookmarkMetadata.getContextMap() != null) {
            for (String context: bookmarkMetadata.getContextList()) {
                if (!validateContextName(context)) {
                    return false;
                }
            }
        }
        //delete if there are any residue files from corrupted bookmark
        deleteBookmark(bookmarkName);
        BookmarkState originalState = bookmarkMetadata.getState();
        bookmarkMetadata.setState(BookmarkState.notReadyState());
        if(createMetaTable(bookmarkName) && saveBookmarkMeta(bookmarkName, bookmarkMetadata)
                && createValuesTable(bookmarkName) ) {
            enableWriteAhead(bookmarkName);
            if (bookmarkMetadata.getContextMap() != null) {
                for (ContextMetadata context: bookmarkMetadata.getContextMetadata()) {
                    createContext(bookmarkName, context);
                }
            }
            BookmarkMetadata setState = new BookmarkMetadata();
            setState.setState(originalState);
            updateBookmarkMeta(bookmarkName, setState);
            return true;
        }
        return false;
    }

    @Override
    public Boolean createContext(String bookmarkName, ContextMetadata context) throws Exception{
        Boolean success = true;
        //register context name in the metadata if it is not already
        BookmarkMetadata contextMeta = getBookmarkMeta(bookmarkName);
        if (!contextMeta.getContextList().contains(context)) {
            success &= updateContextMeta(bookmarkName, context);
        }
        //create context transaction table if it does not exist already
        String checkTxnTableSql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + context + "';";
        Integer txnTableCount = (Integer) getElement(bookmarkName, checkTxnTableSql);
        if (txnTableCount < 1) {
            success &= createTxnTable(bookmarkName, context.getName());
        }
        //create context value field if it does not exist already
        String checkValueTableSql = "SELECT count(*) FROM " + valuesTable + " WHERE name='" + context + "';";
        Integer valueEntryCount = (Integer) getElement(bookmarkName, checkValueTableSql);
        if (valueEntryCount < 1) {
            success &= createValueEntry(bookmarkName, context.getName());
        }
        return success;
    }

    @Override
    public Boolean bookmarkExists(String bookmarkName) throws Exception{
        //bookmark exists if it has a database created and the lock is not in NotReady state
        if (Files.exists(Paths.get(path + bookmarkName + ".db"))) {
            String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + metaTable + "';";
            Integer metaTableCount = (Integer) getElement(bookmarkName, sql);
            if (metaTableCount > 0) {
                BookmarkState state = getState(bookmarkName);
                if (state.isReady()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean contextExists(String bookmarkName, String context) throws Exception{
        //context exists if context is registered in metadata & value and txn context entries is setup
        ContextMetadata contextMetadata = getContextMeta(bookmarkName, context);
        String checkTxnTableSql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + context + "';";
        Integer txnTableCount = (Integer) getElement(bookmarkName, checkTxnTableSql);
        String checkValueTableSql = "SELECT count(*) FROM " + valuesTable + " WHERE name='" + context + "';";
        Integer valueEntryCount = (Integer) getElement(bookmarkName, checkValueTableSql);
        return contextMetadata != null && txnTableCount > 0 && valueEntryCount > 0;
    }

    @Override
    public List<String> getBookmarkList() {
        File[] files = new File(path).listFiles();
        List<String> names = new ArrayList();
        for (File file:files) {
            if (file.getName().endsWith(".db")) {
                String[] split = file.getName().split("\\.");
                names.add(split[0]);
            }
        }
        return names;
    }

    @Override
    public Set<String> getContextList(String bookmarkName) throws Exception{
        BookmarkMetadata bookmarkMetadata = getBookmarkMeta(bookmarkName);
        return bookmarkMetadata.getContextList();
    }

    @Override
    public Boolean updateBookmarkTxn(String bookmarkName, BookmarkTxns txns) throws Exception {
        Statement stmt = null;
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + "/" + bookmarkName + ".db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        try{
            conn.setAutoCommit(false);
            String sql = "INSERT INTO " + txns.getContext() + " (timestamp,metrics) " + " VALUES (?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int count = 0;
            for(Bookmark bookmark : txns){
                String stats_str = "";
                try {
                    stats_str = mapper.writeValueAsString(bookmark);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return false;
                }

                pstmt.setLong(1,bookmark.getTimestamp().getInstant().toEpochMilli());
                pstmt.setString(2,stats_str);
                pstmt.addBatch();
                count++;
                if(count % batch_size == 0){
                    pstmt.executeBatch();
                    conn.commit();
                }
            }
            pstmt.executeBatch();
            conn.commit();
            pstmt.close();
            stmt.close();
            conn.close();
            return true;
        } catch (Exception e) {
            try {conn.rollback();} catch (Exception ex) {}
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
            throw e;
        }

    }

    @Override
    public Boolean saveBookmarkTxn(String bookmarkName, BookmarkTxns txns) throws Exception {
        truncateTable(bookmarkName, txns.getContext());
        return updateBookmarkTxn(bookmarkName, txns);
    }

    @Override
    public BookmarkTxns getBookmarkTxn(String bookmarkName, TxnQuery txnQuery) throws Exception{
        String sql = "SELECT * FROM " + txnQuery.getContext();
        if (txnQuery != null) {
            sql += " WHERE 1=1 ";
            if (txnQuery.getFrom() != null) {
                sql += " AND timestamp>='" + txnQuery.getFrom().mysqlString() + "'";
            }
            if (txnQuery.getTo() != null) {
                sql += " AND timestamp<='" + txnQuery.getTo().mysqlString() + "'";
            }
            if (txnQuery.getLimit() != null) {
                if (txnQuery.getLimit() > 0) {
                    sql += " ORDER BY timestamp DESC LIMIT " + txnQuery.getLimit();
                } else if (txnQuery.getLimit() < 0) {
                    sql += " ORDER BY timestamp ASC LIMIT " + -1*txnQuery.getLimit();
                }
            }
        }
        BookmarkTxns txns = null;
        Statement stmt = null;
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + "/" + bookmarkName + ".db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            ResultSet rs = stmt.executeQuery(sql);
            txns = new BookmarkTxns();
            txns.setContext(txnQuery.getContext());
            while (rs.next()) {
                String metric_str = rs.getString("metrics");
                Bookmark bookmark = new ObjectMapper().readValue(metric_str, Bookmark.class);
                bookmark.setTimestamp(Time.parse(rs.getLong("timestamp")));
                txns.add(bookmark);
            }
            stmt.close();
            conn.close();
            return txns;
        } catch (Exception e) {
            try {stmt.close();} catch (Exception ex){};
            try {conn.close();} catch (Exception ex){};
            throw e;
        }

    }

    @Override
    public BookmarkValues getBookmarkValues(String bookmarkName, String context)  throws Exception{
        BookmarkValues values = null;
        Statement stmt = null;
        Connection conn = null;
        try {
            String sql = "SELECT data FROM " + valuesTable + " WHERE name='" + context + "';";
            String url = "jdbc:sqlite:" + path + "/" + bookmarkName + ".db";
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String metric_str = rs.getString("data");
                values = new ObjectMapper().readValue(metric_str, BookmarkValues.class);
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
        }
        return values;
    }

    @Override
    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception {
        Statement stmt = null;
        Connection conn = createConnection(bookmarkName);
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
            String sql = "UPDATE " + valuesTable + " SET data=json_patch(data, ?) " +
                    "WHERE name='" + values.getContext() + "'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            String data = null;
            ObjectMapper mapper = new ObjectMapper();
            data = mapper.writeValueAsString(values);
            pstmt.setString(1, data);
            Integer updateCount = pstmt.executeUpdate();
            pstmt.close();
            stmt.close();
            conn.close();
            if (updateCount < 1) {
                throw new ConfigurationException("didn't update values - check if context is setup");
            }
            return true;
        } catch (Exception e) {
            try {conn.rollback();} catch (Exception ex) {}
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
            throw e;
        }
    }

    @Override
    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception {
        Statement stmt = null;
        Connection conn = createConnection(bookmarkName);
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
            String sql = "UPDATE " + valuesTable + " SET data=json(?) " +
                    "WHERE name='" + values.getContext() + "'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            String data = mapper.writeValueAsString(values);
            pstmt.setString(1, data);
            Integer updateCount = pstmt.executeUpdate();
            pstmt.close();
            stmt.close();
            conn.close();
            if (updateCount < 1) {
                throw new ConfigurationException("didn't update values - check if context is setup");
            }
            return true;
        } catch (Exception e) {
            try {conn.rollback();} catch (Exception ex) {}
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
            throw e;
        }
    }

    @Override
    public BookmarkMetadata getBookmarkMeta(String bookmarkName)  throws Exception{
        String sql = "SELECT * FROM " + metaTable;
        BookmarkMetadata bookmarkMetadata = null;
        Statement stmt = null;
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + "/" + bookmarkName + ".db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            ResultSet rs = stmt.executeQuery(sql);
            bookmarkMetadata = new BookmarkMetadata();
            while (rs.next()) {
                String name = rs.getString("name");
                if ("config".equals(name)) {
                    String data = rs.getString("data");
                    bookmarkMetadata.setConfig(new ObjectMapper().readValue(data, BookmarkConfig.class));
                } else if ("lock".equals(name)) {
                    bookmarkMetadata.setState(new BookmarkState(rs.getInt("data")));
                } else if ("context".equals(name)) {
                    String data = rs.getString("data");
                    bookmarkMetadata.setContextMetadata(mapper.readValue(
                            data, mapper.getTypeFactory().constructMapType(HashMap.class, String.class, ContextMetadata.class)));
                }
            }
            stmt.close();
            conn.close();
            return bookmarkMetadata;
        } catch (Exception e) {
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
            throw e;
        }
    }

    @Override
    public Boolean updateBookmarkMeta(String bookmarkName, BookmarkMetadata bookmarkMetadata) throws Exception {
        String sql = "";
        if (bookmarkMetadata.getConfig() != null) {
            String configStr = mapper.writeValueAsString(bookmarkMetadata.getConfig());
            sql += "INSERT INTO " + metaTable + " (name, data) VALUES ('config',json('" + configStr + "'))" +
                    " ON CONFLICT(name) DO UPDATE SET data=json_patch(data, json('" + configStr + "'));";
        }
        if (bookmarkMetadata.getState() != null) {
            sql += "INSERT INTO " + metaTable + " (name, data) VALUES ('lock'," + bookmarkMetadata.getState()+ ")" +
                    " ON CONFLICT(name) DO UPDATE SET data=" + bookmarkMetadata.getState() + ";";
        }
        if (bookmarkMetadata.getContextMap() != null) {
            String schemasStr = mapper.writeValueAsString(bookmarkMetadata.getContextMap());
            sql += "INSERT INTO " + metaTable + " (name, data) VALUES ('context',json('" + schemasStr + "'))" +
                    " ON CONFLICT(name) DO UPDATE SET data=json_patch(data, json('" + schemasStr + "'));";
        }
        return executeStatement(bookmarkName, sql);
    }

    @Override
    public Boolean saveBookmarkMeta(String bookmarkName, BookmarkMetadata bookmarkMetadata) throws Exception{
        String sql = "";
        if (bookmarkMetadata.getConfig() != null) {
            String configStr = mapper.writeValueAsString(bookmarkMetadata.getConfig());
            sql += "REPLACE INTO " + metaTable + " (name, data) " + " VALUES ('config',json('"+ configStr +"'));";
        }
        if (bookmarkMetadata.getState() != null) {
            sql += "REPLACE INTO " + metaTable + " (name, data) " + " VALUES ('lock',"+ bookmarkMetadata.getState() +");";
        }
        if (bookmarkMetadata.getContextMap() != null) {
            String schemasStr = mapper.writeValueAsString(bookmarkMetadata.getContextMap());
            sql += "REPLACE INTO " + metaTable + " (name, data) " + " VALUES ('context',json('"+ schemasStr +"'));";
        }
        return executeStatement(bookmarkName, sql);
    }

    @Override
    public ContextMetadata getContextMeta(String bookmarkName, String context) throws Exception {
        String sql = "SELECT json_extract(data,'$." + context + "') FROM " + metaTable + " WHERE name='context'";
        String contextMetaStr = (String)getElement(bookmarkName, sql);
        if (contextMetaStr != null) {
            return mapper.readValue(contextMetaStr, ContextMetadata.class);
        }
        return null;
    }

    public Boolean updateContextMeta(String bookmarkName, ContextMetadata meta) throws Exception {
        String sql = "";
        String schemasStr = "{\"" + meta.getName() + "\":" + mapper.writeValueAsString(meta) + "}";
        sql += "INSERT INTO " + metaTable + " (name, data) VALUES ('context',json('" + schemasStr + "'))" +
                " ON CONFLICT(name) DO UPDATE SET data=json_patch(data, " +
                "json('" +  schemasStr + "'))";
        return executeStatement(bookmarkName, sql);
    }

    public Boolean saveContextMeta(String bookmarkName, ContextMetadata meta) throws Exception {
        String sql = "";
        String schemasStr = "{\"" + meta.getName() + "\":" + mapper.writeValueAsString(meta) + "}";
        sql += "REPLACE INTO " + metaTable + " (name, data) " + " VALUES ('context',json('"+ schemasStr +"'));";
        return executeStatement(bookmarkName, sql);
    }

    public BookmarkState getState(String bookmarkName) throws Exception {
        String sql = "SELECT data FROM " + metaTable + " WHERE name='lock'";
        return new BookmarkState((Integer)getElement(bookmarkName, sql));
    }

    public Boolean switchState(String bookmarkName, BookmarkState state) throws Exception {
        Statement stmt = null;
        Connection conn = createConnection(bookmarkName);
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
            String sql = "UPDATE " + metaTable + " SET data=? WHERE name='lock' AND data!=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, state.getState());
            pstmt.setInt(2, state.getState());
            Integer updateCount = pstmt.executeUpdate();
            pstmt.close();
            stmt.close();
            conn.close();
            if (updateCount < 1) {
                return false;
            }
            return true;
        } catch (Exception e) {
            try {conn.rollback();} catch (Exception ex) {}
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
            throw e;
        }
    }

    @Override
    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutofftime)  throws Exception{
        String sql = "DELETE FROM " + context + " WHERE timestamp<='" + cutofftime.getInstant().toEpochMilli() +"'";
        return executeStatement(bookmarkName, sql);
    }

    @Override
    public Boolean maintenance() throws Exception{
        String sql = "VACUUM;";
        Boolean success = true;
        try {
            List<String> bookmarks = getBookmarkList();
            for (String bookmarkName: bookmarks) {
                if (executeStatement(bookmarkName, sql) != true) {
                    return false;
                }
            }
            return true;
        } catch (SQLException throwables) {
            return false;
        }
    }

    public Double size() throws Exception{
        Double size = 0.0;
        List<String> bookmarks = getBookmarkList();
        for (String bookmarkName: bookmarks) {
            size += size(bookmarkName);
        }
        return size;
    }

    public Double size(String bookmarkName) throws Exception{
        String sql1 = "PRAGMA PAGE_SIZE;";
        Integer page_size = (Integer) getElement(bookmarkName, sql1);
        String sql2 = "PRAGMA PAGE_COUNT;";
        Integer page_count = (Integer) getElement(bookmarkName, sql2);
        return Double.valueOf(1.0) * page_size * page_count;
    }

    @Override
    public Integer recordCount(String bookmarkName, String context) throws Exception{
        String sql = "SELECT count(*) from " + context;
        return (Integer) getElement(bookmarkName, sql);
    }

    @Override
    public Boolean deleteBookmark(String bookmarkName){
        try {
            Files.delete(Paths.get(this.path + "/" + bookmarkName + ".db"));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Boolean validateBookmarkName(String bookmarkName) {
        if (bookmarkName != null && !bookmarkName.matches(".*\\s.*") && bookmarkName.length()>0)    {
            return true;
        }
        return false;
    }

    private Boolean validateContextName(String contextName) {
        if (contextName != null && !contextName.matches(".*\\s.*") && contextName.length()>0)    {
            return true;
        }
        return false;
    }

    private Boolean truncateTable(String bookmarkName, String tableName) throws Exception{
        String sql = "DELETE FROM " + tableName;
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    private Boolean executeStatement(String bookmarkName, String sql) throws Exception {
        Statement stmt = null;
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + "/" + bookmarkName + ".db";
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
            return true;
        } catch (Exception e) {
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
            throw e;
        }
    }

    private Object enableWriteAhead(String bookmarkName) throws Exception{
        String sql = "PRAGMA journal_mode=WAL;";
        return getElement(bookmarkName, sql);
    }

    private Object getElement(String bookmarkName, String sql) throws Exception {
        Object restultObject = null;
        Statement stmt = null;
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + "/" + bookmarkName + ".db";
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                restultObject = rs.getObject(1);
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
        }
        return restultObject;
    }

    private Connection createConnection(String databaseName) {
        Boolean validName = validateBookmarkName(databaseName);
        try {
            if (validName) {
                Connection conn = null;
                String url = "jdbc:sqlite:" + path + "/" + databaseName + ".db";
                conn = DriverManager.getConnection(url);
                return conn;
            }
        } catch (Exception e) {
        }
        throw new IllegalArgumentException("invalid bookmark name or bad configuration");

    }

    private boolean createTxnTable(String bookmarkName, String context) {
        String sql = "CREATE TABLE " + context + " ( " +
                "id INTEGER PRIMARY KEY," +
                "timestamp bigint NOT NULL, " +
                "metrics json NOT NULL " +
                ");" +
                "CREATE INDEX timestamp_idx ON " + context + "(timestamp)";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (Exception throwables) {
            return false;
        }
    }

    private boolean createValueEntry(String bookmarkName, String context) {
        String sql = "INSERT INTO " + valuesTable + "(name,data) values('" + context +"','" + "{}" + "')";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (Exception throwables) {
            return false;
        }
    }

    private boolean createValuesTable(String bookmarkName) {
        String sql = "CREATE TABLE " + valuesTable + " ( " +
                "name TEXT PRIMARY KEY, " +
                "data json NOT NULL" +
                ");";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (Exception throwables) {
            return false;
        }
    }

    private boolean createMetaTable(String bookmarkName) {
        String sql = "CREATE TABLE " + metaTable + " ( " +
                "name string PRIMARY KEY NOT NULL, " +
                "data json NOT NULL" +
                ");";

        sql += "INSERT INTO " + metaTable + " (name, data) " + " VALUES ('config',json('{}'));";
        sql += "INSERT INTO " + metaTable + " (name, data) " + " VALUES ('lock',-1);";
        sql += "INSERT INTO " + metaTable + " (name, data)  VALUES ('context',json('{}'));";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (Exception throwables) {
            return false;
        }
    }
}
