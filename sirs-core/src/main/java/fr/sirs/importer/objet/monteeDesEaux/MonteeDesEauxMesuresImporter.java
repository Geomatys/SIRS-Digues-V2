package fr.sirs.importer.objet.monteeDesEaux;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.MesureMonteeEaux;
import fr.sirs.core.model.RefReferenceHauteur;
import fr.sirs.core.model.RefSource;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.objet.TypeRefHeauImporter;
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
class MonteeDesEauxMesuresImporter extends GenericImporter {

    private Map<Integer, List<MesureMonteeEaux>> mesuresByMonteeDesEaux = null;
    
    private final IntervenantImporter intervenantImporter;
    private final SourceInfoImporter sourceInfoImporter;
    private final TypeRefHeauImporter typeRefHeauImporter;

    MonteeDesEauxMesuresImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final IntervenantImporter intervenantImporter,
            final SourceInfoImporter sourceInfoImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector);
        this.intervenantImporter = intervenantImporter;
        this.sourceInfoImporter = sourceInfoImporter;
        this.typeRefHeauImporter = typeRefHeauImporter;
    }

    private enum Columns {
        ID_MONTEE_DES_EAUX,
        DATE,
        ID_TYPE_REF_HEAU,
        HAUTEUR_EAU,
        DEBIT_MAX,
        ID_INTERV_OBSERVATEUR,
        ID_SOURCE,
        COMMENTAIRE,
        DATE_DERNIERE_MAJ,
    };

    /**
     *
     * @return A map containing all the MesureMonteeEaux elements
     * referenced by the corresponding element reseau internal ID.
     * @throws IOException
     */
    public Map<Integer, List<MesureMonteeEaux>> getMesuresByMonteeDesEaux() 
            throws IOException, AccessDbImporterException {
        if (mesuresByMonteeDesEaux == null) {
            compute();
        }
        return mesuresByMonteeDesEaux;
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
        return DbImporter.TableName.MONTEE_DES_EAUX_MESURES.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        mesuresByMonteeDesEaux = new HashMap<>();
        
        final Map<Integer, RefReferenceHauteur> typesRefHEau = typeRefHeauImporter.getTypeReferences();
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final MesureMonteeEaux mesure = new MesureMonteeEaux();
            
            if (row.getDate(Columns.DATE.toString()) != null) {
                mesure.setDate(DbImporter.parse(row.getDate(Columns.DATE.toString()), dateTimeFormatter));
            }
            
            if(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())!=null){
                mesure.setReferenceHauteurId(typesRefHEau.get(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())).getId());
            }
            
            if (row.getDouble(Columns.HAUTEUR_EAU.toString()) != null) {
                mesure.setHauteur(row.getDouble(Columns.HAUTEUR_EAU.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.DEBIT_MAX.toString()) != null) {
                mesure.setDebitMax(row.getDouble(Columns.DEBIT_MAX.toString()).floatValue());
            }
            
            if(row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString())!=null){
                mesure.setObservateurId(intervenants.get(row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                mesure.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                mesure.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            mesure.setCommentaire(cleanNullString(row.getString(Columns.COMMENTAIRE.toString())));
            
            // En l'absence d'identifiant propre, on met celui de la montée des eaux comme pseudo id.
            mesure.setDesignation(String.valueOf(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString())));
            mesure.setValid(true);
            
            // Set the list ByLigneEauId
            List<MesureMonteeEaux> listByEltReseauId = mesuresByMonteeDesEaux.get(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()));
            if (listByEltReseauId == null) {
                listByEltReseauId = new ArrayList<>();
                mesuresByMonteeDesEaux.put(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()), listByEltReseauId);
            }
            listByEltReseauId.add(mesure);
        }
    }
}
