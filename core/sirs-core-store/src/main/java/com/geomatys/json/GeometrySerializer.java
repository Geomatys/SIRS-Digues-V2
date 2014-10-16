package com.geomatys.json;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.Geometry;

public class GeometrySerializer extends JsonSerializer<Geometry> {

	@Override
	public void serialize(Geometry value,
			com.fasterxml.jackson.core.JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			com.fasterxml.jackson.core.JsonProcessingException {
		jgen.writeString(value.toText());

	}

}
