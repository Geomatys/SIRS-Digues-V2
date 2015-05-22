package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.documentTroncon.document.DocumentManager;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.documentTroncon.document.GenericDocumentRelatedImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
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
public class PositionDocumentImporter extends GenericPositionDocumentImporter<AbstractPositionDocument> {
    
    private final DocumentManager documentManager;
    private final List<GenericDocumentRelatedImporter> documentRelatedImporters;
    
    private final TypeDocumentImporter typeDocumentImporter;
    
    private final SysEvtConventionImporter sysEvtConventionImporter;
    private final SysEvtProfilEnLongImporter sysEvtProfilLongImporter;
    private final SysEvtProfilEnTraversImporter sysEvtProfilTraversImporter;
    private final SysEvtRapportEtudesImporter sysEvtRapportEtudeImporter;
    private final SysEvtJournalImporter sysEvtJournalImporter;
    private final SysEvtMarcheImporter sysEvtMarcheImporter;
    private final SysEvtDocumentAGrandeEchelleImporter sysEvtDocumentAGrandeEchelleImporter;
    
    public PositionDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final BorneDigueImporter borneDigueImporter,
            final IntervenantImporter intervenantImporter,
            final OrganismeImporter organismeImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter){
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                borneDigueImporter, systemeReperageImporter);
        this.typeDocumentImporter = new TypeDocumentImporter(accessDatabase, 
                couchDbConnector);
        documentManager = new DocumentManager(accessDatabase, couchDbConnector, 
                organismeImporter, intervenantImporter, systemeReperageImporter,
                evenementHydrauliqueImporter, typeDocumentImporter);
        
        documentRelatedImporters = documentManager.getDocumentRelatedImporters();
        
        sysEvtConventionImporter = new SysEvtConventionImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                borneDigueImporter, systemeReperageImporter, 
                documentManager.getConventionImporter());
        sysEvtProfilTraversImporter = new SysEvtProfilEnTraversImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                borneDigueImporter, systemeReperageImporter, 
                documentManager.getProfilEnTraversImporter());
        sysEvtProfilLongImporter = new SysEvtProfilEnLongImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                borneDigueImporter, systemeReperageImporter, 
                documentManager.getProfilEnLongImporter());
        sysEvtRapportEtudeImporter = new SysEvtRapportEtudesImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                borneDigueImporter, systemeReperageImporter, 
                documentManager.getRapportEtudeImporter());
        sysEvtJournalImporter = new SysEvtJournalImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, borneDigueImporter,
                systemeReperageImporter, documentManager.getJournalArticleImporter());
        sysEvtMarcheImporter = new SysEvtMarcheImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, borneDigueImporter,
                systemeReperageImporter, documentManager.getMarcheImporter());
        sysEvtDocumentAGrandeEchelleImporter = new SysEvtDocumentAGrandeEchelleImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                borneDigueImporter, systemeReperageImporter, documentManager.getDocumentAGrandeEchelleImporter());
    }
    
    public DocumentManager getDocumentManager() {return this.documentManager;}

//    @Override
//    public void update() throws IOException, AccessDbImporterException {
//        for(final GenericDocumentRelatedImporter related : documentRelatedImporters){
//            related.update();
//        }
//        if(positions==null) compute();
//    }
    
    private enum Columns {
        ID_DOC,
        ID_TRONCON_GESTION,
        ID_TYPE_DOCUMENT,
////        ID_DOSSIER, // Pas dans le nouveau modèle
////        REFERENCE_PAPIER, // Pas dans le nouveau modèle
////        REFERENCE_NUMERIQUE, // Pas dans le nouveau modèle
////        REFERENCE_CALQUE, // Pas dans le nouveau modèle
////        DATE_DOCUMENT,
////        DATE_DEBUT_VAL, // Pas dans le nouveau modèle
////        DATE_FIN_VAL, // Pas dans le nouveau modèle
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
////        NOM,
////        ID_MARCHE,
////        ID_INTERV_CREATEUR,
////        ID_ORG_CREATEUR,
//        ID_ARTICLE_JOURNAL,
//        ID_PROFIL_EN_TRAVERS,
////        ID_PROFIL_EN_LONG, // Utilisation interdite ! C'est ID_DOC qui est utilisé par les profils en long !
////        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
//        ID_CONVENTION,
////        DATE_DERNIERE_MAJ,
////        AUTEUR_RAPPORT,
//        ID_RAPPORT_ETUDE
    }

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
        return DOCUMENT.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        positions = new HashMap<>();
        positionsByTronconId = new HashMap<>();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final AbstractPositionDocument position = importRow(row);
            position.setDesignation(String.valueOf(row.getInt(Columns.ID_DOC.toString())));
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            positions.put(row.getInt(Columns.ID_DOC.toString()), position);

            // Set the list ByTronconId
            List<AbstractPositionDocument> listByTronconId = positionsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                positionsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(position);
                    
        }
        couchDbConnector.executeBulk(positions.values());
    }

    @Override
    AbstractPositionDocument importRow(Row row) throws IOException, AccessDbImporterException {
        
        final Map<Integer, Class> classesDocument = typeDocumentImporter.getClasseDocument();
        
        final Class classeDocument = classesDocument.get(row.getInt(Columns.ID_TYPE_DOCUMENT.toString()));

        if (classeDocument != null) {

            if (classeDocument.equals(Convention.class)) {
                return sysEvtConventionImporter.importRow(row);
            } 
            else if (classeDocument.equals(DocumentGrandeEchelle.class)){
                return sysEvtDocumentAGrandeEchelleImporter.importRow(row);
            }
            else if(classeDocument.equals(ArticleJournal.class)){
                return sysEvtJournalImporter.importRow(row);
            }
            else if(classeDocument.equals(Marche.class)){
                return sysEvtMarcheImporter.importRow(row);
            }
            else if(classeDocument.equals(ProfilLong.class)){
                return sysEvtProfilLongImporter.importRow(row);
            }
            else if(classeDocument.equals(ProfilTravers.class)){
                return sysEvtProfilTraversImporter.importRow(row);
            }
            else if(classeDocument.equals(RapportEtude.class)){
                return sysEvtRapportEtudeImporter.importRow(row);
            }
            else {
                SirsCore.LOGGER.log(Level.FINE, "Type de document non pris en charge : ID = " + row.getInt(Columns.ID_TYPE_DOCUMENT.toString()));
                return null;
            }
        } else {
            SirsCore.LOGGER.log(Level.FINE, "Type de document inconnu !");
                return null;
        }
    }
}
