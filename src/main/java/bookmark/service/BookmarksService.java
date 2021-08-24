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

    public Boolean createBookmark(String bookmarkName, Metadata metadata) {
        return bookmarkDao.createBookmark(bookmarkName, metadata);
    }

    public Boolean bookmarkExists(String bookmarkName)
    {
        return bookmarkDao.bookmarkExists(bookmarkName);
    }

    public Boolean updateBookmarkMetadata(String bookmarkName, Metadata metadata) {
        return bookmarkDao.updateMetadata(bookmarkName, metadata);
    }

    public Boolean saveBookmarkMetadata(String bookmarkName, Metadata metadata) {
        return bookmarkDao.saveMetadata(bookmarkName, metadata);
    }

    public Metadata getBookmarkMetadata(String bookmarkName, List<String> query) {
        return bookmarkDao.getMetadata(bookmarkName, query);
    }

    public BookmarkTxns getBookmarkTxn(String bookmarkName, TxnQuery query)
    {
        return bookmarkDao.getBookmarkTxn(bookmarkName, query);
    }

    public Boolean saveBookmarkTxn(String bookmarkName, BookmarkTxns txns) {
        try {
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                if (txns != null && bookmarkDao.saveBookmarkTxn(bookmarkName, txns)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public Boolean updateBookmarkTxn(String bookmarkName, BookmarkTxns txns) {
        try {
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                if (txns != null && bookmarkDao.updateBookmarkTxn(bookmarkName, txns)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public BookmarkValues getBookmarkValues(String bookmarkName, ValueQuery query)
    {
        try {
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                return bookmarkDao.getBookmarkValues(bookmarkName, query);
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) {
        try {
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                return bookmarkDao.updateBookmarkValues(bookmarkName, values);
            }
        } catch (Exception e) {}
        throw new IllegalArgumentException("Invalid Request");
    }

    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) {
        try {
            if (bookmarkDao.bookmarkExists(bookmarkName)) {
                return bookmarkDao.saveBookmarkValues(bookmarkName, values);
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
            Time cutofftimeDefault = (new Time()).addDays(-1 * defaultRetentionDays);
            Time cutofftime;
            List<String> bookmarkList = bookmarkDao.bookmarkList();
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
