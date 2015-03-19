package fr.sirs.importer.documentTroncon.document.rapportEtude;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.RefRapportEtude;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.documentTroncon.document.GenericDocumentRelatedImporter;
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
public class RapportEtudeImporter extends GenericDocumentRelatedImporter<RapportEtude> {
    
    private final TypeRapportEtudeImporter typeRapportEtudeImporter;
    
    public RapportEtudeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
        this.typeRapportEtudeImporter = new TypeRapportEtudeImporter(
                accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_RAPPORT_ETUDE,
        TITRE_RAPPORT_ETUDE,
        ID_TYPE_RAPPORT_ETUDE,
        AUTEUR_RAPPORT,
        DATE_RAPPORT,
        REFERENCE_PAPIER,
        REFERENCE_NUMERIQUE,
        COMMENTAIRE,
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
        return DbImporter.TableName.RAPPORT_ETUDE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        related = new HashMap<>();
        
        final Map<Integer, RefRapportEtude> typesRapport = typeRapportEtudeImporter.getTypeReferences();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RapportEtude rapport = new RapportEtude();
            
            rapport.setAuteur(cleanNullString(row.getString(Columns.REFERENCE_PAPIER.toString())));
            
            rapport.setLibelle(cleanNullString(row.getString(Columns.TITRE_RAPPORT_ETUDE.toString())));
            
            rapport.setCommentaire(cleanNullString(row.getString(Columns.COMMENTAIRE.toString())));
            
            rapport.setTypeRapportEtudeId(typesRapport.get(row.getInt(Columns.ID_TYPE_RAPPORT_ETUDE.toString())).getId());
            
            rapport.setReferencePapier(cleanNullString(row.getString(Columns.REFERENCE_PAPIER.toString())));
            
            rapport.setReferenceNumerique(cleanNullString(row.getString(Columns.REFERENCE_NUMERIQUE.toString())));
            
            if (row.getDate(Columns.DATE_RAPPORT.toString()) != null) {
                rapport.setDate(LocalDateTime.parse(row.getDate(Columns.DATE_RAPPORT.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                rapport.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            rapport.setDesignation(String.valueOf(row.getInt(Columns.ID_RAPPORT_ETUDE.toString())));
            rapport.setValid(true);
            
            related.put(row.getInt(Columns.ID_RAPPORT_ETUDE.toString()), rapport);
        }
        couchDbConnector.executeBulk(related.values());
    }
}
