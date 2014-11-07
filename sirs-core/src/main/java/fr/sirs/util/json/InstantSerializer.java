package fr.sirs.util.json;

import java.io.IOException;
import java.time.Instant;

import org.codehaus.jackson.JsonGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class InstantSerializer extends JsonSerializer<Instant>{


	@Override
	public void serialize(Instant value,
			com.fasterxml.jackson.core.JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeString(value.toString());
		
	}

}
