package fr.sirs.importer.objet.geometry;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.ObjetPhotographiable;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementGeometrieImporter extends GenericGeometrieImporter<ObjetPhotographiable> {

    private final TypeElementGeometryImporter typeElementGeometryImporter;
    
    private final TypeLargeurFrancBordImporter typeLargeurFrancBordImporter;
    private final SysEvtLargeurFrancBordImporter largeurFrancBordImporter;
    private final TypeProfilFrancBordImporter typeProfilFrontFrancBordImporter;

    public ElementGeometrieImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final SourceInfoImporter typeSourceImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter);
        typeElementGeometryImporter = new TypeElementGeometryImporter(
                accessDatabase);
        typeLargeurFrancBordImporter = new TypeLargeurFrancBordImporter(
                accessDatabase, couchDbConnector);
        largeurFrancBordImporter = new SysEvtLargeurFrancBordImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeLargeurFrancBordImporter);
        typeProfilFrontFrancBordImporter = new TypeProfilFrancBordImporter(
                accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_ELEMENT_GEOMETRIE,
        ID_TYPE_ELEMENT_GEOMETRIE,
//        ID_SOURCE,
        ID_TRONCON_GESTION,
//        DATE_DEBUT_VAL,
//        DATE_FIN_VAL,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        X_DEBUT,
//        Y_DEBUT,
//        X_FIN,
//        Y_FIN,
//        ID_SYSTEME_REP,
//        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
//        DIST_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
//        DIST_BORNEREF_FIN,
//        COMMENTAIRE,
//        ID_TYPE_LARGEUR_FB,
//        ID_TYPE_PROFIL_FB,
//        ID_TYPE_DIST_DIGUE_BERGE,
        DATE_DERNIERE_MAJ
    };

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return ELEMENT_GEOMETRIE.toString();
    }
    
    @Override
    protected void compute() throws IOException, AccessDbImporterException {    
        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();
        
        // Vérification de la cohérence des structures au sens strict.
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();

            final ObjetPhotographiable objet = importRow(row);
            
            if(objet!=null){
                
                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    objet.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
                }
            
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                objets.put(row.getInt(Columns.ID_ELEMENT_GEOMETRIE.toString()), objet);

                // Set the list ByTronconId
                List<ObjetPhotographiable> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    objetsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
        couchDbConnector.executeBulk(objets.values());
    }
    
    
    @Override
    public ObjetPhotographiable importRow(Row row) throws IOException, AccessDbImporterException {
        final Class typeStructure = this.typeElementGeometryImporter.getTypeReferences().get(row.getInt(Columns.ID_TYPE_ELEMENT_GEOMETRIE.toString()));
        if(typeStructure==LargeurFrancBord.class){
            return largeurFrancBordImporter.importRow(row);
        } else{
            SirsCore.LOGGER.log(Level.SEVERE, typeStructure+" : Type de géométrie incohérent.");
            return null;
        }
    }
}
