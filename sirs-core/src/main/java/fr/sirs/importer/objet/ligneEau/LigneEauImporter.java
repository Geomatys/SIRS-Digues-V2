package fr.sirs.importer.objet.ligneEau;


import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LigneEauImporter extends GenericLigneEauImporter {

    private final LigneEauMesuresPrzImporter ligneEauMesuresPrzImporter;
    private final LigneEauMesuresXyzImporter ligneEauMesuresXyzImporter;
    private final SysEvtLigneEauImporter sysEvtLigneEauImporter;

    @Override
    public public  importRow(Row row) throws IOException, AccessDbImporterException {
        return sysEvtLigneEauImporter.importRow(row);
    }

    private enum Columns {
        ID_LIGNE_EAU,
//        ID_EVENEMENT_HYDRAU,
        ID_TRONCON_GESTION,
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
//        ID_TYPE_REF_HEAU,
        ID_SYSTEME_REP_PRZ,
//        DATE,
//        COMMENTAIRE,
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
        return LIGNE_EAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();

        final Map<Integer, SystemeReperage> srs = systemeReperageImporter.getSystemeRepLineaire();

        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LigneEau objet = importRow(row);

            if(objet!=null){
                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    objet.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
                }

                if(row.getInt(Columns.ID_SYSTEME_REP_PRZ.toString())!=null){
                    objet.setSystemeRepDzId(srs.get(row.getInt(Columns.ID_SYSTEME_REP_PRZ.toString())).getId());
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                objets.put(row.getInt(Columns.ID_LIGNE_EAU.toString()), objet);

                // Set the list ByTronconId
                List<LigneEau> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    objetsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
        context.outputDb.executeBulk(objets.values());

        ////////////////////////////////////////////////////////////////////////
        // Mise à jour des événements hydrauliques
        ////////////////////////////////////////////////////////////////////////
        final List<EvenementHydraulique> evenementsToUpdate = new ArrayList<>();
        for(final LigneEau ligne : objets.values()){
            final String evenementId = ligne.getEvenementHydrauliqueId();
            if(evenementId!=null){
                final EvenementHydraulique evenement = evenementHydrauliqueImporter.getEvenementsByCouchDBId().get(evenementId);
                if(evenement!=null){
                    evenement.getLigneEauIds().add(ligne.getId());
                    evenementsToUpdate.add(evenement);
                }
                // Si on n'a pas d'evenement correspondant on annule l'id de l'evenement de la mesure pour retrouver une intégrité des données.
                else {
                    ligne.setEvenementHydrauliqueId(null);
                }
            }
        }
        context.outputDb.executeBulk(evenementsToUpdate);
    }
}
