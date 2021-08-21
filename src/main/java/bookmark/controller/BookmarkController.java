package bookmark.controller;

import bookmark.model.Bookmark;
import bookmark.service.BookmarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class BookmarkController {

    @Autowired
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

    @GetMapping("/bookmarks/{bookmark_name}/txn/{transaction_context}")
    public List<Bookmark> getTransactions(@PathVariable String bookmarkName, @PathVariable String transactionContext,
                                       @RequestParam Map<String, Object> params)
    {
        try {
            return bookmarkService.getTransactions(bookmarkName, transactionContext, params);
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @PostMapping("/bookmarks/{bookmark_name}/txn/{transaction_context}")
    public Map saveTransactions(@PathVariable String bookmarkName,  @PathVariable String transactionContext,
                            @RequestBody List<Bookmark> bookmarks) {
        try {
            return Collections.singletonMap("success",
                    bookmarkService.saveTransactions(bookmarkName, transactionContext ,bookmarks));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @PutMapping("/bookmarks/{bookmark_name}/txn/{transaction_context}")
    public Map updateTransactions(@PathVariable String bookmarkName, @PathVariable String transactionContext,
                              @RequestBody List<Bookmark> bookmarks) {
        try {
            return Collections.singletonMap("success",
                    bookmarkService.updateTransactions(bookmarkName, transactionContext, bookmarks));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @GetMapping("/bookmarks/{bookmarkName}/state")
    public Map getStateValues(@PathVariable String bookmarkName, @RequestParam Map<String, Object> params)
    {
        try {
            return Collections.singletonMap("state",
                    bookmarkService.getStateValues(bookmarkName, params));
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @PostMapping("/bookmarks/{bookmarkName}/state")
    public Map saveStateValues(@PathVariable String bookmarkName, @RequestBody Map<String, Object> stateValues) {
        try {
            return Collections.singletonMap("success",
                    bookmarkService.saveStateValues(bookmarkName, stateValues));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @PutMapping("/bookmarks/{bookmarkName}/state")
    public Map updateStateValues(@PathVariable String bookmarkName, @RequestBody Map<String, Object> stateValues) {
        try {
            return Collections.singletonMap("success",
                    bookmarkService.updateStateValues(bookmarkName, stateValues));
        }catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }
}