package fr.sirs.core.authentication;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Base64;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
class PasswordSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        try {
            jgen.writeString(Base64.getEncoder().withoutPadding().encodeToString(SerialParameters.getEncoder().doFinal(value.getBytes())));
        } catch (Exception ex) {
            throw new IOException("Cannot encode string.", ex);
        }
    }
    
}
