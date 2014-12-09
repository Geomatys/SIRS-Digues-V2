package fr.sirs.importer.link.photo;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.Prestation;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.link.GenericEntityLinker;
import fr.sirs.importer.objet.ObjetManager;
import fr.sirs.importer.system.TypeDonneesSousGroupeImporter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PhotoLocaliseeEnPrImporter extends GenericEntityLinker {

    private Map<Integer, Photo> photos = null;
    private final TypeDonneesSousGroupeImporter typeDonneesSousGroupeImporter;
    private final ObjetManager objetManager;

    public PhotoLocaliseeEnPrImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final ObjetManager objetManager) {
        super(accessDatabase, couchDbConnector);
        typeDonneesSousGroupeImporter = new TypeDonneesSousGroupeImporter(
                accessDatabase, couchDbConnector);
        this.objetManager = objetManager;
    }

    private enum Columns {
        ID_PHOTO,
        ID_ELEMENT_SOUS_GROUPE,
//        ID_TRONCON_GESTION,
        ID_GROUPE_DONNEES,
        ID_SOUS_GROUPE_DONNEES,
//        ID_ORIENTATION,
//        ID_INTERV_PHOTOGRAPH,
//        ID_DOC,
//        REF_PHOTO,
//        DESCRIPTION_PHOTO,
//        NOM_FICHIER_PHOTO,
//        ID_TYPE_COTE,
//        DATE_PHOTO,
//        PR_PHOTO,
//        ID_SYSTEME_REP,
//        X_PHOTO,
//        Y_PHOTO,
//        ID_BORNEREF,
//        AMONT_AVAL,
//        DIST_BORNEREF,
//        AVANT_APRES,
//        DATE_DERNIERE_MAJ
    };

//    public Map<Integer, Photo> getPhotos() throws IOException {
//        if (photos == null) compute();
//        return photos;
//    }

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
        
        final Map<Entry<Integer, Integer>, DbImporter.TableName> types = typeDonneesSousGroupeImporter.getTypes();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Photo photo = new Photo();
            
//            photo.setCote(null);
//            photo.setObjet(null);
//            photo.setOrientation(null);
//            photo.setPhotoIds(null);
//            photo.setPhotographe(null);
//            photo.setTroncon_digue(null);
//            photo.setPR_debut(pR_debut);
//            photo.setPR_fin(pR_fin);
//            photo.setPositionDebut(null);
//            photo.setPositionFin(null);

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
                    break;
                // GEOMETRIES
                case SYS_EVT_PROFIL_FRONT_FRANC_BORD:
                case SYS_EVT_LARGEUR_FRANC_BORD:
                    final Objet geometrie = objetManager.getElementGeometryImporter().getById().get(id);
                    break;
                // DESORDRES
                case SYS_EVT_DESORDRE:
                    final Desordre desordre = objetManager.getDesordreImporter().getById().get(id);
                    break;
                // PRESTATIONS
                case SYS_EVT_PRESTATION:
                    final Prestation prestation = objetManager.getPrestationImporter().getById().get(id);
                    break;
                default:
                    System.out.println("Autre photo : "+tableName);
            }
            }
            
//            photo.
            
            
//            photo.setNom(row.getString(Columns.NOM_INTERVENANT.toString()));
//            
//            photo.setPrenom(row.getString(Columns.PRENOM_INTERVENANT.toString()));
//            
//            photo.setAdresse(cleanNullString(row.getString(Columns.ADRESSE_PERSO_INTERV.toString()))
//                    + cleanNullString(row.getString(Columns.ADRESSE_L1_PERSO_INTERV.toString()))
//                    + cleanNullString(row.getString(Columns.ADRESSE_L2_PERSO_INTERV.toString()))
//                    + cleanNullString(row.getString(Columns.ADRESSE_L3_PERSO_INTERV.toString())));
//            
//            photo.setCode_postal(cleanNullString(String.valueOf(row.getInt(Columns.ADRESSE_CODE_POSTAL_PERSO_INTERV.toString()))));
//            
//            photo.setCommune(row.getString(Columns.ADRESSE_NOM_COMMUNE_PERSO_INTERV.toString()));
//            
//            photo.setTelephone(row.getString(Columns.TEL_PERSO_INTERV.toString()));
//            
//            photo.setEmail(row.getString(Columns.MAIL_INTERV.toString()));
//            
//            photo.setFax(row.getString(Columns.FAX_PERSO_INTERV.toString()));
//            
//            photo.setService(row.getString(Columns.SERVICE_INTERV.toString()));
//            
//            photo.setFonction(row.getString(Columns.FONCTION_INTERV.toString()));
//            
//            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
//                photo.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT.toString()).toString(), dateTimeFormatter));
//            }
//            
//            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
//                photo.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN.toString()).toString(), dateTimeFormatter));
//            }
            
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            photos.put(row.getInt(Columns.ID_PHOTO.toString()), photo);

        }
    }
}
