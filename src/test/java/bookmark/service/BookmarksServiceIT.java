package bookmark.service;

import bookmark.dao.BookmarkSqliteDao;
import bookmark.model.Bookmark;
import bookmark.model.BookmarkTxns;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

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
        List<String> bookmarkList = bookmarksService.getBookmarkList();

        BookmarkTxns bookmarkTxns = new BookmarkTxns();
        bookmarkTxns.addBookmark(new Bookmark(new HashMap(){{put("a","b");}}));
        bookmarksService.saveBookmarkTxn("bookmark-autoTxn", bookmarkTxns);

        assertTrue(bookmarkList.contains("bookmark-defaultMeta"));
        assertTrue(bookmarkList.contains("bookmark-autoTxn"));
        assertTrue(bookmarkList.size() == 2);

    }
}