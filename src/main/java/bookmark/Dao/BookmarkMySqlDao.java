package bookmark.Dao;

import bookmark.model.Bookmark;
import util.time.model.Time;

import java.util.HashMap;
import java.util.List;

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
    public Boolean cleanProgress(String bookmarkName, Time cutofftime) {
        return null;
    }

    @Override
    public Boolean cleanFailed(String bookmarkName, Time cutofftime) {
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
    public Boolean saveFailed(String bookmarkName, List<Bookmark> bookmarks) {
        return null;
    }

    @Override
    public Boolean updateFailed(String bookmarkName, List<Bookmark> bookmarks) {
        return null;
    }

    @Override
    public List<Bookmark> getFailed(String bookmarkName, Time starttime, Time endtime, Integer top) {
        return null;
    }

    @Override
    public Boolean saveProgress(String bookmarkName, List<Bookmark> bookmarks) {
        return null;
    }

    @Override
    public Boolean updateProgress(String bookmarkName, List<Bookmark> bookmarks) {
        return null;
    }

    @Override
    public List<Bookmark> getProgress(String bookmarkName, Time starttime, Time endtime, Integer top) {
        return null;
    }

    @Override
    public Integer getState(String bookmarkName) {
        return null;
    }

    @Override
    public Boolean updateState(String bookmarkName, Integer state) {
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
