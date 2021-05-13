package bookmark.config;

import bookmark.controller.BookmarkController;
import bookmark.dao.BookmarkClientDao;
import bookmark.dao.BookmarkDao;
import bookmark.dao.BookmarkSqliteDao;
import bookmark.service.BookmarkInstanceService;
import bookmark.service.BookmarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

@Configuration
public class BookmarkServiceConfig {

    @Autowired
    ConfigProperties configProperties;

    @Bean
    public BookmarkInstanceService bookmarkInstanceService() {
        BookmarkInstanceService bookmarkInstanceService = new BookmarkInstanceService();
        bookmarkInstanceService.setBookmarksService(bookmarksService());
        bookmarkInstanceService.setBookmarkName(configProperties.getBookmarkName());
        return bookmarkInstanceService;
    }

    private BookmarkDao bookmarkDao(){
        BookmarkDao bookmarkDao = null;
        try {
            if (configProperties.getServerAddress() != null) {
                bookmarkDao = new BookmarkClientDao(configProperties.getServerAddress());
            } else {
                bookmarkDao = new BookmarkSqliteDao(configProperties.getBookmarkPath());
            }
        } catch (Exception e) {
            System.out.println("failed to setup due");
            e.printStackTrace();
            System.exit(1);
        }
        return bookmarkDao;
    }

    private BookmarksService bookmarksService() {
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
}
