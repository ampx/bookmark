package bookmark.service;

import bookmark.dao.BookmarkSqliteDao;
import bookmark.model.meta.BookmarkConfig;
import bookmark.model.meta.BookmarkMetadata;
import bookmark.model.meta.BookmarkState;
import bookmark.model.meta.ContextMetadata;
import bookmark.model.txn.Bookmark;
import bookmark.model.txn.BookmarkTxns;
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
    public void testDefaultContextValue(){
        String bookmarkName = "bookmark";
        BookmarksService bookmarksService = getBookmarksService();

        BookmarkValues values = new BookmarkValues();
        values.put("intVal", 1);
        values.put("floatVal", 2.0);
        values.put("stringVal", "str");
        assertTrue(bookmarksService.updateBookmarkValues(bookmarkName, values));
        BookmarkValues returnValues = bookmarksService.getBookmarkValues(bookmarkName, null);
        assertTrue(returnValues.containsKey("intVal"));
        assertTrue(returnValues.containsKey("floatVal"));
        assertTrue(returnValues.containsKey("stringVal"));
    }

    @Test
    public void testDefaultContextTxn(){
        String bookmarkName = "bookmark";
        BookmarksService bookmarksService = getBookmarksService();

        BookmarkTxns txns = new BookmarkTxns();
        txns.add(new Bookmark(Time.parse("2021-08-01T00:00:00.000Z")){{
            put("intVal", 1);
            put("floatVal", 2.0);
            put("string", "string");
            put("boolVal", true);
        }});
        assertTrue(bookmarksService.saveBookmarkTxn(bookmarkName, txns));
        BookmarkTxns retrievedTxns = bookmarksService.getBookmarkTxn(bookmarkName, null);
        assertTrue(retrievedTxns.size() == 1);
        assertTrue(retrievedTxns.get(0).containsKey("intVal"));
        assertTrue(retrievedTxns.get(0).containsKey("floatVal"));
        assertTrue(retrievedTxns.get(0).containsKey("string"));
        assertTrue(retrievedTxns.get(0).containsKey("boolVal"));
    }
}