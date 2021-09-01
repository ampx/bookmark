package bookmark.dao;

import bookmark.model.meta.BookmarkMetadata;
import bookmark.model.meta.BookmarkState;
import bookmark.model.txn.BookmarkTxns;
import bookmark.model.txn.TxnQuery;
import bookmark.model.value.BookmarkValues;
import bookmark.model.value.ValueQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import util.time.model.Time;

import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Set;

public class BookmarkClientDao implements BookmarkDao{

    String url = "http://localhost:8080";
    RestTemplate restTemplate = new RestTemplate();

    public BookmarkClientDao(String url) throws ConfigurationException {
        this.url = url + "/bookmarks";
        ResponseEntity<String> response = restTemplate.getForEntity(this.url, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            throw new ConfigurationException("Initial configuration failed... bookmark path is not writable");
        }
    }

    @Override
    public Boolean createBookmark(String bookmarkName, BookmarkMetadata bookmarkMetadata) throws Exception {
        return null;
    }

    @Override
    public Boolean createContext(String bookmarkName, String context) throws Exception {
        return null;
    }

    @Override
    public Boolean bookmarkExists(String bookmarkName) throws Exception {
        return null;
    }

    @Override
    public Boolean contextExists(String bookmarkName, String context) throws Exception {
        return null;
    }

    @Override
    public List<String> getBookmarkList() {
        return null;
    }

    @Override
    public Set<String> getContextList(String bookmarkName) throws Exception {
        return null;
    }

    @Override
    public Boolean saveBookmarkTxn(String bookmarkName, BookmarkTxns txns) throws Exception {
        return null;
    }

    @Override
    public Boolean updateBookmarkTxn(String bookmarkName, BookmarkTxns txns) throws Exception {
        return null;
    }

    @Override
    public BookmarkTxns getBookmarkTxn(String bookmarkName, TxnQuery query) throws Exception {
        return null;
    }

    @Override
    public BookmarkValues getBookmarkValues(String bookmarkName, ValueQuery query) throws Exception {
        return null;
    }

    @Override
    public Boolean updateBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception {
        return null;
    }

    @Override
    public Boolean saveBookmarkValues(String bookmarkName, BookmarkValues values) throws Exception {
        return null;
    }

    @Override
    public Boolean cleanTxnRecords(String bookmarkName, String context, Time cutoffTime) throws Exception {
        return null;
    }

    @Override
    public BookmarkMetadata getMetadata(String bookmarkName, List<String> options) throws Exception {
        return null;
    }

    @Override
    public Boolean updateMetadata(String bookmarkName, BookmarkMetadata bookmarkMetadata) throws Exception {
        return null;
    }

    @Override
    public Boolean saveMetadata(String bookmarkName, BookmarkMetadata bookmarkMetadata) throws Exception {
        return null;
    }

    @Override
    public BookmarkState getState(String bookmarkName) throws Exception {
        return null;
    }

    @Override
    public Boolean switchState(String bookmarkName, BookmarkState state) throws Exception {
        return null;
    }

    @Override
    public Boolean maintenance() throws Exception {
        return null;
    }

    @Override
    public Double size() throws Exception {
        return null;
    }

    @Override
    public Double size(String bookmarkName) throws Exception {
        return null;
    }

    @Override
    public Integer recordCount(String bookmarkName, String context) throws Exception {
        return null;
    }

    @Override
    public Boolean deleteBookmark(String bookmarkName) {
        return null;
    }
/*
    @Override
    public Boolean bookmarkExists(String bookmarkName) {
        String bookmarkUrl = url + "/" + bookmarkName;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public List getBookmarkList() {
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
    public Boolean createBookmark(String bookmarkName, Metadata metadata) {
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
    public Integer recordCount(String bookmarkName, String context) {
        return null;
    }

    @Override
    public Boolean deleteBookmark(String bookmarkName) {
        return null;
    }

    @Override
    public Boolean saveTransactions(String bookmarkName, String transactionContext, List<Bookmark> bookmarks) {
        String bookmarkUrl = url + "/" + bookmarkName + "/txn/" + transactionContext ;
        HttpEntity<List<Bookmark>> request = new HttpEntity<>(bookmarks);
        HashMap result = restTemplate.postForObject(bookmarkUrl, request, HashMap.class);
        if (result != null) {
            return (Boolean) result.get("success");
        }
        else return null;
    }

    @Override
    public Boolean updateTransactions(String bookmarkName, String transactionContext, List<Bookmark> bookmarks) {
        String bookmarkUrl = url + "/" + bookmarkName + "/txn/" + transactionContext ;
        HttpEntity<List<Bookmark>> request = new HttpEntity<>(bookmarks);
        ResponseEntity<HashMap> result = restTemplate.exchange(bookmarkUrl, HttpMethod.PUT, request, HashMap.class);
        if (result.getBody() != null) {
            return (Boolean) result.getBody().get("success");
        }
        else return null;
    }

    @Override
    public List<Bookmark> getTransactions(String bookmarkName, String transactionContext, Time starttime, Time endtime, Integer top) {
        String bookmarkUrl = url + "/" + bookmarkName + "/txn/" + transactionContext + "?";
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
        if (response != null && response.length > 0) {
            return Arrays.asList(response);
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> getStateValues(String bookmarkName, String context, List<String>values) {
        String bookmarkUrl = url + "/" + bookmarkName + "/state" ;

        return restTemplate.getForObject(bookmarkUrl, HashMap.class);
    }

    @Override
    public Boolean updateStateValues(String bookmarkName, String context, Map<String, Object> stateValues) {
        String bookmarkUrl = url + "/" + bookmarkName + "/state" ;
        HttpEntity<Map> request = new HttpEntity<>(stateValues);
        ResponseEntity<Map> result = restTemplate.exchange(bookmarkUrl, HttpMethod.PUT, request, Map.class);
        if (result.getBody() != null) {
            return (Boolean) result.getBody().get("success");
        }
        else return null;
    }

    @Override
    public Boolean saveStateValues(String bookmarkName, String context, Map<String, Object> stateValues) {
        String bookmarkUrl = url + "/" + bookmarkName + "/state" ;
        HttpEntity<Map> request = new HttpEntity<>(stateValues);
        ResponseEntity<Map> result = restTemplate.exchange(bookmarkUrl, HttpMethod.POST, request, Map.class);
        if (result.getBody() != null) {
            return (Boolean) result.getBody().get("success");
        }
        else return null;
    }

    @Override
    public Boolean saveBookmarkConfig(String bookmarkName, Map<String, Object> newConfig) {
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
    public Boolean updateBookmarkConfig(String bookmarkName, Map<String, Object> updateConfig) {
        String bookmarkUrl = url + "/" + bookmarkName;
        HashMap config = new HashMap();
        config.put("config", updateConfig);
        HttpEntity<HashMap> request = new HttpEntity<>(config);
        ResponseEntity<HashMap> result = restTemplate.exchange(bookmarkUrl, HttpMethod.PUT, request, HashMap.class);
        if (result.getBody() != null) {
            return (Boolean) result.getBody().get("success");
        }
        else return null;
    }*/
}
