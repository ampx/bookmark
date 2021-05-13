package bookmark.model;

import util.time.model.Time;

import java.util.HashMap;

public class Bookmark {

    Time timestamp;
    HashMap<String, Object> metrics;

    public Time getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Time timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(HashMap<String, Object> metrics) {
        this.metrics = metrics;
    }
}
