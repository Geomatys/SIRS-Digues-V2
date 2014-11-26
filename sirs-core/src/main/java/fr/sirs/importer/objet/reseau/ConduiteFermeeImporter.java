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
import fr.sirs.importer.TronconGestionDigueImporter;
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
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.objet.GenericStructureImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.TypeFonctionImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.TypeSourceImporter;
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
class ConduiteFermeeImporter extends GenericStructureImporter<ReseauHydrauliqueFerme> {

    private Map<Integer, ReseauHydrauliqueFerme> conduites = null;
    private Map<Integer, List<ReseauHydrauliqueFerme>> conduitesByTronconId = null;
    
    private final TypeEcoulementImporter typeEcoulementImporter;
    private final TypeImplantationImporter typeImplantationImporter;
    private final TypeConduiteFermeeImporter typeConduiteFermeeImporter;
    private final TypeUtilisationConduiteImporter typeUtilisationConduiteImporter;

    ConduiteFermeeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter, 
            final OrganismeImporter organismeImporter,
            final TypeSourceImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter,
            final TypeFonctionImporter typeFonctionImporter, 
            final TypeEcoulementImporter typeEcoulementImporter,
            final TypeImplantationImporter typeImplantationImporter,
            final TypeConduiteFermeeImporter typeConduiteFermeeImporter,
            final TypeUtilisationConduiteImporter typeUtilisationConduiteImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, organismeImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter);
        this.typeEcoulementImporter = typeEcoulementImporter;
        this.typeImplantationImporter = typeImplantationImporter;
        this.typeConduiteFermeeImporter = typeConduiteFermeeImporter;
        this.typeUtilisationConduiteImporter = typeUtilisationConduiteImporter;
    }
    
    private enum ConduiteFermeeColumns {
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

    /**
     *
     * @return A map containing all StationPompage instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, ReseauHydrauliqueFerme> getStructures() throws IOException, AccessDbImporterException {
        if (this.conduites == null) {
            compute();
        }
        return conduites;
    }

    /**
     *
     * @return A map containing all StationPompage instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, List<ReseauHydrauliqueFerme>> getStructuresByTronconId() throws IOException, AccessDbImporterException {
        if (this.conduitesByTronconId == null) {
            compute();
        }
        return this.conduitesByTronconId;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_CONDUITE_FERMEE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.conduites = new HashMap<>();
        this.conduitesByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeCote();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypeSource();
        final Map<Integer, RefEcoulement> ecoulements = typeEcoulementImporter.getTypeEcoulement();
        final Map<Integer, RefImplantation> implantations = typeImplantationImporter.getTypeImplantation();
        final Map<Integer, RefConduiteFermee> typesConduites = typeConduiteFermeeImporter.getTypeConduiteFerme();
        final Map<Integer, RefUtilisationConduite> typesUtilisationConduites = typeUtilisationConduiteImporter.getTypeUtilisationConduite();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypePosition();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ReseauHydrauliqueFerme conduiteFermee = new ReseauHydrauliqueFerme();
            
            conduiteFermee.setLibelle(cleanNullString(row.getString(ConduiteFermeeColumns.NOM.toString())));
            
            if(row.getInt(ConduiteFermeeColumns.ID_TYPE_COTE.toString())!=null){
                conduiteFermee.setCoteId(typesCote.get(row.getInt(ConduiteFermeeColumns.ID_TYPE_COTE.toString())).getId());
            }
            
            if(row.getInt(ConduiteFermeeColumns.ID_SOURCE.toString())!=null){
                conduiteFermee.setSourceId(typesSource.get(row.getInt(ConduiteFermeeColumns.ID_SOURCE.toString())).getId());
            }
            
            final TronconDigue troncon = troncons.get(row.getInt(ConduiteFermeeColumns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                conduiteFermee.setTroncon(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + troncons.get(row.getInt(ConduiteFermeeColumns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }
            
            if (row.getDate(ConduiteFermeeColumns.DATE_DEBUT_VAL.toString()) != null) {
                conduiteFermee.setDate_debut(LocalDateTime.parse(row.getDate(ConduiteFermeeColumns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(ConduiteFermeeColumns.DATE_FIN_VAL.toString()) != null) {
                conduiteFermee.setDate_fin(LocalDateTime.parse(row.getDate(ConduiteFermeeColumns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDouble(ConduiteFermeeColumns.PR_DEBUT_CALCULE.toString()) != null) {
                conduiteFermee.setPR_debut(row.getDouble(ConduiteFermeeColumns.PR_DEBUT_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(ConduiteFermeeColumns.PR_FIN_CALCULE.toString()) != null) {
                conduiteFermee.setPR_fin(row.getDouble(ConduiteFermeeColumns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(ConduiteFermeeColumns.X_DEBUT.toString()) != null && row.getDouble(ConduiteFermeeColumns.Y_DEBUT.toString()) != null) {
                        conduiteFermee.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(ConduiteFermeeColumns.X_DEBUT.toString()),
                                row.getDouble(ConduiteFermeeColumns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ConduiteFermeeImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(ConduiteFermeeColumns.X_FIN.toString()) != null && row.getDouble(ConduiteFermeeColumns.Y_FIN.toString()) != null) {
                        conduiteFermee.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(ConduiteFermeeColumns.X_FIN.toString()),
                                row.getDouble(ConduiteFermeeColumns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ConduiteFermeeImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(ConduiteFermeeImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getInt(ConduiteFermeeColumns.ID_SYSTEME_REP.toString()) != null) {
                conduiteFermee.setSystemeRepId(systemesReperage.get(row.getInt(ConduiteFermeeColumns.ID_SYSTEME_REP.toString())).getId());
            }
            
            if (row.getDouble(ConduiteFermeeColumns.ID_BORNEREF_DEBUT.toString()) != null) {
                conduiteFermee.setBorneDebutId(bornes.get((int) row.getDouble(ConduiteFermeeColumns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }
            
            conduiteFermee.setBorne_debut_aval(row.getBoolean(ConduiteFermeeColumns.AMONT_AVAL_DEBUT.toString()));
            
            if (row.getDouble(ConduiteFermeeColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                conduiteFermee.setBorne_debut_distance(row.getDouble(ConduiteFermeeColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            
            if (row.getDouble(ConduiteFermeeColumns.ID_BORNEREF_FIN.toString()) != null) {
                if(bornes.get((int) row.getDouble(ConduiteFermeeColumns.ID_BORNEREF_FIN.toString()).doubleValue())!=null){
                    conduiteFermee.setBorneFinId(bornes.get((int) row.getDouble(ConduiteFermeeColumns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
                }
            }
            
            conduiteFermee.setBorne_fin_aval(row.getBoolean(ConduiteFermeeColumns.AMONT_AVAL_FIN.toString()));
            
            if (row.getDouble(ConduiteFermeeColumns.DIST_BORNEREF_FIN.toString()) != null) {
                conduiteFermee.setBorne_fin_distance(row.getDouble(ConduiteFermeeColumns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            conduiteFermee.setCommentaire(row.getString(ConduiteFermeeColumns.COMMENTAIRE.toString()));
            
            if(row.getInt(ConduiteFermeeColumns.ID_ECOULEMENT.toString())!=null){
                if(ecoulements.get(row.getInt(ConduiteFermeeColumns.ID_ECOULEMENT.toString()))!=null){
                    conduiteFermee.setEcoulementId(ecoulements.get(row.getInt(ConduiteFermeeColumns.ID_ECOULEMENT.toString())).getId());
                }
            }
            
            if(row.getInt(ConduiteFermeeColumns.ID_IMPLANTATION.toString())!=null){
                if(implantations.get(row.getInt(ConduiteFermeeColumns.ID_IMPLANTATION.toString()))!=null){
                    conduiteFermee.setImplantationId(implantations.get(row.getInt(ConduiteFermeeColumns.ID_IMPLANTATION.toString())).getId());
                }
            }
            
            if(row.getInt(ConduiteFermeeColumns.ID_UTILISATION_CONDUITE.toString())!=null){
                if(typesUtilisationConduites.get(row.getInt(ConduiteFermeeColumns.ID_UTILISATION_CONDUITE.toString()))!=null){
                    conduiteFermee.setUtilisationConduiteId(typesUtilisationConduites.get(row.getInt(ConduiteFermeeColumns.ID_UTILISATION_CONDUITE.toString())).getId());
                }
            }
            
            if(row.getInt(ConduiteFermeeColumns.ID_TYPE_CONDUITE_FERMEE.toString())!=null){
                if(typesConduites.get(row.getInt(ConduiteFermeeColumns.ID_TYPE_CONDUITE_FERMEE.toString()))!=null){
                    conduiteFermee.setTypeConduiteFermeeId(typesConduites.get(row.getInt(ConduiteFermeeColumns.ID_TYPE_CONDUITE_FERMEE.toString())).getId());
                }
            }
            
            if(row.getBoolean(ConduiteFermeeColumns.AUTORISE.toString())!=null){
                conduiteFermee.setAutorise(row.getBoolean(ConduiteFermeeColumns.AUTORISE.toString()));
            }
            
            if(row.getInt(ConduiteFermeeColumns.ID_TYPE_POSITION.toString())!=null){
                conduiteFermee.setPosition_structure(typesPosition.get(row.getInt(ConduiteFermeeColumns.ID_TYPE_POSITION.toString())).getId());
            }
            
            if (row.getDouble(ConduiteFermeeColumns.DIAMETRE.toString()) != null) {
                conduiteFermee.setDiametre(row.getDouble(ConduiteFermeeColumns.DIAMETRE.toString()).floatValue());
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
            conduites.put(row.getInt(ConduiteFermeeColumns.ID_ELEMENT_RESEAU.toString()), conduiteFermee);

            // Set the list ByTronconId
            List<ReseauHydrauliqueFerme> listByTronconId = conduitesByTronconId.get(row.getInt(ConduiteFermeeColumns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                conduitesByTronconId.put(row.getInt(ConduiteFermeeColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(conduiteFermee);
        }
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ConduiteFermeeColumns c : ConduiteFermeeColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
