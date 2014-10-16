package com.geomatys.json;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {

    
   
    
    

	@Override
	public Geometry deserialize(com.fasterxml.jackson.core.JsonParser jp,
			DeserializationContext ctxt) throws IOException,
			com.fasterxml.jackson.core.JsonProcessingException {
		// TODO Auto-generated method stub
		try {
			Geometry poly = (Geometry) new WKTReader().read(jp.getText());
			// JTS.setCRS(poly, PROJECTION);
			return poly;
		} catch (ParseException e) {
			throw new IOException(e);
		}
	}

}
