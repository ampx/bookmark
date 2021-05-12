package bookmark.controller;

import bookmark.model.Bookmark;
import bookmark.service.BookmarksService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class BookmarkController {

    BookmarksService bookmarkService;

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
        throw new ResponseStatusException(NOT_FOUND);
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
            throw new ResponseStatusException(BAD_REQUEST);
        }

    }

    @PostMapping("/bookmarks/{bookmarkName}/bookmark")
    public Map saveBookmark(@PathVariable String bookmarkName, @RequestBody List<Bookmark> bookmarks) {
        try {
            return Collections.singletonMap("success", bookmarkService.saveBookmark(bookmarkName, bookmarks));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @PutMapping("/bookmarks/{bookmarkName}/bookmark")
    public Map updateBookmark(@PathVariable String bookmarkName, @RequestBody List<Bookmark> bookmarks) {
        try {
            return Collections.singletonMap("success", bookmarkService.updateBookmark(bookmarkName, bookmarks));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @GetMapping("/bookmarks/{bookmarkName}/failed")
    public List<Bookmark> getFailedBookmarks(@PathVariable String bookmarkName, @RequestParam Map<String, Object> params)
    {
        try {
            return bookmarkService.getFailedBookmarks(bookmarkName, params);
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @PostMapping("/bookmarks/{bookmarkName}/failed")
    public Map saveFailedBookmarks(@PathVariable String bookmarkName, @RequestBody List<Bookmark> bookmarks) {
        try {
            return Collections.singletonMap("success", bookmarkService.saveFailedBookmarks(bookmarkName, bookmarks));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @PutMapping("/bookmarks/{bookmarkName}/failed")
    public Map updateFailedBookmarks(@PathVariable String bookmarkName, @RequestBody List<Bookmark> bookmarks) {
        try {
            return Collections.singletonMap("success", bookmarkService.updateFailedBookmarks(bookmarkName, bookmarks));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @GetMapping("/bookmarks/{bookmarkName}/state")
    public Map getBookmarkState(@PathVariable String bookmarkName)
    {
        try {
            return Collections.singletonMap("state", bookmarkService.getBookmarkState(bookmarkName));
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @PostMapping("/bookmarks/{bookmarkName}/state")
    public Map saveBookmarkState(@PathVariable String bookmarkName, @RequestBody Map stateMap) {
        try {
            Integer state = (Integer)stateMap.get("state");
            return Collections.singletonMap("success", bookmarkService.updateBookmarkState(bookmarkName, state));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @PutMapping("/bookmarks/{bookmarkName}/state")
    public Map updateBookmarkState(@PathVariable String bookmarkName, @RequestBody Map stateMap) {
        try {
            Integer state = (Integer)stateMap.get("state");
            return Collections.singletonMap("success", bookmarkService.updateBookmarkState(bookmarkName, state));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }
}