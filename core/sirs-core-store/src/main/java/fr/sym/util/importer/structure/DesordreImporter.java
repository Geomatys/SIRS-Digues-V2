/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sym.util.importer.AccessDbImporterException;
import fr.sym.util.importer.DbImporter;
import fr.sym.util.importer.SystemeReperageImporter;
import fr.sym.util.importer.TronconGestionDigueImporter;
import fr.symadrem.sirs.core.model.Desordre;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class DesordreImporter extends GenericStructureImporter {

    private Map<Integer, Desordre> desordres = null;
    private Map<Integer, List<Desordre>> desordresByTronconId = null;

    DesordreImporter(Database accessDatabase, TronconGestionDigueImporter tronconGestionDigueImporter, SystemeReperageImporter systemeReperageImporter) {
        super(accessDatabase, tronconGestionDigueImporter, systemeReperageImporter);
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_DESORDRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();

        this.desordres = new HashMap<>();
        this.desordresByTronconId = new HashMap<>();
        while (it.hasNext()) {
            final Row row = it.next();
            final Desordre desordre = new Desordre();

            if (row.getDouble(DesordreColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                desordre.setBorne_debut_distance(row.getDouble(DesordreColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            if (row.getDouble(DesordreColumns.DIST_BORNEREF_FIN.toString()) != null) {
                desordre.setBorne_fin_distance(row.getDouble(DesordreColumns.DIST_BORNEREF_FIN.toString()).floatValue());
            }

            final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                desordre.setTroncon(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }

            if (row.getDouble(DesordreColumns.PR_DEBUT_CALCULE.toString()) != null) {
                desordre.setPR_debut(row.getDouble(DesordreColumns.PR_DEBUT_CALCULE.toString()).floatValue());
            }

            if (row.getDouble(DesordreColumns.PR_FIN_CALCULE.toString()) != null) {
                desordre.setPR_fin(row.getDouble(DesordreColumns.PR_FIN_CALCULE.toString()).floatValue());
            }
            desordre.setSysteme_rep_id(systemeReperageImporter.getSystemeRepLineaire().get(row.getInt(DesordreColumns.ID_SYSTEME_REP.toString())).getId());

            if (row.getDate(DesordreColumns.DATE_DEBUT_VAL.toString()) != null) {
                desordre.setDate_debut(LocalDateTime.parse(row.getDate(DesordreColumns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(DesordreColumns.DATE_FIN_VAL.toString()) != null) {
                desordre.setDate_fin(LocalDateTime.parse(row.getDate(DesordreColumns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
            desordres.put(row.getInt(DesordreColumns.ID_DESORDRE.toString()), desordre);

            // Set the list ByTronconId
            List<Desordre> listByTronconId = desordresByTronconId.get(row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                desordresByTronconId.put(row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(desordre);
            desordresByTronconId.put(row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
        }
    }

    private enum DesordreColumns {

        ID_DESORDRE,
        //            id_nom_element,
        //            ID_SOUS_GROUPE_DONNEES,
        //            LIBELLE_SOUS_GROUPE_DONNEES,
        //            ID_TYPE_DESORDRE,
        //            LIBELLE_TYPE_DESORDRE,
        //            DECALAGE_DEFAUT,
        //            DECALAGE,
        //            LIBELLE_SOURCE,
        //            LIBELLE_TYPE_COTE,
        //            LIBELLE_SYSTEME_REP,
        //            NOM_BORNE_DEBUT,
        //            NOM_BORNE_FIN,
        //            DISPARU_OUI_NON,
        //            DEJA_OBSERVE_OUI_NON,
        //            LIBELLE_TYPE_POSITION,
        //            ID_TYPE_COTE,
        //            ID_TYPE_POSITION,
        ID_TRONCON_GESTION,
        //            ID_SOURCE,
                    DATE_DEBUT_VAL,
                    DATE_FIN_VAL,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
                    ID_SYSTEME_REP,
        //            ID_BORNEREF_DEBUT,
        //            AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
        //            ID_BORNEREF_FIN,
        //            AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN,
//            LIEU_DIT_DESORDRE,
//            DESCRIPTION_DESORDRE,
//            ID_AUTO

        //Empty fields
//     ID_PRESTATION,
//     LIBELLE_PRESTATION,
//     X_DEBUT,
//     Y_DEBUT,
//     X_FIN,
//     Y_FIN,
//     COMMENTAIRE,
    };

    /**
     *
     * @return A map containing all TronconDigue instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, Desordre> getDesordres() throws IOException, AccessDbImporterException {
        if (this.desordres == null) {
            compute();
        }
        return desordres;
    }

    /**
     *
     * @return A map containing all TronconDigue instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, List<Desordre>> getDesordresByTronconId() throws IOException, AccessDbImporterException {
        if (this.desordresByTronconId == null) {
            compute();
        }
        return this.desordresByTronconId;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (DesordreColumns c : DesordreColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
