package bookmark.model.meta;

import java.util.Map;
import java.util.Set;

public class ContextMetadata {
    String name;
    Map<String, Types> schema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addIntField(String fieldName) {
        schema.put(fieldName, Types.INT);
    }

    public void addFloatField(String fieldName) {
        schema.put(fieldName, Types.FLOAT);
    }

    public void addBoolField(String fieldName) {
        schema.put(fieldName, Types.BOOL);
    }

    public void addStringField(String fieldName) {
        schema.put(fieldName, Types.STRING);
    }

    public void addFieldField(String fieldName, Types type) {
        schema.put(fieldName, type);
    }

    public Set<String> getFieldNames() {
        return schema.keySet();
    }

    public Types getType(String fieldName) {
        return schema.get(fieldName);
    }

    public enum Types {
        INT,
        FLOAT,
        BOOL,
        STRING
    }
}
