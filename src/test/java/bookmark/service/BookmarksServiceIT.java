package bookmark.service;

import bookmark.dao.BookmarkSqliteDao;
import bookmark.model.meta.BookmarkMetadata;
import bookmark.model.meta.BookmarkState;
import bookmark.model.meta.ContextMetadata;
import bookmark.model.txn.Bookmark;
import bookmark.model.txn.BookmarkTxns;
import bookmark.model.value.BookmarkValues;
import bookmark.service.bookmark.BookmarksService;
import org.junit.jupiter.api.Test;

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
        bookmarkTxns.addBookmark(new Bookmark(new HashMap(){{put("a","b");}}));
        bookmarksService.saveBookmarkTxn("bookmark-autoTxnSave", bookmarkTxns);
        bookmarksService.updateBookmarkTxn("bookmark-autoTxnUpdate", bookmarkTxns);

        BookmarkValues values = new BookmarkValues();
        values.setValues(new HashMap(){{put("a","b");}});
        bookmarksService.saveBookmarkValues("bookmark-autoValueSave", values);
        bookmarksService.updateBookmarkValues("bookmark-autoValueUpdate", values);

        bookmarksService.updateState("bookmark-autoState", new BookmarkState(3));

        BookmarkMetadata bookmarkMetadata = new BookmarkMetadata();
        bookmarkMetadata.setState(BookmarkState.notReadyState());
        bookmarksService.saveBookmarkMetadata("bookmark-autoMetaSave", bookmarkMetadata);
        bookmarksService.updateBookmarkMetadata("bookmark-autoMetaUpdate", bookmarkMetadata);

        List<String> bookmarkList = bookmarksService.getBookmarkList();
        assertTrue(bookmarkList.contains("bookmark-defaultMeta"));
        assertTrue(bookmarkList.contains("bookmark-autoTxnSave"));
        assertTrue(bookmarkList.contains("bookmark-autoTxnUpdate"));
        assertTrue(bookmarkList.contains("bookmark-autoValueSave"));
        assertTrue(bookmarkList.contains("bookmark-autoValueUpdate"));
        assertTrue(bookmarkList.contains("bookmark-autoState"));
        assertTrue(bookmarkList.contains("bookmark-autoMetaSave"));
        assertTrue(bookmarkList.contains("bookmark-autoMetaUpdate"));
        assertTrue(bookmarkList.size() == 8);
    }

    @Test
    public void testMetaConfiguration() {
/*
        BookmarkMetadata bookmarkMetadata0 = new BookmarkMetadata();
        bookmarkMetadata0.setContextList(new ArrayList(){{add("context0");}});
        bookmarkMetadata0.setState(new BookmarkState(4));
        TxnSchemas schemas = new TxnSchemas();
        TxnSchema schema = new TxnSchema();
        schema.addInt("intField");
        schemas.addSchema("context0", schema);
        bookmarkMetadata0.setSchemas(schemas);
        bookmarkMetadata0.setConfig(new HashMap(){{put("a", "b");}});

        BookmarksService bookmarksService = getBookmarksService();
        bookmarksService.createBookmark("bookmark-defaultMeta", bookmarkMetadata0);

        bookmarkMetadata0.getContextList().add("context1");
        schema.addString("stringField");
        TxnSchema context1Schema = new TxnSchema();
        schema.addBool("boolField");
        schemas.addSchema("context1", );
        assertTrue(true);*/
    }
}