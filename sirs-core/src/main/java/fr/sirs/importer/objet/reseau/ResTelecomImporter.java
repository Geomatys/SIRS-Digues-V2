package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefImplantation;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefReseauTelecomEnergie;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.ReseauTelecomEnergie;
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
class ResTelecomImporter extends GenericStructureImporter<ReseauTelecomEnergie> {

    private Map<Integer, ReseauTelecomEnergie> reseaux = null;
    private Map<Integer, List<ReseauTelecomEnergie>> reseauxByTronconId = null;
    
    private final TypeImplantationImporter typeImplantationImporter;
    private final TypeReseauTelecomImporter typeReseauTelecomImporter;

    ResTelecomImporter(final Database accessDatabase,
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
            final TypeImplantationImporter typeImplantationImporter,
            final TypeReseauTelecomImporter typeReseauTelecomImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, organismeImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter);
        this.typeImplantationImporter = typeImplantationImporter;
        this.typeReseauTelecomImporter = typeReseauTelecomImporter;
    }
    
    private enum ResTelecomColumns {
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
        ID_IMPLANTATION,
//        ID_UTILISATION_CONDUITE,
//        ID_TYPE_CONDUITE_FERMEE,
//        AUTORISE,
//        ID_TYPE_OUVR_HYDRAU_ASSOCIE,
        ID_TYPE_RESEAU_COMMUNICATION,
//        ID_OUVRAGE_COMM_NRJ,
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

    /**
     *
     * @return A map containing all ReseauTelecomEnergie instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, ReseauTelecomEnergie> getStructures() throws IOException, AccessDbImporterException {
        if (this.reseaux == null) {
            compute();
        }
        return reseaux;
    }

    /**
     *
     * @return A map containing all ReseauTelecomEnergie instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, List<ReseauTelecomEnergie>> getStructuresByTronconId() 
            throws IOException, AccessDbImporterException {
        if (this.reseauxByTronconId == null) {
            compute();
        }
        return this.reseauxByTronconId;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_RESEAU_TELECOMMUNICATION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.reseaux = new HashMap<>();
        this.reseauxByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeCote();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypeSource();
        final Map<Integer, RefImplantation> implantations = typeImplantationImporter.getTypeImplantation();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypePosition();
        final Map<Integer, RefReseauTelecomEnergie> typesReseau = typeReseauTelecomImporter.getTypeReseauTelecom();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ReseauTelecomEnergie reseau = new ReseauTelecomEnergie();
            
            reseau.setLibelle(cleanNullString(row.getString(ResTelecomColumns.NOM.toString())));
            
            if(row.getInt(ResTelecomColumns.ID_TYPE_COTE.toString())!=null){
                reseau.setCoteId(typesCote.get(row.getInt(ResTelecomColumns.ID_TYPE_COTE.toString())).getId());
            }
            
            if(row.getInt(ResTelecomColumns.ID_SOURCE.toString())!=null){
                reseau.setSourceId(typesSource.get(row.getInt(ResTelecomColumns.ID_SOURCE.toString())).getId());
            }
            
            final TronconDigue troncon = troncons.get(row.getInt(ResTelecomColumns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                reseau.setTroncon(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + troncons.get(row.getInt(ResTelecomColumns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }
            
            if (row.getDate(ResTelecomColumns.DATE_DEBUT_VAL.toString()) != null) {
                reseau.setDate_debut(LocalDateTime.parse(row.getDate(ResTelecomColumns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(ResTelecomColumns.DATE_FIN_VAL.toString()) != null) {
                reseau.setDate_fin(LocalDateTime.parse(row.getDate(ResTelecomColumns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDouble(ResTelecomColumns.PR_DEBUT_CALCULE.toString()) != null) {
                reseau.setPR_debut(row.getDouble(ResTelecomColumns.PR_DEBUT_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(ResTelecomColumns.PR_FIN_CALCULE.toString()) != null) {
                reseau.setPR_fin(row.getDouble(ResTelecomColumns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(ResTelecomColumns.X_DEBUT.toString()) != null && row.getDouble(ResTelecomColumns.Y_DEBUT.toString()) != null) {
                        reseau.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(ResTelecomColumns.X_DEBUT.toString()),
                                row.getDouble(ResTelecomColumns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ResTelecomImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(ResTelecomColumns.X_FIN.toString()) != null && row.getDouble(ResTelecomColumns.Y_FIN.toString()) != null) {
                        reseau.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(ResTelecomColumns.X_FIN.toString()),
                                row.getDouble(ResTelecomColumns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ResTelecomImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(ResTelecomImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getInt(ResTelecomColumns.ID_SYSTEME_REP.toString()) != null) {
                reseau.setSystemeRepId(systemesReperage.get(row.getInt(ResTelecomColumns.ID_SYSTEME_REP.toString())).getId());
            }
            
            if (row.getDouble(ResTelecomColumns.ID_BORNEREF_DEBUT.toString()) != null) {
                reseau.setBorneDebutId(bornes.get((int) row.getDouble(ResTelecomColumns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }
            
            reseau.setBorne_debut_aval(row.getBoolean(ResTelecomColumns.AMONT_AVAL_DEBUT.toString()));
            
            if (row.getDouble(ResTelecomColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                reseau.setBorne_debut_distance(row.getDouble(ResTelecomColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            
            if (row.getDouble(ResTelecomColumns.ID_BORNEREF_FIN.toString()) != null) {
                if(bornes.get((int) row.getDouble(ResTelecomColumns.ID_BORNEREF_FIN.toString()).doubleValue())!=null){
                    reseau.setBorneFinId(bornes.get((int) row.getDouble(ResTelecomColumns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
                }
            }
            
            reseau.setBorne_fin_aval(row.getBoolean(ResTelecomColumns.AMONT_AVAL_FIN.toString()));
            
            if (row.getDouble(ResTelecomColumns.DIST_BORNEREF_FIN.toString()) != null) {
                reseau.setBorne_fin_distance(row.getDouble(ResTelecomColumns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            reseau.setCommentaire(row.getString(ResTelecomColumns.COMMENTAIRE.toString()));
            
            if(row.getInt(ResTelecomColumns.ID_IMPLANTATION.toString())!=null){
                if(implantations.get(row.getInt(ResTelecomColumns.ID_IMPLANTATION.toString()))!=null){
                    reseau.setImplantaitonId(implantations.get(row.getInt(ResTelecomColumns.ID_IMPLANTATION.toString())).getId());
                }
            }
            
            if(row.getInt(ResTelecomColumns.ID_TYPE_RESEAU_COMMUNICATION.toString())!=null){
                if(typesReseau.get(row.getInt(ResTelecomColumns.ID_TYPE_RESEAU_COMMUNICATION.toString()))!=null){
                    reseau.setTypeReseauTelecomEnergieId(typesReseau.get(row.getInt(ResTelecomColumns.ID_TYPE_RESEAU_COMMUNICATION.toString())).getId());
                }
            }
            
            if(row.getInt(ResTelecomColumns.ID_TYPE_POSITION.toString())!=null){
                reseau.setPosition_structure(typesPosition.get(row.getInt(ResTelecomColumns.ID_TYPE_POSITION.toString())).getId());
            }
            
            if (row.getDouble(ResTelecomColumns.HAUTEUR.toString()) != null) {
                reseau.setHauteur(row.getDouble(ResTelecomColumns.HAUTEUR.toString()).floatValue());
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            reseaux.put(row.getInt(ResTelecomColumns.ID_ELEMENT_RESEAU.toString()), reseau);

            // Set the list ByTronconId
            List<ReseauTelecomEnergie> listByTronconId = reseauxByTronconId.get(row.getInt(ResTelecomColumns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                reseauxByTronconId.put(row.getInt(ResTelecomColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(reseau);
        }
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ResTelecomColumns c : ResTelecomColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
