package fr.sirs.importer.objet.laisseCrue;


import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.RefReferenceHauteur;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.GenericObjetImporter;
import fr.sirs.importer.objet.TypeRefHeauImporter;
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
public class LaisseCrueImporter extends GenericObjetImporter<LaisseCrue> {

    protected final IntervenantImporter intervenantImporter;
    protected final EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    protected final TypeRefHeauImporter typeRefHeauImporter;

    private enum Columns {
        ID_LAISSE_CRUE,
//        id_nom_element, // Redondant avec ID_LAISSE_CRUE
//        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        LIBELLE_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        DECALAGE_DEFAUT, // Affichage
//        DECALAGE, // Affichage
//        LIBELLE_SOURCE, // Redondant avec l'importation des sources
//        LIBELLE_SYSTEME_REP, // Redondant avec l'importation des SR
//        NOM_BORNE_DEBUT, // Redondant avec l'importation des bornes
//        NOM_BORNE_FIN, // Redondant avec l'importation des bornes
//        NOM_EVENEMENT_HYDRAU, // Redondant avec l'importation des événements hydrauliques
//        TypeRefHEau, // Redondant avec l'importation des hauteurs d'eau
//        NomPrenomObservateur, // Redondant avec l'importation des intervenants observateurs
        ID_SOURCE,
        ID_EVENEMENT_HYDRAU,
        DATE,
        ID_TYPE_REF_HEAU,
        HAUTEUR_EAU,
        ID_INTERV_OBSERVATEUR,
        POSITION,
//        ID_AUTO

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
        return LAISSE_CRUE.toString();
    }


    @Override
    public LaisseCrue importRow(Row row, LaisseCrue laisseCrue) throws IOException, AccessDbImporterException {
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        final Map<Integer, EvenementHydraulique> evenementsHydrau = evenementHydrauliqueImporter.getEvenements();

        final Map<Integer, RefReferenceHauteur> referenceHauteur = typeRefHeauImporter.getTypeReferences();

        if (row.getInt(Columns.ID_SOURCE.toString()) != null) {
            laisseCrue.setSourceId(sourceInfoImporter.getImportedId(row.getInt(Columns.ID_SOURCE.toString())));
        }

        if (row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString()) != null) {
            laisseCrue.setEvenementHydrauliqueId(evenementsHydrau.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())).getId());
        }

        if (row.getDate(Columns.DATE.toString()) != null) {
            laisseCrue.setDate(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE.toString()), dateTimeFormatter));
        }

        if (row.getInt(Columns.ID_TYPE_REF_HEAU.toString()) != null) {
            laisseCrue.setReferenceHauteurId(referenceHauteur.get(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())).getId());
        }

        if (row.getDouble(Columns.HAUTEUR_EAU.toString()) != null) {
            laisseCrue.setHauteur(row.getDouble(Columns.HAUTEUR_EAU.toString()).floatValue());
        }

        if (row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString()) != null) {
            laisseCrue.setObservateurId(intervenants.get(row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString())).getId());
        }

        laisseCrue.setPositionLaisse(cleanNullString(row.getString(Columns.POSITION.toString())));

        return laisseCrue;
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();

        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LaisseCrue objet = importRow(row);

            if(objet!=null){


                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                objets.put(row.getInt(Columns.ID_LAISSE_CRUE.toString()), objet);

                // Set the list ByTronconId
                List<LaisseCrue> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
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
        for(final LaisseCrue laisse : objets.values()){
            final String evenementId = laisse.getEvenementHydrauliqueId();
            if(evenementId!=null){
                final EvenementHydraulique evenement = evenementHydrauliqueImporter.getEvenementsByCouchDBId().get(evenementId);
                if(evenement!=null){
                    evenement.getLaisseCrueIds().add(laisse.getId());
                    evenementsToUpdate.add(evenement);
                }
                // Si on n'a pas d'evenement correspondant on annule l'id de l'evenement de la mesure pour retrouver une intégrité des données.
                else {
                    laisse.setEvenementHydrauliqueId(null);
                }
            }
        }
        context.outputDb.executeBulk(evenementsToUpdate);
    }
}
