package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.SIRS;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.PluginImporter;
import fr.sirs.importer.documentTroncon.document.convention.ConventionImporter;
import fr.sirs.importer.link.ElementReseauConventionImporter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class AotCotImporter implements PluginImporter {

    
    private PositionConventionImporter conventionLocImporter;
    private ConventionImporter conventionImporter;
    private ElementReseauConventionImporter elementReseauConventionImporter;
    
    private CouchDbConnector connector;
    private Database database;
    
    @Override
    public void importation(DbImporter coreImporter) {
        SIRS.LOGGER.log(Level.INFO, "Intérieur d'importation de plugin");
        try {
            connector = coreImporter.getConnector();
            database = coreImporter.getDatabase();
            conventionImporter = new ConventionImporter(database, connector, 
                    coreImporter.getIntervenantImporter(), 
                    coreImporter.getOrganismeImporter());
            conventionLocImporter = new PositionConventionImporter(database, connector, 
                    coreImporter.getTronconGestionDigueImporter(), 
                    coreImporter.getBorneDigueImporter(), 
                    coreImporter.getSystemeReperageImporter(), 
                    conventionImporter);
    
            conventionLocImporter.getPositions();
            
            elementReseauConventionImporter = new ElementReseauConventionImporter(
                    database, connector, coreImporter.getObjetManager().getElementReseauImporter(), conventionImporter);
            elementReseauConventionImporter.link();
            
            
        } catch (IOException ex) {
            Logger.getLogger(AotCotImporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccessDbImporterException ex) {
            Logger.getLogger(AotCotImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
