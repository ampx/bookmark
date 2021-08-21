package bookmark.dao;

import bookmark.model.Bookmark;
import util.time.model.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarkMySqlDao implements BookmarkDao{

    @Override
    public Boolean bookmarkExists(String bookmarkName) {
        return null;
    }

    @Override
    public List bookmarkList() {
        return null;
    }

    @Override
    public HashMap<String, HashMap> getBookmarkConfig(String bookmarkName) {
        return null;
    }

    @Override
    public Boolean createBookmark(String bookmarkName, HashMap config) {
        return null;
    }

    @Override
    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutofftime) {
        return null;
    }

    @Override
    public Boolean maintenance() {
        return null;
    }

    @Override
    public Double size() {
        return null;
    }

    @Override
    public Double size(String bookmarkName) {
        return null;
    }

    @Override
    public Integer recordCount(String bookmarkName) {
        return null;
    }

    @Override
    public Boolean deleteBookmark(String bookmarkName) {
        return null;
    }

    @Override
    public Boolean saveTransactions(String bookmarkName, String transactionContext, List<Bookmark> bookmarks) {
        return null;
    }

    @Override
    public Boolean updateTransactions(String bookmarkName, String transactionContext, List<Bookmark> bookmarks) {
        return null;
    }

    @Override
    public List<Bookmark> getTransactions(String bookmarkName, String transactionContext, Time starttime, Time endtime, Integer top) {
        return null;
    }

    @Override
    public Map<String, Object> getStateValues(String bookmarkName, String stateEntry) {
        return null;
    }

    @Override
    public Boolean updateStateValues(String bookmarkName, Map<String, Object> stateValues) {
        return null;
    }

    @Override
    public Boolean saveStateValues(String bookmarkName, Map<String, Object> stateValues) {
        return null;
    }

    @Override
    public Boolean saveBookmarkConfig(String bookmarkName, HashMap newConfig) {
        return null;
    }

    @Override
    public Boolean updateBookmarkConfig(String bookmarkName, HashMap<String, Object> updateConfig) {
        return null;
    }
}
