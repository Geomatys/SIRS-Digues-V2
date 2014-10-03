package com.geomatys.json;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;


public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>{


    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, org.codehaus.jackson.JsonProcessingException {
        return LocalDateTime.parse(jp.getText());
    }

}
