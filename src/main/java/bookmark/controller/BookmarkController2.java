package bookmark.controller;

import bookmark.model.*;
import bookmark.service.BookmarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class BookmarkController2 {

    @Autowired
    BookmarksService bookmarkService;

    //list bookmarks
    @GetMapping("/bookmarks")
    public List<String> getServiceMeta(){
       return bookmarkService.getBookmarkList();
    }

    //check bookmark exists
    @GetMapping("/bookmark/{bookmark-name}")
    public Boolean bookmarkExists(@PathVariable String bookmarkName, @RequestParam List<String> items)
    {
        return bookmarkService.bookmarkExists(bookmarkName);
    }

    //create bookmark
    @PutMapping("/bookmark/{bookmark-name}")
    public ResponseEntity<?> createBookmarkUpdateMeta(@PathVariable String bookmarkName,
                                                @RequestBody Metadata metadata) {
        try {
            if (!bookmarkService.bookmarkExists(bookmarkName) ) {
                if (bookmarkService.createBookmark(bookmarkName, metadata)) {
                    return successResp;
                }
            } else if(bookmarkService.updateBookmarkMetadata(bookmarkName, metadata)){
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }

    //create bookmark
    @PostMapping("/bookmark/{bookmark-name}")
    public ResponseEntity<?> createBookmarkOverwriteMeta(@PathVariable String bookmarkName,
                                              @RequestBody Metadata metadata) {
        try {
            if (!bookmarkService.bookmarkExists(bookmarkName) ) {
                if (bookmarkService.createBookmark(bookmarkName, metadata)) {
                    return successResp;
                }
            } else if(bookmarkService.saveBookmarkMetadata(bookmarkName, metadata)){
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }

    //get bookmark meta/ check bookmark exists
    @GetMapping("/bookmark/{bookmark-name}/meta")
    public Metadata getBookmarkMeta(@PathVariable String bookmarkName, @RequestParam List<String> items)
    {
        return bookmarkService.getBookmarkMetadata(bookmarkName, items);
    }

    //update bookmark config
    @PutMapping("/bookmark/{bookmark-name}/meta")
    public ResponseEntity<?> updateBookmarkMeta(@PathVariable String bookmarkName,
                                                @RequestBody Metadata metadata) {
        try {
            if (bookmarkService.updateBookmarkMetadata(bookmarkName, metadata)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }

    //overwrite config
    @PostMapping("/bookmark/{bookmark-name}/meta")
    public ResponseEntity<?> saveBookmarkMeta(@PathVariable String bookmarkName,
                                              @RequestBody Metadata metadata) {
        try {
            if (bookmarkService.saveBookmarkMetadata(bookmarkName, metadata)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }

    @GetMapping("/bookmark/{bookmark-name}/data/{context}/txn")
    public BookmarkTxns getTransactions(@PathVariable String bookmarkName,
                                        @PathVariable String context,
                                        @RequestParam TxnQuery txnQuery) {
        return bookmarkService.getBookmarkTxn(bookmarkName, txnQuery);
    }

    @PostMapping("/bookmark/{bookmark-name}/data/{context}/txn")
    public ResponseEntity<?> saveTransactions(@PathVariable String bookmarkName,
                                              @PathVariable String context,
                                              @RequestBody BookmarkTxns txns) {
        try {
            if (bookmarkService.saveBookmarkTxn(bookmarkName, txns)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }

    @PutMapping("/bookmark/{bookmark_name}/data/{context}/txn")
    public ResponseEntity<?> updateTransactions(@PathVariable String bookmarkName,
                                                @PathVariable String context,
                                                @RequestBody BookmarkTxns txns) {
        try {
            if (bookmarkService.updateBookmarkTxn(bookmarkName, txns)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }

    @GetMapping("/bookmark/{bookmark_name}/data/{context}/values")
    public BookmarkValues getTransactions(@PathVariable String bookmarkName, @PathVariable String transactionContext,
                                          @RequestParam ValueQuery query) {
        return bookmarkService.getBookmarkValues(bookmarkName, query);
    }

    @PostMapping("/bookmark/{bookmark_name}/data/{context}/values")
    public ResponseEntity<?> saveValues(@PathVariable String bookmarkName,  @PathVariable String transactionContext,
                                @RequestBody BookmarkValues values) {
        try {
            if (bookmarkService.saveBookmarkValues(bookmarkName, values)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }

    @PutMapping("/bookmark/{bookmark_name}/data/{context}/values")
    public ResponseEntity<?> updateValues(@PathVariable String bookmarkName,
                            @PathVariable String transactionContext,
                            @RequestBody BookmarkValues values) {
        try {
            if (bookmarkService.updateBookmarkValues(bookmarkName, values)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }

    @GetMapping("/bookmark/{bookmarkName}/state")
    public State getValues(@PathVariable String bookmarkName)
    {
        return bookmarkService.getState(bookmarkName);
    }

    /*@PostMapping("/bookmark/{bookmarkName}/state")
    public ResponseEntity<?> saveStateValues(@PathVariable String bookmarkName, @RequestBody State state) {
        try {
            if (bookmarkService.updateState(bookmarkName, state)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }*/

    @PutMapping("/bookmark/{bookmarkName}/state")
    public ResponseEntity<?> updateStateValues(@PathVariable String bookmarkName, @RequestBody State state) {
        try {
            if (bookmarkService.updateState(bookmarkName, state)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }

    ResponseEntity successResp = new ResponseEntity<>(HttpStatus.OK);
    ResponseEntity badReqResp = new ResponseEntity<>(BAD_REQUEST);
}