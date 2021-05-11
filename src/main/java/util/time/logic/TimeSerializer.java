package util.time.logic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import util.time.model.Time;

import java.io.IOException;

public class TimeSerializer extends StdSerializer<Time> {

    public TimeSerializer() {
        this(null);
    }

    public TimeSerializer(Class<Time> t) {
        super(t);
    }

    @Override
    public void serialize(
            Time time, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("time", time.toString());
        jgen.writeEndObject();
    }
}