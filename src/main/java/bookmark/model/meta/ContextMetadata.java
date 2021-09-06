package bookmark.model.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContextMetadata{
    String name = "defaultContext";
    Map<String, Types> schema = new HashMap<>();

    public ContextMetadata(){}

    public ContextMetadata(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addIntTxnField(String fieldName) {
        schema.put(fieldName, Types.INT);
    }

    public void addFloatTxnField(String fieldName) {
        schema.put(fieldName, Types.FLOAT);
    }

    public void addBoolTxnField(String fieldName) {
        schema.put(fieldName, Types.BOOL);
    }

    public void addStringTxnField(String fieldName) {
        schema.put(fieldName, Types.STRING);
    }

    public void addTxnField(String fieldName, Types type) {
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
