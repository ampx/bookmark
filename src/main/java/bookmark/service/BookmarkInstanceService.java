package bookmark.service;

import bookmark.model.Bookmark;
import util.time.model.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarkInstanceService {
    private String bookmarkName;
    private BookmarksService bookmarksService;

    public List<String> getBookmarkList()
    {
        return bookmarksService.getBookmarkList();
    }

    public Boolean createBookmark(HashMap<String, Object> config) {
        return bookmarksService.createBookmark(bookmarkName, config);
    }

    public Boolean bookmarkExists()
    {
        return bookmarksService.bookmarkExists(bookmarkName);
    }

    public Boolean updateBookmarkConfig(HashMap<String, Object> config) {
        return bookmarksService.updateBookmarkConfig(bookmarkName, config);
    }

    public Boolean saveBookmarkConfig(HashMap<String, Object> config) {
        return bookmarksService.saveBookmarkConfig(bookmarkName, config);
    }

    public List<Bookmark> getTransactions(String context, Map<String, Object> filters)
    {
        return bookmarksService.getTransactions(bookmarkName, context, filters);
    }

    public Boolean saveTransactions(String context, List<Bookmark> bookmarks) {
        return bookmarksService.saveTransactions(bookmarkName, context, bookmarks);
    }

    public Boolean updateTransactions(String context, List<Bookmark> bookmarks) {
        return bookmarksService.updateTransactions(bookmarkName, context, bookmarks);
    }

    public Map<String, Object> getStateValues(Map<String, Object> filters)
    {
        return bookmarksService.getStateValues(bookmarkName, filters);
    }

    public Boolean updateStateValues(Map<String, Object> stateValues) {
        return bookmarksService.updateStateValues(bookmarkName, stateValues);
    }

    public Boolean saveStateValues(Map<String, Object> stateValues) {
        return bookmarksService.saveStateValues(bookmarkName, stateValues);
    }

    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    public void setBookmarksService(BookmarksService bookmarksService) {
        this.bookmarksService = bookmarksService;
    }

    public BookmarkTransactionInstanceService createTransactionInstanceService(String context) {
        BookmarkTransactionInstanceService bookmarkTransactionInstanceService = new BookmarkTransactionInstanceService();
        bookmarkTransactionInstanceService.setBookmarkInstanceService(this);
        bookmarkTransactionInstanceService.setContextName(context);
        return bookmarkTransactionInstanceService;
    }
}
