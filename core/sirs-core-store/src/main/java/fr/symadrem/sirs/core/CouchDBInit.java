
package fr.symadrem.sirs.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.internal.io.Installation;
import org.geotoolkit.referencing.operation.transform.NTv2Transform;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Utilitaire de creation de la connection a la base CouchDB.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class CouchDBInit {
    
    static {
        final File directory = Installation.NTv2.directory(true);
        if (!new File(directory, NTv2Transform.RGF93).isFile()) {
            directory.mkdirs();
            final File out = new File(directory, NTv2Transform.RGF93);
            try {
                out.createNewFile();
                IOUtilities.copy(CouchDBInit.class.getResourceAsStream("/fr/sirs/ntv2/ntf_r93.gsb"), new FileOutputStream(out));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static final String DB_CONNECTOR = "connector";
    
    public static ClassPathXmlApplicationContext create(String databaseUrl, String databaseName, String configFile) throws MalformedURLException {
        
        final HttpClient httpClient = new StdHttpClient.Builder().url(databaseUrl).build();
        final CouchDbInstance couchsb = new StdCouchDbInstance(httpClient);
        final CouchDbConnector connector = couchsb.createConnector(databaseName,false);
        
        final ClassPathXmlApplicationContext applicationContextParent = new ClassPathXmlApplicationContext();
        applicationContextParent.refresh();
        applicationContextParent.getBeanFactory().registerSingleton(DB_CONNECTOR, connector);

        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext( new String[]{
            configFile}, applicationContextParent);

        return applicationContext;
    }
    
}
