package com.geomatys.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {

    
    
    
    private  static CoordinateReferenceSystem PROJECTION;
    
    static {
//        try {
//            PROJECTION  = CRS.decode("EPSG:2154");
//        } catch (FactoryException e) {
//            throw new IllegalStateException(e);
//        }
    }
    
    @Override
    public Geometry deserialize(JsonParser parser, DeserializationContext arg1) throws IOException, JsonProcessingException {
        try {
            Geometry poly = (Geometry) new WKTReader().read(parser.getText());
           // JTS.setCRS(poly, PROJECTION);
            return poly;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

}
