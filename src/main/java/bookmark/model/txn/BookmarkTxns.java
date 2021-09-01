package bookmark.model.txn;

import java.util.ArrayList;
import java.util.List;

public class BookmarkTxns {
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public void addBookmark(Bookmark bookmark) {
        bookmarks.add(bookmark);
    }

    String context;
    List<Bookmark> bookmarks = new ArrayList<>(10);
}
