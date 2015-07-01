package fr.sirs.core.authentication;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.Static;
import org.elasticsearch.common.io.Streams;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
class SerialParameters extends Static {
    
    private static Cipher encoder;
    private static Cipher decoder;
    
    private static String[] params;
    
    static synchronized Cipher getEncoder() throws Exception {
        if (encoder == null) {
            Base64.Decoder tmpDecoder = Base64.getDecoder();
            final String[] parameters = getParameters();
            final SecretKeySpec key = new SecretKeySpec(
                    tmpDecoder.decode(parameters[2]), 
                    new String(tmpDecoder.decode(parameters[1])));
            encoder = Cipher.getInstance(new String(tmpDecoder.decode(parameters[0])));
            if (parameters.length > 3) {
                final IvParameterSpec iv = new IvParameterSpec(tmpDecoder.decode(parameters[3]));
                encoder.init(Cipher.ENCRYPT_MODE, key, iv);
            } else {
                encoder.init(Cipher.ENCRYPT_MODE, key);
            }  
        }
        return encoder;
    }
    
    static synchronized Cipher getDecoder() throws Exception {
        if (decoder == null) {
            Base64.Decoder tmpDecoder = Base64.getDecoder();
            final String[] parameters = getParameters();
            final SecretKeySpec key = new SecretKeySpec(
                    tmpDecoder.decode(parameters[2]), 
                    new String(tmpDecoder.decode(parameters[1])));
            decoder = Cipher.getInstance(new String(tmpDecoder.decode(parameters[0])));
            if (parameters.length > 3) {
                final IvParameterSpec iv = new IvParameterSpec(tmpDecoder.decode(parameters[3]));
                decoder.init(Cipher.DECRYPT_MODE, key, iv);
            } else {
                decoder.init(Cipher.DECRYPT_MODE, key);
            }
        }
        return decoder;
    }
    
    private static String[] getParameters() throws IOException {
        if (params == null) {
            Base64.Decoder tmpDecoder = Base64.getDecoder();
            final String tmp = new String(tmpDecoder.decode("L2ZyL3NpcnMvY29yZS9jb21wb25lbnQvbWlzYw"));
            List<String> lines = Streams.readAllLines(AuthenticationWallet.class.getResourceAsStream(tmp));
            ArgumentChecks.ensureSizeBetween("Wallet parameters", 3, 4, lines.size());
            params = lines.toArray(new String[0]);
        }
        return params;
    }
}
