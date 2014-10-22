/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.symadrem.sirs.core.model.BorneDigue;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.SystemeReperage;
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
public class BorneDigueImporter extends GenericImporter {

    private Map<Integer, BorneDigue> bornesDigue = null;
    private Map<Integer, List<BorneDigue>> bornesDigueByTronconId = null;

    public BorneDigueImporter(Database accessDatabase) {
        super(accessDatabase);
    }

    /*==========================================================================
     BORNE_DIGUE
     ----------------------------------------------------------------------------
    x ID_BORNE
    x ID_TRONCON_GESTION
    * NOM_BORNE
    X_POINT
    Y_POINT
    Z_POINT
    * DATE_DEBUT_VAL
    * DATE_FIN_VAL
    * COMMENTAIRE_BORNE
    * FICTIVE
    X_POINT_ORIGINE
    Y_POINT_ORIGINE
    * DATE_DERNIERE_MAJ
     */
    public static enum BorneDigueColumns {

        ID("ID_BORNE"), ID_TRONCON("ID_TRONCON_GESTION"), NOM("NOM_BORNE"),
        X("X_POINT"), Y("Y_POINT"), Z("Z_POINT"), DEBUT("DATE_DEBUT_VAL"), FIN("DATE_FIN_VAL"),
        FICTIVE("FICTIVE"), X_ORIGINE("X_POINT_ORIGINE"), Y_ORIGINE("Y_POINT_ORIGINE"),
        COMMENTAIRE("COMMENTAIRE_BORNE"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private BorneDigueColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };

    public Map<Integer, BorneDigue> getBorneDigue() throws IOException {

        if (bornesDigue == null) {
            bornesDigue = new HashMap<>();
            bornesDigueByTronconId = new HashMap<>();
            final Iterator<Row> it = this.accessDatabase.getTable("BORNE_DIGUE").iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final BorneDigue borne = new BorneDigue();
                
                borne.setNom(row.getString(BorneDigueColumns.NOM.toString()));
                borne.setCommentaire(row.getString(BorneDigueColumns.COMMENTAIRE.toString()));
                if (row.getDate(BorneDigueColumns.MAJ.toString()) != null) {
                    borne.setDateMaj(LocalDateTime.parse(row.getDate(BorneDigueColumns.MAJ.toString()).toString(), dateTimeFormatter));
                }
                if (row.getDate(BorneDigueColumns.DEBUT.toString()) != null) {
                    borne.setDate_debut(LocalDateTime.parse(row.getDate(BorneDigueColumns.DEBUT.toString()).toString(), dateTimeFormatter));
                }
                if (row.getDate(BorneDigueColumns.FIN.toString()) != null) {
                    borne.setDate_fin(LocalDateTime.parse(row.getDate(BorneDigueColumns.FIN.toString()).toString(), dateTimeFormatter));
                }
                borne.setFictive(row.getBoolean(BorneDigueColumns.FICTIVE.toString()));
                GeometryFactory geometryFactory = new GeometryFactory();
                if (row.getDouble(BorneDigueColumns.Z.toString()) != null) {
                    borne.setPositionBorne(
                            geometryFactory.createPoint(new Coordinate(
                                            row.getDouble(BorneDigueColumns.X.toString()),
                                            row.getDouble(BorneDigueColumns.Y.toString()),
                                            row.getDouble(BorneDigueColumns.Z.toString()))));
                } else {
                    borne.setPositionBorne(
                            geometryFactory.createPoint(new Coordinate(
                                            row.getDouble(BorneDigueColumns.X.toString()),
                                            row.getDouble(BorneDigueColumns.Y.toString()))));
                }
                
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                bornesDigue.put(row.getInt(BorneDigueColumns.ID.toString()), borne);
                
                // Set the list ByTronconId
                List<BorneDigue> listByTronconId = bornesDigueByTronconId.get(row.getInt(BorneDigueColumns.ID_TRONCON.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    bornesDigueByTronconId.put(row.getInt(BorneDigueColumns.ID_TRONCON.toString()), listByTronconId);
                }
                listByTronconId.add(borne);
                bornesDigueByTronconId.put(row.getInt(BorneDigueColumns.ID_TRONCON.toString()), listByTronconId);
            }
        }
        return bornesDigue;
    }
    
    public Map<Integer, List<BorneDigue>> getBorneDigueByTronconId() throws IOException{
        if(bornesDigueByTronconId==null) this.getBorneDigue();
        return bornesDigueByTronconId;
    }
}
