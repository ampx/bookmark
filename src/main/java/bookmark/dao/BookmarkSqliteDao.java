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

    private boolean createTxnContext(String bookmarkName, String context) {
        String sql = "CREATE TABLE " + context + " ( " +
                "id INTEGER PRIMARY KEY," +
                "timestamp bigint NOT NULL, " +
                "metrics json NOT NULL " +
                ");" +
                "CREATE INDEX timestamp_idx ON " + context + "(timestamp)";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
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
        } catch (SQLException throwables) {
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
        } catch (SQLException throwables) {
            return false;
        }
    }

    @Override
    public Boolean updateBookmarkTxn(String bookmarkName, BookmarkTxns txns){
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
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException internal_e) {
                internal_e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public Boolean saveBookmarkTxn(String bookmarkName, BookmarkTxns txns){
        truncateTable(bookmarkName, txns.getContext());
        return updateBookmarkTxn(bookmarkName, txns);
    }

    @Override
    public BookmarkTxns getBookmarkTxn(String bookmarkName, TxnQuery txnQuery) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return txns;
    }

    @Override
    public BookmarkValues getBookmarkValues(String bookmarkName, ValueQuery query) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return values;
    }

    @Override
    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) {
        return updateJsonOnKey(bookmarkName, valuesTable, values.getContext(), values.getValues());
    }

    @Override
    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) {
        return saveJsonOnKey(bookmarkName, valuesTable, values.getContext(), values.getValues());
    }

    @Override
    public Metadata getMetadata(String bookmarkName, List<String> options) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return metadata;
    }

    @Override
    public Boolean updateMetadata(String bookmarkName, Metadata metadata) {
        try {
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
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Boolean saveMetadata(String bookmarkName, Metadata metadata) {
        try {
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
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> getJsonByKey(String bookmarkName, String tableName, String key, List<String> values) {
        String sql = null;
        if (values != null) {
            String valuesExtStr = "";
            for (String value: values) {
                valuesExtStr += ", '$.'" + value;
            }
            sql = "SELECT json_extract(stateValues " + valuesExtStr + ") as data";
        } else {
            sql = "SELECT data";
        }
        sql += " FROM " + tableName + " WHERE name='" + key + "';";
        Map<String, Object> valuesMap = null;
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
            while (rs.next()) {
                String metric_str = rs.getString("data");
                if (metric_str != null && metric_str.startsWith("{")) {
                    valuesMap = new ObjectMapper().readValue(metric_str, HashMap.class);
                } else if (metric_str != null) {
                    valuesMap = new HashMap<>(1);
                    if (metric_str.startsWith("[")) {
                        valuesMap.put("data", new ObjectMapper().readValue(metric_str, ArrayList.class));
                    } else {
                        valuesMap.put("data", metric_str);
                    }
                }
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return valuesMap;
    }

    private Boolean updateJsonOnKey(String bookmarkName, String tableName, Object key, Map<String, Object> json) {
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
            String sql = "INSERT INTO " + tableName + " (name, data) VALUES (?,?)" +
                    " ON CONFLICT(name) DO UPDATE SET data=json_patch(data, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int count = 0;

            String data = null;
            if (json != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    data = mapper.writeValueAsString(json);
                } catch (JsonProcessingException e) {
                    return false;
                }
            }
            pstmt.setObject(1, key);
            pstmt.setString(2, data);
            pstmt.setString(4, data);
            pstmt.addBatch();
            pstmt.executeBatch();
            conn.commit();
            pstmt.close();
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException internal_e) {
                internal_e.printStackTrace();
            }
            return false;
        }
    }

    private Boolean saveJsonOnKey(String bookmarkName, String tableName, Object key, Map<String, Object> json) {
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
            String sql = "REPLACE INTO " + tableName + " (name, data) " + " VALUES (?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int count = 0;

            String data = null;
            if (json != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    data = mapper.writeValueAsString(json);
                } catch (JsonProcessingException e) {
                    return false;
                }
            }
            pstmt.setObject(1, key);
            pstmt.setString(2, data);
            pstmt.addBatch();
            pstmt.executeBatch();
            conn.commit();
            pstmt.close();
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException internal_e) {
                internal_e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public Boolean bookmarkExists(String bookmarkName) {
        if (Files.exists(Paths.get(path + bookmarkName + ".db"))) {
            Metadata metadata = getMetadata(bookmarkName, null);
            if (metadata.getLock() >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> bookmarkList() {
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
    public Boolean createBookmark(String bookmarkName, Metadata metadata) {
        if (metadata == null) {
            metadata = new Metadata();
        }
        metadata.setLock(State.notReady);
        if(validBookmarkName(bookmarkName) && !bookmarkExists(bookmarkName) && createMetaTable(bookmarkName)
            && saveMetadata(bookmarkName, metadata) && createValuesTable(bookmarkName) ) {
            enableWriteAhead(bookmarkName);
            if (metadata.getContextList() != null) {
                for (String context: metadata.getContextList()) {
                    createTxnContext(bookmarkName, context);
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
    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutofftime) {
        String sql = "DELETE FROM " + context + " WHERE timestamp<='" + cutofftime.getInstant().toEpochMilli() +"'";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    @Override
    public Boolean maintenance() {
        String sql = "VACUUM;";
        Boolean success = true;
        try {
            List<String> bookmarks = bookmarkList();
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

    public Double size() {
        Double size = 0.0;
        List<String> bookmarks = bookmarkList();
        for (String bookmarkName: bookmarks) {
            size += size(bookmarkName);
        }
        return size;
    }

    public Double size(String bookmarkName) {
        String sql1 = "PRAGMA PAGE_SIZE;";
        Integer page_size = (Integer) getElement(bookmarkName, sql1);
        String sql2 = "PRAGMA PAGE_COUNT;";
        Integer page_count = (Integer) getElement(bookmarkName, sql2);
        return Double.valueOf(1.0) * page_size * page_count;
    }

    @Override
    public Integer recordCount(String bookmarkName, String context) {
        String sql = "SELECT count(*) from " + context;
        return (Integer) getElement(bookmarkName, sql);
    }

    @Override
    public Boolean deleteBookmark(String bookmarkName) {
        try {
            Files.delete(Paths.get(this.path + "/" + bookmarkName + ".db"));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Boolean truncateTable(String bookmarkName, String tableName) {
        String sql = "DELETE FROM " + tableName;
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    private Boolean executeStatement(String bookmarkName, String sql) throws SQLException {
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

    private Object enableWriteAhead(String bookmarkName) {
        String sql = "PRAGMA journal_mode=WAL;";
        return getElement(bookmarkName, sql);
    }

    private Object getElement(String bookmarkName, String sql) {
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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return restultObject;
    }

    private Boolean validBookmarkName(String bookmarkName){
        if (bookmarkName != null && !bookmarkName.matches(".*\\s.*") && bookmarkName.length()>0)    {
            return true;
        }
        return false;
    }

    /*
    public Boolean bookmarkExists(String bookmarkName) {
        return Files.exists(Paths.get(path + bookmarkName + ".db"));
    }

    public List bookmarkList() {
        File[] files = new File(path).listFiles();
        List names = new ArrayList();
        for (File file:files) {
            if (file.getName().endsWith(".db")) {
                String[] split = file.getName().split("\\.");
                names.add(split[0]);
            }
        }
        return names;
    }

    public HashMap<String, HashMap> getBookmarkConfig(String bookmarkName) {
        String sql = "SELECT * FROM " + configTable;
        if (bookmarkName != null) {
            sql += " WHERE name='" + bookmarkName + "'";
        }
        return getConfigMap(bookmarkName, sql);
    }

    public Boolean createBookmark(String bookmarkName, HashMap config) {
        if (validBookmarkName(bookmarkName) && !bookmarkExists(bookmarkName) && createProgressTable(bookmarkName)
                && createStateTable(bookmarkName)
                && addBookmarkState(bookmarkName)
                && createFailedTable(bookmarkName)
                && createConfigTable(bookmarkName)
                && addBookmarkConfig(bookmarkName, config)) {
            enableWriteAhead(bookmarkName);
            return true;
        }
        return false;
    }

    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutofftime) {
        String sql = "DELETE FROM " + context + " WHERE timestamp<='" + cutofftime.mysqlString() +"'";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    public Boolean maintenance() {
        String sql = "VACUUM;";
        Boolean success = true;
        try {
            List<String> bookmarks = bookmarkList();
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

    public Double size() {
        Double size = 0.0;
        List<String> bookmarks = bookmarkList();
        for (String bookmarkName: bookmarks) {
            size += size(bookmarkName);
        }
        return size;
    }

    public Double size(String bookmarkName) {
        String sql1 = "PRAGMA PAGE_SIZE;";
        Integer page_size = (Integer) getElement(bookmarkName, sql1);
        String sql2 = "PRAGMA PAGE_COUNT;";
        Integer page_count = (Integer) getElement(bookmarkName, sql2);
        return Double.valueOf(1.0) * page_size * page_count;
    }

    @Override
    public Integer recordCount(String bookmarkName) {
        return null;
    }

    public Integer getTxnCount(String bookmarkName, String context) {
        String sql = "SELECT COUNT(*) FROM " + context;
        return (Integer) getElement(bookmarkName, sql);
    }

    public Boolean deleteBookmark(String bookmarkName) {
        try {
            Files.delete(Paths.get(this.path + "/" + bookmarkName + ".db"));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Boolean saveTransactions(String bookmarkName, String context, List<Bookmark> bookmarks) {
        if (truncateTable(bookmarkName, context)) {
            updateTransactions(bookmarkName, context, bookmarks);
            return true;
        }
        return null;
    }

    public Boolean updateTransactions(String bookmarkName, String context, List<Bookmark> bookmarks) {
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
            String sql = "INSERT INTO " + context + " (timestamp,metrics) " + " VALUES (?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int count = 0;
            for(Bookmark bookmark : bookmarks){
                String stats_str = "";
                if (bookmark.getMetrics() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        stats_str = mapper.writeValueAsString(bookmark.getMetrics());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                pstmt.setString(1,bookmark.getTimestamp().mysqlString());
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
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException internal_e) {
                internal_e.printStackTrace();
            }
            return false;
        }
    }

    public List<Bookmark> getTransactions(String bookmarkName, String transactionContext,Time starttime, Time endtime, Integer top) {
        String sql = "SELECT * FROM " + transactionContext;
        if (starttime != null || endtime != null || top != null) {
            sql += " WHERE 1=1 ";
            if (starttime != null) {
                sql += " AND timestamp>='" + starttime.mysqlString() + "'";
            }
            if (endtime != null) {
                sql += " AND timestamp<='" + endtime.mysqlString() + "'";
            }
            if (top != null) {
                if (top > 0) {
                    sql += " ORDER BY timestamp DESC LIMIT " + top;
                } else if (top < 0) {
                    sql += " ORDER BY timestamp ASC LIMIT " + -1*top;
                }
            }
        }
        List<Bookmark> bookmarks = null;
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
            bookmarks = new ArrayList<>();
            while (rs.next()) {
                Bookmark bookmark = new Bookmark();
                bookmark.setTimestamp(Time.parse(rs.getString("timestamp"),
                        "uuuu-MM-dd HH:mm:ss.SSS"));
                String metric_str = rs.getString("metrics");
                if (metric_str != null && metric_str.contains("{")) {
                    bookmark.setMetrics(new ObjectMapper().readValue(metric_str, HashMap.class));
                }
                bookmarks.add(bookmark);
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return bookmarks;
    }

    public Map<String, Object> getStateValues(String bookmarkName, String stateEntry) {
        String sql;
        if (stateEntry != null) {
            sql = "SELECT key,value FROM " + stateTable + " WHERE key='" + stateEntry;
        } else {
            sql = "SELECT key,value FROM " + stateTable;
        }
        Map<String, Object> stateValue = new HashMap<>();
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
            while (rs.next()) {
                stateValue.put(rs.getString("key"),rs.getObject("value"));
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stateValue;
    }

    public Boolean updateStateValues(String bookmarkName, Map<String, Object> stateValues) {
        String sql = "";
        for (String key: stateValues.keySet()) {
            sql += "UPSERT " + stateTable + " SET key='" + key +
                    "',value='" + stateValues.get(key) + "' WHERE key='" + key + "';";
        }
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    public Boolean saveStateValues(String bookmarkName, Map<String, Object> stateValues) {
        String sql = "";
        for (String key: stateValues.keySet()) {
            sql += "UPSERT " + stateTable + " SET key='" + key +
                    "',value='" + stateValues.get(key) + "' WHERE key='" + key + "';";
        }
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    public Boolean saveBookmarkConfig(String bookmarkName, HashMap newConfig) {
        String config_str = null;
        if (newConfig != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                config_str = mapper.writeValueAsString(newConfig);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }
        String sql = "UPDATE " + configTable + " SET config='" + config_str +
                "' WHERE name='" + bookmarkName + "'";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    public Boolean updateBookmarkConfig(String bookmarkName, HashMap<String, Object> updateConfig) {
        HashMap<String, HashMap> bookmarkConfig = getBookmarkConfig(bookmarkName);
        if (bookmarkConfig != null) {
            HashMap currentConfig = bookmarkConfig.get(bookmarkName);
            if (currentConfig != null) {
                for (String key : updateConfig.keySet()){
                    currentConfig.put(key, updateConfig.get(key));
                }
            } else {
                currentConfig = updateConfig;
            }
            return saveBookmarkConfig(bookmarkName, currentConfig);
        }
        return null;
    }

    private Boolean addBookmarkState(String bookmarkName) {
        String sql = "INSERT INTO " + stateTable + "(name,timestamp,state) VALUES('" + bookmarkName + "','"
                + Time.now().mysqlString() + "'," + BookmarksService.UNLOCKED + ")";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    private Boolean addBookmarkConfig(String bookmarkName, HashMap config) {
        String configJsonString = null;
        if (config != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                configJsonString = mapper.writeValueAsString(config);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }
        String sql = "INSERT INTO " + configTable + "(name,config) VALUES('" + bookmarkName +  "','"
                + configJsonString + "')";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    private Object getElement(String bookmarkName, String sql) {
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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return restultObject;
    }

    private List<Object> getList(String bookmarkName, String sql) {
        List result = null;
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
            result = new ArrayList();
            while (rs.next()) {
                result.add(rs.getObject(1));
            }
            stmt.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    private HashMap getConfigMap(String bookmarkName, String sql) {
        HashMap result = null;
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
            result = new HashMap();
            while (rs.next()) {
                String name = rs.getString(1);
                HashMap config = null;
                String config_str = rs.getString(2);
                if (config_str != null) {
                    try {
                        config = new ObjectMapper().readValue(config_str, HashMap.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                result.put(name, config);
            }
            stmt.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    private Boolean truncateTable(String bookmarkName, String tableName) {
        String sql = "DELETE FROM " + tableName;
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    private Boolean createTxnTable(String bookmarkName, String context) {
        String sql = "CREATE TABLE " + context + " ( " +
                "timestamp TEXT PRIMARY KEY NOT NULL, " +
                "metrics TEXT NOT NULL " +
                ")";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    private Boolean createFailedTable(String bookmarkName) {
        String sql = "CREATE TABLE " + failedTable + " ( " +
                "timestamp TEXT PRIMARY KEY NOT NULL, " +
                "metrics TEXT NOT NULL " +
                ")";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    private Boolean createStateTable(String bookmarkName) {
        String sql = "CREATE TABLE " + stateTable + " ( " +
                "name TEXT PRIMARY KEY NOT NULL, " +
                "timestamp TEXT NOT NULL ," +
                "state INT NOT NULL)";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    private Boolean createConfigTable(String bookmarkName) {
        String sql = "CREATE TABLE " + configTable + "( " +
                "name TEXT PRIMARY KEY NOT NULL, " +
                "config TEXT)";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    private Object enableWriteAhead(String bookmarkName) {
        String sql = "PRAGMA journal_mode=WAL;";
        return getElement(bookmarkName, sql);
    }

    private Boolean executeStatement(String bookmarkName, String sql) throws SQLException {
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

    private Boolean validBookmarkName(String bookmarkName){
        if (bookmarkName != null && !bookmarkName.matches(".*\\s.*") && bookmarkName.length()>0)    {
            return true;
        }
        return false;
    }*/
}
