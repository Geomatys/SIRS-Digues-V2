package fr.sirs.util.json;

import java.io.IOException;
import java.time.Instant;

import org.codehaus.jackson.JsonParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;


public class InstantDeserializer extends JsonDeserializer<Instant>{


	@Override
	public Instant deserialize(com.fasterxml.jackson.core.JsonParser jp,
			DeserializationContext ctxt) throws IOException,
			JsonProcessingException {
		return Instant.parse(jp.getText());
	}

}
