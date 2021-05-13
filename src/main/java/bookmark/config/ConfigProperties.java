package bookmark.config;

public class ConfigProperties {
    private String bookmarkName;
    private String serverAddress;
    private String bookmarkPath;
    private Integer retentionDays;
    private Integer cleanupPeriodMins;

    public String getBookmarkName() {
        return bookmarkName;
    }

    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getBookmarkPath() {
        return bookmarkPath;
    }

    public void setBookmarkPath(String bookmarkPath) {
        this.bookmarkPath = bookmarkPath;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public Integer getCleanupPeriodMins() {
        return cleanupPeriodMins;
    }

    public void setCleanupPeriodMins(Integer cleanupPeriodMins) {
        this.cleanupPeriodMins = cleanupPeriodMins;
    }
}
