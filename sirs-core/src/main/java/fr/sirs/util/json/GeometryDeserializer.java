package fr.sirs.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;

import net.iharder.Base64;

import org.geotoolkit.geometry.jts.JTS;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {
    
    @Override
    public Geometry deserialize(JsonParser parser, DeserializationContext arg1) throws IOException, JsonProcessingException {
        if(parser.getText()==null || parser.getText().isEmpty()) return null;
        try {
            Geometry poly = (Geometry) new WKBReader().read(Base64.decode(parser.getText()));
            JTS.setCRS(poly, InjectorCore.getBean(SessionCore.class).getProjection());
            return poly;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

}
