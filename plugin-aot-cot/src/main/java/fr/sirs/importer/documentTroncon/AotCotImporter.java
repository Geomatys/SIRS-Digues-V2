package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.SIRS;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.PluginImporter;
import fr.sirs.importer.documentTroncon.document.convention.NewConventionImporter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class AotCotImporter implements PluginImporter {

    
    private SysEvtNewConventionImporter conventionLocImporter;
    private NewConventionImporter conventionImporter;
    
    private CouchDbConnector connector;
    private Database database;
    
    @Override
    public void importation(DbImporter coreImporter) {
        SIRS.LOGGER.log(Level.INFO, "Intérieur d'importation de plugin");
        try {
            connector = coreImporter.getConnector();
            database = coreImporter.getDatabase();
            conventionImporter = new NewConventionImporter(database, connector, 
                    coreImporter.getIntervenantImporter(), 
                    coreImporter.getOrganismeImporter());
            conventionLocImporter = new SysEvtNewConventionImporter(database, connector, 
                    coreImporter.getTronconGestionDigueImporter(), 
                    coreImporter.getBorneDigueImporter(), 
                    coreImporter.getSystemeReperageImporter(), 
                    conventionImporter);
    
            conventionLocImporter.getPositions();
        } catch (IOException ex) {
            Logger.getLogger(AotCotImporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccessDbImporterException ex) {
            Logger.getLogger(AotCotImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
