package bookmark.dao;

import bookmark.model.Bookmark;
import bookmark.service.BookmarksService;
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
    String stateTable = "states";
    String metaTable = "meta";
    String defaultContextName = "default";
    Integer batch_size = 200;
    Integer timeoutMillis = 60000;

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

    public boolean createTxnContext(String bookmarkName, String context) {
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

    public boolean createStateTable(String bookmarkName) {
        String sql = "CREATE TABLE " + stateTable + " ( " +
                "context string PRIMARY KEY NOT NULL, " +
                "values json NOT NULL" +
                ")";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    public boolean createMetaTable(String bookmarkName) {
        String sql = "CREATE TABLE " + metaTable + " ( " +
                "name string PRIMARY KEY NOT NULL, " +
                "info json NOT NULL" +
                ")";
        try {
            return executeStatement(bookmarkName, sql);
        } catch (SQLException throwables) {
            return false;
        }
    }

    public Boolean updateTransactions(String bookmarkName, String context, List<Bookmark> bookmarks){
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
    public Boolean bookmarkExists(String bookmarkName) {
        return null;
    }

    @Override
    public List bookmarkList() {
        return null;
    }

    @Override
    public HashMap<String, HashMap> getBookmarkConfig(String bookmarkName) {
        return null;
    }

    @Override
    public Boolean createBookmark(String bookmarkName, HashMap config) {
        return null;
    }

    @Override
    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutofftime) {
        return null;
    }

    @Override
    public Boolean maintenance() {
        return null;
    }

    @Override
    public Double size() {
        return null;
    }

    @Override
    public Double size(String bookmarkName) {
        return null;
    }

    @Override
    public Integer recordCount(String bookmarkName) {
        return null;
    }

    @Override
    public Boolean deleteBookmark(String bookmarkName) {
        return null;
    }

    public Boolean saveTransactions(String bookmarkName, String context, List<Bookmark> bookmarks){
        truncateTable(bookmarkName, context);
        return updateTransactions(bookmarkName, context, bookmarks);
    }

    public List<Bookmark> getTransactions(String bookmarkName, String context,
                                          Time starttime, Time endtime, Integer top) {
        String sql = "SELECT * FROM " + context;
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
                bookmark.setTimestamp(Time.parse(rs.getLong("timestamp")));
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

    @Override
    public Map<String, Object> getStateValues(String bookmarkName, String stateEntry) {
        return null;
    }

    @Override
    public Boolean updateStateValues(String bookmarkName, Map<String, Object> stateValues) {
        return null;
    }

    @Override
    public Boolean saveStateValues(String bookmarkName, Map<String, Object> stateValues) {
        return null;
    }

    @Override
    public Boolean saveBookmarkConfig(String bookmarkName, HashMap<String, Object> newConfig) {
        return null;
    }

    @Override
    public Boolean updateBookmarkConfig(String bookmarkName, HashMap<String, Object> updateConfig) {
        return null;
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
