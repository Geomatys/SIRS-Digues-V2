package fr.sirs.importer.link.photo;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Contact;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefOrientationPhoto;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TypeCoteImporter;
import fr.sirs.importer.objet.ObjetManager;
import fr.sirs.importer.system.TypeDonneesSousGroupeImporter;
import fr.sirs.importer.documentTroncon.PositionDocumentImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
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
public class PhotoLocaliseeEnPrImporter extends PhotoImporter {

    protected final SystemeReperageImporter systemeReperageImporter;
    protected final BorneDigueImporter borneDigueImporter;
            

    public PhotoLocaliseeEnPrImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final ObjetManager objetManager,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter, 
            final IntervenantImporter intervenantImporter,
            final PositionDocumentImporter documentImporter,
            final OrientationImporter orientationImporter, 
            final TypeCoteImporter typeCoteImporter,
            final TypeDonneesSousGroupeImporter typeDonneesSousGroupeImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                objetManager, intervenantImporter, documentImporter, 
                orientationImporter, typeCoteImporter,
                typeDonneesSousGroupeImporter);
        this.systemeReperageImporter = systemeReperageImporter;
        this.borneDigueImporter = borneDigueImporter;
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
        return PHOTO_LOCALISEE_EN_PR.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        photos = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        
        final Map<Integer, RefOrientationPhoto> orientations = orientationImporter.getTypeReferences();
        final Map<Integer, RefCote> cotes = typeCoteImporter.getTypeReferences();
        final Map<Entry<Integer, Integer>, DbImporter.TableName> types = typeDonneesSousGroupeImporter.getTypes();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            
            final Photo photo = createAnonymValidElement(Photo.class);
            
            if(row.getInt(Columns.ID_ORIENTATION.toString())!=null){
                photo.setOrientationPhoto(orientations.get(row.getInt(Columns.ID_ORIENTATION.toString())).getId());
            }
            
            if (row.getInt(Columns.ID_INTERV_PHOTOGRAPH.toString()) != null) {
                photo.setPhotographeId(intervenants.get(row.getInt(Columns.ID_INTERV_PHOTOGRAPH.toString())).getId());
            }
            
            photo.setLibelle(cleanNullString(row.getString(Columns.REF_PHOTO.toString())));
            
            photo.setCommentaire(cleanNullString(row.getString(Columns.DESCRIPTION_PHOTO.toString())));
            
            photo.setChemin(cleanNullString(row.getString(Columns.NOM_FICHIER_PHOTO.toString())));
            
            if(row.getInt(Columns.ID_TYPE_COTE.toString())!=null){
                photo.setCoteId(cotes.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
            }
            
            if (row.getDate(Columns.DATE_PHOTO.toString()) != null) {
                photo.setDate(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE_PHOTO.toString()), dateTimeFormatter));
            }
            
            if (row.getDouble(Columns.PR_PHOTO.toString()) != null) {
                photo.setPrDebut(row.getDouble(Columns.PR_PHOTO.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.PR_PHOTO.toString()) != null) {
                photo.setPrFin(row.getDouble(Columns.PR_PHOTO.toString()).floatValue());
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
                    Logger.getLogger(PhotoLocaliseeEnPrImporter.class.getName()).log(Level.WARNING, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_PHOTO.toString()) != null 
                            && row.getDouble(Columns.Y_PHOTO.toString()) != null) {
                        photo.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_PHOTO.toString()),
                                row.getDouble(Columns.Y_PHOTO.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(PhotoLocaliseeEnPrImporter.class.getName()).log(Level.WARNING, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(PhotoLocaliseeEnPrImporter.class.getName()).log(Level.WARNING, null, ex);
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
            photo.setGeometry(buildGeometry(troncon.getGeometry(), photo, tronconGestionDigueImporter.getBorneDigueRepository()));

            final Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<>(
                    row.getInt(Columns.ID_GROUPE_DONNEES.toString()), 
                    row.getInt(Columns.ID_SOUS_GROUPE_DONNEES.toString()));
            
            final DbImporter.TableName tableName = types.get(entry);
            final Integer id = row.getInt(Columns.ID_ELEMENT_SOUS_GROUPE.toString());
            
            if(tableName!=null && id!=null){
                attachPhoto(id, tableName, photo);
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            photos.put(row.getInt(Columns.ID_PHOTO.toString()), photo);
        }
    }
}
