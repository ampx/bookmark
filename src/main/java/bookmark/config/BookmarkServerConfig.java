package bookmark.config;

import bookmark.dao.BookmarkClientDao;
import bookmark.dao.BookmarkDao;
import bookmark.dao.BookmarkSqliteDao;
import bookmark.service.BookmarkInstanceService;
import bookmark.service.BookmarksService;
import org.springframework.context.annotation.Bean;

public class BookmarkServerConfig {

    ConfigProperties configProperties;

    @Bean
    public BookmarksService bookmarksService() {
        BookmarksService bookmarksService = new BookmarksService();
        bookmarksService.setBookmarkDao(bookmarkDao());
        if (configProperties.getCleanupPeriodMins() != null) {
            bookmarksService.setCleanupPeriodMins(configProperties.getCleanupPeriodMins());
            if (configProperties.getRetentionDays() != null) {
                bookmarksService.setDefaultRetentionDays(configProperties.getRetentionDays());
            }
            bookmarksService.enableCleanup();
        }
        return bookmarksService;
    }

    private BookmarkDao bookmarkDao(){
        BookmarkDao bookmarkDao = null;
        try {
             bookmarkDao = new BookmarkSqliteDao(configProperties.getBookmarkPath());
        } catch (Exception e) {
            System.out.println("failed to setup due");
            e.printStackTrace();
            System.exit(1);
        }
        return bookmarkDao;
    }


}
