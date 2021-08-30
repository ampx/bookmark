package bookmark.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Metadata {
    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public Integer getLock() {
        return lock;
    }

    public void setLock(Integer lock) {
        this.lock = lock;
    }

    public Map<String, Map> getSchema() {
        return schema;
    }

    public void setSchema(HashMap<String, Map> schema) {
        this.schema = schema;
    }

    public List<String> getContextList() {
        return contextList;
    }

    public void setContextList(List<String> contextList) {
        this.contextList = contextList;
    }

    public void setTxnRetentionDays(Integer days) {
        if (config == null) {
            config = new HashMap<>(5);
        }
        config.put("txnRetentionDays", days);
    }

    public Integer getTxnRetentionDays() {
        if (config == null) {
            return null;
        }
        return (Integer) config.get("txnRetentionDays");
    }

    public String getBookmarkName() {
        return bookmarkName;
    }

    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    String bookmarkName;
    Map<String, Object> config;
    Integer lock;
    Map<String, Map> schema;
    List<String> contextList;
}
