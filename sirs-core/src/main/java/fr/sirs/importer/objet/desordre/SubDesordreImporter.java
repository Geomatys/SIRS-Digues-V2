package fr.sirs.importer.objet.desordre;

import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.TypeSourceImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.importer.objet.GenericStructureImporter;
import fr.sirs.importer.objet.structure.StructureImporter;
import fr.sirs.importer.objet.TypeFonctionImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class SubDesordreImporter extends GenericStructureImporter<Desordre> {
    
    private Map<Integer, Desordre> desordres = null;
    private Map<Integer, List<Desordre>> desordresByTronconId = null;
    
    private final TypeDesordreImporter typeDesordreImporter;

    SubDesordreImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final StructureImporter structureImporter, 
            final TypeDesordreImporter typeDesordreImporter, 
            final TypeSourceImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypeMateriauImporter typeMateriauImporter, 
            final TypeNatureImporter typeNatureImporter, 
            final TypeFonctionImporter typeFonctionImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, null, 
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter);
        this.typeDesordreImporter = typeDesordreImporter;
    }

    private enum SubDesordreColumns {

        ID_DESORDRE,
        //            id_nom_element,// Aucun intéret
        //            ID_SOUS_GROUPE_DONNEES,// Aucun intéret
        //            LIBELLE_SOUS_GROUPE_DONNEES,// Aucun intéret
                    ID_TYPE_DESORDRE,
        //            LIBELLE_TYPE_DESORDRE,// Dans TypeDesordreImporter
        //            DECALAGE_DEFAUT, // Info d'affichage
        //            DECALAGE, // Info d'affichage
        //            LIBELLE_SOURCE, // Dans TypeSourceImporter
        //            LIBELLE_TYPE_COTE, // Dans TypeCoteImporter
        //            LIBELLE_SYSTEME_REP, // Dans SystemeRepImporter
        //            NOM_BORNE_DEBUT, //Dans BorneImporter
        //            NOM_BORNE_FIN, //Dans BorneImporter
        //            DISPARU_OUI_NON,
        //            DEJA_OBSERVE_OUI_NON,
        //            LIBELLE_TYPE_POSITION,// Dans typePositionImporter
                    ID_TYPE_COTE,
                    ID_TYPE_POSITION,
        ID_TRONCON_GESTION,
                    ID_SOURCE,
                    DATE_DEBUT_VAL,
                    DATE_FIN_VAL,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
                    ID_SYSTEME_REP,
                    ID_BORNEREF_DEBUT,
                    AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
                    ID_BORNEREF_FIN,
                    AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN,
            LIEU_DIT_DESORDRE,
//            DESCRIPTION_DESORDRE,
//            ID_AUTO

        //Empty fields
//     ID_PRESTATION,
//     LIBELLE_PRESTATION, // Dans l'importateur de prestations
     X_DEBUT,
     Y_DEBUT,
     X_FIN,
     Y_FIN,
     COMMENTAIRE,
    };

    /**
     *
     * @return A map containing all Desordre instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, Desordre> getStructures() throws IOException, AccessDbImporterException {
        if (this.desordres == null) {
            compute();
        }
        return desordres;
    }

    /**
     *
     * @return A map containing all Desordre instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, List<Desordre>> getStructuresByTronconId() throws IOException, AccessDbImporterException {
        if (this.desordresByTronconId == null) {
            compute();
        }
        return this.desordresByTronconId;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_DESORDRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.desordres = new HashMap<>();
        this.desordresByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, RefTypeDesordre> typesDesordre = typeDesordreImporter.getTypeDesordre();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypeSource();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypePosition();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeCote();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Desordre desordre = new Desordre();
            
            if (row.getDouble(SubDesordreColumns.ID_BORNEREF_DEBUT.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(SubDesordreColumns.ID_BORNEREF_DEBUT.toString()).doubleValue());
                if(b!=null) desordre.setBorneDebutId(b.getId());
            }
            if (row.getDouble(SubDesordreColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                desordre.setBorne_debut_distance(row.getDouble(SubDesordreColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            if (row.getDouble(SubDesordreColumns.PR_DEBUT_CALCULE.toString()) != null) {
                desordre.setPR_debut(row.getDouble(SubDesordreColumns.PR_DEBUT_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(SubDesordreColumns.ID_BORNEREF_FIN.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(SubDesordreColumns.ID_BORNEREF_FIN.toString()).doubleValue());
                if (b!=null) desordre.setBorneFinId(b.getId());
            }
            if (row.getDouble(SubDesordreColumns.DIST_BORNEREF_FIN.toString()) != null) {
                desordre.setBorne_fin_distance(row.getDouble(SubDesordreColumns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            if (row.getDouble(SubDesordreColumns.PR_FIN_CALCULE.toString()) != null) {
                desordre.setPR_fin(row.getDouble(SubDesordreColumns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            if(row.getInt(SubDesordreColumns.ID_SYSTEME_REP.toString())!=null){
                desordre.setSystemeRepId(systemesReperage.get(row.getInt(SubDesordreColumns.ID_SYSTEME_REP.toString())).getId());
            }

            desordre.setBorne_debut_aval(row.getBoolean(SubDesordreColumns.AMONT_AVAL_DEBUT.toString())); 
            desordre.setBorne_fin_aval(row.getBoolean(SubDesordreColumns.AMONT_AVAL_FIN.toString()));
            desordre.setLieu_dit(row.getString(SubDesordreColumns.LIEU_DIT_DESORDRE.toString()));
            
            if(row.getInt(SubDesordreColumns.ID_TYPE_DESORDRE.toString())!=null){
                desordre.setTypeDesordreId(typesDesordre.get(row.getInt(SubDesordreColumns.ID_TYPE_DESORDRE.toString())).getId());
            }
            
            if(row.getInt(SubDesordreColumns.ID_SOURCE.toString())!=null){
                desordre.setSourceId(typesSource.get(row.getInt(SubDesordreColumns.ID_SOURCE.toString())).getId());
            }
            
            if(row.getInt(SubDesordreColumns.ID_TYPE_POSITION.toString())!=null){
                desordre.setPosition_structure(typesPosition.get(row.getInt(SubDesordreColumns.ID_TYPE_POSITION.toString())).getId());
            }
            
            if(row.getInt(SubDesordreColumns.ID_TYPE_COTE.toString())!=null){
                desordre.setCoteId(typesCote.get(row.getInt(SubDesordreColumns.ID_TYPE_COTE.toString())).getId());
            }
            
            final TronconDigue troncon = troncons.get(row.getInt(SubDesordreColumns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                desordre.setTroncon(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + troncons.get(row.getInt(SubDesordreColumns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }
            
            if (row.getDate(SubDesordreColumns.DATE_DEBUT_VAL.toString()) != null) {
                desordre.setDate_debut(LocalDateTime.parse(row.getDate(SubDesordreColumns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(SubDesordreColumns.DATE_FIN_VAL.toString()) != null) {
                desordre.setDate_fin(LocalDateTime.parse(row.getDate(SubDesordreColumns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            
            if (row.getString(SubDesordreColumns.COMMENTAIRE.toString()) != null) {
                desordre.setCommentaire(row.getString(SubDesordreColumns.COMMENTAIRE.toString()));
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(SubDesordreColumns.X_DEBUT.toString()) != null && row.getDouble(SubDesordreColumns.Y_DEBUT.toString()) != null) {
                        desordre.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(SubDesordreColumns.X_DEBUT.toString()),
                                row.getDouble(SubDesordreColumns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SubDesordreImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(SubDesordreColumns.X_FIN.toString()) != null && row.getDouble(SubDesordreColumns.Y_FIN.toString()) != null) {
                        desordre.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(SubDesordreColumns.X_FIN.toString()),
                                row.getDouble(SubDesordreColumns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SubDesordreImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(SubDesordreImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            desordres.put(row.getInt(SubDesordreColumns.ID_DESORDRE.toString()), desordre);

            // Set the list ByTronconId
            List<Desordre> listByTronconId = desordresByTronconId.get(row.getInt(SubDesordreColumns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
            }
            listByTronconId.add(desordre);
            desordresByTronconId.put(row.getInt(SubDesordreColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
        }
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (SubDesordreColumns c : SubDesordreColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
