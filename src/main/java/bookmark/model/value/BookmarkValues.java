package bookmark.model.value;

import java.util.HashMap;
import java.util.Map;

public class BookmarkValues {
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public void addValue(String name, Object value) {
        values.put(name, value);
    }

    String context;
    Map<String, Object> values = new HashMap<>();
}
