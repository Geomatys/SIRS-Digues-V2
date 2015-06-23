package fr.sirs.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.time.LocalDate;

public class LocalDateSerializer extends JsonSerializer<LocalDate>{
    
    @Override
    public void serialize(LocalDate value,
		com.fasterxml.jackson.core.JsonGenerator jgen,
		SerializerProvider arg2) throws IOException,
		JsonProcessingException {
        // TODO Auto-generated method stub
        jgen.writeString(value.toString());
    }
}
