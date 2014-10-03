package com.geomatys.json;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime>{


    @Override
    public void serialize(LocalDateTime value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, org.codehaus.jackson.JsonProcessingException {
        // TODO Auto-generated method stub
        jgen.writeString(value.toString());
        
    }

}
