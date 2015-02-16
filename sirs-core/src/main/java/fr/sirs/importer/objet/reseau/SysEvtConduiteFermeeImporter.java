package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.RefConduiteFermee;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefEcoulement;
import fr.sirs.core.model.RefImplantation;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefUtilisationConduite;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.objet.TypeCoteImporter;
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
class SysEvtConduiteFermeeImporter extends GenericReseauImporter<ReseauHydrauliqueFerme> {
    
    private final EcoulementImporter typeEcoulementImporter;
    private final ImplantationImporter typeImplantationImporter;
    private final TypeConduiteFermeeImporter typeConduiteFermeeImporter;
    private final UtilisationConduiteImporter typeUtilisationConduiteImporter;

    SysEvtConduiteFermeeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter, 
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePositionImporter typePositionImporter,
            final EcoulementImporter typeEcoulementImporter,
            final ImplantationImporter typeImplantationImporter,
            final TypeConduiteFermeeImporter typeConduiteFermeeImporter,
            final UtilisationConduiteImporter typeUtilisationConduiteImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, 
                typePositionImporter, null);
        this.typeEcoulementImporter = typeEcoulementImporter;
        this.typeImplantationImporter = typeImplantationImporter;
        this.typeConduiteFermeeImporter = typeConduiteFermeeImporter;
        this.typeUtilisationConduiteImporter = typeUtilisationConduiteImporter;
    }
    
    private enum Columns {
        ID_ELEMENT_RESEAU,
//        id_nom_element, // Redondant ave ID_ELEMENENT_RESEAU
//        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        LIBELLE_TYPE_ELEMENT_RESEAU, // Redondant avec le type de données
//        DECALAGE_DEFAUT, // Affichage
//        DECALAGE, // Affichage
//        LIBELLE_SOURCE, // Redondant avec l'importation des sources
//        LIBELLE_TYPE_COTE, // Redondant avec l'importation des types de cotés
//        LIBELLE_SYSTEME_REP, // Redondant avec l'importation des SR
//        NOM_BORNE_DEBUT, // Redondant avec l'importation des bornes
//        NOM_BORNE_FIN, // Redondant avec l'importation des bornes
//        LIBELLE_ECOULEMENT,
//        LIBELLE_IMPLANTATION,
//        LIBELLE_UTILISATION_CONDUITE,
//        LIBELLE_TYPE_CONDUITE_FERMEE,
//        LIBELLE_TYPE_OUVR_HYDRAU_ASSOCIE,
//        LIBELLE_TYPE_RESEAU_COMMUNICATION,
//        LIBELLE_TYPE_VOIE_SUR_DIGUE,
//        NOM_OUVRAGE_VOIRIE,
//        LIBELLE_TYPE_POSITION, // Redondant avec l'importation des positions
//        LIBELLE_TYPE_OUVRAGE_VOIRIE,
//        LIBELLE_TYPE_RESEAU_EAU,
//        LIBELLE_TYPE_REVETEMENT,
//        LIBELLE_TYPE_USAGE_VOIE,
        NOM,
//        ID_TYPE_ELEMENT_RESEAU, // Redondant avec le type de données
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
        ID_ECOULEMENT,
        ID_IMPLANTATION,
        ID_UTILISATION_CONDUITE,
        ID_TYPE_CONDUITE_FERMEE,
        AUTORISE,
//        ID_TYPE_OUVR_HYDRAU_ASSOCIE,
//        ID_TYPE_RESEAU_COMMUNICATION,
//        ID_OUVRAGE_COMM_NRJ,
//        ID_TYPE_VOIE_SUR_DIGUE,
//        ID_OUVRAGE_VOIRIE,
//        ID_TYPE_REVETEMENT,
//        ID_TYPE_USAGE_VOIE,
        ID_TYPE_POSITION,
//        LARGEUR,
//        ID_TYPE_OUVRAGE_VOIRIE,
//        HAUTEUR,
        DIAMETRE,
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
        return DbImporter.TableName.SYS_EVT_CONDUITE_FERMEE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.structures = new HashMap<>();
        this.structuresByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        
        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeReferences();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypeReferences();
        
        final Map<Integer, RefEcoulement> ecoulements = typeEcoulementImporter.getTypeReferences();
        final Map<Integer, RefImplantation> implantations = typeImplantationImporter.getTypeReferences();
        final Map<Integer, RefConduiteFermee> typesConduites = typeConduiteFermeeImporter.getTypeReferences();
        final Map<Integer, RefUtilisationConduite> typesUtilisationConduites = typeUtilisationConduiteImporter.getTypeReferences();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ReseauHydrauliqueFerme conduiteFermee = new ReseauHydrauliqueFerme();
            
            conduiteFermee.setLibelle(cleanNullString(row.getString(Columns.NOM.toString())));
            
            if(row.getInt(Columns.ID_TYPE_COTE.toString())!=null){
                conduiteFermee.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                conduiteFermee.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
            }
            
//            final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
//            if (troncon.getId() != null) {
//                conduiteFermee.setTroncon(troncon.getId());
//            } else {
//                throw new AccessDbImporterException("Le tronçon "
//                        + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
//            }
            
            if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
                conduiteFermee.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
                conduiteFermee.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                conduiteFermee.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                conduiteFermee.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        conduiteFermee.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtConduiteFermeeImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        conduiteFermee.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_FIN.toString()),
                                row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtConduiteFermeeImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(SysEvtConduiteFermeeImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
                conduiteFermee.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                conduiteFermee.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }
            
            conduiteFermee.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));
            
            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                conduiteFermee.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                if(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue())!=null){
                    conduiteFermee.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
                }
            }
            
            conduiteFermee.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
            
            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                conduiteFermee.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            conduiteFermee.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            if(row.getInt(Columns.ID_ECOULEMENT.toString())!=null){
                if(ecoulements.get(row.getInt(Columns.ID_ECOULEMENT.toString()))!=null){
                    conduiteFermee.setEcoulementId(ecoulements.get(row.getInt(Columns.ID_ECOULEMENT.toString())).getId());
                }
            }
            
            if(row.getInt(Columns.ID_IMPLANTATION.toString())!=null){
                if(implantations.get(row.getInt(Columns.ID_IMPLANTATION.toString()))!=null){
                    conduiteFermee.setImplantationId(implantations.get(row.getInt(Columns.ID_IMPLANTATION.toString())).getId());
                }
            }
            
            if(row.getInt(Columns.ID_UTILISATION_CONDUITE.toString())!=null){
                if(typesUtilisationConduites.get(row.getInt(Columns.ID_UTILISATION_CONDUITE.toString()))!=null){
                    conduiteFermee.setUtilisationConduiteId(typesUtilisationConduites.get(row.getInt(Columns.ID_UTILISATION_CONDUITE.toString())).getId());
                }
            }
            
            if(row.getInt(Columns.ID_TYPE_CONDUITE_FERMEE.toString())!=null){
                if(typesConduites.get(row.getInt(Columns.ID_TYPE_CONDUITE_FERMEE.toString()))!=null){
                    conduiteFermee.setTypeConduiteFermeeId(typesConduites.get(row.getInt(Columns.ID_TYPE_CONDUITE_FERMEE.toString())).getId());
                }
            }
            
            if(row.getBoolean(Columns.AUTORISE.toString())!=null){
                conduiteFermee.setAutorise(row.getBoolean(Columns.AUTORISE.toString()));
            }
            
//            if(row.getInt(Columns.ID_TYPE_POSITION.toString())!=null){
//                conduiteFermee.setPosition_structure(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
//            }
            
            if (row.getDouble(Columns.DIAMETRE.toString()) != null) {
                conduiteFermee.setDiametre(row.getDouble(Columns.DIAMETRE.toString()).floatValue());
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            structures.put(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()), conduiteFermee);

            // Set the list ByTronconId
            List<ReseauHydrauliqueFerme> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(conduiteFermee);
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
