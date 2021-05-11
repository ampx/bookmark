package bookmark.Dao;

import bookmark.model.Bookmark;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.time.model.Time;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface BookmarkDao {
    public Boolean bookmarkExists(String bookmarkName);

    public List bookmarkList();

    public HashMap<String, HashMap> getBookmarkConfig(String bookmarkName);

    public Boolean createBookmark(String bookmarkName, HashMap config);

    public Boolean cleanProgress(String bookmarkName, Time cutofftime);

    public Boolean cleanFailed(String bookmarkName, Time cutofftime);

    public Boolean maintenance();

    public Double size();

    public Double size(String bookmarkName) ;

    public Integer recordCount(String bookmarkName);

    public Boolean deleteBookmark(String bookmarkName);

    public Boolean saveFailed(String bookmarkName, List<Bookmark> bookmarks);

    public Boolean updateFailed(String bookmarkName, List<Bookmark> bookmarks);

    public List<Bookmark> getFailed(String bookmarkName, Time starttime, Time endtime, Integer top);


    public Boolean saveProgress(String bookmarkName, List<Bookmark> bookmarks);

    public Boolean updateProgress(String bookmarkName, List<Bookmark> bookmarks);

    public List<Bookmark> getProgress(String bookmarkName, Time starttime, Time endtime, Integer top);

    public Integer getState(String bookmarkName);

    public Boolean updateState(String bookmarkName, Integer state);

    public Boolean saveBookmarkConfig(String bookmarkName, HashMap newConfig);

    public Boolean updateBookmarkConfig(String bookmarkName, HashMap<String, Object> updateConfig);
}
