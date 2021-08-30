package bookmark.service;

public class BookmarkService {

    private String bookmarkName;
    private BookmarksService bookmarksService;

    public ContextService createTransactionInstanceService(String context) {
        ContextService contextService = new ContextService();
        contextService.setBookmarkInstanceService(this);
        contextService.setContextName(context);
        return contextService;
    }
}
