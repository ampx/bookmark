package bookmark.model;

import model.util.time.Time;

import java.util.HashMap;

public class Bookmark {
    static final Integer UNLOCKED = 0;
    static final Integer LOCKED = 1;
    static final Integer ERROR = 2;

    Time timestamp;
    HashMap metrics;

    public Time getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Time timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap getMetrics() {
        return metrics;
    }

    public void setMetrics(HashMap metrics) {
        this.metrics = metrics;
    }
}
