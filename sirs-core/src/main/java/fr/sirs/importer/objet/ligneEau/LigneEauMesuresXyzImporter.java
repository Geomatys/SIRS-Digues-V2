package fr.sirs.importer.objet.ligneEau;

import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.MesureLigneEauXYZ;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
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
class LigneEauMesuresXyzImporter extends DocumentImporter {

    private Map<Integer, List<MesureLigneEauXYZ>> mesuresByLigneEau = null;

    private enum Columns {
        ID_LIGNE_EAU,
        X,
        Y,
        HAUTEUR_EAU,
//        PR_CALCULE,
        ID_POINT,
//        DATE_DERNIERE_MAJ
    };

    /**
     *
     * @return A map containing all the MesureLigneEau elements
     * referenced by the corresponding element reseau internal ID.
     * @throws IOException
     */
    public Map<Integer, List<MesureLigneEauXYZ>> getMesuresByLigneEau() throws IOException {
        if (mesuresByLigneEau == null) {
            compute();
        }
        return mesuresByLigneEau;
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
        return LIGNE_EAU_MESURES_XYZ.toString();
    }

    @Override
    protected void compute() throws IOException {
        mesuresByLigneEau = new HashMap<>();

        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final MesureLigneEauXYZ mesure = createAnonymValidElement(MesureLigneEauXYZ.class);

            if (row.getDouble(Columns.HAUTEUR_EAU.toString()) != null) {
                mesure.setZ(row.getDouble(Columns.HAUTEUR_EAU.toString()).floatValue());
            }

            if (row.getDouble(Columns.X.toString()) != null) {
                mesure.setX(row.getDouble(Columns.X.toString()).floatValue());
            }

            if (row.getDouble(Columns.Y.toString()) != null) {
                mesure.setY(row.getDouble(Columns.Y.toString()).floatValue());
            }

            // Pas d'ID : on met arbitrairement celui de la ligne d'eau comme pseudo id.
            mesure.setDesignation(String.valueOf(row.getInt(Columns.ID_LIGNE_EAU.toString())));

            // Set the list ByLigneEauId
            List<MesureLigneEauXYZ> listByEltReseauId = mesuresByLigneEau.get(row.getInt(Columns.ID_LIGNE_EAU.toString()));
            if (listByEltReseauId == null) {
                listByEltReseauId = new ArrayList<>();
                mesuresByLigneEau.put(row.getInt(Columns.ID_LIGNE_EAU.toString()), listByEltReseauId);
            }
            listByEltReseauId.add(mesure);
        }
    }
}
