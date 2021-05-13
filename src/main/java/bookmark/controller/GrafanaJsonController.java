package bookmark.controller;

import bookmark.model.Bookmark;
import bookmark.service.BookmarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import util.table.logic.GrafanaDTO;
import util.table.model.Table;

import java.util.*;

@RestController
public class GrafanaJsonController {

    @Autowired
    BookmarksService bookmarksService;

    List<String> bookmarkDataTypes = Arrays.asList(new String[]{"*", "progress", "state", "failed"});

    @PostMapping("/api/v2/query")
    public Table query(@RequestBody HashMap request) {
        HashMap targetQuery = (HashMap<String, Object>)
                ((HashMap<String, Object>) ((ArrayList)request.get("targets")).get(0)).get("data");
        String target = (String) targetQuery.get("target");
        String bookmarkName = (String) targetQuery.get("bookmarkName");
        if (target.equals("progress")) {
            List<Bookmark> records = bookmarksService.getBookmarks(bookmarkName, (HashMap<String, Object>) targetQuery.get("args"));
            return GrafanaDTO.bookmarkRecordsToTable(records);
        } else if (target.equals("failed")) {
            List<Bookmark> records = bookmarksService.getFailedBookmarks(bookmarkName, (HashMap<String, Object>) targetQuery.get("args"));
            return GrafanaDTO.bookmarkRecordsToTable(records);
        } else if (target.equals("state")) {
            Integer state = bookmarksService.getBookmarkState(bookmarkName);
            return GrafanaDTO.stateToTable(state);
        }
        return null;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/v2")
    public String healthCheck() {
        return "Bookmark Grafana Backend";
    }

    @PostMapping("/api/v2/search")
    public List<String> search(@RequestBody HashMap request) {
        if (request != null && request.containsKey("bookmarks")) {
            return bookmarksService.getBookmarkList();
        } else {
            return bookmarkDataTypes;
        }
    }
}

