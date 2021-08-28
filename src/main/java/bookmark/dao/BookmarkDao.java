package bookmark.dao;

import bookmark.model.*;
import util.time.model.Time;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BookmarkDao {

    public Boolean createBookmark(String bookmarkName, Metadata metadata);

    public Boolean createContext(String bookmarkName, String context);

    public Boolean bookmarkExists(String bookmarkName);

    public Boolean contextExists(String bookmarkName, String context);

    public List<String> getBookmarkList();

    public List<String> getContextList(String bookmarkName);

    public Boolean saveBookmarkTxn(String bookmarkName, BookmarkTxns txns);

    public Boolean updateBookmarkTxn(String bookmarkName, BookmarkTxns txns);

    public BookmarkTxns getBookmarkTxn(String bookmarkName, TxnQuery query);

    public BookmarkValues getBookmarkValues(String bookmarkName, ValueQuery query);

    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) throws ConfigurationException;

    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) throws ConfigurationException;

    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutoffTime);

    public Metadata getMetadata(String bookmarkName, List<String> options);

    public Boolean updateMetadata(String bookmarkName, Metadata metadata);

    public Boolean saveMetadata(String bookmarkName, Metadata metadata);

    public Boolean maintenance();

    public Double size();

    public Double size(String bookmarkName) ;

    public Integer recordCount(String bookmarkName, String context);

    public Boolean deleteBookmark(String bookmarkName);
}
