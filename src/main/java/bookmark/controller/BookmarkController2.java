package bookmark.controller;

import bookmark.model.meta.BookmarkMetadata;
import bookmark.model.meta.BookmarkState;
import bookmark.model.txn.BookmarkTxns;
import bookmark.model.txn.TxnQuery;
import bookmark.model.value.BookmarkValues;
import bookmark.model.value.ValueQuery;
import bookmark.service.bookmark.BookmarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

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
    public Map<String, Boolean> bookmarkExists(@PathVariable String bookmarkName)
    {
        return Collections.singletonMap("success", bookmarkService.bookmarkExists(bookmarkName));
    }

    //create bookmark
    /*@PutMapping("/bookmark/{bookmark-name}")
    public ResponseEntity<?> createBookmarkUpdateMeta(@PathVariable String bookmarkName,
                                                @RequestBody BookmarkMetadata bookmarkMetadata) {
        try {
            if(bookmarkService.updateBookmarkMetadata(bookmarkName, bookmarkMetadata)){
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }*/

    //create bookmark
    /*@PostMapping("/bookmark/{bookmark-name}")
    public ResponseEntity<?> createBookmarkOverwriteMeta(@PathVariable String bookmarkName,
                                              @RequestBody BookmarkMetadata bookmarkMetadata) {
        try {
            if(bookmarkService.saveBookmarkMetadata(bookmarkName, bookmarkMetadata)){
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }*/

    //get bookmark meta
    @GetMapping("/bookmark/{bookmark-name}/meta")
    public BookmarkMetadata getBookmarkMeta(@PathVariable String bookmarkName)
    {
        return bookmarkService.getBookmarkMetadata(bookmarkName);
    }

    //update bookmark config
    /*@PutMapping("/bookmark/{bookmark-name}/meta")
    public ResponseEntity<?> updateBookmarkMeta(@PathVariable String bookmarkName,
                                                @RequestBody BookmarkMetadata bookmarkMetadata) {
        try {
            if (bookmarkService.updateBookmarkMetadata(bookmarkName, bookmarkMetadata)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }*/

    //overwrite config
    /*@PostMapping("/bookmark/{bookmark-name}/meta")
    public ResponseEntity<?> saveBookmarkMeta(@PathVariable String bookmarkName,
                                              @RequestBody BookmarkMetadata bookmarkMetadata) {
        try {
            if (bookmarkService.saveBookmarkMetadata(bookmarkName, bookmarkMetadata)) {
                return successResp;
            }
        } catch (Exception e) {}
        return badReqResp;
    }*/

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
        return bookmarkService.getContextValues(bookmarkName, query.getContext());
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
    public BookmarkState getValues(@PathVariable String bookmarkName)
    {
        return bookmarkService.getState(bookmarkName);
    }

    @PutMapping("/bookmark/{bookmarkName}/state")
    public ResponseEntity<?> updateStateValues(@PathVariable String bookmarkName, @RequestBody BookmarkState state) {
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