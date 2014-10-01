package com.geomatys.json;

import java.io.IOException;
import java.time.Instant;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;


public class InstantDeserializer extends JsonDeserializer<Instant>{


    @Override
    public Instant deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, org.codehaus.jackson.JsonProcessingException {
        return Instant.parse(jp.getText());
    }

}
