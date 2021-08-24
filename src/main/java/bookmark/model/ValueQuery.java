package bookmark.model;

import java.util.List;

public class ValueQuery {
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<String> getValueNames() {
        return valueNames;
    }

    public void setValueNames(List<String> valueNames) {
        this.valueNames = valueNames;
    }

    public String context;
    public List<String> valueNames;

}
