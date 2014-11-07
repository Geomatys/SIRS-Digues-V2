package fr.sirs.util.json;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime>{
@Override
public void serialize(LocalDateTime value,
		com.fasterxml.jackson.core.JsonGenerator jgen,
		SerializerProvider arg2) throws IOException,
		JsonProcessingException {
        // TODO Auto-generated method stub
        jgen.writeString(value.toString());
        
    }

}
