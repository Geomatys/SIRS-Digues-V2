package fr.sirs.util.json;

import java.io.IOException;

import net.iharder.Base64;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;

public class GeometrySerializer extends JsonSerializer<Geometry> {

    @Override
    public void serialize(Geometry value,
            com.fasterxml.jackson.core.JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            com.fasterxml.jackson.core.JsonProcessingException {
        WKBWriter wkbWriter = new WKBWriter();
        jgen.writeString(Base64.encodeBytes(wkbWriter.write(value)));

    }

}
