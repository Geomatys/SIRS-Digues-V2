package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Contact;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.ObjetReseau;
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
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
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
public class ElementReseauImporter extends GenericReseauImporter<ObjetReseau> {

    private final TypeElementReseauImporter typeElementReseauImporter;

    private final OrganismeImporter organismeImporter;
    private final IntervenantImporter intervenantImporter;

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
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final OrganismeImporter organismeImporter,
            final IntervenantImporter intervenantImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePositionImporter typePositionImporter,
            final TypeNatureImporter typeNatureImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
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
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, typeEcoulementImporter,
                typeImplantationImporter, typeConduiteFermeeImporter,
                typeUtilisationConduiteImporter);
        pompeImporter = new ElementReseauPompeImporter(accessDatabase, couchDbConnector);
        sysEvtStationDePompageImporter = new SysEvtStationDePompageImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, pompeImporter);
        typeReseauTelecomImporter = new TypeReseauTelecommunicImporter(
                accessDatabase, couchDbConnector);
        sysEvtReseauTelecommunicationImporter = new SysEvtReseauTelecommunicationImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, typeImplantationImporter,
                typeReseauTelecomImporter);
        typeOuvrageTelecomImporter = new TypeOuvrageTelecomNrjImporter(
                accessDatabase, couchDbConnector);
        sysEvtOuvrageTelecommunicationImporter = new SysEvtOuvrageTelecommunicationImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, typeOuvrageTelecomImporter);
        typeOuvrageAssocieImporter = new TypeOuvrageHydrauAssocieImporter(
                accessDatabase, couchDbConnector);
        sysEvtAutreOuvrageHydrauliqueImporter = new SysEvtAutreOuvrageHydrauliqueImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter,
                typeOuvrageAssocieImporter);
        typeUsageVoieImporter = new TypeUsageVoieImporter(accessDatabase,
                couchDbConnector);
        sysEvtCheminAccesImporter = new SysEvtCheminAccesImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, typeNatureImporter,
                typeUsageVoieImporter);
        typeOrientationOuvrageFranchissementImporter = new TypeOrientationOuvrageFranchissementImporter(
                accessDatabase, couchDbConnector);
        typeRevetementImporter = new TypeRevetementImporter(accessDatabase,
                couchDbConnector);
        typeOuvrageFranchissementImporter = new TypeOuvrageFranchissementImporter(
                accessDatabase, couchDbConnector);
        sysEvtPointAccesImporter = new SysEvtPointAccesImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, typeUsageVoieImporter,
                typeRevetementImporter);
        typeVoieSurDigueImporter = new TypeVoieSurDigueImporter(accessDatabase,
                couchDbConnector);
        sysEvtVoieSurDigueImporter = new SysEvtVoieSurDigueImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, typeUsageVoieImporter,
                typeRevetementImporter, typeVoieSurDigueImporter);
        typeOuvrageVoirieImporter = new TypeOuvrageVoirieImporter(
                accessDatabase, couchDbConnector);
        sysEvtOuvrageVoirieImporter = new SysEvtOuvrageVoirieImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, typeOuvrageVoirieImporter);
        typeReseauEauImporter = new TypeReseauEauImporter(accessDatabase,
                couchDbConnector);
        sysEvtReseauEauImporter = new SysEvtReseauEauImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter, typeReseauEauImporter);
        typeOuvrageParticulierImporter = new TypeOuvrageParticulierImporter(
                accessDatabase, couchDbConnector);
        sysEvtOuvrageParticulierImporter = new SysEvtOuvrageParticulierImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter);
        typeNatureBatardeauxImporter = new TypeNatureBatardeauxImporter(
                accessDatabase, couchDbConnector);
        typeMoyenManipBatardeauxImporter = new TypeMoyenManipBatardeauxImporter(
                accessDatabase, couchDbConnector);
        typeSeuilImporter = new TypeSeuilImporter(accessDatabase,
                couchDbConnector);
        typeGlissiereImporter = new TypeGlissiereImporter(accessDatabase,
                couchDbConnector);
        sysEvtOuvertureBatardableImporter = new SysEvtOuvertureBatardableImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter,
                typeCoteImporter, typePositionImporter);
    }

    public TypeElementReseauImporter getTypeElementReseauImporter() {
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
        return ELEMENT_RESEAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();
        
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

            final ObjetReseau objet = importRow(row);

            if (objet != null) {
                if (objet instanceof OuvrageFranchissement) {

                    final OuvrageFranchissement pointAcces = (OuvrageFranchissement) objet;

                    if (row.getInt(Columns.ID_TYPE_POSITION.toString()) != null) {
                        pointAcces.setPositionBasId(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
                    }

                    if (row.getInt(Columns.ID_TYPE_POSITION_HAUTE.toString()) != null) {
                        pointAcces.setPositionHautId(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION_HAUTE.toString())).getId());
                    }

                    if (row.getInt(Columns.ID_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString()) != null) {
                        pointAcces.setOrientationOuvrageId(typesOrientationOuvrageFranchissement.get(row.getInt(Columns.ID_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString())).getId());
                    }

                    if (row.getInt(Columns.ID_TYPE_OUVRAGE_FRANCHISSEMENT.toString()) != null) {
                        pointAcces.setTypeOuvrageFranchissementId(typesOuvrageFranchissement.get(row.getInt(Columns.ID_TYPE_OUVRAGE_FRANCHISSEMENT.toString())).getId());
                    }
                } else if (objet instanceof OuvrageParticulier) {

                    final OuvrageParticulier ouvrage = (OuvrageParticulier) objet;

                    if (row.getInt(Columns.ID_TYPE_OUVRAGE_PARTICULIER.toString()) != null) {
                        ouvrage.setTypeOuvrageParticulierId(typesOuvrageParticulier.get(row.getInt(Columns.ID_TYPE_OUVRAGE_PARTICULIER.toString())).getId());
                    }
                } else if (objet instanceof OuvertureBatardable) {

                    final OuvertureBatardable ouverture = (OuvertureBatardable) objet;

                    if (row.getDouble(Columns.Z_SEUIL.toString()) != null) {
                        ouverture.setZSeuil(row.getDouble(Columns.Z_SEUIL.toString()).floatValue());
                    }

                    if (row.getInt(Columns.ID_TYPE_SEUIL.toString()) != null) {
                        ouverture.setTypeSeuilId(typesSeuil.get(row.getInt(Columns.ID_TYPE_SEUIL.toString())).getId());
                    }

                    if (row.getInt(Columns.ID_TYPE_GLISSIERE.toString()) != null) {
                        ouverture.setTypeGlissiereId(typesGlissiere.get(row.getInt(Columns.ID_TYPE_GLISSIERE.toString())).getId());
                    }

                    if (row.getInt(Columns.ID_TYPE_NATURE_BATARDEAUX.toString()) != null) {
                        ouverture.setNatureBatardeauxId(typesNatureBatardeaux.get(row.getInt(Columns.ID_TYPE_NATURE_BATARDEAUX.toString())).getId());
                    }

                    if (row.getInt(Columns.NOMBRE.toString()) != null) {
                        ouverture.setNombreBatardeaux(row.getInt(Columns.NOMBRE.toString()));
                    }

                    if (row.getDouble(Columns.POIDS.toString()) != null) {
                        ouverture.setPoidsUnitaireBatardeaux(row.getDouble(Columns.POIDS.toString()).floatValue());
                    }

                    if (row.getInt(Columns.ID_TYPE_MOYEN_MANIP_BATARDEAUX.toString()) != null) {
                        ouverture.setMoyenManipBatardeauxId(typesMoyenManipBatardeaux.get(row.getInt(Columns.ID_TYPE_MOYEN_MANIP_BATARDEAUX.toString())).getId());
                    }

                    if (row.getInt(Columns.ID_ORG_STOCKAGE_BATARDEAUX.toString()) != null) {
                        ouverture.setOrganismeStockantId(organismes.get(row.getInt(Columns.ID_ORG_STOCKAGE_BATARDEAUX.toString())).getId());
                    }

                    if (row.getInt(Columns.ID_ORG_MANIP_BATARDEAUX.toString()) != null) {
                        ouverture.setOrganismeManipulateurId(organismes.get(row.getInt(Columns.ID_ORG_MANIP_BATARDEAUX.toString())).getId());
                    }

                    if (row.getInt(Columns.ID_INTERV_MANIP_BATARDEAUX.toString()) != null) {
                        ouverture.setIntervenantManupulateurId(contacts.get(row.getInt(Columns.ID_INTERV_MANIP_BATARDEAUX.toString())).getId());
                    }
                }

                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    objet.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                }
                
                if(row.getInt(Columns.ID_INTERV_MANIP_BATARDEAUX.toString())!=null){
                    ouverture.setIntervenantManupulateurId(contacts.get(row.getInt(Columns.ID_INTERV_MANIP_BATARDEAUX.toString())).getId());
                }       
            }
            
            
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                objet.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            if (nouvelObjet) {
            
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                objets.put(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()), objet);

                // Set the list ByTronconId
                List<ObjetReseau> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    objetsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
        couchDbConnector.executeBulk(objets.values());
    }

    @Override
    public ObjetReseau importRow(Row row) throws IOException, AccessDbImporterException {
        final Class typeStructure = typeElementReseauImporter.getTypeReferences().get(row.getInt(Columns.ID_TYPE_ELEMENT_RESEAU.toString()));
        if (typeStructure == OuvrageHydrauliqueAssocie.class) {
            return sysEvtAutreOuvrageHydrauliqueImporter.importRow(row);
        } else if (typeStructure == VoieAcces.class) {
            return sysEvtCheminAccesImporter.importRow(row);
        } else if (typeStructure == ReseauHydrauliqueFerme.class) {
            return sysEvtConduiteFermeeImporter.importRow(row);
        } else if (typeStructure == OuvertureBatardable.class) {
            return sysEvtOuvertureBatardableImporter.importRow(row);
        } else if (typeStructure == OuvrageParticulier.class) {
            return sysEvtOuvrageParticulierImporter.importRow(row);
        } else if (typeStructure == OuvrageTelecomEnergie.class) {
            return sysEvtOuvrageTelecommunicationImporter.importRow(row);
        } else if (typeStructure == OuvrageVoirie.class) {
            return sysEvtOuvrageVoirieImporter.importRow(row);
        } else if (typeStructure == OuvrageFranchissement.class) {
            return sysEvtPointAccesImporter.importRow(row);
        } else if (typeStructure == ReseauHydrauliqueCielOuvert.class) {
            return sysEvtReseauEauImporter.importRow(row);
        } else if (typeStructure == ReseauTelecomEnergie.class) {
            return sysEvtReseauTelecommunicationImporter.importRow(row);
        } else if (typeStructure == StationPompage.class) {
            return sysEvtStationDePompageImporter.importRow(row);
        } else if (typeStructure == VoieDigue.class) {
            return sysEvtVoieSurDigueImporter.importRow(row);
        } else {
            SirsCore.LOGGER.log(Level.SEVERE, typeStructure + " : Type de réseau incohérent.");
            return null;
        }
    }
}
