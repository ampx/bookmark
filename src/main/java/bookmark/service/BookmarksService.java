package bookmark.service;

import bookmark.Dao.BookmarkDao;
import util.time.model.Time;

import java.util.*;

public class BookmarksService {

    Timer timer;
    Integer cleanupPeriodMins = 30;
    Integer defaultRetentionDays = 30;
    BookmarkDao bookmarkDao;

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
