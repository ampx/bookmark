package bookmark.model.meta;

import java.util.*;

public class BookmarkMetadata {
    public BookmarkConfig getConfig() {
        return config;
    }

    public void setConfig(BookmarkConfig config) {
        this.config = config;
    }

    public BookmarkState getState() {
        return lock;
    }

    public void setState(BookmarkState lock) {
        this.lock = lock;
    }

    public String getBookmarkName() {
        return bookmarkName;
    }

    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    public Collection<ContextMetadata> getContextMetadata() {
        return contextMetadata.values();
    }

    public Map<String, ContextMetadata> getContextMap() {
        return contextMetadata;
    }

    public void addContextMetadata(ContextMetadata metadata) {
        contextMetadata.put(metadata.getName(), metadata);
    }

    public ContextMetadata getContextMetadata(String context) {
        return contextMetadata.get(context);
    }

    public void setContextMetadata(HashMap<String, ContextMetadata> contextMetadata) {
        this.contextMetadata = contextMetadata;
    }

    public Set<String> getContextList() {
        return contextMetadata.keySet();
    }

    String bookmarkName;
    BookmarkConfig config;
    BookmarkState lock ;
    HashMap<String, ContextMetadata> contextMetadata;
}
