package com.geomatys.json;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.JsonParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;


public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>{



	@Override
	public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser jp,
			DeserializationContext ctxt) throws IOException,
			JsonProcessingException {
		return LocalDateTime.parse(jp.getText());
	}

}
