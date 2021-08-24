package bookmark.dao;

import bookmark.model.Bookmark;
import org.junit.jupiter.api.Test;
import util.time.model.Time;

import javax.naming.ConfigurationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BookmarkSqliteDaoTest {

    String bookmarkName = "testBookmark";
    String context = "testContext";

    public BookmarkSqliteDao createDao() {
        try {
            return new BookmarkSqliteDao("./");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    void updateTransactions() throws ConfigurationException {
        BookmarkSqliteDao dao = createDao();
        dao.deleteBookmark(bookmarkName);
        dao.createBookmark(bookmarkName, null);
        dao.createTxnContext(bookmarkName, context);
        Bookmark bookmark = new Bookmark();
        bookmark.setTimestamp(Time.parse(0));
        HashMap metrics = new HashMap(){{
            put("value0", "metric0");
            put("value1", 1);
            put("value2", true);
        }};
        bookmark.setMetrics(metrics);
        dao.updateTransactions(bookmarkName, context, new ArrayList<Bookmark>(){{add(bookmark);}});
        Bookmark retrievedBookmark = dao.getTransactions(bookmarkName, context, null, null, 5).get(0);
        assertEquals(bookmark.getTimestamp().getInstant().toEpochMilli(),
                retrievedBookmark.getTimestamp().getInstant().toEpochMilli());
        assertEquals(bookmark.getMetrics(),
                retrievedBookmark.getMetrics());
    }

    @Test
    void saveTransactions() {
    }

    @Test
    void updateStateValues() throws ConfigurationException {
        BookmarkSqliteDao dao = createDao();
        dao.deleteBookmark(bookmarkName);
        dao.createBookmark(bookmarkName, null);
        dao.createStateTable(bookmarkName);
        dao.createTxnContext(bookmarkName, context);

        Map<String, Object> metrics = new HashMap(){{
            put("value0", "metric0");
            put("value1", 1);
            put("value2", true);
        }};
        dao.updateBookmarkValues(bookmarkName, context, metrics);
        Map<String, Object> retrievedMetrics = dao.getBookmarkValues(bookmarkName, context, null);
        assertEquals(metrics, retrievedMetrics);
    }


}