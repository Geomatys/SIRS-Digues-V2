package fr.sirs.importer.objet.monteeDesEaux;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.objet.TypeRefHeauImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
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
public class MonteeDesEauxImporter extends GenericMonteeDesEauxImporter {

    private final MonteeDesEauxMesuresImporter monteeDesEauxMesuresImporter;
    private final SysEvtMonteeDesEauHydroImporter sysEvtMonteeDesEauHydroImporter;
    
    private final ElementReseauImporter elementReseauImporter;

    public MonteeDesEauxImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final ElementReseauImporter elementReseauImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final IntervenantImporter intervenantImporter,
            final TypeRefHeauImporter typeRefHeauImporter,
            final SourceInfoImporter sourceInfoImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                evenementHydrauliqueImporter);
        monteeDesEauxMesuresImporter = new MonteeDesEauxMesuresImporter(
                accessDatabase, couchDbConnector, intervenantImporter,
                sourceInfoImporter, typeRefHeauImporter);
        sysEvtMonteeDesEauHydroImporter = new SysEvtMonteeDesEauHydroImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                evenementHydrauliqueImporter, monteeDesEauxMesuresImporter);
        this.elementReseauImporter = elementReseauImporter;
    }

    private enum Columns {

        ID_MONTEE_DES_EAUX,
        //        ID_EVENEMENT_HYDRAU,
        ID_TRONCON_GESTION,
        //        PR_CALCULE,
        //        X,
        //        Y,
        //        ID_SYSTEME_REP,
        //        ID_BORNEREF,
        //        AMONT_AVAL,
        //        DIST_BORNEREF,
        //        COMMENTAIRE,
        ID_ECHELLE_LIMNI,// Correspondance ?? Référence quelle table ??
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
        return MONTEE_DES_EAUX.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();

        Map<Integer, ObjetReseau> reseaux = elementReseauImporter.getById();
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final MonteeEaux objet = importRow(row);

            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                objet.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            if (row.getInt(Columns.ID_ECHELLE_LIMNI.toString()) != null) {
                final ObjetReseau reseau = reseaux.get(row.getInt(Columns.ID_ECHELLE_LIMNI.toString()));
                if(reseau instanceof EchelleLimnimetrique){
                    objet.setEchelleLimnimetriqueId(reseau.getId());
                }
            }

            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            objets.put(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()), objet);

            // Set the list ByTronconId
            List<MonteeEaux> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                objetsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(objet);
        }
        context.outputDb.executeBulk(objets.values());
        
        ////////////////////////////////////////////////////////////////////////
        // Mise à jour des événements hydrauliques
        ////////////////////////////////////////////////////////////////////////
        final List<EvenementHydraulique> evenementsToUpdate = new ArrayList<>();
        for(final MonteeEaux montee : objets.values()){
            final String evenementId = montee.getEvenementHydrauliqueId();
            if(evenementId!=null){
                final EvenementHydraulique evenement = evenementHydrauliqueImporter.getEvenementsByCouchDBId().get(evenementId);
                if(evenement!=null){
                    evenement.getMonteeEauxIds().add(montee.getId());
                    evenementsToUpdate.add(evenement);
                }
                // Si on n'a pas d'evenement correspondant on annule l'id de l'evenement de la mesure pour retrouver une intégrité des données.
                else {
                    montee.setEvenementHydrauliqueId(null);
                }
            }
        }
        context.outputDb.executeBulk(evenementsToUpdate);
    }

    @Override
    public public  importRow(Row row) throws IOException, AccessDbImporterException {
        return sysEvtMonteeDesEauHydroImporter.importRow(row);
    }
}
