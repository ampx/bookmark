package bookmark.dao;

import bookmark.model.Bookmark;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import util.time.model.Time;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BookmarkClientDao implements BookmarkDao{

    String url = "http://localhost:8080/bookmarks";
    RestTemplate restTemplate = new RestTemplate();

    @Override
    public Boolean bookmarkExists(String bookmarkName) {
        String bookmarkUrl = url + "/" + bookmarkName;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public List bookmarkList() {
        String bookmarkUrl = url + "?data=bookmarks" ;
        String[] response = restTemplate.getForObject(bookmarkUrl, String[].class);
        if (response != null && response.length>0) {
            return Arrays.asList(response);
        } else {
            return null;
        }
    }

    @Override
    public HashMap<String, HashMap> getBookmarkConfig(String bookmarkName) {
        return null;
    }

    @Override
    public Boolean createBookmark(String bookmarkName, HashMap config) {
        String bookmarkUrl = url + "/" + bookmarkName;
        HashMap postBody = new HashMap();
        postBody.put("name", bookmarkName);
        postBody.put("config", config);
        HttpEntity<HashMap> request = new HttpEntity<>(postBody);
        HashMap result = restTemplate.postForObject(bookmarkUrl, request, HashMap.class);
        if (result != null) {
            return (Boolean) result.get("success");
        }
        else return null;
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
        String bookmarkUrl = url + "/" + bookmarkName + "/failed" ;
        HttpEntity<List<Bookmark>> request = new HttpEntity<>(bookmarks);
        HashMap result = restTemplate.postForObject(bookmarkUrl, request, HashMap.class);
        if (result != null) {
            return (Boolean) result.get("success");
        }
        else return null;
    }

    @Override
    public Boolean updateFailed(String bookmarkName, List<Bookmark> bookmarks) {
        String bookmarkUrl = url + "/" + bookmarkName + "/failed" ;
        HttpEntity<List<Bookmark>> request = new HttpEntity<>(bookmarks);
        ResponseEntity<HashMap> result = restTemplate.exchange(bookmarkUrl, HttpMethod.PUT, request, HashMap.class);
        if (result.getBody() != null) {
            return (Boolean) result.getBody().get("success");
        }
        else return null;
    }

    @Override
    public List<Bookmark> getFailed(String bookmarkName, Time starttime, Time endtime, Integer top) {
        String bookmarkUrl = url + "/" + bookmarkName + "/failed?" ;
        if (starttime != null) {
            bookmarkUrl += "from=" + starttime.toString();
        }
        if (endtime != null) {
            bookmarkUrl += "to=" + endtime.toString();
        }
        if (top != null) {
            bookmarkUrl += "top=" + top;
        }
        Bookmark[] response = restTemplate.getForObject(bookmarkUrl, Bookmark[].class);
        if (response != null && response.length>0) {
            return Arrays.asList(response);
        } else {
            return null;
        }
    }

    @Override
    public Boolean saveProgress(String bookmarkName, List<Bookmark> bookmarks) {
        String bookmarkUrl = url + "/" + bookmarkName + "/bookmark" ;
        HttpEntity<List<Bookmark>> request = new HttpEntity<>(bookmarks);
        HashMap result = restTemplate.postForObject(bookmarkUrl, request, HashMap.class);
        if (result != null) {
            return (Boolean) result.get("success");
        }
        else return null;
    }

    @Override
    public Boolean updateProgress(String bookmarkName, List<Bookmark> bookmarks) {
        String bookmarkUrl = url + "/" + bookmarkName + "/bookmark" ;
        HttpEntity<List<Bookmark>> request = new HttpEntity<>(bookmarks);
        ResponseEntity<HashMap> result = restTemplate.exchange(bookmarkUrl, HttpMethod.PUT, request, HashMap.class);
        if (result.getBody() != null) {
            return (Boolean) result.getBody().get("success");
        }
        else return null;
    }

    @Override
    public List<Bookmark> getProgress(String bookmarkName, Time starttime, Time endtime, Integer top) {
        String bookmarkUrl = url + "/" + bookmarkName + "/bookmark?" ;
        if (starttime != null) {
            bookmarkUrl += "from=" + starttime.toString();
        }
        if (endtime != null) {
            bookmarkUrl += "to=" + endtime.toString();
        }
        if (top != null) {
            bookmarkUrl += "top=" + top;
        }
        Bookmark[] response = restTemplate.getForObject(bookmarkUrl, Bookmark[].class);
        if (response != null && response.length>0) {
            return Arrays.asList(response);
        } else {
            return null;
        }
    }

    @Override
    public Integer getState(String bookmarkName) {
        String bookmarkUrl = url + "/" + bookmarkName + "/state" ;
        HashMap response = restTemplate.getForObject(bookmarkUrl, HashMap.class);
        if (response != null ) {
            return (Integer) response.get("state");
        } else {
            return null;
        }
    }

    @Override
    public Boolean updateState(String bookmarkName, Integer state) {
        String bookmarkUrl = url + "/" + bookmarkName + "/state" ;
        HashMap putBody = new HashMap();
        putBody.put("state", state);
        HttpEntity<HashMap> request = new HttpEntity<>(putBody);
        ResponseEntity<HashMap> result = restTemplate.exchange(bookmarkUrl, HttpMethod.PUT, request, HashMap.class);
        if (result.getBody() != null) {
            return (Boolean) result.getBody().get("success");
        }
        else return null;
    }

    @Override
    public Boolean saveBookmarkConfig(String bookmarkName, HashMap<String, Object> newConfig) {
        String bookmarkUrl = url + "/" + bookmarkName;
        HashMap config = new HashMap();
        config.put("config", newConfig);
        HttpEntity<HashMap> request = new HttpEntity<>(config);
        HashMap result = restTemplate.postForObject(bookmarkUrl, request, HashMap.class);
        if (result != null) {
            return (Boolean) result.get("success");
        }
        else return null;
    }

    @Override
    public Boolean updateBookmarkConfig(String bookmarkName, HashMap<String, Object> updateConfig) {
        String bookmarkUrl = url + "/" + bookmarkName;
        HashMap config = new HashMap();
        config.put("config", updateConfig);
        HttpEntity<HashMap> request = new HttpEntity<>(config);
        ResponseEntity<HashMap> result = restTemplate.exchange(bookmarkUrl, HttpMethod.PUT, request, HashMap.class);
        if (result.getBody() != null) {
            return (Boolean) result.getBody().get("success");
        }
        else return null;
    }
}
