package bookmark.service;

public class ContextService {

    String contextName;
    BookmarkService bookmarkService;

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public void setBookmarkInstanceService(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

}
