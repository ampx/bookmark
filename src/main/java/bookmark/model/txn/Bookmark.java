package bookmark.model.txn;

import util.time.model.Time;

import java.util.HashMap;

public class Bookmark {

    public Bookmark() {

    }

    public Bookmark(Time time, HashMap<String, Object> metrics) {
        this.timestamp = time;
        this.metrics = metrics;
    }

    public Bookmark(HashMap<String, Object> metrics) {
        this.timestamp = Time.now();
        this.metrics = metrics;
    }

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
