/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Geometry;
import fr.symadrem.sirs.core.model.GestionTroncon;
import fr.symadrem.sirs.core.model.SystemeReperage;
import fr.symadrem.sirs.core.model.TronconDigue;
import fr.symadrem.sirs.core.model.TypeRive;
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
public class TronconGestionDigueImporter extends GenericImporter {

    private Map<Integer, TronconDigue> tronconDigueIds = null;
    private TronconDigueGeomImporter tronconDigueGeomImporter;
    private TypeRiveImporter typeRiveImporter;
    private SystemeReperageImporter systemeReperageImporter;
    private TronconGestionDigueGestionnaireImporter tronconGestionDigueGestionnaireImporter;
    private DigueImporter digueImporter;
    
    private TronconGestionDigueImporter(Database accessDatabase) {
        super(accessDatabase);
    }
    
    public TronconGestionDigueImporter(final Database accessDatabase, 
            final DigueImporter digueImporter,
            final TronconDigueGeomImporter tronconDigueGeomImporter, 
            final TypeRiveImporter typeRiveImporter, 
            final SystemeReperageImporter systemeReperageImporter,
            final TronconGestionDigueGestionnaireImporter tronconGestionDigueGestionnaireImporter){
        this(accessDatabase);
        this.digueImporter = digueImporter;
        this.tronconDigueGeomImporter = tronconDigueGeomImporter;
        this.typeRiveImporter = typeRiveImporter;
        this.systemeReperageImporter = systemeReperageImporter;
        this.tronconGestionDigueGestionnaireImporter = tronconGestionDigueGestionnaireImporter;
    }
    
    

    /*==========================================================================
     TRONCON_GESTION_DIGUE.
    ----------------------------------------------------------------------------
     x ID_TRONCON_GESTION
     x ID_ORG_GESTIONNAIRE // Dans la table TRONCON_GESTION_DIGUE_GESTIONNAIRE qui contient l'historique des gestionnaires.
     * ID_DIGUE
     * ID_TYPE_RIVE // On part a priori sur une enumeration statique.
     * DATE_DEBUT_VAL_TRONCON
     * DATE_FIN_VAL_TRONCON
     * NOM_TRONCON_GESTION
     * COMMENTAIRE_TRONCON
     x DATE_DEBUT_VAL_GESTIONNAIRE_D // Dans la table TRONCON_GESTION_DIGUE_GESTIONNAIRE qui contient l'historique des gestionnaires.
     x DATE_FIN_VAL_GESTIONNAIRE_D // Dans la table TRONCON_GESTION_DIGUE_GESTIONNAIRE qui contient l'historique des gestionnaires.
     ID_SYSTEME_REP_DEFAUT
     x LIBELLE_TRONCON_GESTION // Les libellés sont nulls et sont appelés à dispararaitre de la nouvelle base.
     * DATE_DERNIERE_MAJ
    ----------------------------------------------------------------------------
     * TODO : s'occuper du lien avec les gestionnaires.
     * TODO : s'occuper du lien avec les rives.
     TODO : s'occuper du lien avec les systèmes de repérage.
    TODO : faire les structures.
     */
    public static enum TronconGestionDigueColumns {

        ID("ID_TRONCON_GESTION"), GESTIONNAIRE("ID_ORG_GESTIONNAIRE"), DIGUE("ID_DIGUE"), TYPE_RIVE("ID_TYPE_RIVE"),
        DEBUT_VAL_TRONCON("DATE_DEBUT_VAL_TRONCON"), FIN_VAL_TRONCON("DATE_FIN_VAL_TRONCON"),
        NOM("NOM_TRONCON_GESTION"), COMMENTAIRE("COMMENTAIRE_TRONCON"),
        DEBUT_VAL_GESTIONNAIRE("DATE_DEBUT_VAL_GESTIONNAIRE_D"), FIN_VAL_GESTIONNAIRE("DATE_FIN_VAL_GESTIONNAIRE_D"), 
        SYSTEME_REP("ID_SYSTEME_REP_DEFAUT"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private TronconGestionDigueColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };
    

    public Map<Integer, TronconDigue> getTronconsDigues() throws IOException {

        if(tronconDigueIds == null){
            tronconDigueIds = new HashMap<>();
            final Iterator<Row> it = this.accessDatabase.getTable("TRONCON_GESTION_DIGUE").iterator();

            final Map<Integer, Geometry> tronconDigueGeoms = tronconDigueGeomImporter.getTronconDigueGeoms();
            final Map<Integer, TypeRive> typesRive = typeRiveImporter.getTypeRive();
            final Map<Integer, SystemeReperage> systemesRep = systemeReperageImporter.getSystemeRepLineaire();

            while (it.hasNext()) {
                final Row row = it.next();
                final TronconDigue tronconDigue = new TronconDigue();
                tronconDigue.setNom(row.getString(TronconGestionDigueColumns.NOM.toString()));
                tronconDigue.setCommentaire(row.getString(TronconGestionDigueColumns.COMMENTAIRE.toString()));
                if (row.getDate(TronconGestionDigueColumns.MAJ.toString()) != null) {
                    tronconDigue.setDateMaj(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.MAJ.toString()).toString(), dateTimeFormatter));
                }
                if (row.getDate(TronconGestionDigueColumns.DEBUT_VAL_TRONCON.toString()) != null) {
                    tronconDigue.setDate_debut(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.DEBUT_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
                }
                if (row.getDate(TronconGestionDigueColumns.FIN_VAL_TRONCON.toString()) != null) {
                    tronconDigue.setDate_fin(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.FIN_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
                tronconDigueIds.put(row.getInt(TronconGestionDigueColumns.ID.toString()), tronconDigue);

                // Set the references.
                tronconDigue.setDigueId(digueImporter.getDigues().get(row.getInt(TronconGestionDigueColumns.DIGUE.toString())).getId());

                final List<GestionTroncon> gestions = new ArrayList<>();
                tronconGestionDigueGestionnaireImporter.getGestionnaires().values().stream().forEach((gestion) -> {gestions.add(gestion);});
                tronconDigue.setGestionnaires(gestions);

                tronconDigue.setTypeRive(typesRive.get(row.getInt(TronconGestionDigueColumns.TYPE_RIVE.toString())).toString());

                // Set the geometry
                tronconDigue.setGeometry(tronconDigueGeoms.get(row.getInt(TronconGestionDigueColumns.ID.toString())));
            }
        }
            return tronconDigueIds;
    }
}
