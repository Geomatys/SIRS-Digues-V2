package fr.sirs.importer.objet.reseau;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Contact;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.RefMoyenManipBatardeaux;
import fr.sirs.core.model.RefNatureBatardeaux;
import fr.sirs.core.model.RefOrientationOuvrage;
import fr.sirs.core.model.RefOuvrageFranchissement;
import fr.sirs.core.model.RefOuvrageParticulier;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSeuil;
import fr.sirs.core.model.RefTypeGlissiere;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauHydroCielOuvert;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.objet.GenericObjetImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
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
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementReseauImporter extends GenericReseauImporter<Objet> {
    
    private final TypeElementReseauImporter typeElementReseauImporter;
    
    private final OrganismeImporter organismeImporter;
    private final IntervenantImporter intervenantImporter;
    
    private final List<GenericObjetImporter> reseauImporters = new ArrayList<>();
    private final EcoulementImporter typeEcoulementImporter;
    private final ImplantationImporter typeImplantationImporter;
    private final TypeConduiteFermeeImporter typeConduiteFermeeImporter;
    private final UtilisationConduiteImporter typeUtilisationConduiteImporter;
    private final SysEvtConduiteFermeeImporter sysEvtConduiteFermeeImporter;
    private final ElementReseauPompeImporter pompeImporter;
    private final SysEvtStationDePompageImporter sysEvtStationDePompageImporter;
    private final TypeReseauTelecommunicImporter typeReseauTelecomImporter;
    private final SysEvtReseauTelecommunicationImporter sysEvtReseauTelecommunicationImporter;
    private final TypeOuvrageTelecomNrjImporter typeOuvrageTelecomImporter;
    private final SysEvtOuvrageTelecommunicationImporter sysEvtOuvrageTelecommunicationImporter;
    private final TypeOuvrageHydrauAssocieImporter typeOuvrageAssocieImporter;
    private final SysEvtAutreOuvrageHydrauliqueImporter sysEvtAutreOuvrageHydrauliqueImporter;
    private final TypeUsageVoieImporter typeUsageVoieImporter;
    private final SysEvtCheminAccesImporter sysEvtCheminAccesImporter;
    private final TypeOrientationOuvrageFranchissementImporter typeOrientationOuvrageFranchissementImporter;
    private final TypeRevetementImporter typeRevetementImporter;
    private final TypeOuvrageFranchissementImporter typeOuvrageFranchissementImporter;
    private final SysEvtPointAccesImporter sysEvtPointAccesImporter;
    private final TypeVoieSurDigueImporter typeVoieSurDigueImporter;
    private final SysEvtVoieSurDigueImporter sysEvtVoieSurDigueImporter;
    private final TypeOuvrageVoirieImporter typeOuvrageVoirieImporter;
    private final SysEvtOuvrageVoirieImporter sysEvtOuvrageVoirieImporter;
    private final TypeReseauEauImporter typeReseauEauImporter;
    private final SysEvtReseauEauImporter sysEvtReseauEauImporter;
    private final TypeOuvrageParticulierImporter typeOuvrageParticulierImporter;
    private final SysEvtOuvrageParticulierImporter sysEvtOuvrageParticulierImporter;
    private final TypeNatureBatardeauxImporter typeNatureBatardeauxImporter;
    private final TypeMoyenManipBatardeauxImporter typeMoyenManipBatardeauxImporter;
    private final TypeSeuilImporter typeSeuilImporter;
    private final TypeGlissiereImporter typeGlissiereImporter;
    private final SysEvtOuvertureBatardableImporter sysEvtOuvertureBatardableImporter;

    public ElementReseauImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final OrganismeImporter organismeImporter,
            final IntervenantImporter intervenantImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter, 
            final TypePositionImporter typePositionImporter,
            final TypeNatureImporter typeNatureImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, 
                typePositionImporter, typeNatureImporter);
        this.organismeImporter = organismeImporter;
        this.intervenantImporter = intervenantImporter;
        
        typeElementReseauImporter = new TypeElementReseauImporter(
                accessDatabase, couchDbConnector);
        typeEcoulementImporter = new EcoulementImporter(accessDatabase, 
                couchDbConnector);
        typeImplantationImporter = new ImplantationImporter(accessDatabase, 
                couchDbConnector);
        typeConduiteFermeeImporter = new TypeConduiteFermeeImporter(
                accessDatabase, couchDbConnector);
        typeUtilisationConduiteImporter = new UtilisationConduiteImporter(
                accessDatabase, couchDbConnector);
        sysEvtConduiteFermeeImporter = new SysEvtConduiteFermeeImporter(accessDatabase, 
                couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter, typeEcoulementImporter, 
                typeImplantationImporter, typeConduiteFermeeImporter, 
                typeUtilisationConduiteImporter);
        reseauImporters.add(sysEvtConduiteFermeeImporter);
        pompeImporter = new ElementReseauPompeImporter(accessDatabase, couchDbConnector);
        sysEvtStationDePompageImporter = new SysEvtStationDePompageImporter(accessDatabase, 
                couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter, pompeImporter);
        reseauImporters.add(sysEvtStationDePompageImporter);
        typeReseauTelecomImporter = new TypeReseauTelecommunicImporter(
                accessDatabase, couchDbConnector);
        sysEvtReseauTelecommunicationImporter = new SysEvtReseauTelecommunicationImporter(accessDatabase, 
                couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter, typeImplantationImporter, 
                typeReseauTelecomImporter);
        reseauImporters.add(sysEvtReseauTelecommunicationImporter);
        typeOuvrageTelecomImporter = new TypeOuvrageTelecomNrjImporter(
                accessDatabase, couchDbConnector);
        sysEvtOuvrageTelecommunicationImporter = new SysEvtOuvrageTelecommunicationImporter(accessDatabase, 
                couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter, typeOuvrageTelecomImporter);
        reseauImporters.add(sysEvtOuvrageTelecommunicationImporter);
        typeOuvrageAssocieImporter = new TypeOuvrageHydrauAssocieImporter(
                accessDatabase, couchDbConnector);
        sysEvtAutreOuvrageHydrauliqueImporter = new SysEvtAutreOuvrageHydrauliqueImporter(
                accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter, 
                typeOuvrageAssocieImporter);
        reseauImporters.add(sysEvtAutreOuvrageHydrauliqueImporter);
        typeUsageVoieImporter = new TypeUsageVoieImporter(accessDatabase, 
                couchDbConnector);
        sysEvtCheminAccesImporter = new SysEvtCheminAccesImporter(
                accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter, typeNatureImporter, 
                typeUsageVoieImporter);
        reseauImporters.add(sysEvtCheminAccesImporter);
        typeOrientationOuvrageFranchissementImporter = new TypeOrientationOuvrageFranchissementImporter(
                accessDatabase, couchDbConnector);
        typeRevetementImporter = new TypeRevetementImporter(accessDatabase, 
                couchDbConnector);
        typeOuvrageFranchissementImporter = new TypeOuvrageFranchissementImporter(
                accessDatabase, couchDbConnector);
        sysEvtPointAccesImporter = new SysEvtPointAccesImporter(accessDatabase, 
                couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter, typeUsageVoieImporter, 
                typeRevetementImporter);
        reseauImporters.add(sysEvtPointAccesImporter);
        typeVoieSurDigueImporter = new TypeVoieSurDigueImporter(accessDatabase, 
                couchDbConnector);
        sysEvtVoieSurDigueImporter = new SysEvtVoieSurDigueImporter(
                accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter, typeUsageVoieImporter, 
                typeRevetementImporter, typeVoieSurDigueImporter);
        reseauImporters.add(sysEvtVoieSurDigueImporter);
        typeOuvrageVoirieImporter = new TypeOuvrageVoirieImporter(
                accessDatabase, couchDbConnector);
        sysEvtOuvrageVoirieImporter = new SysEvtOuvrageVoirieImporter(
                accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, typeOuvrageVoirieImporter);
        reseauImporters.add(sysEvtOuvrageVoirieImporter);
        typeReseauEauImporter = new TypeReseauEauImporter(accessDatabase, 
                couchDbConnector);
        sysEvtReseauEauImporter = new SysEvtReseauEauImporter(accessDatabase, 
                couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter, typeReseauEauImporter);
        reseauImporters.add(sysEvtReseauEauImporter);
        typeOuvrageParticulierImporter = new TypeOuvrageParticulierImporter(
                accessDatabase, couchDbConnector);
        sysEvtOuvrageParticulierImporter = new SysEvtOuvrageParticulierImporter(
                accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter);
        reseauImporters.add(sysEvtOuvrageParticulierImporter);
        typeNatureBatardeauxImporter = new TypeNatureBatardeauxImporter(
                accessDatabase, couchDbConnector);
        typeMoyenManipBatardeauxImporter = new TypeMoyenManipBatardeauxImporter(
                accessDatabase, couchDbConnector);
        typeSeuilImporter = new TypeSeuilImporter(accessDatabase, 
                couchDbConnector);
        typeGlissiereImporter = new TypeGlissiereImporter(accessDatabase, 
                couchDbConnector);
        sysEvtOuvertureBatardableImporter = new SysEvtOuvertureBatardableImporter(
                accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter);
        reseauImporters.add(sysEvtOuvertureBatardableImporter);
    }
    
    public TypeElementReseauImporter getTypeElementReseauImporter(){
        return typeElementReseauImporter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_TYPE_ELEMENT_RESEAU,
//        ID_TYPE_COTE,
//        ID_SOURCE,
        ID_TRONCON_GESTION,
//        DATE_DEBUT_VAL,
//        DATE_FIN_VAL,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        X_DEBUT,
//        Y_DEBUT,
//        X_FIN,
//        Y_FIN,
//        ID_SYSTEME_REP,
//        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
//        DIST_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
//        DIST_BORNEREF_FIN,
//        COMMENTAIRE,
//        NOM,
//        ID_ECOULEMENT,
//        ID_IMPLANTATION,
//        ID_UTILISATION_CONDUITE,
//        ID_TYPE_CONDUITE_FERMEE,
//        AUTORISE,
//        ID_TYPE_OUVR_HYDRAU_ASSOCIE,
//        ID_TYPE_RESEAU_COMMUNICATION,
//        ID_OUVRAGE_COMM_NRJ,
//        N_SECTEUR,
//        ID_TYPE_VOIE_SUR_DIGUE,
//        ID_OUVRAGE_VOIRIE,
//        ID_TYPE_REVETEMENT,
//        ID_TYPE_USAGE_VOIE,
//        LARGEUR,
//        ID_TYPE_OUVRAGE_VOIRIE,
        ID_TYPE_POSITION,
        ID_TYPE_POSITION_HAUTE,
//        HAUTEUR,
//        DIAMETRE,
//        ID_TYPE_RESEAU_EAU,
//        ID_ORG_PROPRIO,
//        ID_ORG_GESTION,
//        ID_INTERV_PROPRIO,
//        ID_INTERV_GARDIEN,
        ID_TYPE_OUVRAGE_PARTICULIER,
//        DATE_DEBUT_ORGPROPRIO,
//        DATE_FIN_ORGPROPRIO,
//        DATE_DEBUT_GESTION,
//        DATE_FIN_GESTION,
//        DATE_DEBUT_INTERVPROPRIO,
//        DATE_FIN_INTERVPROPRIO,
//        ID_TYPE_OUVRAGE_TELECOM_NRJ,
        ID_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT,
        ID_TYPE_OUVRAGE_FRANCHISSEMENT,
        DATE_DERNIERE_MAJ,
        Z_SEUIL,
        ID_TYPE_SEUIL,
        ID_TYPE_GLISSIERE,
        ID_TYPE_NATURE_BATARDEAUX,
        NOMBRE,
        POIDS,
        ID_TYPE_MOYEN_MANIP_BATARDEAUX,
        ID_ORG_STOCKAGE_BATARDEAUX,
        ID_ORG_MANIP_BATARDEAUX,
        ID_INTERV_MANIP_BATARDEAUX,
//        ID_TYPE_NATURE,
//        ID_TYPE_NATURE_HAUT,
//        ID_TYPE_NATURE_BAS,
//        ID_TYPE_REVETEMENT_HAUT,
//        ID_TYPE_REVETEMENT_BAS
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
        return DbImporter.TableName.ELEMENT_RESEAU.toString();
    }
    
    @Override
    protected void compute() throws IOException, AccessDbImporterException {    

        structures = new HashMap<>();
        structuresByTronconId = new HashMap<>();

        // Remplissage initial des structures par les importateurs subordonnés.
        for (final GenericObjetImporter gsi : reseauImporters){
            final Map<Integer, Objet> objets = gsi.getById();
            if(objets!=null){
                for (final Integer key : objets.keySet()){
                    if(structures.get(key)!=null){
                        throw new AccessDbImporterException(objets.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+structures.get(key).getClass().getCanonicalName());
                    }
                    else {
                        structures.put(key, objets.get(key));
                    }
                }
            }
            
            final Map<Integer, List<Objet>> objetsByTronconId = gsi.getByTronconId();

            if (objetsByTronconId != null) {
                objetsByTronconId.keySet().stream().map((key) -> {
                    if (structuresByTronconId.get(key) == null) {
                        structuresByTronconId.put(key, new ArrayList<>());
                    }
                    return key;
                }).forEach((key) -> {
                    if (objetsByTronconId.get(key) != null) {
                        structuresByTronconId.get(key).addAll(objetsByTronconId.get(key));
                    }
                });
            }
        }

        final Map<Integer, RefOrientationOuvrage> typesOrientationOuvrageFranchissement = typeOrientationOuvrageFranchissementImporter.getTypeReferences();
        final Map<Integer, RefOuvrageParticulier> typesOuvrageParticulier = typeOuvrageParticulierImporter.getTypeReferences();
        final Map<Integer, RefOuvrageFranchissement> typesOuvrageFranchissement = typeOuvrageFranchissementImporter.getTypeReferences();
        final Map<Integer, RefNatureBatardeaux> typesNatureBatardeaux = typeNatureBatardeauxImporter.getTypeReferences();
        final Map<Integer, RefMoyenManipBatardeaux> typesMoyenManipBatardeaux = typeMoyenManipBatardeauxImporter.getTypeReferences();
        final Map<Integer, RefSeuil> typesSeuil = typeSeuilImporter.getTypeReferences();
        final Map<Integer, RefTypeGlissiere> typesGlissiere = typeGlissiereImporter.getTypeReferences();
        final Map<Integer, Contact> contacts = intervenantImporter.getIntervenants();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypeReferences();

        // Vérification de la cohérence des structures au sens strict.
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();

            final int structureId = row.getInt(Columns.ID_ELEMENT_RESEAU.toString());
            final Objet objet;
            final boolean nouvelObjet;
            
            if(structures.get(structureId)!=null){
                objet = structures.get(structureId);
                nouvelObjet=false;
            }
            else{
                SirsCore.LOGGER.log(Level.FINE, "Nouvel objet !!");
                objet = importRow(row);
                nouvelObjet=true;
            }
            
            
            if(objet instanceof OuvrageFranchissement){
                
                final OuvrageFranchissement pointAcces = (OuvrageFranchissement) objet;
            
                if(row.getInt(Columns.ID_TYPE_POSITION.toString())!=null){
                    pointAcces.setPositionBasId(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
                }

                if(row.getInt(Columns.ID_TYPE_POSITION_HAUTE.toString())!=null){
                    pointAcces.setPositionHautId(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION_HAUTE.toString())).getId());
                }
            
                if(row.getInt(Columns.ID_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString())!=null){
                    pointAcces.setOrientationOuvrageId(typesOrientationOuvrageFranchissement.get(row.getInt(Columns.ID_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString())).getId());
                }
            
                if(row.getInt(Columns.ID_TYPE_OUVRAGE_FRANCHISSEMENT.toString())!=null){
                    pointAcces.setTypeOuvrageFranchissementId(typesOuvrageFranchissement.get(row.getInt(Columns.ID_TYPE_OUVRAGE_FRANCHISSEMENT.toString())).getId());
                }
            }
            else if (objet instanceof OuvrageParticulier){
                
                final OuvrageParticulier ouvrage = (OuvrageParticulier) objet;
            
                if(row.getInt(Columns.ID_TYPE_OUVRAGE_PARTICULIER.toString())!=null){
                    ouvrage.setTypeOuvrageParticulierId(typesOuvrageParticulier.get(row.getInt(Columns.ID_TYPE_OUVRAGE_PARTICULIER.toString())).getId());
                }
            }
            else if (objet instanceof OuvertureBatardable){
                
                final OuvertureBatardable ouverture = (OuvertureBatardable) objet;

                if (row.getDouble(Columns.Z_SEUIL.toString()) != null) {
                    ouverture.setZ_du_seuil(row.getDouble(Columns.Z_SEUIL.toString()).floatValue());
                }
                
                if(row.getInt(Columns.ID_TYPE_SEUIL.toString())!=null){
                    ouverture.setTypeSeuilId(typesSeuil.get(row.getInt(Columns.ID_TYPE_SEUIL.toString())).getId());
                }
                
                if(row.getInt(Columns.ID_TYPE_GLISSIERE.toString())!=null){
                    ouverture.setTypeGlissiereId(typesGlissiere.get(row.getInt(Columns.ID_TYPE_GLISSIERE.toString())).getId());
                }
                
                if(row.getInt(Columns.ID_TYPE_NATURE_BATARDEAUX.toString())!=null){
                    ouverture.setNatureBatardeauxId(typesNatureBatardeaux.get(row.getInt(Columns.ID_TYPE_NATURE_BATARDEAUX.toString())).getId());
                }
            
                if (row.getInt(Columns.NOMBRE.toString()) != null) {
                    ouverture.setNombre_batardeaux(row.getInt(Columns.NOMBRE.toString()));
                }

                if (row.getDouble(Columns.POIDS.toString()) != null) {
                    ouverture.setPoids_unitaires_batardeaux(row.getDouble(Columns.POIDS.toString()).floatValue());
                }
                
                if(row.getInt(Columns.ID_TYPE_MOYEN_MANIP_BATARDEAUX.toString())!=null){
                    ouverture.setMoyenManipBatardeauxId(typesMoyenManipBatardeaux.get(row.getInt(Columns.ID_TYPE_MOYEN_MANIP_BATARDEAUX.toString())).getId());
                }
                
                if(row.getInt(Columns.ID_ORG_STOCKAGE_BATARDEAUX.toString())!=null){
                    ouverture.setOrganismesStockantsId(organismes.get(row.getInt(Columns.ID_ORG_STOCKAGE_BATARDEAUX.toString())).getId());
                }
                
                if(row.getInt(Columns.ID_ORG_MANIP_BATARDEAUX.toString())!=null){
                    ouverture.setOrganismesManipulateursId(organismes.get(row.getInt(Columns.ID_ORG_MANIP_BATARDEAUX.toString())).getId());
                }
                
                if(row.getInt(Columns.ID_INTERV_MANIP_BATARDEAUX.toString())!=null){
                    ouverture.setIntervenantManupulateurId(contacts.get(row.getInt(Columns.ID_INTERV_MANIP_BATARDEAUX.toString())).getId());
                }       
            }
            
            
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                objet.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if (nouvelObjet) {
            
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()), objet);

                // Set the list ByTronconId
                List<Objet> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
    }
    
    

    @Override
    public Objet importRow(Row row) throws IOException, AccessDbImporterException {
        final Class typeStructure = typeElementReseauImporter.getTypeReferences().get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
        if(typeStructure==OuvrageHydrauliqueAssocie.class){
            return sysEvtAutreOuvrageHydrauliqueImporter.importRow(row);
        } else if(typeStructure==VoieAcces.class){
            return sysEvtCheminAccesImporter.importRow(row);
        } else if(typeStructure==ReseauHydrauliqueFerme.class){
            return sysEvtConduiteFermeeImporter.importRow(row);
        } else if(typeStructure==OuvertureBatardable.class){
            return sysEvtOuvertureBatardableImporter.importRow(row);
        } else if(typeStructure==OuvrageParticulier.class){
            return sysEvtOuvrageParticulierImporter.importRow(row);
        } else if(typeStructure==OuvrageTelecomEnergie.class){
            return sysEvtOuvrageTelecommunicationImporter.importRow(row);
        } else if(typeStructure==OuvrageVoirie.class){
            return sysEvtOuvrageVoirieImporter.importRow(row);
        } else if(typeStructure==OuvrageFranchissement.class){
            return sysEvtPointAccesImporter.importRow(row);
        } else if(typeStructure==ReseauHydroCielOuvert.class){
            return sysEvtReseauEauImporter.importRow(row);
        } else if(typeStructure==ReseauTelecomEnergie.class){
            return sysEvtReseauTelecommunicationImporter.importRow(row);
        } else if(typeStructure==StationPompage.class){
            return sysEvtStationDePompageImporter.importRow(row);
        } else if(typeStructure==VoieDigue.class){
            return sysEvtVoieSurDigueImporter.importRow(row);
        } else{
            SirsCore.LOGGER.log(Level.SEVERE, "Type incohérent.");
            return null;
        }
    }
}
