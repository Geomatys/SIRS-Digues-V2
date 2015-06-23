package fr.sirs.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.time.LocalDate;


public class LocalDateDeserializer extends JsonDeserializer<LocalDate>{
    
    @Override
    public LocalDate deserialize(com.fasterxml.jackson.core.JsonParser jp,
                    DeserializationContext ctxt) throws IOException,
                    JsonProcessingException {
            return LocalDate.parse(jp.getText());
    }
}
