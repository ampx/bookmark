package bookmark.service;

import bookmark.model.Bookmark;

import java.util.List;
import java.util.Map;

public class BookmarkTransactionInstanceService {

    String contextName;
    BookmarkInstanceService bookmarkInstanceService;

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public void setBookmarkInstanceService(BookmarkInstanceService bookmarkInstanceService) {
        this.bookmarkInstanceService = bookmarkInstanceService;
    }

    public List<Bookmark> getTransactions(Map<String, Object> filters)
    {
        return bookmarkInstanceService.getTransactions(contextName, filters);
    }

    public Boolean saveTransactions(List<Bookmark> bookmarks) {
        return bookmarkInstanceService.saveTransactions(contextName, bookmarks);
    }

    public Boolean updateTransactions(List<Bookmark> bookmarks) {
        return bookmarkInstanceService.updateTransactions(contextName, bookmarks);
    }

}
