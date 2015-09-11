package fr.sirs.importer.system;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Epi;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
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
public class TypeDonneesSousGroupeImporter extends DocumentImporter {
    
    private Map<Entry<Integer, Integer>, DbImporter.TableName> types = null;
    private Map<Entry<Integer, Integer>, Class<? extends Element>> classes = null;

    public TypeDonneesSousGroupeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_GROUPE_DONNEES,
        ID_SOUS_GROUPE_DONNEES,
        ID_TYPE_DONNEE,
//        LIBELLE_SOUS_GROUPE_DONNEES,
        NOM_TABLE_EVT,
//        ID_NOM_TABLE_EVTTYPE_OBJET_CARTO,
//        DECALAGE,
//        DATE_DERNIERE_MAJ
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
        return TYPE_DONNEES_SOUS_GROUPE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        classes = new HashMap<>();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<>(
                    row.getInt(Columns.ID_GROUPE_DONNEES.toString()), 
                    row.getInt(Columns.ID_SOUS_GROUPE_DONNEES.toString()));
            try{
                final DbImporter.TableName tableName = valueOf(row.getString(String.valueOf(Columns.NOM_TABLE_EVT.toString())));
                if(tableName!=null){
                    types.put(entry, tableName);
                    final Class<? extends Element> clazz = getClazz(tableName);
                    if(clazz!=null){
                        classes.put(entry, clazz);
                    }
                }
            } catch(IllegalArgumentException e){
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }
    }
    
    private Map<Entry<Integer, Integer>, Class<? extends Element>> getClasses() throws IOException{
        if(classes == null) compute();
        return classes;
    }
    
    private Class<? extends Element> getClazz(final DbImporter.TableName tableName){
    
        switch(tableName){
            // STRUCTURES
            case SYS_EVT_CRETE: return Crete.class;
            case SYS_EVT_EPIS: return Epi.class;
            case SYS_EVT_FONDATION: return Fondation.class;
            case SYS_EVT_OUVRAGE_REVANCHE: return OuvrageRevanche.class;
            case SYS_EVT_PIED_DE_DIGUE: return PiedDigue.class;
//            case SYS_EVT_PIED_FRONT_FRANC_BORD: return PiedFrontFrancBord.class;
            case SYS_EVT_SOMMET_RISBERME: return SommetRisberme.class;
            case SYS_EVT_TALUS_DIGUE: return TalusDigue.class;
//            case SYS_EVT_TALUS_FRANC_BORD: return FrontFrancBord.class;
            case SYS_EVT_TALUS_RISBERME: return TalusRisberme.class;
//            case SYS_EVT_BRISE_LAME:
            // RESEAUX
            case SYS_EVT_AUTRE_OUVRAGE_HYDRAULIQUE: return OuvrageHydrauliqueAssocie.class;
            case SYS_EVT_CHEMIN_ACCES: return VoieAcces.class;
            case SYS_EVT_CONDUITE_FERMEE: return ReseauHydrauliqueFerme.class;
            case SYS_EVT_OUVERTURE_BATARDABLE: return OuvertureBatardable.class;
            case SYS_EVT_OUVRAGE_PARTICULIER: return OuvrageParticulier.class;
            case SYS_EVT_OUVRAGE_TELECOMMUNICATION: return OuvrageTelecomEnergie.class;
            case SYS_EVT_OUVRAGE_VOIRIE: return OuvrageVoirie.class;
            case SYS_EVT_POINT_ACCES: return OuvrageFranchissement.class;
            case SYS_EVT_RESEAU_EAU: return ReseauHydrauliqueCielOuvert.class;
            case SYS_EVT_RESEAU_TELECOMMUNICATION: return ReseauTelecomEnergie.class;
            case SYS_EVT_STATION_DE_POMPAGE: return StationPompage.class;
            case SYS_EVT_VOIE_SUR_DIGUE: return VoieDigue.class;
            // GEOMETRIES
//            case SYS_EVT_PROFIL_FRONT_FRANC_BORD: return ProfilFrontFrancBord.class;
            case SYS_EVT_LARGEUR_FRANC_BORD: return LargeurFrancBord.class;
            // DESORDRES
            case SYS_EVT_DESORDRE: return Desordre.class;
            // PRESTATIONS
            case SYS_EVT_PRESTATION: return Prestation.class;
            // MONTEES DES EAUX
            case SYS_EVT_MONTEE_DES_EAUX_HYDRO: return MonteeEaux.class;
            // LAISSE CRUES
            case SYS_EVT_LAISSE_CRUE: return LaisseCrue.class;
            // LIGNE EAU
            case SYS_EVT_LIGNE_EAU: return LigneEau.class;
            // POSITIONS DE DOCUMENTS
            case SYS_EVT_CONVENTION:
            case SYS_EVT_DOCUMENT_A_GRANDE_ECHELLE:
            case SYS_EVT_JOURNAL:
            case SYS_EVT_MARCHE:
            case SYS_EVT_RAPPORT_ETUDES: return PositionDocument.class;
            // POSITIONS DE PROFILS EN TRAVERS:
            case SYS_EVT_PROFIL_EN_TRAVERS: return PositionProfilTravers.class;
            // PROFILS EN LONG:
            case SYS_EVT_PROFIL_EN_LONG: return ProfilLong.class;
//            case SYS_EVT_DISTANCE_PIED_DE_DIGUE_TRONCON:
//            case SYS_EVT_ILE_TRONCON:
//            case SYS_EVT_PROPRIETAIRE_TRONCON:
//            case SYS_EVT_GARDIEN_TRONCON:
//            case SYS_EVT_VEGETATION:
//            case SYS_EVT_PHOTO_LOCALISEE_EN_PR:
//            case SYS_EVT_EMPRISE_COMMUNALE:
//            case SYS_EVT_EMPRISE_SYNDICAT:
//            case SYS_EVT_SITUATION_FONCIERE:
            default: return null;
//SYS_RQ_EXTRAIT_DESORDRE_TGD
//SYS_RQ_MONTANT_PRESTATION_TGD
//SYS_RQ_PROPRIETAIRE_TRAVERSEE_TGD
//SYS_RQ_SENSIBILITE_EVT_HYDRAU_TGD
        }
    }

    
    public Map<Entry<Integer, Integer>, DbImporter.TableName> getTypes() throws IOException{
        if(types==null) compute();
        return types;
    }
}
