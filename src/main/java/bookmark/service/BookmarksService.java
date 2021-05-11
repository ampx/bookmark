package bookmark.service;

import bookmark.Dao.BookmarkDao;
import bookmark.model.Bookmark;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import util.time.model.Time;

import javax.naming.ConfigurationException;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class BookmarksService {

    Timer timer;
    Integer cleanupPeriodMins = 30;
    Integer defaultRetentionDays = 30;
    BookmarkDao bookmarkDao;




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

    public List<Bookmark> getBookmarks(String bookmarkName, Map<String, Object> filters)
    {
        List<Bookmark> bookmarks = null;
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (filters.containsKey("data") && filters.get("data").equals("*")) {
                bookmarks = bookmarkDao.getProgress(bookmarkName, null, null, null);
            } else if (filters.containsKey("top") || filters.containsKey("from") || filters.containsKey("to")) {
                Time from = null;
                Time to = null;
                if (filters.containsKey("from")) {
                    from = Time.parse((String) filters.get("from"));
                }
                if (filters.containsKey("to")) {
                    to = Time.parse((String) filters.get("to"));
                }
                bookmarks = bookmarkDao.getProgress(bookmarkName, from, to, Integer.parseInt((String) filters.get("top")));
            } else { //return last inserted bookmark
                bookmarks = bookmarkDao.getProgress(bookmarkName, null, null, 1);
            }

        } else {
            throw new IllegalArgumentException("Bookmark doesn't exists");
        }
        return bookmarks;
    }

    public Boolean saveBookmark(String bookmarkName, List<Bookmark> bookmarks) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (bookmarks != null && bookmarkDao.saveProgress(bookmarkName, bookmarks)) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Bookmark doesn't exists");
        }
    }

    public Boolean updateBookmark(String bookmarkName, List<Bookmark> bookmarks) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (bookmarks != null && bookmarkDao.updateProgress(bookmarkName, bookmarks)) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Bookmark doesn't exists");
        }
    }

    public List<Bookmark> getFailedBookmarks(String bookmarkName, HashMap<String, Object> filter)
    {
        List<Bookmark> bookmarks = null;
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (filter.containsKey("data") && filter.get("data").equals("*")) {
                bookmarks = bookmarkDao.getFailed(bookmarkName, null, null, null);
            } else if (filter.containsKey("top") || filter.containsKey("from") || filter.containsKey("to")) {
                Time from = null;
                Time to = null;
                if (filter.containsKey("from")) {
                    from = Time.parse((String) filter.get("from"));
                }
                if (filter.containsKey("to")) {
                    to = Time.parse((String) filter.get("to"));
                }
                bookmarks = bookmarkDao.getFailed(bookmarkName, from, to, (Integer) filter.get("top"));
            } else {
                bookmarks = bookmarkDao.getFailed(bookmarkName, null, null, 1);
            }
        } else {
            throw new IllegalArgumentException("Bookmark doesn't exists");
        }
        return bookmarks;
    }

    public Map saveFailedBookmarks(String bookmarkName, List<Bookmark> bookmarks) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (bookmarks != null && bookmarkDao.saveFailed(bookmarkName, bookmarks)) {
                return Collections.singletonMap("success", true);
            } else {
                return Collections.singletonMap("success", false);
            }
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
    }

    public Map updateFailedBookmarks(String bookmarkName, List<Bookmark> bookmarks) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (bookmarks != null && bookmarkDao.updateFailed(bookmarkName, bookmarks)) {
                return Collections.singletonMap("success", true);
            } else {
                return Collections.singletonMap("success", false);
            }
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
    }








    public Map getBookmarkState(String bookmarkName)
    {
        List<Bookmark> bookmarks = null;
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            return Collections.singletonMap("state", bookmarkDao.getState(bookmarkName));
        }
        throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
    }

    public Map saveBookmarkState(String bookmarkName, Map stateMap) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (stateMap != null && stateMap.containsKey("state")
                    && bookmarkDao.updateState(bookmarkName, (Integer)stateMap.get("state"))) {
                return Collections.singletonMap("success", true);
            } else {
                return Collections.singletonMap("success", false);
            }
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
    }

    public Map updateBookmarkState(String bookmarkName, Map stateMap) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (stateMap != null && stateMap.containsKey("state")
                    && bookmarkDao.updateState(bookmarkName, (Integer)stateMap.get("state"))) {
                return Collections.singletonMap("success", true);
            } else {
                return Collections.singletonMap("success", false);
            }
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
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
                bookmarkDao.cleanProgress(bookmarkName, cutofftime);
                bookmarkDao.cleanFailed(bookmarkName, cutofftime);
            }
        }
    }
}
