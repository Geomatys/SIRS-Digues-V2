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
import fr.sirs.core.model.RefImplantation;
import fr.sirs.core.model.RefOuvrageTelecomEnergie;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefReseauTelecomEnergie;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.ReseauConduiteFermee;
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
class OuvTelecomImporter extends GenericStructureImporter<OuvrageTelecomEnergie> {

    private Map<Integer, OuvrageTelecomEnergie> ouvrages = null;
    private Map<Integer, List<OuvrageTelecomEnergie>> ouvragesByTronconId = null;
    
    private final TypeImplantationImporter typeImplantationImporter;
    private final TypeOuvrageTelecomImporter typeOuvrageTelecomImporter;

    OuvTelecomImporter(final Database accessDatabase,
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
            final TypeOuvrageTelecomImporter typeReseauTelecomImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, organismeImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter);
        this.typeImplantationImporter = typeImplantationImporter;
        this.typeOuvrageTelecomImporter = typeReseauTelecomImporter;
    }
    
    private enum OuvTelecomColumns {
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

    /**
     *
     * @return A map containing all OuvrageTelecomEnergie instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, OuvrageTelecomEnergie> getStructures() throws IOException, AccessDbImporterException {
        if (this.ouvrages == null) {
            compute();
        }
        return ouvrages;
    }

    /**
     *
     * @return A map containing all OuvrageTelecomEnergie instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, List<OuvrageTelecomEnergie>> getStructuresByTronconId() 
            throws IOException, AccessDbImporterException {
        if (this.ouvragesByTronconId == null) {
            compute();
        }
        return this.ouvragesByTronconId;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_RESEAU_TELECOMMUNICATION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.ouvrages = new HashMap<>();
        this.ouvragesByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeCote();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypeSource();
        final Map<Integer, RefImplantation> implantations = typeImplantationImporter.getTypeImplantation();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypePosition();
        final Map<Integer, RefOuvrageTelecomEnergie> typesOuvrage = typeOuvrageTelecomImporter.getTypeOuvrageTelecom();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final OuvrageTelecomEnergie reseau = new OuvrageTelecomEnergie();
            
            reseau.setLibelle(cleanNullString(row.getString(OuvTelecomColumns.NOM.toString())));
            
            if(row.getInt(OuvTelecomColumns.ID_TYPE_COTE.toString())!=null){
                reseau.setCoteId(typesCote.get(row.getInt(OuvTelecomColumns.ID_TYPE_COTE.toString())).getId());
            }
            
            if(row.getInt(OuvTelecomColumns.ID_SOURCE.toString())!=null){
                reseau.setSourceId(typesSource.get(row.getInt(OuvTelecomColumns.ID_SOURCE.toString())).getId());
            }
            
            final TronconDigue troncon = troncons.get(row.getInt(OuvTelecomColumns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                reseau.setTroncon(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + troncons.get(row.getInt(OuvTelecomColumns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }
            
            if (row.getDate(OuvTelecomColumns.DATE_DEBUT_VAL.toString()) != null) {
                reseau.setDate_debut(LocalDateTime.parse(row.getDate(OuvTelecomColumns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(OuvTelecomColumns.DATE_FIN_VAL.toString()) != null) {
                reseau.setDate_fin(LocalDateTime.parse(row.getDate(OuvTelecomColumns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDouble(OuvTelecomColumns.PR_DEBUT_CALCULE.toString()) != null) {
                reseau.setPR_debut(row.getDouble(OuvTelecomColumns.PR_DEBUT_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(OuvTelecomColumns.PR_FIN_CALCULE.toString()) != null) {
                reseau.setPR_fin(row.getDouble(OuvTelecomColumns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(OuvTelecomColumns.X_DEBUT.toString()) != null && row.getDouble(OuvTelecomColumns.Y_DEBUT.toString()) != null) {
                        reseau.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(OuvTelecomColumns.X_DEBUT.toString()),
                                row.getDouble(OuvTelecomColumns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(OuvTelecomImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(OuvTelecomColumns.X_FIN.toString()) != null && row.getDouble(OuvTelecomColumns.Y_FIN.toString()) != null) {
                        reseau.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(OuvTelecomColumns.X_FIN.toString()),
                                row.getDouble(OuvTelecomColumns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(OuvTelecomImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(OuvTelecomImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getInt(OuvTelecomColumns.ID_SYSTEME_REP.toString()) != null) {
                reseau.setSystemeRepId(systemesReperage.get(row.getInt(OuvTelecomColumns.ID_SYSTEME_REP.toString())).getId());
            }
            
            if (row.getDouble(OuvTelecomColumns.ID_BORNEREF_DEBUT.toString()) != null) {
                reseau.setBorneDebutId(bornes.get((int) row.getDouble(OuvTelecomColumns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }
            
            reseau.setBorne_debut_aval(row.getBoolean(OuvTelecomColumns.AMONT_AVAL_DEBUT.toString()));
            
            if (row.getDouble(OuvTelecomColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                reseau.setBorne_debut_distance(row.getDouble(OuvTelecomColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            
            if (row.getDouble(OuvTelecomColumns.ID_BORNEREF_FIN.toString()) != null) {
                if(bornes.get((int) row.getDouble(OuvTelecomColumns.ID_BORNEREF_FIN.toString()).doubleValue())!=null){
                    reseau.setBorneFinId(bornes.get((int) row.getDouble(OuvTelecomColumns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
                }
            }
            
            reseau.setBorne_fin_aval(row.getBoolean(OuvTelecomColumns.AMONT_AVAL_FIN.toString()));
            
            if (row.getDouble(OuvTelecomColumns.DIST_BORNEREF_FIN.toString()) != null) {
                reseau.setBorne_fin_distance(row.getDouble(OuvTelecomColumns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            reseau.setCommentaire(row.getString(OuvTelecomColumns.COMMENTAIRE.toString()));
            
//            if(row.getInt(OuvTelecomColumns.ID_IMPLANTATION.toString())!=null){
//                if(implantations.get(row.getInt(OuvTelecomColumns.ID_IMPLANTATION.toString()))!=null){
//                    reseau.setImplantaitonId(implantations.get(row.getInt(OuvTelecomColumns.ID_IMPLANTATION.toString())).getId());
//                }
//            }
            
            if(row.getInt(OuvTelecomColumns.ID_OUVRAGE_COMM_NRJ.toString())!=null){
                if(typesOuvrage.get(row.getInt(OuvTelecomColumns.ID_OUVRAGE_COMM_NRJ.toString()))!=null){
                    reseau.setTypeOuvrageTelecomEnergieId(typesOuvrage.get(row.getInt(OuvTelecomColumns.ID_OUVRAGE_COMM_NRJ.toString())).getId());
                }
            }
            
            if(row.getInt(OuvTelecomColumns.ID_TYPE_POSITION.toString())!=null){
                reseau.setPosition_structure(typesPosition.get(row.getInt(OuvTelecomColumns.ID_TYPE_POSITION.toString())).getId());
            }
            
//            if (row.getDouble(OuvTelecomColumns.HAUTEUR.toString()) != null) {
//                reseau.setHauteur(row.getDouble(OuvTelecomColumns.HAUTEUR.toString()).floatValue());
//            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
            ouvrages.put(row.getInt(OuvTelecomColumns.ID_ELEMENT_RESEAU.toString()), reseau);

            // Set the list ByTronconId
            List<OuvrageTelecomEnergie> listByTronconId = ouvragesByTronconId.get(row.getInt(OuvTelecomColumns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                ouvragesByTronconId.put(row.getInt(OuvTelecomColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(reseau);
        }
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (OuvTelecomColumns c : OuvTelecomColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
