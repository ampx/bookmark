package bookmark.service.bookmark;

import bookmark.model.meta.BookmarkMetadata;
import bookmark.model.txn.BookmarkTxns;
import bookmark.model.txn.TxnQuery;
import bookmark.model.value.BookmarkValues;
import util.time.model.Time;

public class BookmarkService {

    private String bookmarkName;
    private BookmarksService bookmarksService;

    public BookmarkService(BookmarksService bookmarksService, String bookmarkName) {
        this(bookmarksService, bookmarkName, null);
    }

    public BookmarkService(BookmarksService bookmarksService, String bookmarkName, BookmarkMetadata meta) {
        this.bookmarkName = bookmarkName;
        this.bookmarksService = bookmarksService;
        bookmarksService.createBookmark(bookmarkName, meta);
    }

    public BookmarkTxns getBookmarkTxn(TxnQuery query)
    {
        return bookmarksService.getBookmarkTxn(bookmarkName, query);
    }

    public BookmarkTxns getNewestContextTxn(String context, Integer count)
    {
        return bookmarksService.getNewestContextTxn(bookmarkName, context, count);
    }

    public BookmarkTxns getNewestBookmarkTxn(Integer count)
    {
        return bookmarksService.getNewestBookmarkTxn(bookmarkName, count);
    }

    public BookmarkTxns getOldestContextTxn(String context, Integer count)
    {
        return bookmarksService.getOldestContextTxn(bookmarkName, context, count);
    }

    public BookmarkTxns getOldestBookmarkTxn(Integer count)
    {
        return bookmarksService.getOldestBookmarkTxn(bookmarkName, count);
    }

    public BookmarkTxns getContextTxnBetween(String context, Time from, Time to)
    {
        return bookmarksService.getContextTxnBetween(bookmarkName, context, from, to);
    }

    public BookmarkTxns getBookmarkTxnBetween(Time from, Time to)
    {
        return bookmarksService.getBookmarkTxnBetween(bookmarkName, from ,to);
    }

    public Boolean saveBookmarkTxn(BookmarkTxns txns) {
        return bookmarksService.saveBookmarkTxn(bookmarkName, txns);
    }

    public Boolean updateBookmarkTxn(BookmarkTxns txns) {
        return bookmarksService.updateBookmarkTxn(bookmarkName, txns);
    }

    public BookmarkValues getContextValues(String context)
    {
        return bookmarksService.getContextValues(bookmarkName, context);
    }

    public BookmarkValues getBookmarkValues()
    {
        return bookmarksService.getBookmarkValues(bookmarkName);
    }

    public Boolean updateBookmarkValues(BookmarkValues values) {
        return bookmarksService.updateBookmarkValues(bookmarkName, values);
    }

    public Boolean saveBookmarkValues(BookmarkValues values) {
        return bookmarksService.saveBookmarkValues(bookmarkName, values);
    }
}
