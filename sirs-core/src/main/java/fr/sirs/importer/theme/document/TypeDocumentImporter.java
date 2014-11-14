package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.RefTypeDocument;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeDocumentImporter extends GenericImporter {

    private Map<Integer, Class> classesDocument = null;
    private Map<Integer, RefTypeDocument> typesDocument = null;

    TypeDocumentImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TypeDocumentGrandeEchelleImporter typeDocumentGrandeEchelleImporter) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum TypeDocumentColumns {
        ID_TYPE_DOCUMENT,
        LIBELLE_TYPE_DOCUMENT,
//        ID_TYPE_GENERAL_DOCUMENT, // Ignoré dans le nouveau modèle
        NOM_TABLE_EVT,
//        ID_TYPE_OBJET_CARTO,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database types of Document elements 
     * (classes) referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, Class> getClasseDocument() throws IOException {
        if(classesDocument == null) compute();
        return classesDocument;
    }

    /**
     * 
     * @return A map containing all the database types of Document elements 
     * (RefTypeDocument) referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefTypeDocument> getTypeDocument() throws IOException {
        if(typesDocument == null) compute();
        return typesDocument;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeDocumentColumns c : TypeDocumentColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_DOCUMENT.toString();
    }

    @Override
    protected void compute() throws IOException {
        classesDocument = new HashMap<>();
        typesDocument = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            final RefTypeDocument typeDocument = new RefTypeDocument();
            
            typeDocument.setLibelle(row.getString(TypeDocumentColumns.LIBELLE_TYPE_DOCUMENT.toString()));
            
            if (row.getDate(TypeDocumentColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeDocument.setDateMaj(LocalDateTime.parse(row.getDate(TypeDocumentColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
//            if (row.getInt(TypeDocumentColumns.ID_TYPE_GENERAL_DOCUMENT.toString()) != null) {
//                final RefDocumentGrandeEchelle typeDocumentGrandeEchelle = typeDocumentGrandeEchelleImporter.getTypeDocumentGrandeEchelle().get(row.getInt(TypeDocumentColumns.ID_TYPE_GENERAL_DOCUMENT.toString()));
//                if(typeDocumentGrandeEchelle!=null){
////                    typeDocument.set(typeDocumentGrandeEchelle.getId);
//                }
//            }
            
            try {
                final Class classe;
                final DbImporter.TableName table = DbImporter.TableName.valueOf(row.getString(TypeDocumentColumns.NOM_TABLE_EVT.toString()));
                switch (table) {
                    case SYS_EVT_CONVENTION:
                        classe = Convention.class;
                        break;
////                case SYS_EVT_COUPE_OUVRAGE:
////                    classe = TalusDigue.class; break;
//                case SYS_EVT_DOCUMENT_MARCHE:
//                    classe = .class; break;
//                case SYS_EVT_FICHE_INSPECTION_VISUELLE:
//                    classe = .class; break;
//                case SYS_EVT_JOURNAL:
//                    classe = .class; break;
//                case SYS_EVT_MARCHE:
//                    classe = .class; break;
//                case SYS_EVT_PLAN_TOPO:
//                    classe = .class; break;
                    case SYS_EVT_PROFIL_EN_LONG:
                        classe = ProfilLong.class;
                        break;
                    case SYS_EVT_PROFIL_EN_TRAVERS:
                        classe = ProfilTravers.class;
                        break;
                    case SYS_EVT_RAPPORT_ETUDES:
                        classe = RapportEtude.class;
                        break;
////                case SYS_EVT_SONDAGE:
////                    classe = OuvrageRevanche.class; break;
                    default:
                        classe = null;
                }
                
                classesDocument.put(row.getInt(String.valueOf(TypeDocumentColumns.ID_TYPE_DOCUMENT.toString())), classe);
                typesDocument.put(row.getInt(String.valueOf(TypeDocumentColumns.ID_TYPE_DOCUMENT.toString())), typeDocument);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        couchDbConnector.executeBulk(typesDocument.values());
    }
}
