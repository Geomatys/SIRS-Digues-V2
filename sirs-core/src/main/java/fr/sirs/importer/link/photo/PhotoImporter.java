package fr.sirs.importer.link.photo;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.contact.IntervenantImporter;
import fr.sirs.importer.v2.references.TypeCoteImporter;
import fr.sirs.importer.documentTroncon.PositionDocumentImporter;
import fr.sirs.importer.link.GenericEntityLinker;
import fr.sirs.importer.objet.ObjetManager;
import fr.sirs.importer.system.TypeDonneesSousGroupeImporter;
import fr.sirs.importer.v2.linear.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class PhotoImporter extends GenericEntityLinker {

    protected Map<Integer, Photo> photos = null;

    protected final ObjetManager objetManager;
    protected final TypeDonneesSousGroupeImporter typeDonneesSousGroupeImporter;
    protected final TronconGestionDigueImporter tronconGestionDigueImporter;
    protected final IntervenantImporter intervenantImporter;
    protected final PositionDocumentImporter documentImporter;
    protected final OrientationImporter orientationImporter;
    protected final TypeCoteImporter typeCoteImporter;

    public PhotoImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final ObjetManager objetManager,
            final IntervenantImporter intervenantImporter,
            final PositionDocumentImporter documentImporter,
            final OrientationImporter orientationImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypeDonneesSousGroupeImporter typeDonneesSousGroupeImporter) {
        super(accessDatabase, couchDbConnector);
        this.typeDonneesSousGroupeImporter = typeDonneesSousGroupeImporter;
        this.orientationImporter = orientationImporter;
        this.tronconGestionDigueImporter = tronconGestionDigueImporter;
        this.objetManager = objetManager;
        this.intervenantImporter = intervenantImporter;
        this.documentImporter = documentImporter;
        this.typeCoteImporter = typeCoteImporter;
    }

    protected void attachPhoto(final Integer id, final DbImporter.TableName tableName, final Photo photo) throws IOException, AccessDbImporterException {
        switch (tableName) {
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
                objetManager.getElementStructureImporter().getById().get(id).getPhotos().add(photo);
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
                objetManager.getElementReseauImporter().getById().get(id).getPhotos().add(photo);
                break;
            // GEOMETRIES
            case SYS_EVT_PROFIL_FRONT_FRANC_BORD:
            case SYS_EVT_LARGEUR_FRANC_BORD:
                objetManager.getElementGeometryImporter().getById().get(id).getPhotos().add(photo);
                break;
            // DESORDRES
            case SYS_EVT_DESORDRE: //ATTENTION, L'indication de la table des désordres est trompeuse : l'id fourni est en réalité un id d'observation !!!!
                final Observation observation = objetManager.getDesordreImporter().getDesordreObservationImporter().getObservations().get(id);
                if (observation != null) {
                    observation.getPhotos().add(photo);
                } else {    
                    SirsCore.LOGGER.log(Level.FINE, "Observation nulle : " + id);
                }
                break;
            // PRESTATIONS
            case SYS_EVT_PRESTATION:
                objetManager.getPrestationImporter().getById().get(id).getPhotos().add(photo);
                break;
            // MONTEES DES EAUX
            case SYS_EVT_MONTEE_DES_EAUX_HYDRO:
                objetManager.getMonteeDesEauxImporter().getById().get(id).getPhotos().add(photo);
                break;
            // LAISSE CRUES
            case SYS_EVT_LAISSE_CRUE:
                objetManager.getLaisseCrueImporter().getById().get(id).getPhotos().add(photo);
                break;
            // LAISSE CRUES
            case SYS_EVT_LIGNE_EAU:
                objetManager.getLigneEauImporter().getById().get(id).getPhotos().add(photo);
                break;
                
            // POSITIONS DE PROFILS EN TRAVERS:
            case SYS_EVT_PROFIL_EN_TRAVERS: 
                ((PositionProfilTravers) documentImporter.getPositions().get(id)).getPhotos().add(photo);
                break;
            // PROFILS EN LONG:
            case SYS_EVT_PROFIL_EN_LONG:
            // POSITIONS DE DOCUMENTS
            case SYS_EVT_CONVENTION:
            case SYS_EVT_DOCUMENT_A_GRANDE_ECHELLE:
            case SYS_EVT_JOURNAL:
            case SYS_EVT_MARCHE:
            case SYS_EVT_RAPPORT_ETUDES:
            default:
                SirsCore.LOGGER.log(Level.FINE, "Autre photo : {0}", tableName);
        }
    }
}
