package bookmark.dao;

import bookmark.model.meta.BookmarkMetadata;
import bookmark.model.meta.BookmarkState;
import bookmark.model.meta.ContextMetadata;
import bookmark.model.txn.BookmarkTxns;
import bookmark.model.txn.TxnQuery;
import bookmark.model.value.BookmarkValues;
import bookmark.model.value.ValueQuery;
import util.time.model.Time;

import java.util.List;
import java.util.Set;

public interface BookmarkDao {

    public Boolean createBookmark(String bookmarkName, BookmarkMetadata bookmarkMetadata) throws Exception;

    public Boolean createContext(String bookmarkName, String context) throws Exception;

    public Boolean bookmarkExists(String bookmarkName) throws Exception;

    public Boolean contextExists(String bookmarkName, String context) throws Exception;

    public List<String> getBookmarkList();

    public Set<String> getContextList(String bookmarkName) throws Exception;

    public Boolean saveBookmarkTxn(String bookmarkName, BookmarkTxns txns) throws Exception;

    public Boolean updateBookmarkTxn(String bookmarkName, BookmarkTxns txns) throws Exception;

    public BookmarkTxns getBookmarkTxn(String bookmarkName, TxnQuery query)  throws Exception;

    public BookmarkValues getBookmarkValues(String bookmarkName, ValueQuery query)  throws Exception;

    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception;

    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception;

    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutoffTime) throws Exception;

    public BookmarkMetadata getBookmarkMeta(String bookmarkName) throws Exception;

    public Boolean updateBookmarkMeta(String bookmarkName, BookmarkMetadata meta) throws Exception;

    public Boolean saveBookmarkMeta(String bookmarkName, BookmarkMetadata meta) throws Exception;

    public ContextMetadata getContextMeta(String bookmarkName, String context) throws Exception;

    public Boolean updateContextMeta(String bookmarkName, ContextMetadata meta) throws Exception;

    public Boolean saveContextMeta(String bookmarkName, ContextMetadata meta) throws Exception;

    public BookmarkState getState(String bookmarkName) throws Exception;

    public Boolean switchState(String bookmarkName, BookmarkState state) throws Exception;

    public Boolean maintenance() throws Exception;

    public Double size() throws Exception;

    public Double size(String bookmarkName) throws Exception;

    public Integer recordCount(String bookmarkName, String context) throws Exception;

    public Boolean deleteBookmark(String bookmarkName);
}
