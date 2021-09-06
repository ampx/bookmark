package bookmark.service;

import bookmark.dao.BookmarkSqliteDao;
import bookmark.model.meta.BookmarkConfig;
import bookmark.model.meta.BookmarkMetadata;
import bookmark.model.meta.BookmarkState;
import bookmark.model.meta.ContextMetadata;
import bookmark.model.txn.Bookmark;
import bookmark.model.txn.BookmarkTxns;
import bookmark.model.txn.TxnQuery;
import bookmark.model.value.BookmarkValues;
import bookmark.service.bookmark.BookmarksService;
import org.junit.jupiter.api.Test;
import util.time.model.Time;

import javax.naming.ConfigurationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookmarksServiceIT {

    public BookmarksService getBookmarksService() {
        try {
            BookmarkSqliteDao dao = new BookmarkSqliteDao("./");
            BookmarksService bookmarksService = new BookmarksService();
            bookmarksService.setBookmarkDao(dao);
            List<String> bookmarks = bookmarksService.getBookmarkList();
            if (bookmarks != null) {
                for (String bookmark: bookmarks) {
                    dao.deleteBookmark(bookmark);
                }
            }
            return bookmarksService;
        } catch (ConfigurationException e) {
            System.exit(1);
            return null;
        }
    }

    @Test
    public void creationTest() {
        BookmarksService bookmarksService = getBookmarksService();
        bookmarksService.createBookmark("bookmark-defaultMeta", null);

        BookmarkTxns bookmarkTxns = new BookmarkTxns();
        bookmarkTxns.add(new Bookmark(){{put("a","b");}});
        bookmarksService.saveBookmarkTxn("bookmark-autoTxnSave", bookmarkTxns);
        bookmarksService.updateBookmarkTxn("bookmark-autoTxnUpdate", bookmarkTxns);

        BookmarkValues values = new BookmarkValues(){{put("a","b");}};
        bookmarksService.saveBookmarkValues("bookmark-autoValueSave", values);
        bookmarksService.updateBookmarkValues("bookmark-autoValueUpdate", values);

        bookmarksService.updateState("bookmark-autoState", new BookmarkState(3));

        BookmarkMetadata bookmarkMetadata = new BookmarkMetadata();
        bookmarkMetadata.setState(BookmarkState.notReadyState());

        List<String> bookmarkList = bookmarksService.getBookmarkList();
        assertTrue(bookmarkList.contains("bookmark-defaultMeta"));
        assertTrue(bookmarkList.contains("bookmark-autoTxnSave"));
        assertTrue(bookmarkList.contains("bookmark-autoTxnUpdate"));
        assertTrue(bookmarkList.contains("bookmark-autoValueSave"));
        assertTrue(bookmarkList.contains("bookmark-autoValueUpdate"));
        assertTrue(bookmarkList.contains("bookmark-autoState"));
        assertTrue(bookmarkList.size() == 6);
    }

    @Test
    public void testMetaConfiguration() {
        String bookmarkName = "bookmark";
        BookmarksService bookmarksService = getBookmarksService();
        bookmarksService.createBookmark(bookmarkName, null);

        BookmarkConfig config = new BookmarkConfig();
        config.setTxnRetentionPolicy(99);
        assertTrue(bookmarksService.updateBookmarkConfig(bookmarkName, config));
        assertTrue(bookmarksService.updateState(bookmarkName, new BookmarkState(55)));
        assertFalse(bookmarksService.updateState(bookmarkName, new BookmarkState(55)));
        assertTrue(bookmarksService.createContext(bookmarkName, new ContextMetadata("newContext")));
        BookmarkMetadata metadata = bookmarksService.getBookmarkMetadata(bookmarkName);

        assertTrue(metadata.getConfig().getTxnRetentionPolicy().equals(99));
        assertTrue(metadata.getState().getState().equals(55));
        assertTrue(metadata.getContextList().size() == 1);
        assertTrue(metadata.getContextList().contains("newContext"));
    }

    @Test
    public void saveBookmarkTxn() {
        String bookmarkName = "bookmark";
        BookmarksService bookmarksService = getBookmarksService();

        //insert 2 bookmarks with unique values
        BookmarkTxns olderTxns = new BookmarkTxns();
        olderTxns.add(new Bookmark(Time.parse("2021-08-01T00:00:00.000Z")){{
            put("intVal", 1);
            put("floatVal", 2.0);
        }});
        assertTrue(bookmarksService.saveBookmarkTxn(bookmarkName, olderTxns));

        //validate that first bookmark has been saved
        BookmarkTxns retrievedTxns = bookmarksService.getOldestBookmarkTxn(bookmarkName, 10);
        assertTrue(retrievedTxns.size() == 1);
        assertTrue(retrievedTxns.get(0).size() == 2);
        assertTrue(retrievedTxns.get(0).get("intVal").equals(1));
        assertTrue(retrievedTxns.get(0).get("floatVal").equals(2.0));
        assertTrue(retrievedTxns.get(0).getTimestamp().toString().equals(olderTxns.get(0).getTimestamp().toString()));

        BookmarkTxns newerTxns = new BookmarkTxns();
        newerTxns.add(new Bookmark(Time.parse("2021-09-01T00:00:00.000Z")){{
            put("string", "string");
            put("boolVal", true);
        }});
        assertTrue(bookmarksService.saveBookmarkTxn(bookmarkName, newerTxns));

        //validate newer bookmark has been saved and older once have been deleted
        retrievedTxns = bookmarksService.getNewestBookmarkTxn(bookmarkName, 10);
        assertTrue(retrievedTxns.size() == 1);
        assertTrue(retrievedTxns.get(0).size() == 2);
        assertTrue(retrievedTxns.get(0).get("string").equals("string"));
        assertTrue(retrievedTxns.get(0).get("boolVal").equals(true));
        assertTrue(retrievedTxns.get(0).getTimestamp().toString().equals(newerTxns.get(0).getTimestamp().toString()));
    }

    @Test
    public void updateBookmarkTxn() {
        String bookmarkName = "bookmark";
        BookmarksService bookmarksService = getBookmarksService();

        //insert 2 bookmarks with unique values
        BookmarkTxns olderTxns = new BookmarkTxns();
        olderTxns.add(new Bookmark(Time.parse("2021-08-01T00:00:00.000Z")){{
            put("intVal", 1);
            put("floatVal", 2.0);
        }});
        assertTrue(bookmarksService.updateBookmarkTxn(bookmarkName, olderTxns));
        BookmarkTxns newerTxns = new BookmarkTxns();
        newerTxns.add(new Bookmark(Time.parse("2021-09-01T00:00:00.000Z")){{
            put("string", "string");
            put("boolVal", true);
        }});
        assertTrue(bookmarksService.updateBookmarkTxn(bookmarkName, newerTxns));

        //validate that older bookmark still exists and has not been overwritten
        BookmarkTxns retrievedTxns = bookmarksService.getOldestBookmarkTxn(bookmarkName, 1);
        assertTrue(retrievedTxns.size() == 1);
        assertTrue(retrievedTxns.get(0).size() == 2);
        assertTrue(retrievedTxns.get(0).get("intVal").equals(1));
        assertTrue(retrievedTxns.get(0).get("floatVal").equals(2.0));
        assertTrue(retrievedTxns.get(0).getTimestamp().toString().equals(olderTxns.get(0).getTimestamp().toString()));

        //validate newer bookmark exists
        retrievedTxns = bookmarksService.getNewestBookmarkTxn(bookmarkName, 1);
        assertTrue(retrievedTxns.size() == 1);
        assertTrue(retrievedTxns.get(0).size() == 2);
        assertTrue(retrievedTxns.get(0).get("string").equals("string"));
        assertTrue(retrievedTxns.get(0).get("boolVal").equals(true));
        assertTrue(retrievedTxns.get(0).getTimestamp().toString().equals(newerTxns.get(0).getTimestamp().toString()));
    }

    @Test
    public void testSaveBookmarkValues() {
        String bookmarkName = "bookmark";
        BookmarksService bookmarksService = getBookmarksService();

        BookmarkValues initialValues = new BookmarkValues(){{
            put("intVal", 1);
            put("floatVal", 2.0);
        }};

        assertTrue(bookmarksService.saveBookmarkValues(bookmarkName, initialValues));
        BookmarkValues retrievedValues = bookmarksService.getBookmarkValues(bookmarkName);
        assertTrue(retrievedValues.size() == 2);
        assertTrue(retrievedValues.get("intVal").equals(1));
        assertTrue(retrievedValues.get("floatVal").equals(2.0));

        BookmarkValues newValues = new BookmarkValues(){{
            put("string", "string");
            put("boolVal", true);
        }};

        assertTrue(bookmarksService.saveBookmarkValues(bookmarkName, newValues));
        retrievedValues = bookmarksService.getBookmarkValues(bookmarkName);
        assertTrue(retrievedValues.size() == 2);
        assertTrue(retrievedValues.get("string").equals("string"));
        assertTrue(retrievedValues.get("boolVal").equals("true"));
    }

    @Test
    public void testUpdateBookmarkValues() {
        String bookmarkName = "bookmark";
        BookmarksService bookmarksService = getBookmarksService();

        BookmarkValues initialValues = new BookmarkValues(){{
            put("intVal", 1);
            put("floatVal", 2.0);
        }};

        assertTrue(bookmarksService.updateBookmarkValues(bookmarkName, initialValues));

        BookmarkValues newValues = new BookmarkValues(){{
            put("floatVal", 3.0);
            put("string", "string");
            put("boolVal", true);
        }};

        assertTrue(bookmarksService.updateBookmarkValues(bookmarkName, newValues));
        BookmarkValues retrievedValues = bookmarksService.getBookmarkValues(bookmarkName);
        assertTrue(retrievedValues.size() == 4);
        assertTrue(retrievedValues.get("intVal").equals(1));
        assertTrue(retrievedValues.get("floatVal").equals(3.0));
        assertTrue(retrievedValues.get("string").equals("string"));
        assertTrue(retrievedValues.get("boolVal").equals(true));
    }
}