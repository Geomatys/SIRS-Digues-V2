package com.geomatys.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {

    
    private final static CoordinateReferenceSystem PROJECTION;
    
    static {
        try {
            PROJECTION  = CRS.decode("EPSG:2157");
        } catch (FactoryException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Override
    public Geometry deserialize(JsonParser parser, DeserializationContext arg1) throws IOException, JsonProcessingException {
        try {
            Geometry poly = (Geometry) new WKTReader().read(parser.getText());
            JTS.setCRS(poly, PROJECTION);
            return poly;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

}
