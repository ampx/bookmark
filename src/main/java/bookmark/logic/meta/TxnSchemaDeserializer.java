package bookmark.logic.meta;

import bookmark.model.meta.TxnSchema;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Iterator;

public class TxnSchemaDeserializer extends StdDeserializer<TxnSchema> {
    protected TxnSchemaDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TxnSchema deserialize(JsonParser jParse, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        JsonNode node = jParse.getCodec().readTree(jParse);
        TxnSchema schema = new TxnSchema();
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            String type = node.get(fieldName).asText();
            schema.addField(fieldName, TxnSchema.Types.valueOf(type));
        }
        return schema;
    }
}
