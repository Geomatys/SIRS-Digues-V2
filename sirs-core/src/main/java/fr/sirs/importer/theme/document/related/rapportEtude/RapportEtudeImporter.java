package fr.sirs.importer.theme.document.related.rapportEtude;

import fr.sirs.importer.theme.document.related.convention.*;
import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.ConventionRepository;
import fr.sirs.core.component.RapportEtudeRepository;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.RefConvention;
import fr.sirs.core.model.RefRapportEtude;
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
public class RapportEtudeImporter extends GenericImporter {

    private Map<Integer, RapportEtude> rapportsEtude = null;
    private RapportEtudeRepository rapportEtudeRepository;
    private TypeRapportEtudeImporter typeRapportEtudeImporter;
    
    private RapportEtudeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public RapportEtudeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final RapportEtudeRepository rapportEtudeRepository,
            final TypeRapportEtudeImporter typeRapportEtudeImporter) {
        this(accessDatabase, couchDbConnector);
        this.rapportEtudeRepository = rapportEtudeRepository;
        this.typeRapportEtudeImporter = typeRapportEtudeImporter;
    }

    private enum RapportEtudeColumns {
        ID_RAPPORT_ETUDE,
//        TITRE_RAPPORT_ETUDE, // Pas dans le nouveau modèle
        ID_TYPE_RAPPORT_ETUDE,
//        AUTEUR_RAPPORT, // Pas dans le nouveau modèle
//        DATE_RAPPORT, // Pas dans le nouveau modèle
        REFERENCE_PAPIER,
        REFERENCE_NUMERIQUE,
//        COMMENTAIRE, // Pas dans le nouveau modèle
//        DATE_DERNIERE_MAJ // Pas dans le nouveau modèle
    };
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (RapportEtudeColumns c : RapportEtudeColumns.values()) {
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
        rapportsEtude = new HashMap<>();
        
        final Map<Integer, RefRapportEtude> typesRapport = typeRapportEtudeImporter.getTypeRapportEtude();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RapportEtude rapport = new RapportEtude();
            
            rapport.setTypeRapportEtudeId(typesRapport.get(row.getInt(RapportEtudeColumns.ID_TYPE_RAPPORT_ETUDE.toString())).getId());
            
            rapport.setReference_papier(row.getString(RapportEtudeColumns.REFERENCE_PAPIER.toString()));
            
            rapport.setReference_numerique(row.getString(RapportEtudeColumns.REFERENCE_NUMERIQUE.toString()));
            
            rapportsEtude.put(row.getInt(RapportEtudeColumns.ID_RAPPORT_ETUDE.toString()), rapport);
            rapportEtudeRepository.add(rapport);
        }
    }
    
    /**
     *
     * @return A map containing all RapportEtude instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, RapportEtude> getRapportsEtude() throws IOException, AccessDbImporterException {
        if (rapportsEtude == null)  compute();
        return rapportsEtude;
    }
}
