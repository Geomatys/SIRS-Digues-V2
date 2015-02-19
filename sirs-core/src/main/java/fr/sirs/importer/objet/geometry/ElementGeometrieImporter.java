package fr.sirs.importer.objet.geometry;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ProfilFrontFrancBord;
import fr.sirs.importer.objet.GenericObjetImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementGeometrieImporter extends GenericGeometrieImporter<Objet> {

    private final TypeElementGeometryImporter typeElementGeometryImporter;
    
    private final List<GenericObjetImporter> structureImporters = new ArrayList<>();
    private final TypeLargeurFrancBordImporter typeLargeurFrancBordImporter;
    private final SysEvtLargeurFrancBordImporter largeurFrancBordImporter;
    private final TypeProfilFrancBordImporter typeProfilFrontFrancBordImporter;
    private final SysEvtProfilFrontFrancBordImporter profilFrontFrancBordImporter;

    public ElementGeometrieImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final SourceInfoImporter typeSourceImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter);
        typeElementGeometryImporter = new TypeElementGeometryImporter(
                accessDatabase);
        typeLargeurFrancBordImporter = new TypeLargeurFrancBordImporter(
                accessDatabase, couchDbConnector);
        largeurFrancBordImporter = new SysEvtLargeurFrancBordImporter(accessDatabase,
                couchDbConnector,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeLargeurFrancBordImporter);
        structureImporters.add(largeurFrancBordImporter);
        typeProfilFrontFrancBordImporter = new TypeProfilFrancBordImporter(
                accessDatabase, couchDbConnector);
        profilFrontFrancBordImporter = new SysEvtProfilFrontFrancBordImporter(
                accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeProfilFrontFrancBordImporter);
        structureImporters.add(profilFrontFrancBordImporter);
    }

    private enum Columns {
        ID_ELEMENT_GEOMETRIE,
//        ID_TYPE_ELEMENT_GEOMETRIE,
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
        return DbImporter.TableName.ELEMENT_GEOMETRIE.toString();
    }
    
    @Override
    protected void compute() throws IOException, AccessDbImporterException {    
        structures = new HashMap<>();
        structuresByTronconId = new HashMap<>();

        // Remplissage initial des structures par les importateurs subordonnés.
        for (final GenericObjetImporter gsi : structureImporters){
            final Map<Integer, Objet> objets = gsi.getById();
            if(objets!=null){
                for (final Integer key : objets.keySet()){
                    if(structures.get(key)!=null){
                        throw new AccessDbImporterException(objets.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+structures.get(key).getClass().getCanonicalName());
                    }
                    else {
                        structures.put(key, objets.get(key));
                    }
                }
            }
            
            final Map<Integer, List<Objet>> objetsByTronconId = gsi.getByTronconId();

            if (objetsByTronconId != null) {
                objetsByTronconId.keySet().stream().map((key) -> {
                    if (structuresByTronconId.get(key) == null) {
                        structuresByTronconId.put(key, new ArrayList<>());
                    }
                    return key;
                }).forEach((key) -> {
                    if (objetsByTronconId.get(key) != null) {
                        structuresByTronconId.get(key).addAll(objetsByTronconId.get(key));
                    }
                });
            }
        }


        // Vérification de la cohérence des structures au sens strict.
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();

            final int structureId = row.getInt(Columns.ID_ELEMENT_GEOMETRIE.toString());
            final Objet objet;
            final boolean nouvelObjet;
            
            if(structures.get(structureId)!=null){
                objet = structures.get(structureId);
                nouvelObjet=false;
            }
            else{
                SirsCore.LOGGER.log(Level.FINE, "Nouvel objet !!");
                objet = importRow(row);
                nouvelObjet=true;
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                objet.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if (nouvelObjet) {
            
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_ELEMENT_GEOMETRIE.toString()), objet);

                // Set the list ByTronconId
                List<Objet> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
    }
    
    
    @Override
    public Objet importRow(Row row) throws IOException, AccessDbImporterException {
        final Class typeStructure = this.typeElementGeometryImporter.getTypeReferences().get(row.getInt(Columns.ID_ELEMENT_GEOMETRIE.toString()));
        if(typeStructure==LargeurFrancBord.class){
            return largeurFrancBordImporter.importRow(row);
        } else if(typeStructure==ProfilFrontFrancBord.class){
            return profilFrontFrancBordImporter.importRow(row);
        } else{
            SirsCore.LOGGER.log(Level.SEVERE, "Type incohérent.");
            return null;
        }
    }
}
