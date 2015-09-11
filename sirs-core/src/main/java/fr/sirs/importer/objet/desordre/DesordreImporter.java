package fr.sirs.importer.objet.desordre;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.core.model.Desordre;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DesordreImporter extends GenericDesordreImporter {

    private final TypeDesordreImporter typeDesordreImporter;
    private final SysEvtDesordreImporter sysEvtDesordreImporter;
    private final DesordreObservationImporter desordreObservationImporter;

    public DesordreObservationImporter getDesordreObservationImporter(){
        return desordreObservationImporter;
    }

    private enum Columns {

        ID_DESORDRE,
        //        ID_TYPE_DESORDRE,
        //        ID_TYPE_COTE,
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
        //        COMMENTAIRE, // Apparemment bsolète voir le champ DESCRIPTION_DESORDRE
        //        LIEU_DIT_DESORDRE,
        //        ID_TYPE_POSITION,
        //        ID_PRESTATION, // La colonne est vide dans la base de l'Isère. Il s'agit visiblement d'une colonne obsolète remplacée par la table d'association DESORDRE_PRESTATION
        //        ID_CRUE, // La colonne est vide dans la base de l'Isère. Il s'agit visiblement d'une colonne obsolète remplacée par la table d'association DESORDRE_EVENEMENT_HYDRAULIQUE
        //        DESCRIPTION_DESORDRE,
        //        DISPARU,
        //        DEJA_OBSERVE,
        DATE_DERNIERE_MAJ
    };

    @Override
    public String getTableName() {
        return DESORDRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();

        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Desordre objet = importRow(row);



            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            objets.put(row.getInt(Columns.ID_DESORDRE.toString()), objet);

            // Set the list ByTronconId
            List<Desordre> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                objetsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(objet);
        }

        context.outputDb.executeBulk(objets.values());
    }

    @Override
    public public  importRow(Row row) throws IOException, AccessDbImporterException {
        return sysEvtDesordreImporter.importRow(row);
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
