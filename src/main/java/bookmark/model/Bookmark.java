package bookmark.model;

import util.time.model.Time;

import java.util.HashMap;

public class Bookmark {

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
