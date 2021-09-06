package bookmark.model.value;

import java.util.List;

public class ValueQuery {

    public ValueQuery() {
        this.context = "defaultContext";
    }

    public ValueQuery(String context) {
        this.context = context;
    }

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
