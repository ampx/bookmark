package bookmark.logic.meta;

import bookmark.model.meta.TxnSchema;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class TxnSchemaSerializer extends StdSerializer<TxnSchema> {
    protected TxnSchemaSerializer(Class<TxnSchema> t) {
        super(t);
    }

    @Override
    public void serialize(TxnSchema item, JsonGenerator jgen,
                          SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        for (String fieldName: item.getFieldNames()) {
            jgen.writeStringField(fieldName, item.getType(fieldName).name());
        }
        jgen.writeEndObject();
    }
}
