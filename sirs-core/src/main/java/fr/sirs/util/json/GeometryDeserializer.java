package fr.sirs.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import fr.sirs.core.SirsCore;
import net.iharder.Base64;

import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {

    public static CoordinateReferenceSystem PROJECTION;
    
    
    @Override
    public Geometry deserialize(JsonParser parser, DeserializationContext arg1) throws IOException, JsonProcessingException {
        if(parser.getText()==null || parser.getText().isEmpty()) return null;
        try {
            Geometry poly = (Geometry) new WKBReader().read(Base64.decode(parser.getText()));
            JTS.setCRS(poly, SirsCore.getEpsgCode());
            return poly;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

}
