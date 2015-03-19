package fr.sirs.importer.link.photo;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefOrientationPhoto;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.link.GenericEntityLinker;
import fr.sirs.importer.objet.ObjetManager;
import fr.sirs.importer.system.TypeDonneesSousGroupeImporter;
import fr.sirs.importer.documentTroncon.DocumentImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class PhotoLocaliseeEnPrImporter extends GenericEntityLinker {

    private Map<Integer, Photo> photos = null;
    private final TypeDonneesSousGroupeImporter typeDonneesSousGroupeImporter;
    private final ObjetManager objetManager;
    private final TronconGestionDigueImporter tronconGestionDigueImporter;
    private final SystemeReperageImporter systemeReperageImporter;
    private final BorneDigueImporter borneDigueImporter;
    private final IntervenantImporter intervenantImporter;
    private final DocumentImporter documentImporter;
    private final OrientationImporter orientationImporter;

    public PhotoLocaliseeEnPrImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter, 
            final IntervenantImporter intervenantImporter,
            final DocumentImporter documentImporter,
            final OrientationImporter orientationImporter) {
        super(accessDatabase, couchDbConnector);
        this.typeDonneesSousGroupeImporter = new TypeDonneesSousGroupeImporter(
                accessDatabase, couchDbConnector);;
        this.orientationImporter = orientationImporter;
        this.tronconGestionDigueImporter = tronconGestionDigueImporter;
        this.objetManager = tronconGestionDigueImporter.getObjetManager();
        this.systemeReperageImporter = systemeReperageImporter;
        this.borneDigueImporter = borneDigueImporter;
        this.intervenantImporter = intervenantImporter;
        this.documentImporter = documentImporter;
    }

    private enum Columns {
        ID_PHOTO,
        ID_ELEMENT_SOUS_GROUPE,
        ID_TRONCON_GESTION,
        ID_GROUPE_DONNEES,
        ID_SOUS_GROUPE_DONNEES,
        ID_ORIENTATION,
        ID_INTERV_PHOTOGRAPH,
        ID_DOC,
        REF_PHOTO,
        DESCRIPTION_PHOTO,
        NOM_FICHIER_PHOTO,
        ID_TYPE_COTE,
        DATE_PHOTO,
        PR_PHOTO,
        ID_SYSTEME_REP,
        X_PHOTO,
        Y_PHOTO,
        ID_BORNEREF,
        AMONT_AVAL,
        DIST_BORNEREF,
//        AVANT_APRES, // Pas dans le nouveau modèle
//        DATE_DERNIERE_MAJ // Pas dans le nouveau modèle
    };
    

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PHOTO_LOCALISEE_EN_PR.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        photos = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        final Map<Integer, DocumentTroncon> docTroncons = documentImporter.getDocuments();
        
        final Map<Integer, RefOrientationPhoto> orientations = orientationImporter.getTypeReferences();
        final Map<Integer, RefCote> cotes = objetManager.getTypeCoteImporter().getTypeReferences();
        final Map<Entry<Integer, Integer>, DbImporter.TableName> types = typeDonneesSousGroupeImporter.getTypes();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Photo photo = new Photo();
            
//            photo.setObjet(null);
//            photo.setPhotoIds(null);
            
            final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                photo.setTroncon_digue(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }
            
            if(row.getInt(Columns.ID_ORIENTATION.toString())!=null){
                photo.setOrientationPhoto(orientations.get(row.getInt(Columns.ID_ORIENTATION.toString())).getId());
            }
            
            if (row.getInt(Columns.ID_INTERV_PHOTOGRAPH.toString()) != null) {
                photo.setPhotographe(intervenants.get(row.getInt(Columns.ID_INTERV_PHOTOGRAPH.toString())).getId());
            }
            
            if (row.getInt(Columns.ID_DOC.toString()) != null) {
                photo.setDocumentRelated(docTroncons.get(row.getInt(Columns.ID_DOC.toString())).getId());
            }
            
            photo.setLibelle(cleanNullString(row.getString(Columns.REF_PHOTO.toString())));
            
            photo.setCommentaire(cleanNullString(row.getString(Columns.DESCRIPTION_PHOTO.toString())));
            
            photo.setReferenceNumerique(cleanNullString(row.getString(Columns.NOM_FICHIER_PHOTO.toString())));
            
            if(row.getInt(Columns.ID_TYPE_COTE.toString())!=null){
                photo.setCoteId(cotes.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
            }
            
            if (row.getDate(Columns.DATE_PHOTO.toString()) != null) {
                try{
                    photo.setDate(LocalDateTime.parse(row.getDate(Columns.DATE_PHOTO.toString()).toString(), dateTimeFormatter));
                } catch (DateTimeParseException e){
                    SirsCore.LOGGER.log(Level.FINE, e.getMessage());
                }
            }
            
            if (row.getDouble(Columns.PR_PHOTO.toString()) != null) {
                photo.setPR_debut(row.getDouble(Columns.PR_PHOTO.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.PR_PHOTO.toString()) != null) {
                photo.setPR_fin(row.getDouble(Columns.PR_PHOTO.toString()).floatValue());
            }
            
            if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
                photo.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X_PHOTO.toString()) != null 
                            && row.getDouble(Columns.Y_PHOTO.toString()) != null) {
                        photo.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_PHOTO.toString()),
                                row.getDouble(Columns.Y_PHOTO.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(PhotoLocaliseeEnPrImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_PHOTO.toString()) != null 
                            && row.getDouble(Columns.Y_PHOTO.toString()) != null) {
                        photo.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_PHOTO.toString()),
                                row.getDouble(Columns.Y_PHOTO.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(PhotoLocaliseeEnPrImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(PhotoLocaliseeEnPrImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getDouble(Columns.ID_BORNEREF.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF.toString()).doubleValue());
                if(b!=null){
                    photo.setBorneDebutId(b.getId());
                }
                else{
                    SirsCore.LOGGER.log(Level.FINE, "Borne inconnue : "+row.getDouble(Columns.ID_BORNEREF.toString()));
                }
            }
            
            if (row.getDouble(Columns.ID_BORNEREF.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF.toString()).doubleValue());
                if(b!=null){
                    photo.setBorneFinId(b.getId());
                }
                else{
                    SirsCore.LOGGER.log(Level.FINE, "Borne inconnue : "+row.getDouble(Columns.ID_BORNEREF.toString()));
                }
            }
            
            photo.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL.toString()));
            
            photo.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL.toString()));
            
            if (row.getDouble(Columns.DIST_BORNEREF.toString()) != null) {
                photo.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF.toString()) != null) {
                photo.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF.toString()).floatValue());
            }
            photo.setDesignation(String.valueOf(row.getInt(Columns.ID_PHOTO.toString())));
            photo.setValid(true);

            final Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<Integer, Integer>(
                    row.getInt(Columns.ID_GROUPE_DONNEES.toString()), 
                    row.getInt(Columns.ID_SOUS_GROUPE_DONNEES.toString()));
            
            final DbImporter.TableName tableName = types.get(entry);
            final Integer id = row.getInt(Columns.ID_ELEMENT_SOUS_GROUPE.toString());
            
            if(tableName!=null && id!=null){
            switch(tableName){
                // STRUCTURES
                case SYS_EVT_CRETE:
                case SYS_EVT_EPIS:
                case SYS_EVT_FONDATION:
                case SYS_EVT_OUVRAGE_REVANCHE:
                case SYS_EVT_PIED_DE_DIGUE:
                case SYS_EVT_PIED_FRONT_FRANC_BORD:
                case SYS_EVT_SOMMET_RISBERME:
                case SYS_EVT_TALUS_DIGUE:
                case SYS_EVT_TALUS_FRANC_BORD:
                case SYS_EVT_TALUS_RISBERME:
                    final Objet structure = objetManager.getElementStructureImporter().getById().get(id);
                    structure.getPhoto().add(photo);
                    break;
                // RESEAUX
                case SYS_EVT_AUTRE_OUVRAGE_HYDRAULIQUE:
                case SYS_EVT_CHEMIN_ACCES:
                case SYS_EVT_CONDUITE_FERMEE:
                case SYS_EVT_OUVERTURE_BATARDABLE:
                case SYS_EVT_OUVRAGE_PARTICULIER:
                case SYS_EVT_OUVRAGE_TELECOMMUNICATION:
                case SYS_EVT_OUVRAGE_VOIRIE:
                case SYS_EVT_POINT_ACCES:
                case SYS_EVT_RESEAU_EAU:
                case SYS_EVT_RESEAU_TELECOMMUNICATION:
                case SYS_EVT_STATION_DE_POMPAGE:
                case SYS_EVT_VOIE_SUR_DIGUE:
                    final Objet reseau = objetManager.getElementReseauImporter().getById().get(id);
                    reseau.getPhoto().add(photo);
                    break;
                // GEOMETRIES
                case SYS_EVT_PROFIL_FRONT_FRANC_BORD:
                case SYS_EVT_LARGEUR_FRANC_BORD:
                    final Objet geometrie = objetManager.getElementGeometryImporter().getById().get(id);
                    geometrie.getPhoto().add(photo);
                    break;
                // DESORDRES
                case SYS_EVT_DESORDRE:
                    final Desordre desordre = objetManager.getDesordreImporter().getById().get(id);
                    if(desordre!=null){
                        desordre.getPhoto().add(photo);
                    } else {
                        SirsCore.LOGGER.log(Level.FINE, "Désordre null : "+id);
                    }
                    break;
                // PRESTATIONS
                case SYS_EVT_PRESTATION:
                    final Prestation prestation = objetManager.getPrestationImporter().getById().get(id);
                    prestation.getPhoto().add(photo);
                    break;
                // MONTEES DES EAUX
                case SYS_EVT_MONTEE_DES_EAUX_HYDRO:
                    final MonteeEaux monteeEaux = objetManager.getMonteeDesEauxImporter().getById().get(id);
                    monteeEaux.getPhoto().add(photo);
                    break;
                // LAISSE CRUES
                case SYS_EVT_LAISSE_CRUE:
                    final LaisseCrue laisseCrue = objetManager.getLaisseCrueImporter().getById().get(id);
                    laisseCrue.getPhoto().add(photo);
                    break;
                default:
                    SirsCore.LOGGER.log(Level.FINE, "Autre photo : "+tableName);
            }
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            photos.put(row.getInt(Columns.ID_PHOTO.toString()), photo);

        }
    }
}
