/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sym.util.importer.AccessDbImporterException;
import fr.sym.util.importer.GenericImporter;
import fr.sym.util.importer.TronconGestionDigueImporter;
import fr.symadrem.sirs.core.model.PiedDigue;
import fr.symadrem.sirs.core.model.TronconDigue;
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
class PiedDigueImporter extends GenericImporter {

    private Map<Integer, PiedDigue> piedsDigue = null;
    private Map<Integer, List<PiedDigue>> piedsDigueByTronconId = null;
    private TronconGestionDigueImporter tronconGestionDigueImporter;

    private PiedDigueImporter(Database accessDatabase) {
        super(accessDatabase);
    }

    PiedDigueImporter(final Database accessDatabase, final TronconGestionDigueImporter tronconGestionDigueImporter) {
        this(accessDatabase);
        this.tronconGestionDigueImporter = tronconGestionDigueImporter;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for(PiedDigueColumns c : PiedDigueColumns.values())
            columns.add(c.toString());
        return columns;
    }

    @Override
    public String getTableName() {
        return "SYS_EVT_PIED_DE_DIGUE";
    }
    
    private enum PiedDigueColumns {
//        ID_ELEMENT_STRUCTURE,
//        id_nom_element,
//        ID_SOUS_GROUPE_DONNEES,
//        LIBELLE_TYPE_ELEMENT_STRUCTURE,
//        DECALAGE_DEFAUT,
//        DECALAGE,
//        LIBELLE_SOURCE,
//        LIBELLE_TYPE_COTE,
//        LIBELLE_SYSTEME_REP,
//        NOM_BORNE_DEBUT,
//        NOM_BORNE_FIN,
//        LIBELLE_TYPE_MATERIAU,
//        LIBELLE_TYPE_NATURE,
//        LIBELLE_TYPE_FONCTION,
//        ID_TYPE_ELEMENT_STRUCTURE,
//        ID_TYPE_COTE,
//        ID_SOURCE,
//        ID_TRONCON_GESTION,
//        DATE_DEBUT_VAL,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        ID_SYSTEME_REP,
//        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
//        DIST_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
//        DIST_BORNEREF_FIN,
//        COMMENTAIRE,
//        N_COUCHE,
//        ID_TYPE_MATERIAU,
//        ID_TYPE_NATURE,
//        ID_TYPE_FONCTION,
//        ID_AUTO
        
        
        // Empty fields
//     LIBELLE_TYPE_NATURE_HAUT,
//     LIBELLE_TYPE_MATERIAU_HAUT,
//     LIBELLE_TYPE_NATURE_BAS,
//     LIBELLE_TYPE_MATERIAU_BAS,
//     LIBELLE_TYPE_OUVRAGE_PARTICULIER,
//     LIBELLE_TYPE_POSITION,
//     RAISON_SOCIALE_ORG_PROPRIO,
//     RAISON_SOCIALE_ORG_GESTION,
//     INTERV_PROPRIO,
//     INTERV_GARDIEN,
//     LIBELLE_TYPE_COMPOSITION,
//     LIBELLE_TYPE_VEGETATION,
        //     DATE_FIN_VAL,
        //     X_DEBUT,
        //     Y_DEBUT,
        //     X_FIN,
        //     Y_FIN,
        //     EPAISSEUR,
        //     TALUS_INTERCEPTE_CRETE,
        //     ID_TYPE_NATURE_HAUT,
        //     ID_TYPE_MATERIAU_HAUT,
        //     ID_TYPE_MATERIAU_BAS,
        //     ID_TYPE_NATURE_BAS,
        //     LONG_RAMP_HAUT,
        //     LONG_RAMP_BAS,
        //     PENTE_INTERIEURE,
        //     ID_TYPE_OUVRAGE_PARTICULIER,
        //     ID_TYPE_POSITION,
        //     ID_ORG_PROPRIO,
        //     ID_ORG_GESTION,
        //     ID_INTERV_PROPRIO,
        //     ID_INTERV_GARDIEN,
        //     DATE_DEBUT_ORGPROPRIO,
        //     DATE_FIN_ORGPROPRIO,
        //     DATE_DEBUT_GESTION,
        //     DATE_FIN_GESTION,
        //     DATE_DEBUT_INTERVPROPRIO,
        //     DATE_FIN_INTERVPROPRIO,
        //     ID_TYPE_COMPOSITION,
        //     DISTANCE_TRONCON,
        //     LONGUEUR,
        //     DATE_DEBUT_GARDIEN,
        //     DATE_FIN_GARDIEN,
        //     LONGUEUR_PERPENDICULAIRE,
        //     LONGUEUR_PARALLELE,
        //     COTE_AXE,
        //     ID_TYPE_VEGETATION,
        //     HAUTEUR,
        //     DIAMETRE,
        //     DENSITE,
        //     EPAISSEUR_Y11,
        //     EPAISSEUR_Y12,
        //     EPAISSEUR_Y21,
        //     EPAISSEUR_Y22,
    };
        
    /**
     *
     * @return A map containing all TronconDigue instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, PiedDigue> getPiedsDigue() throws IOException, AccessDbImporterException {

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();

        if (this.piedsDigue == null) {
            this.piedsDigue = new HashMap<>();
            this.piedsDigueByTronconId = new HashMap<>();
            while (it.hasNext()) {
                final Row row = it.next();
                final PiedDigue piedDigue = new PiedDigue();
//            crete.setBorne_debut(borne_debut);
//            crete.setBorne_debut_aval(true);
//            crete.setBorne_debut_distance(borne_debut_distance);
//            crete.setBorne_fin(borne_debut);
//            crete.setBorne_fin_aval(true);
//            crete.setBorne_fin_distance(borne_debut_distance);
//            crete.setCommentaire(null);
//            crete.setContactStructure(null);
//            crete.setConventionIds(null);
//            crete.setCote(null);
//            crete.setDateMaj(LocalDateTime.MIN);
//            crete.setDate_debut(LocalDateTime.MIN);
//            crete.setDate_fin(LocalDateTime.MIN);EPAISSEUR
//            if(row.getDouble("EPAISSEUR")!=null) desordre.setEpaisseur(row.getDouble("EPAISSEUR").floatValue());
//            crete.setFonction(null);
//            crete.setGeometry(null);
//            crete.setListeCote(null);
//            crete.setListeFonction(null);
//            crete.setListeMateriau(null);
//            crete.setListeSource(null);
//            crete.setMateriau(null);N_COUCHE
//            desordre.setNum_couche(row.getInt("N_COUCHE"));
//            crete.setOrganismeStructure(null);
//            crete.setPR_debut(PR_debut);
//            crete.setPR_fin(PR_fin);
//            crete.setParent(crete);
//            crete.setPosition(null);
//            crete.setPosition_structure(null);
//            crete.setSource(null);
//            crete.setSysteme_rep_id(systeme_rep_id);
                final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt("ID_TRONCON_GESTION"));
                if (troncon.getId() != null) {
                    piedDigue.setTroncon(troncon.getId());
                } else {
                    throw new AccessDbImporterException("Le tronçon "
                            + tronconGestionDigueImporter.getTronconsDigues().get(row.getInt("ID_TRONCON_GESTION")) + " n'a pas encore d'identifiant CouchDb !");
                }

//            tronconDigue.setNom(row.getString(TronconGestionDigueColumns.NOM.toString()));
//            tronconDigue.setCommentaire(row.getString(TronconGestionDigueColumns.COMMENTAIRE.toString()));
//            if (row.getDate(TronconGestionDigueColumns.MAJ.toString()) != null) {
//                tronconDigue.setDateMaj(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.MAJ.toString()).toString(), dateTimeFormatter));
//            }
//            if (row.getDate(TronconGestionDigueColumns.DEBUT_VAL_TRONCON.toString()) != null) {
//                tronconDigue.setDate_debut(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.DEBUT_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
//            }
//            if (row.getDate(TronconGestionDigueColumns.FIN_VAL_TRONCON.toString()) != null) {
//                tronconDigue.setDate_fin(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.FIN_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
//            }
//
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
                piedsDigue.put(row.getInt("ID_ELEMENT_STRUCTURE"), piedDigue);

                // Set the list ByTronconId
                List<PiedDigue> listByTronconId = piedsDigueByTronconId.get(row.getInt("ID_TRONCON_GESTION"));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    piedsDigueByTronconId.put(row.getInt("ID_TRONCON_GESTION"), listByTronconId);
                }
                listByTronconId.add(piedDigue);
                piedsDigueByTronconId.put(row.getInt("ID_TRONCON_GESTION"), listByTronconId);
//
//            // Set the references.
//            tronconDigue.setDigueId(digueIds.get(row.getInt(TronconGestionDigueColumns.DIGUE.toString())).getId());
//            
//            final List<GestionTroncon> gestions = new ArrayList<>();
//            this.getGestionnaires().stream().forEach((gestion) -> {gestions.add(gestion);});
//            tronconDigue.setGestionnaires(gestions);
//            
//            tronconDigue.setTypeRive(typesRive.get(row.getInt(TronconGestionDigueColumns.TYPE_RIVE.toString())).toString());
//
//            // Set the geometry
//            tronconDigue.setGeometry(tronconDigueGeoms.get(row.getInt(TronconGestionDigueColumns.ID.toString())));
//            
//            tronconsDigues.add(tronconDigue);
            }
        }
        return piedsDigue;
    }

    /**
     *
     * @return A map containing all TronconDigue instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, List<PiedDigue>> getPiedsDigueByTronconId() throws IOException, AccessDbImporterException {
        if (this.piedsDigueByTronconId == null) {
            this.getPiedsDigue();
        }
        return this.piedsDigueByTronconId;
    }
}
