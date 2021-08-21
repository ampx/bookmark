package bookmark.dao;

import bookmark.model.Bookmark;
import util.time.model.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BookmarkDao {
    public Boolean bookmarkExists(String bookmarkName);

    public List bookmarkList();

    public HashMap<String, HashMap> getBookmarkConfig(String bookmarkName);

    public Boolean createBookmark(String bookmarkName, HashMap config);

    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutofftime);

    public Boolean maintenance();

    public Double size();

    public Double size(String bookmarkName) ;

    public Integer recordCount(String bookmarkName);

    public Boolean deleteBookmark(String bookmarkName);

    public Boolean saveTransactions(String bookmarkName, String transactionContext, List<Bookmark> bookmarks);

    public Boolean updateTransactions(String bookmarkName, String transactionContext, List<Bookmark> bookmarks);

    public List<Bookmark> getTransactions(String bookmarkName, String transactionContext,
                                          Time starttime, Time endtime, Integer top);

    public Map<String, Object> getStateValues(String bookmarkName, String stateEntry);

    public Boolean updateStateValues(String bookmarkName, Map<String, Object> stateValues);

    public Boolean saveStateValues(String bookmarkName, Map<String, Object> stateValues);

    public Boolean saveBookmarkConfig(String bookmarkName, HashMap<String, Object> newConfig);

    public Boolean updateBookmarkConfig(String bookmarkName, HashMap<String, Object> updateConfig);
}
