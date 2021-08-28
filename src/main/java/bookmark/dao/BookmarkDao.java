package bookmark.dao;

import bookmark.model.*;
import util.time.model.Time;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BookmarkDao {

    public Boolean createBookmark(String bookmarkName, Metadata metadata) throws Exception;

    public Boolean createContext(String bookmarkName, String context) throws Exception;

    public Boolean bookmarkExists(String bookmarkName) throws Exception;

    public Boolean contextExists(String bookmarkName, String context) throws Exception;

    public List<String> getBookmarkList();

    public List<String> getContextList(String bookmarkName) throws Exception;

    public Boolean saveBookmarkTxn(String bookmarkName, BookmarkTxns txns) throws Exception;

    public Boolean updateBookmarkTxn(String bookmarkName, BookmarkTxns txns) throws Exception;

    public BookmarkTxns getBookmarkTxn(String bookmarkName, TxnQuery query)  throws Exception;

    public BookmarkValues getBookmarkValues(String bookmarkName, ValueQuery query)  throws Exception;

    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception;

    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception;

    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutoffTime) throws Exception;

    public Metadata getMetadata(String bookmarkName, List<String> options) throws Exception;

    public Boolean updateMetadata(String bookmarkName, Metadata metadata) throws Exception;

    public Boolean saveMetadata(String bookmarkName, Metadata metadata) throws Exception;

    public Boolean maintenance() throws Exception;

    public Double size() throws Exception;

    public Double size(String bookmarkName) throws Exception;

    public Integer recordCount(String bookmarkName, String context) throws Exception;

    public Boolean deleteBookmark(String bookmarkName);
}
