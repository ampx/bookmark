package bookmark.service;

import bookmark.dao.BookmarkDao;
import bookmark.model.Bookmark;
import util.time.model.Time;

import java.util.*;

public class BookmarksService {

    Timer timer;
    Integer cleanupPeriodMins = 30;
    Integer defaultRetentionDays = 30;
    BookmarkDao bookmarkDao;

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
        return bookmarkDao.bookmarkList();
    }

    public Boolean createBookmark(String name, HashMap<String, Object> config) {
        return bookmarkDao.createBookmark(name, config);
    }

    public Boolean bookmarkExists(String bookmarkName)
    {
        return bookmarkDao.bookmarkExists(bookmarkName);
    }

    public Boolean updateBookmarkConfig(String bookmarkName, HashMap<String, Object> config) {
        return bookmarkDao.updateBookmarkConfig(bookmarkName, config);
    }

    public Boolean saveBookmarkConfig(String bookmarkName, HashMap<String, Object> config) {
        return bookmarkDao.saveBookmarkConfig(bookmarkName, config);
    }

    public List<Bookmark> getTransactions(String bookmarkName, String transactionContext, Map<String, Object> filters)
    {
        try {
            List<Bookmark> bookmarks = null;
            if (filters == null) {
                filters = new HashMap();
            }
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                if (filters.containsKey("data") && filters.get("data").equals("*")) {
                    bookmarks = bookmarkDao.getTransactions(bookmarkName, transactionContext, null, null, null);
                } else if (filters.containsKey("top") || filters.containsKey("from") || filters.containsKey("to")) {
                    Time from = null;
                    Time to = null;
                    if (filters.containsKey("from")) {
                        from = Time.parse((String) filters.get("from"));
                    }
                    if (filters.containsKey("to")) {
                        to = Time.parse((String) filters.get("to"));
                    }
                    bookmarks = bookmarkDao.getTransactions(bookmarkName, transactionContext, from, to, Integer.parseInt((String) filters.get("top")));
                } else { //return last inserted bookmark
                    bookmarks = bookmarkDao.getTransactions(bookmarkName, transactionContext, null, null, 1);
                }
                return bookmarks;
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public Boolean saveTransactions(String bookmarkName, String transactionContext, List<Bookmark> bookmarks) {
        try {
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                if (bookmarks != null && bookmarkDao.saveTransactions(bookmarkName, transactionContext, bookmarks)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public Boolean updateTransactions(String bookmarkName, String transactionContext, List<Bookmark> bookmarks) {
        try {
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                if (bookmarks != null && bookmarkDao.updateTransactions(bookmarkName, transactionContext, bookmarks)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public Map<String, Object> getStateValues(String bookmarkName, Map<String, Object> filters)
    {
        try {
            if (filters == null) {
                filters = new HashMap();
            }
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                return bookmarkDao.getStateValues(bookmarkName, (String) filters.get("data"));
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public Boolean updateStateValues(String bookmarkName, Map<String, Object> stateValues) {
        try {
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                return bookmarkDao.updateStateValues(bookmarkName, stateValues);
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public Boolean saveStateValues(String bookmarkName, Map<String, Object> stateValues) {
        try {
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                return bookmarkDao.saveStateValues(bookmarkName, stateValues);
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
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
            HashMap<String, HashMap> configs = bookmarkDao.getBookmarkConfig(null);
            Time cutofftimeDefault = (new Time()).addDays(-1 * defaultRetentionDays);
            Time cutofftime;
            for (String bookmarkName : configs.keySet()) {
                HashMap config = configs.get(bookmarkName);
                if (config != null && config.containsKey("retentionDays")) {
                    try {
                        cutofftime = (new Time()).addDays(-1L * Long.valueOf((Integer) config.get("retentionDays")));
                    } catch (Exception e) {
                        cutofftime = cutofftimeDefault;
                    }
                } else cutofftime = cutofftimeDefault;
                bookmarkDao.cleanTxnRecords(bookmarkName, null, cutofftime);
            }
        }
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
