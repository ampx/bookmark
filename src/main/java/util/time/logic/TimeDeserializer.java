package util.time.logic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import util.time.model.Time;

import java.io.IOException;

public class TimeDeserializer extends StdDeserializer<Time> {

    public TimeDeserializer() {
        this(null);
    }

    public TimeDeserializer(Class<Time> t) {
        super(t);
    }

    @Override
    public Time deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String time_str = node.get("time").asText();
        return Time.parse(time_str);
    }
}
