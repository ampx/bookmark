package bookmark.service.bookmark;

import bookmark.model.txn.BookmarkTxns;
import bookmark.model.txn.TxnQuery;
import bookmark.model.value.BookmarkValues;
import util.time.model.Time;

public class ContextService {

    String contextName;
    BookmarkService bookmarkService;

    public BookmarkTxns getBookmarkTxn(TxnQuery query)
    {
        return bookmarkService.getBookmarkTxn(query);
    }

    public BookmarkTxns getNewestContextTxn(Integer count)
    {
        return bookmarkService.getNewestContextTxn(contextName, count);
    }

    public BookmarkTxns getOldestContextTxn(Integer count)
    {
        return bookmarkService.getOldestContextTxn(contextName, count);
    }

    public BookmarkTxns getContextTxnBetween(Time from, Time to)
    {
        return bookmarkService.getContextTxnBetween(contextName, from, to);
    }

    public Boolean saveBookmarkTxn(BookmarkTxns txns) {
        txns.setContext(contextName);
        return bookmarkService.saveBookmarkTxn(txns);
    }

    public Boolean updateBookmarkTxn(BookmarkTxns txns) {
        txns.setContext(contextName);
        return bookmarkService.updateBookmarkTxn(txns);
    }

    public BookmarkValues getContextValues()
    {
        return bookmarkService.getContextValues(contextName);
    }

    public Boolean updateBookmarkValues(BookmarkValues values) {
        values.setContext(contextName);
        return bookmarkService.updateBookmarkValues(values);
    }

    public Boolean saveBookmarkValues(BookmarkValues values) {
        values.setContext(contextName);
        return bookmarkService.saveBookmarkValues(values);
    }

}
