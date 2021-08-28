package bookmark.dao;

import bookmark.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.time.model.Time;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Boolean createBookmark(String bookmarkName, Metadata metadata) throws Exception{
        //make sure the bookmark name is valid & bookmark does not already exists
        if (!validateBookmarkName(bookmarkName) || bookmarkExists(bookmarkName)) {
            return false;
        }
        //reject bookmark if there is invalid context name in metadata
        if (metadata.getContextList() != null) {
            for (String context: metadata.getContextList()) {
                if (!validateContextName(context)) {
                    return false;
                }
            }
        }
        //delete if there are any residue files from corrupted bookmark
        deleteBookmark(bookmarkName);
        if (metadata == null) {
            metadata = new Metadata();
        }
        metadata.setLock(State.notReady);
        if(createMetaTable(bookmarkName) && saveMetadata(bookmarkName, metadata)
                && createValuesTable(bookmarkName) ) {
            enableWriteAhead(bookmarkName);
            if (metadata.getContextList() != null) {
                for (String context: metadata.getContextList()) {
                    createContext(bookmarkName, context);
                }
            }
            Metadata unlock = new Metadata();
            unlock.setLock(State.unlocked);
            updateMetadata(bookmarkName, metadata);
            return true;
        }
        return false;
    }

    @Override
    public Boolean createContext(String bookmarkName, String context) throws Exception{
        Boolean success = true;
        //register context name in the metadata if it is not already
        Metadata contextMeta = getMetadata(bookmarkName, new ArrayList(1){{add("contextList");}});
        if (!contextMeta.getContextList().contains(context)) {
            Metadata metadata = new Metadata();
            List<String> contextList = new ArrayList<>(1);
            contextList.add(context);
            metadata.setContextList(contextList);
            success &= updateMetadata(bookmarkName, metadata);
        }
        //create context transaction table if it does not exist already
        String checkTxnTableSql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + context + "';";
        Integer txnTableCount = (Integer) getElement(bookmarkName, checkTxnTableSql);
        if (txnTableCount < 1) {
            success &= createTxnTable(bookmarkName, context);
        }
        //create context value field if it does not exist already
        String checkValueTableSql = "SELECT count(*) FROM " + valuesTable + " WHERE name='" + context + "';";
        Integer valueEntryCount = (Integer) getElement(bookmarkName, checkValueTableSql);
        if (valueEntryCount < 1) {
            success &= createValueEntry(bookmarkName, context);
        }
        return success;
    }

    @Override
    public Boolean bookmarkExists(String bookmarkName) throws Exception{
        //bookmark exists if it has a database created and the lock is not in NotReady state
        if (Files.exists(Paths.get(path + bookmarkName + ".db"))) {
            Metadata metadata = getMetadata(bookmarkName, null);
            if (metadata.getLock() >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean contextExists(String bookmarkName, String context) throws Exception{
        //context exists if context is registered in metadata & value and txn context entries is setup
        Metadata contextMeta = getMetadata(bookmarkName, new ArrayList(1){{add("contextList");}});
        String checkTxnTableSql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + context + "';";
        Integer txnTableCount = (Integer) getElement(bookmarkName, checkTxnTableSql);
        String checkValueTableSql = "SELECT count(*) FROM " + valuesTable + " WHERE name='" + context + "';";
        Integer valueEntryCount = (Integer) getElement(bookmarkName, checkValueTableSql);
        return contextMeta.getContextList().contains(context)
                && txnTableCount > 0 && valueEntryCount > 0;
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
    public List<String> getContextList(String bookmarkName) throws Exception{
        Metadata contextMeta = getMetadata(bookmarkName, new ArrayList(1){{add("contextList");}});
        return contextMeta.getContextList();
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
            for(Bookmark bookmark : txns.getBookmarks()){
                String stats_str = "";
                if (bookmark.getMetrics() != null) {
                    try {
                        stats_str = mapper.writeValueAsString(bookmark.getMetrics());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return false;
                    }
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
            new BookmarkTxns();
            txns.setContext(txnQuery.getContext());
            while (rs.next()) {
                Bookmark bookmark = new Bookmark();
                bookmark.setTimestamp(Time.parse(rs.getLong("timestamp")));
                String metric_str = rs.getString("metrics");
                if (metric_str != null && metric_str.contains("{")) {
                    bookmark.setMetrics(new ObjectMapper().readValue(metric_str, HashMap.class));
                }
                txns.addBookmark(bookmark);
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
    public BookmarkValues getBookmarkValues(String bookmarkName, ValueQuery query)  throws Exception{
        String sql = null;
        if (query != null) {
            String valuesExtStr = "";
            for (String value: query.getValueNames()) {
                valuesExtStr += ", '$.'" + value;
            }
            sql = "SELECT json_extract(stateValues " + valuesExtStr + ") as stateValues";
        } else {
            sql = "SELECT stateValues";
        }
        sql += " FROM " + valuesTable + " WHERE context='" + query.getContext() + "';";
        BookmarkValues values = null;
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
            values = new BookmarkValues();
            while (rs.next()) {
                String metric_str = rs.getString("stateValues");
                if (metric_str != null && metric_str.contains("{")) {
                    values.setValues(new ObjectMapper().readValue(metric_str, HashMap.class));
                }
            }
            stmt.close();
            conn.close();
            return values;
        } catch (Exception e) {
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
            throw e;
        }
    }

    @Override
    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception {
        Statement stmt = null;
        Connection conn = createConnection(bookmarkName);
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
            conn.setAutoCommit(false);
            String sql = "UPDATE " + valuesTable + " SET data=json_patch(data, ?) " +
                    "WHERE name=" + values.getContext();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            String data = null;
            if (values.getValues() != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    data = mapper.writeValueAsString(values.getValues());
                } catch (JsonProcessingException e) {
                    return false;
                }
            }
            pstmt.setString(1, data);
            Integer updateCount = pstmt.executeUpdate();
            pstmt.close();
            stmt.close();
            conn.close();
            if (updateCount < 1) {
                throw new ConfigurationException("didn't update values - check if context is setup");
            }
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
    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception {
        Statement stmt = null;
        Connection conn = createConnection(bookmarkName);
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
            conn.setAutoCommit(false);
            String sql = "UPDATE " + valuesTable + " SET data=json(?) " +
                    "WHERE name=" + values.getContext();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            String data = null;
            if (values.getValues() != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    data = mapper.writeValueAsString(values.getValues());
                } catch (JsonProcessingException e) {
                    return false;
                }
            }
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
    public Metadata getMetadata(String bookmarkName, List<String> options)  throws Exception{
        String sql = "SELECT ";
        if (options != null && options.size() > 0) {
            sql += String.join(",", options);
        } else {
            sql += "*";
        }
        sql += " FROM " + metaTable;
        Metadata metadata = null;
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
            metadata = new Metadata();
            while (rs.next()) {
                String name = rs.getString("name");
                if ("config".equals(name)) {
                    String data = rs.getString("data");
                    metadata.setConfig(new ObjectMapper().readValue(data, HashMap.class));
                } else if ("lock".equals(name)) {
                    metadata.setLock(rs.getInt("data"));
                } else if ("schemas".equals(name)) {
                    String data = rs.getString("data");
                    metadata.setSchema(new ObjectMapper().readValue(data, HashMap.class));
                } else if ("contextList".equals(name)) {
                    String data = rs.getString("data");
                    metadata.setContextList(new ObjectMapper().readValue(data, List.class));
                }
            }
            stmt.close();
            conn.close();
            return metadata;
        } catch (Exception e) {
            try {stmt.close();} catch (Exception ex) {}
            try {conn.close();} catch (Exception ex) {}
            throw e;
        }
    }

    @Override
    public Boolean updateMetadata(String bookmarkName, Metadata metadata) throws Exception {
        String sql = "";
        if (metadata.getConfig() != null) {
            String configStr = mapper.writeValueAsString(metadata.getConfig());
            sql += "INSERT INTO " + metaTable + " (name, data) VALUES ('config'," + configStr + ")" +
                    " ON CONFLICT(name) DO UPDATE SET data=json_patch(data, ?)";
        }
        if (metadata.getLock() != null) {
            sql += "INSERT INTO " + metaTable + " (name, data) VALUES ('lock'," + metadata.getLock() + ")" +
                    " ON CONFLICT(name) DO UPDATE SET data=" + metadata.getLock();
        }
        if (metadata.getSchema() != null) {
            String schemasStr = mapper.writeValueAsString(metadata.getSchema());
            sql += "INSERT INTO " + metaTable + " (name, data) VALUES ('schemas'," + schemasStr + ")" +
                    " ON CONFLICT(name) DO UPDATE SET data=json_patch(data, ?)";
        }
        if (metadata.getContextList() != null) {
            String contextListStr = mapper.writeValueAsString(metadata.getContextList());
            sql += "INSERT INTO " + metaTable + " (name, data) VALUES ('contextList'," + metadata.getContextList() + ")" +
                    " ON CONFLICT(name) DO UPDATE SET data=json_patch(data, [" + contextListStr + "])";
        }
        return executeStatement(bookmarkName, sql);
    }

    @Override
    public Boolean saveMetadata(String bookmarkName, Metadata metadata) throws Exception{
        String sql = "";
        if (metadata.getConfig() != null) {
            String configStr = mapper.writeValueAsString(metadata.getConfig());
            sql += "REPLACE INTO " + metaTable + " (name, data) " + " VALUES ('config',"+ configStr +")";
        }
        if (metadata.getLock() != null) {
            sql += "REPLACE INTO " + metaTable + " (name, data) " + " VALUES ('lock',"+ metadata.getLock() +")";
        }
        if (metadata.getSchema() != null) {
            String schemasStr = mapper.writeValueAsString(metadata.getSchema());
            sql += "REPLACE INTO " + metaTable + " (name, data) " + " VALUES ('schemas',"+ schemasStr +")";
        }
        if (metadata.getContextList() != null) {
            String contextListStr = mapper.writeValueAsString(metadata.getContextList());
            sql += "REPLACE INTO " + metaTable + " (name, data) " + " VALUES ('contextList',"+ contextListStr +")";
        }
        return executeStatement(bookmarkName, sql);
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

        stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
        return true;
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
            if (rs.next()) {
                restultObject = rs.getObject(1);
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            throw e;
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
            }
        } catch (Exception e) {}
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
                ")";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (Exception throwables) {
            return false;
        }
    }
}
