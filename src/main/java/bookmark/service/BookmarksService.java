package bookmark.service;

import bookmark.dao.BookmarkDao;
import bookmark.model.*;
import util.time.model.Time;

import java.util.*;

public class BookmarksService {

    Timer timer;
    Integer cleanupPeriodMins = 30;
    Integer defaultRetentionDays = 30;
    Integer maintenancePeriodDays = 7;
    BookmarkDao bookmarkDao;
    Metadata defaultMeta;
    String defaultContextName = "default";

    public void setCleanupPeriodMins(Integer cleanupPeriodMins) {
        this.cleanupPeriodMins = cleanupPeriodMins;
    }

    public void setDefaultRetentionDays(Integer defaultRetentionDays) {
        this.defaultRetentionDays = defaultRetentionDays;
    }

    public void setBookmarkDao(BookmarkDao bookmarkDao) {
        this.bookmarkDao = bookmarkDao;
    }

    public List<String> getBookmarkList()
    {
        return bookmarkDao.getBookmarkList();
    }

    public Boolean bookmarkExists(String bookmarkName)
    {
        return bookmarkDao.bookmarkExists(bookmarkName);
    }

    public Boolean updateBookmarkMetadata(String bookmarkName, Metadata metadata) {
        try {
            return bookmarkDao.updateMetadata(bookmarkName, metadata);
        } catch (Exception e) {
            return validateBookmarkSetup(bookmarkName, defaultMeta) &&
                    bookmarkDao.updateMetadata(bookmarkName, metadata);
        }
    }

    public Boolean saveBookmarkMetadata(String bookmarkName, Metadata metadata) {
        try {
            return bookmarkDao.saveMetadata(bookmarkName, metadata);
        } catch (Exception e) {
            return validateBookmarkSetup(bookmarkName, defaultMeta) &&
                    bookmarkDao.saveMetadata(bookmarkName, metadata);
        }
    }

    public Metadata getBookmarkMetadata(String bookmarkName, List<String> query) {
        return bookmarkDao.getMetadata(bookmarkName, query);
    }

    public BookmarkTxns getBookmarkTxn(String bookmarkName, TxnQuery query)
    {
        return bookmarkDao.getBookmarkTxn(bookmarkName, query);
    }

    public Boolean saveBookmarkTxn(String bookmarkName, BookmarkTxns txns) {
        if (txns.getContext() == null) {
            txns.setContext(defaultContextName);
        }
        try {
            return bookmarkDao.saveBookmarkTxn(bookmarkName, txns);
        } catch (Exception e) {
            return validateBookmarkSetup(bookmarkName, defaultMeta) &&
                    validateTxnContextSetup(bookmarkName, txns.getContext(), txns) &&
                    bookmarkDao.saveBookmarkTxn(bookmarkName, txns);
        }
    }

    public Boolean updateBookmarkTxn(String bookmarkName, BookmarkTxns txns) {
        if (txns.getContext() == null) {
            txns.setContext(defaultContextName);
        }
        try {
            return bookmarkDao.updateBookmarkTxn(bookmarkName, txns);
        } catch (Exception e) {
            return validateBookmarkSetup(bookmarkName, defaultMeta) &&
                    validateTxnContextSetup(bookmarkName, txns.getContext(), txns) &&
                    bookmarkDao.updateBookmarkTxn(bookmarkName, txns);
        }
    }

    public BookmarkValues getBookmarkValues(String bookmarkName, ValueQuery query)
    {
        try {
            return bookmarkDao.getBookmarkValues(bookmarkName, query);
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) {
        if (values.getContext() == null) {
            values.setContext(defaultContextName);
        }
        try {
            return bookmarkDao.updateBookmarkValues(bookmarkName, values);
        } catch (Exception e) {
            return validateBookmarkSetup(bookmarkName, defaultMeta) &&
                    validateValueContextSetup(bookmarkName, values.getContext()) &&
                    bookmarkDao.updateBookmarkValues(bookmarkName, values);
        }
    }

    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) {
        if (values.getContext() == null) {
            values.setContext(defaultContextName);
        }
        try {
            return bookmarkDao.saveBookmarkValues(bookmarkName, values);
        } catch (Exception e) {
            return validateBookmarkSetup(bookmarkName, defaultMeta) &&
                    validateValueContextSetup(bookmarkName, values.getContext()) &&
                    bookmarkDao.saveBookmarkValues(bookmarkName, values);
        }
    }

    public void enableCleanup(){
        if (timer != null) {
            disableCleanup();
        }
        timer = new Timer();
        timer.schedule(new BookmarkCleanup(), 0, 1000 * 60 * this.cleanupPeriodMins);
    }

    public void disableCleanup() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private class BookmarkCleanup extends TimerTask {
        public void run(){
            Time cutofftimeDefault = (new Time()).addDays(-1 * defaultRetentionDays);
            Time cutofftime;
            List<String> bookmarkList = bookmarkDao.getBookmarkList();
            List<String> configQuery = new ArrayList(1){{add("config");}};
            for (String bookmarkName : bookmarkList) {
                Metadata metadata = bookmarkDao.getMetadata(bookmarkName, configQuery);
                if (metadata != null && metadata.getConfig() != null && metadata.getConfig().containsKey("retentionDays")) {
                    try {
                        cutofftime = (new Time()).addDays(-1L * Long.valueOf((Integer) metadata.getConfig().get("retentionDays")));
                    } catch (Exception e) {
                        cutofftime = cutofftimeDefault;
                    }
                } else cutofftime = cutofftimeDefault;
                bookmarkDao.cleanTxnRecords(bookmarkName, null, cutofftime);
            }
        }
    }

    private HashMap<String, String> inferSchema(BookmarkTxns txns) {
        HashMap<String, String> schema = new HashMap<>();
        if (txns != null && txns.getBookmarks().size() > 0) {
            for (Bookmark bookmark : txns.getBookmarks()) {
                HashMap<String, Object> metrics = bookmark.getMetrics();
                for (String key : metrics.keySet()) {
                    if (metrics.get(key) != null) {
                        if (metrics.get(key) instanceof Long || metrics.get(key) instanceof Integer) {
                            schema.put(key, "INT");
                        } else if (metrics.get(key) instanceof Double || metrics.get(key) instanceof Float) {
                            schema.put(key, "FLOAT");
                        } else if (metrics.get(key) instanceof Boolean) {
                            schema.put(key, "BOOL");
                        } else {
                            schema.put(key, "STRING");
                        }
                    }
                }
            }
        }
        return null;
    }

    public Boolean createBookmark(String bookmarkName, Metadata metadata) {
        if (!bookmarkExists(bookmarkName)) {
            return bookmarkDao.createBookmark(bookmarkName, metadata);
        }
        return false;
    }

    private Boolean validateBookmarkSetup(String bookmarkName, Metadata metadata) {
        Boolean success = true;
        if (!bookmarkExists(bookmarkName)) {
            success &= bookmarkDao.createBookmark(bookmarkName, metadata);
        }
        return success;
    }

    private Boolean validateTxnContextSetup(String bookmarkName, String context, BookmarkTxns txn) {
        Boolean success = true;
        if (context!= null && !bookmarkDao.contextExists(bookmarkName, context)) {
            Metadata metadata = bookmarkDao.getMetadata(bookmarkName, null);
            if (metadata.getSchema() == null || metadata.getSchema().get(context) == null) {
                Metadata schema = new Metadata();
                HashMap<String, Map> schemaMap = new HashMap(1) {{put(context, inferSchema(txn));}};
                schema.setSchema(schemaMap);
                bookmarkDao.updateMetadata(bookmarkName, schema);
            }
            success &= bookmarkDao.createTxnContext(bookmarkName, context);
        }
        return success;
    }

    private Boolean validateValueContextSetup(String bookmarkName, String context) {
        Boolean success = true;
        if (context!= null && !bookmarkDao.valueContextExists(bookmarkName, context)) {
            success &= bookmarkDao.createValueContext(bookmarkName, context);
        }
        return success;
    }

    public BookmarkInstanceService createInstantService(String bookmarkName) {
        BookmarkInstanceService bookmarkInstanceService = new BookmarkInstanceService();
        bookmarkInstanceService.setBookmarkName(bookmarkName);
        bookmarkInstanceService.setBookmarksService(this);
        return bookmarkInstanceService;
    }

    public BookmarkTransactionInstanceService createTransactionInstanceService(String bookmarkName, String context) {
        BookmarkTransactionInstanceService bookmarkTransactionInstanceService = new BookmarkTransactionInstanceService();
        bookmarkTransactionInstanceService.setBookmarkInstanceService(createInstantService(bookmarkName));
        bookmarkTransactionInstanceService.setContextName(context);
        return bookmarkTransactionInstanceService;
    }
}
