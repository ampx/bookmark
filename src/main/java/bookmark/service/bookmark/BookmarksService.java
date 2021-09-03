package bookmark.service.bookmark;

import bookmark.dao.BookmarkDao;
import bookmark.model.meta.BookmarkConfig;
import bookmark.model.meta.BookmarkMetadata;
import bookmark.model.meta.BookmarkState;
import bookmark.model.meta.ContextMetadata;
import bookmark.model.txn.Bookmark;
import bookmark.model.txn.BookmarkTxns;
import bookmark.model.txn.TxnQuery;
import bookmark.model.value.BookmarkValues;
import bookmark.model.value.ValueQuery;
import util.time.model.Time;

import java.util.*;

public class BookmarksService {

    Timer timer;
    Integer cleanupPeriodMins = 30;
    Integer defaultRetentionDays = 30;
    Integer maintenancePeriodDays = 7;
    BookmarkDao bookmarkDao;
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

    public Boolean createBookmark(String bookmarkName, BookmarkMetadata bookmarkMetadata) {
        try {
            if (!bookmarkExists(bookmarkName)) {
                if (bookmarkMetadata == null) {
                    bookmarkMetadata = getDefaultMeta();
                }
                return bookmarkDao.createBookmark(bookmarkName, bookmarkMetadata);
            }
            return false;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public List<String> getBookmarkList()
    {
        try {
            return bookmarkDao.getBookmarkList();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public Boolean bookmarkExists(String bookmarkName)
    {
        try {
            return bookmarkDao.bookmarkExists(bookmarkName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public Boolean createContext(String bookmarkName, String context) {
        try {
            return bookmarkDao.createContext(bookmarkName, context);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public Set<String> getContextList(String bookmarkName)
    {
        try {
            return bookmarkDao.getContextList(bookmarkName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public Boolean contextExists(String bookmarkName, String context) {
        try{
            return bookmarkDao.contextExists(bookmarkName, context);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public Boolean updateBookmarkMetadata(String bookmarkName, BookmarkMetadata bookmarkMetadata) {
        try {
            return bookmarkDao.updateBookmarkMeta(bookmarkName, bookmarkMetadata);
        } catch (Exception e) {
            try {
                return validateBookmarkSetup(bookmarkName, getDefaultMeta()) &&
                        bookmarkDao.updateBookmarkMeta(bookmarkName, bookmarkMetadata);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Request");
            }
        }
    }

    public Boolean saveBookmarkMetadata(String bookmarkName, BookmarkMetadata bookmarkMetadata) {
        try {
            return bookmarkDao.saveBookmarkMeta(bookmarkName, bookmarkMetadata);
        } catch (Exception e) {
            try {
                return validateBookmarkSetup(bookmarkName, getDefaultMeta()) &&
                        bookmarkDao.saveBookmarkMeta(bookmarkName, bookmarkMetadata);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Request");
            }
        }
    }

    public BookmarkMetadata getBookmarkMetadata(String bookmarkName) {
        try {
            return bookmarkDao.getBookmarkMeta(bookmarkName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public BookmarkTxns getBookmarkTxn(String bookmarkName, TxnQuery query)
    {
        try {
            return bookmarkDao.getBookmarkTxn(bookmarkName, query);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public Boolean saveBookmarkTxn(String bookmarkName, BookmarkTxns txns) {
        try {
            return bookmarkDao.saveBookmarkTxn(bookmarkName, txns);
        } catch (Exception e) {
            try {
                return validateBookmarkSetup(bookmarkName, getDefaultMeta()) &&
                        validateTxnContextSetup(bookmarkName, txns.getContext(), txns) &&
                        bookmarkDao.saveBookmarkTxn(bookmarkName, txns);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Request");
            }
        }
    }

    public Boolean updateBookmarkTxn(String bookmarkName, BookmarkTxns txns) {
        if (txns.getContext() == null) {
            txns.setContext(defaultContextName);
        }
        try {
            return bookmarkDao.updateBookmarkTxn(bookmarkName, txns);
        } catch (Exception e) {
            try {
                return validateBookmarkSetup(bookmarkName, getDefaultMeta()) &&
                        validateTxnContextSetup(bookmarkName, txns.getContext(), txns) &&
                        bookmarkDao.updateBookmarkTxn(bookmarkName, txns);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Request");
            }
        }
    }

    public BookmarkValues getBookmarkValues(String bookmarkName, ValueQuery query)
    {
        try {
            return bookmarkDao.getBookmarkValues(bookmarkName, query);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) {
        if (values.getContext() == null) {
            values.setContext(defaultContextName);
        }
        try {
            return bookmarkDao.updateBookmarkValues(bookmarkName, values);
        } catch (Exception e) {
            try {
                return validateBookmarkSetup(bookmarkName, getDefaultMeta()) &&
                        validateContextSetup(bookmarkName, values.getContext()) &&
                        bookmarkDao.updateBookmarkValues(bookmarkName, values);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Request");
            }
        }
    }

    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) {
        if (values.getContext() == null) {
            values.setContext(defaultContextName);
        }
        try {
            return bookmarkDao.saveBookmarkValues(bookmarkName, values);
        } catch (Exception e) {
            try {
                return validateBookmarkSetup(bookmarkName, getDefaultMeta()) &&
                        validateContextSetup(bookmarkName, values.getContext()) &&
                        bookmarkDao.saveBookmarkValues(bookmarkName, values);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Request");
            }
        }
    }

    public BookmarkState getState(String bookmarkName) {
        try {
            return bookmarkDao.getState(bookmarkName);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public Boolean updateState(String bookmarkName, BookmarkState state) {
        try {
            return bookmarkDao.switchState(bookmarkName, state);
        } catch (Exception ex) {
            try {
                return validateBookmarkSetup(bookmarkName, getDefaultMeta()) &&
                        bookmarkDao.switchState(bookmarkName, state);
            } catch (Exception e) {}
            throw new IllegalArgumentException("Invalid Request");
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
            Time cutOffTime = null;
            List<String> bookmarkList = bookmarkDao.getBookmarkList();
            List<String> configQuery = new ArrayList(1){{add("config");}};
            for (String bookmarkName : bookmarkList) {
                try {
                    BookmarkMetadata bookmarkMetadata = bookmarkDao.getBookmarkMeta(bookmarkName);
                    if (bookmarkMetadata != null) {
                        BookmarkConfig config = bookmarkMetadata.getConfig();
                        if (config.defaultRetentionPolicy()) {
                            cutOffTime = (new Time()).addDays(-1 * defaultRetentionDays);
                        } else if (!config.ttlDisabled()) {
                            cutOffTime = (new Time()).addDays(-1 * config.getTxnRetentionPolicy());
                        }
                    }
                    Set<String> contextList = bookmarkMetadata.getContextList();
                    if (contextList != null) {
                        for (String context: contextList) {
                            bookmarkDao.cleanTxnRecords(bookmarkName, context, cutOffTime);
                        }
                    }
                } catch (Exception e) {}
            }
        }
    }

    private void inferSchema(BookmarkTxns txns, ContextMetadata contextMeta) {
        if (txns != null && txns.getBookmarks().size() > 0) {
            for (Bookmark bookmark : txns.getBookmarks()) {
                HashMap<String, Object> metrics = bookmark.getMetrics();
                for (String key : metrics.keySet()) {
                    if (metrics.get(key) != null) {
                        if (metrics.get(key) instanceof Long || metrics.get(key) instanceof Integer) {
                            contextMeta.addIntTxnField(key);
                        } else if (metrics.get(key) instanceof Double || metrics.get(key) instanceof Float) {
                            contextMeta.addFloatTxnField(key);
                        } else if (metrics.get(key) instanceof Boolean) {
                            contextMeta.addBoolTxnField(key);
                        } else {
                            contextMeta.addStringTxnField(key);
                        }
                    }
                }
            }
        }
    }

    private Boolean validateBookmarkSetup(String bookmarkName, BookmarkMetadata bookmarkMetadata) throws Exception{
        Boolean success = true;
        if (!bookmarkExists(bookmarkName)) {
            success &= bookmarkDao.createBookmark(bookmarkName, bookmarkMetadata);
        }
        return success;
    }

    private Boolean validateTxnContextSetup(String bookmarkName, String context, BookmarkTxns txn) throws Exception{
        Boolean success = true;
        if (context!= null && !bookmarkDao.contextExists(bookmarkName, context)) {
            BookmarkMetadata bookmarkMetadata = bookmarkDao.getBookmarkMeta(bookmarkName);
            if (bookmarkMetadata.getContextMap() == null || bookmarkMetadata.getContextMetadata(context) == null) {
                ContextMetadata metadata = new ContextMetadata();
                metadata.setName(context);
                inferSchema(txn, metadata);
                bookmarkDao.updateContextMeta(bookmarkName, metadata);
            }
            success &= bookmarkDao.createContext(bookmarkName, context);
        }
        return success;
    }

    private Boolean validateContextSetup(String bookmarkName, String context) throws Exception{
        Boolean success = true;
        if (context!= null && !bookmarkDao.contextExists(bookmarkName, context)) {
            success &= bookmarkDao.createContext(bookmarkName, context);
        }
        return success;
    }

    public BookmarkService createInstantService(String bookmarkName) {
        BookmarkService bookmarkService = new BookmarkService();
        //bookmarkInstanceService.setBookmarkName(bookmarkName);
        //bookmarkInstanceService.setBookmarksService(this);
        return bookmarkService;
    }

    public ContextService createTransactionInstanceService(String bookmarkName, String context) {
        ContextService contextService = new ContextService();
        contextService.setBookmarkInstanceService(createInstantService(bookmarkName));
        contextService.setContextName(context);
        return contextService;
    }

    public BookmarkMetadata getDefaultMeta() {
        BookmarkMetadata bookmarkMetadata = new BookmarkMetadata();
        BookmarkConfig config = new BookmarkConfig();
        config.setTxnRetentionPolicy(defaultRetentionDays);
        bookmarkMetadata.setConfig(config);
        return bookmarkMetadata;
    }
}