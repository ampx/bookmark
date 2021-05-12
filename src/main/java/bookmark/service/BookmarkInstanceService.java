package bookmark.service;

import bookmark.model.Bookmark;
import util.time.model.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarkInstanceService {
    String bookmarkName;
    BookmarksService bookmarksService;

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

    public List<Bookmark> getBookmarks(Map<String, Object> filters)
    {
        return bookmarksService.getBookmarks(bookmarkName, filters);
    }

    public Boolean saveBookmark(List<Bookmark> bookmarks) {
        return bookmarksService.saveBookmark(bookmarkName, bookmarks);
    }

    public Boolean updateBookmark(List<Bookmark> bookmarks) {
        return bookmarksService.updateBookmark(bookmarkName, bookmarks);
    }

    public List<Bookmark> getFailedBookmarks(Map<String, Object> filter)
    {
        return bookmarksService.getFailedBookmarks(bookmarkName, filter);
    }

    public Boolean saveFailedBookmarks(String bookmarkName, List<Bookmark> bookmarks) {
        return bookmarksService.saveFailedBookmarks(bookmarkName, bookmarks);
    }

    public Boolean updateFailedBookmarks(List<Bookmark> bookmarks) {
        return bookmarksService.updateFailedBookmarks(bookmarkName, bookmarks);
    }

    public Integer getBookmarkState()
    {
        return bookmarksService.getBookmarkState(bookmarkName);
    }

    public Boolean updateBookmarkState(Integer state) {
        return bookmarksService.updateBookmarkState(bookmarkName, state);
    }


}
