package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefOuvrageTelecomEnergie;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.TypeFonctionImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
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
class SysEvtOuvrageTelecommunicationImporter extends GenericReseauImporter<OuvrageTelecomEnergie> {
    
    private final TypeOuvrageTelecomNrjImporter typeOuvrageTelecomImporter;

    SysEvtOuvrageTelecommunicationImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter, 
            final OrganismeImporter organismeImporter,
            final IntervenantImporter intervenantImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter,
            final TypeFonctionImporter typeFonctionImporter, 
            final TypeOuvrageTelecomNrjImporter typeReseauTelecomImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, organismeImporter,
                intervenantImporter, typeSourceImporter, typeCoteImporter, 
                typePositionImporter, typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        this.typeOuvrageTelecomImporter = typeReseauTelecomImporter;
    }
    
    private enum Columns {
        ID_ELEMENT_RESEAU,
//        id_nom_element,
//        ID_SOUS_GROUPE_DONNEES,
//        LIBELLE_TYPE_ELEMENT_RESEAU,
//        DECALAGE_DEFAUT,
//        DECALAGE,
//        LIBELLE_SOURCE,
//        LIBELLE_TYPE_COTE,
//        LIBELLE_SYSTEME_REP,
//        NOM_BORNE_DEBUT,
//        NOM_BORNE_FIN,
//        LIBELLE_ECOULEMENT,
//        LIBELLE_IMPLANTATION,
//        LIBELLE_UTILISATION_CONDUITE,
//        LIBELLE_TYPE_CONDUITE_FERMEE,
//        LIBELLE_TYPE_OUVR_HYDRAU_ASSOCIE,
//        LIBELLE_TYPE_RESEAU_COMMUNICATION,
//        LIBELLE_TYPE_VOIE_SUR_DIGUE,
//        NOM_OUVRAGE_VOIRIE,
//        LIBELLE_TYPE_POSITION,
//        LIBELLE_TYPE_OUVRAGE_VOIRIE,
//        LIBELLE_TYPE_RESEAU_EAU,
//        LIBELLE_TYPE_REVETEMENT,
//        LIBELLE_TYPE_USAGE_VOIE,
        NOM,
//        ID_TYPE_ELEMENT_RESEAU,
        ID_TYPE_COTE,
        ID_SOURCE,
        ID_TRONCON_GESTION,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        X_DEBUT,
        Y_DEBUT,
        X_FIN,
        Y_FIN,
        ID_SYSTEME_REP,
        ID_BORNEREF_DEBUT,
        AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
        ID_BORNEREF_FIN,
        AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN,
        COMMENTAIRE,
//        N_SECTEUR,
//        ID_ECOULEMENT,
//        ID_IMPLANTATION,
//        ID_UTILISATION_CONDUITE,
//        ID_TYPE_CONDUITE_FERMEE,
//        AUTORISE,
//        ID_TYPE_OUVR_HYDRAU_ASSOCIE,
//        ID_TYPE_RESEAU_COMMUNICATION,
        ID_OUVRAGE_COMM_NRJ,
//        ID_TYPE_VOIE_SUR_DIGUE,
//        ID_OUVRAGE_VOIRIE,
//        ID_TYPE_REVETEMENT,
//        ID_TYPE_USAGE_VOIE,
        ID_TYPE_POSITION,
//        LARGEUR,
//        ID_TYPE_OUVRAGE_VOIRIE,
        HAUTEUR,
//        DIAMETRE,
//        ID_TYPE_RESEAU_EAU,
//        ID_TYPE_NATURE,
//        LIBELLE_TYPE_NATURE,
//        ID_TYPE_NATURE_HAUT,
//        LIBELLE_TYPE_NATURE_HAUT,
//        ID_TYPE_NATURE_BAS,
//        LIBELLE_TYPE_NATURE_BAS,
//        ID_TYPE_REVETEMENT_HAUT,
//        LIBELLE_TYPE_REVETEMENT_HAUT,
//        ID_TYPE_REVETEMENT_BAS,
//        LIBELLE_TYPE_REVETEMENT_BAS,
//        ID_AUTO
    };

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_OUVRAGE_TELECOMMUNICATION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.structures = new HashMap<>();
        this.structuresByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypes();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypes();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypes();
        final Map<Integer, RefOuvrageTelecomEnergie> typesOuvrage = typeOuvrageTelecomImporter.getTypes();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final OuvrageTelecomEnergie ouvrage = new OuvrageTelecomEnergie();
            
            ouvrage.setLibelle(cleanNullString(row.getString(Columns.NOM.toString())));
            
            if(row.getInt(Columns.ID_TYPE_COTE.toString())!=null){
                ouvrage.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                ouvrage.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
            }
            
            final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                ouvrage.setTroncon(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }
            
            if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
                ouvrage.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
                ouvrage.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                ouvrage.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                ouvrage.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        ouvrage.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtOuvrageTelecommunicationImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        ouvrage.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_FIN.toString()),
                                row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtOuvrageTelecommunicationImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(SysEvtOuvrageTelecommunicationImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
                ouvrage.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                ouvrage.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }
            
            ouvrage.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));
            
            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                ouvrage.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                if(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue())!=null){
                    ouvrage.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
                }
            }
            
            ouvrage.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
            
            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                ouvrage.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            ouvrage.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            if(row.getInt(Columns.ID_OUVRAGE_COMM_NRJ.toString())!=null){
                if(typesOuvrage.get(row.getInt(Columns.ID_OUVRAGE_COMM_NRJ.toString()))!=null){
                    ouvrage.setTypeOuvrageTelecomEnergieId(typesOuvrage.get(row.getInt(Columns.ID_OUVRAGE_COMM_NRJ.toString())).getId());
                }
            }
            
            if(row.getInt(Columns.ID_TYPE_POSITION.toString())!=null){
                ouvrage.setPosition_structure(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            structures.put(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()), ouvrage);

            // Set the list ByTronconId
            List<OuvrageTelecomEnergie> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(ouvrage);
        }
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
