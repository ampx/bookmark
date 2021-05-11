package bookmark.controller;

import bookmark.Dao.BookmarkDao;
import bookmark.model.Bookmark;
import bookmark.service.BookmarksService;
import util.time.model.Time;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.naming.ConfigurationException;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class BookmarkController {

    BookmarksService bookmarkService;

    {
        try {
            bookmarkDao = new BookmarkDao(null);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @GetMapping("/bookmarks")
    public Map getBookmarksData(@RequestParam Map<String, String> params)
    {
        if (params.containsKey("data") && params.get("data").equals("bookmarks")) {
            return Collections.singletonMap("bookmarks", bookmarkService.getBookmarkList());
        } else {
            return null;
        }
    }

    @PutMapping("/bookmarks")
    public Map createBookmark(@RequestBody Map<String, Object> body) {
        return Collections.singletonMap("success", bookmarkService.createBookmark((String) body.get("name"),
                (HashMap)body.get("config")));
    }

    @PostMapping("/bookmarks")
    public Map updateBookmark(@RequestBody Map<String, String> body) {
        return Collections.singletonMap("success", false);
    }

    @GetMapping("/bookmarks/{bookmarkName}")
    public void getBookmarkData(@PathVariable String bookmarkName, @RequestParam Map<String, String> params)
    {
        if (bookmarkService.bookmarkExists(bookmarkName)) {
            return;
        }
        throw new ResponseStatusException(NOT_FOUND, "Unable to find resource");
    }

    @PutMapping("/bookmarks/{bookmarkName}")
    public Map updateBookmarkConfig(@PathVariable String bookmarkName, @RequestBody Map<String, Object> body) {
        return Collections.singletonMap("success", bookmarkService.updateBookmarkConfig(bookmarkName,
                (HashMap)body.get("config")));
    }

    @PostMapping("/bookmarks/{bookmarkName}")
    public Map saveBookmarkConfig(@PathVariable String bookmarkName, @RequestBody Map<String, Object> body) {
        return Collections.singletonMap("success", bookmarkService.saveBookmarkConfig(bookmarkName,
                (HashMap)body.get("config")));
    }

    @GetMapping("/bookmarks/{bookmarkName}/bookmark")
    public List<Bookmark> getBookmarks(@PathVariable String bookmarkName, @RequestParam Map<String, Object> params)
    {
        try {
            return bookmarkService.getBookmarks(bookmarkName, params);
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }

    }

    @PostMapping("/bookmarks/{bookmarkName}/bookmark")
    public Map saveBookmark(@PathVariable String bookmarkName, @RequestBody List<Bookmark> bookmarks) {
        try {
            return Collections.singletonMap("success", bookmarkService.saveBookmark(bookmarkName, bookmarks));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
    }

    @PutMapping("/bookmarks/{bookmarkName}/bookmark")
    public Map updateBookmark(@PathVariable String bookmarkName, @RequestBody List<Bookmark> bookmarks) {
        try {
            return Collections.singletonMap("success", bookmarkService.updateBookmark(bookmarkName, bookmarks));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
    }

    @GetMapping("/bookmarks/{bookmarkName}/failed")
    public List<Bookmark> getFailedBookmarks(@PathVariable String bookmarkName, @RequestParam Map<String, Object> params)
    {
        List<Bookmark> bookmarks = null;
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (params.containsKey("data") && params.get("data").equals("*")) {
                bookmarks = bookmarkDao.getFailed(bookmarkName, null, null, null);
            } else if (params.containsKey("top") || params.containsKey("from") || params.containsKey("to")) {
                Time from = null;
                Time to = null;
                if (params.containsKey("from")) {
                    from = Time.parse((String) params.get("from"));
                }
                if (params.containsKey("to")) {
                    to = Time.parse((String) params.get("to"));
                }
                bookmarks = bookmarkDao.getFailed(bookmarkName, from, to, (Integer) params.get("top"));
            } else {
                bookmarks = bookmarkDao.getFailed(bookmarkName, null, null, 1);
            }
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
        return bookmarks;
    }

    @PostMapping("/bookmarks/{bookmarkName}/failed")
    public Map saveFailedBookmarks(@PathVariable String bookmarkName, @RequestBody List<Bookmark> bookmarks) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (bookmarks != null && bookmarkDao.saveFailed(bookmarkName, bookmarks)) {
                return Collections.singletonMap("success", true);
            } else {
                return Collections.singletonMap("success", false);
            }
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
    }

    @PutMapping("/bookmarks/{bookmarkName}/failed")
    public Map updateFailedBookmarks(@PathVariable String bookmarkName, @RequestBody List<Bookmark> bookmarks) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (bookmarks != null && bookmarkDao.updateFailed(bookmarkName, bookmarks)) {
                return Collections.singletonMap("success", true);
            } else {
                return Collections.singletonMap("success", false);
            }
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
    }







    @GetMapping("/bookmarks/{bookmarkName}/state")
    public Map getBookmarkState(@PathVariable String bookmarkName)
    {
        List<Bookmark> bookmarks = null;
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            return Collections.singletonMap("state", bookmarkDao.getState(bookmarkName));
        }
        throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
    }

    @PostMapping("/bookmarks/{bookmarkName}/state")
    public Map saveBookmarkState(@PathVariable String bookmarkName, @RequestBody Map stateMap) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (stateMap != null && stateMap.containsKey("state")
                    && bookmarkDao.updateState(bookmarkName, (Integer)stateMap.get("state"))) {
                return Collections.singletonMap("success", true);
            } else {
                return Collections.singletonMap("success", false);
            }
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
    }

    @PutMapping("/bookmarks/{bookmarkName}/state")
    public Map updateBookmarkState(@PathVariable String bookmarkName, @RequestBody Map stateMap) {
        if (bookmarkDao.bookmarkExists(bookmarkName)) {
            if (stateMap != null && stateMap.containsKey("state")
                    && bookmarkDao.updateState(bookmarkName, (Integer)stateMap.get("state"))) {
                return Collections.singletonMap("success", true);
            } else {
                return Collections.singletonMap("success", false);
            }
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Unable to find resource");
        }
    }



/*
    @GetMapping("/bookmarks/exists")
    public Map exists(@RequestParam String bookmarkName)
    {
        return Collections.singletonMap("success", bookmarkDao.exists(bookmarkName));
    }

    @PostMapping("/bookmarks/create")
    public Map create(@RequestBody String bookmarkName) {
        return Collections.singletonMap("success", bookmarkDao.create(bookmarkName));
    }

    @DeleteMapping("/bookmarks/delete")
    public Map delete(@RequestParam String bookmarkName)
    {
        return Collections.singletonMap("success", bookmarkDao.delete(bookmarkName));
    }

    @GetMapping("/bookmarks")
    public List<bookmark.Bookmark> getBookmarks(@RequestParam String bookmarkName)
    {
        return bookmarkDao.getAllBookmarks(bookmarkName);
    }

    /*@PostMapping("/bookmarks/insert")
    public Map insert(@RequestBody String requestJson) {
        HashMap<String, Object> body = null;
        try {
            body = new ObjectMapper().readValue(requestJson, HashMap.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return Collections.singletonMap("success", bookmarkDao.bookmark((String)body.get("name"), Time.parse((String) body.get("timestamp")), (HashMap)body.get("metrics")));
    }*/
}