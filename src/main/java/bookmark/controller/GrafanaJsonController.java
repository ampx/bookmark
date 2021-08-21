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
    List emptyResponse = new ArrayList();
/*
    @PostMapping("/bookmarks/query")
    public List<Table> query(@RequestBody HashMap request) {
        try {
            HashMap<String, Object> target = ((HashMap<String, Object>) ((ArrayList) request.get("targets")).get(0));
            String targetName = (String) target.get("target");
            Object data = target.get("data");
            if (data instanceof HashMap) {
                HashMap targetQuery = (HashMap<String, Object>) data;
                String bookmarkName = (String) targetQuery.get("bookmarkName");
                if (targetName.equals("progress")) {
                    List<Bookmark> records = bookmarksService.getBookmarks(bookmarkName, (HashMap<String, Object>) targetQuery.get("args"));
                    return Arrays.asList(new Table[]{GrafanaDTO.bookmarkRecordsToTable(records)});
                } else if (targetName.equals("failed")) {
                    List<Bookmark> records = bookmarksService.getFailedBookmarks(bookmarkName, (HashMap<String, Object>) targetQuery.get("args"));
                    return Arrays.asList(new Table[]{GrafanaDTO.bookmarkRecordsToTable(records)});
                } else if (targetName.equals("state")) {
                    Integer state = bookmarksService.getBookmarkState(bookmarkName);
                    return Arrays.asList(new Table[]{GrafanaDTO.stateToTable(state)});
                } else if (targetName.equals("*")) {
                    Integer state = bookmarksService.getBookmarkState(bookmarkName);
                    List<Bookmark> records = bookmarksService.getBookmarks(bookmarkName, (HashMap<String, Object>) targetQuery.get("args"));
                    return Arrays.asList(new Table[]{GrafanaDTO.bookmarkRecordsToTable(records), GrafanaDTO.stateToTable(state)});
                }
            }
        } catch(Exception e){}
        return emptyResponse;
    }
*/
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/bookmarks/v2")
    public String healthCheck() {
        return "Bookmark Grafana Backend";
    }

    @PostMapping("/bookmarks/search")
    public List<String> search(@RequestBody HashMap request) {
        if (request != null && request.containsKey("target")) {
            String target = (String) request.get("target");
            if (target.equals("bookmarks")) {
                return bookmarksService.getBookmarkList();
            } else if (target.equals("")) {
                return bookmarkDataTypes;
            }
        }
        return new ArrayList<>();
    }
}

