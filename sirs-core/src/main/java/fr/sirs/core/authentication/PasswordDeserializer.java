package fr.sirs.core.authentication;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Base64;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PasswordDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final String text = jp.getText();
        if (text == null || text.isEmpty())
            return "";
        try {
            return new String(SerialParameters.getDecoder().doFinal(Base64.getDecoder().decode(text)));
        } catch (Exception e) {
            throw new IOException("Cannot decode string", e);
        }
    }
}
