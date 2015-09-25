package fr.sirs.importer.v2;

import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import static fr.sirs.importer.DbImporter.TableName.DESORDRE_EVENEMENT_HYDRAU;
import static fr.sirs.importer.DbImporter.TableName.DESORDRE_JOURNAL;
import static fr.sirs.importer.DbImporter.TableName.DESORDRE_PRESTATION;
import static fr.sirs.importer.DbImporter.TableName.LAISSE_CRUE_JOURNAL;
import static fr.sirs.importer.DbImporter.TableName.LIGNE_EAU_JOURNAL;
import static fr.sirs.importer.DbImporter.TableName.MARCHE_FINANCEUR;
import static fr.sirs.importer.DbImporter.TableName.MONTEE_DES_EAUX_JOURNAL;
import static fr.sirs.importer.DbImporter.TableName.PRESTATION_EVENEMENT_HYDRAU;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_AUTRE_OUVRAGE_HYDRAU;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_CONDUITE_FERMEE;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_OUVRAGE_TEL_NRJ;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_RESEAU_EAU;
import org.apache.sis.util.Static;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SimpleLinkers extends Static {

    /*
     * DESORDRES
     */
    @Component
    public static class DesordreEvenementHydroLinker extends JoinTableLinker<EvenementHydraulique, Desordre> {

        public DesordreEvenementHydroLinker() {
            super(DESORDRE_EVENEMENT_HYDRAU.name(), EvenementHydraulique.class, Desordre.class, "ID_EVENEMENT_HYDRAU", "ID_DESORDRE");
        }
    }

    @Component
    public static class DesordrePrestationLinker extends JoinTableLinker<Prestation, Desordre> {

        public DesordrePrestationLinker() {
            super(DESORDRE_PRESTATION.name(), Prestation.class, Desordre.class, "ID_PRESTATION", "ID_DESORDRE", true);
        }
    }

    @Component
    public static class DesordreJournalLinker extends JoinTableLinker<ArticleJournal, Desordre> {

        public DesordreJournalLinker() {
            super(DESORDRE_JOURNAL.name(), ArticleJournal.class, Desordre.class, "ID_ARTICLE_JOURNAL", "ID_DESORDRE");
        }
    }

    /*
     * ARTICLE DE JOURNAL
     */
    @Component
    public static class LaisseCrueJournalLinker extends JoinTableLinker<ArticleJournal, LaisseCrue> {

        public LaisseCrueJournalLinker() {
            super(LAISSE_CRUE_JOURNAL.name(), ArticleJournal.class, LaisseCrue.class, "ID_ARTICLE_JOURNAL", "ID_LAISSE_CRUE");
        }
    }

    @Component
    public static class LigneEauJournalLinker extends JoinTableLinker<ArticleJournal, LigneEau> {

        public LigneEauJournalLinker() {
            super(LIGNE_EAU_JOURNAL.name(), ArticleJournal.class, LigneEau.class, "ID_ARTICLE_JOURNAL", "ID_LIGNE_EAU");
        }
    }

    @Component
    public static class MonteeEauxJournalLinker extends JoinTableLinker<ArticleJournal, MonteeEaux> {

        public MonteeEauxJournalLinker() {
            super(MONTEE_DES_EAUX_JOURNAL.name(), ArticleJournal.class, MonteeEaux.class, "ID_ARTICLE_JOURNAL", "ID_MONTEE_DES_EAUX");
        }
    }

    /*
     * MARCHE
     */
    @Component
    public static class MarcheFinanceurLinker extends JoinTableLinker<Organisme, Marche> {

        public MarcheFinanceurLinker() {
            super(MARCHE_FINANCEUR.name(), Organisme.class, Marche.class, "ID_ORGANISME", "ID_MARCHE");
        }
    }

    /*
     * PRESTATION
     */
    @Component
    public static class PrestationEvenementHydroLinker extends JoinTableLinker<EvenementHydraulique, Prestation> {

        public PrestationEvenementHydroLinker() {
            super(PRESTATION_EVENEMENT_HYDRAU.name(), EvenementHydraulique.class, Prestation.class, "ID_EVENEMENT_HYDRAU", "ID_PRESTATION");
        }
    }

    @Component
    public static class PrestationIntervenantLinker extends JoinTableLinker<Contact, Prestation> {

        public PrestationIntervenantLinker() {
            super(PRESTATION_EVENEMENT_HYDRAU.name(), Contact.class, Prestation.class, "ID_INTERVENANT", "ID_PRESTATION");
        }
    }

    /*
     * ELEMENT RESEAU
     */
    @Component
    public static class ReseauHydroOuvrageAssocieLinker extends JoinTableLinker<ReseauHydrauliqueFerme, OuvrageHydrauliqueAssocie> {

        public ReseauHydroOuvrageAssocieLinker() {
            super(ELEMENT_RESEAU_AUTRE_OUVRAGE_HYDRAU.name(), ReseauHydrauliqueFerme.class, OuvrageHydrauliqueAssocie.class, "ID_ELEMENT_RESEAU", "ID_ELEMENT_RESEAU_AUTRE_OUVRAGE_HYDRAU", true);
        }
    }

    @Component
    public static class ReseauHydroStationPompageLinker extends JoinTableLinker<StationPompage, ReseauHydrauliqueFerme> {

        public ReseauHydroStationPompageLinker() {
            super(ELEMENT_RESEAU_CONDUITE_FERMEE.name(), StationPompage.class, ReseauHydrauliqueFerme.class, "ID_ELEMENT_RESEAU", "ID_ELEMENT_RESEAU_CONDUITE_FERMEE", true);
        }
    }

    @Component
    public static class ReseauOuvrageTelecomEnergieLinker extends JoinTableLinker<ReseauTelecomEnergie, OuvrageTelecomEnergie> {

        public ReseauOuvrageTelecomEnergieLinker() {
            super(ELEMENT_RESEAU_OUVRAGE_TEL_NRJ.name(), ReseauTelecomEnergie.class, OuvrageTelecomEnergie.class, "ID_ELEMENT_RESEAU", "ID_ELEMENT_RESEAU_OUVRAGE_TEL_NRJ", true);
        }
    }


    @Component
    public static class ReseauHydroOuvertFermeLinker extends JoinTableLinker<ReseauHydrauliqueFerme, ReseauHydrauliqueCielOuvert> {

        public ReseauHydroOuvertFermeLinker() {
            super(ELEMENT_RESEAU_RESEAU_EAU.name(), ReseauHydrauliqueFerme.class, ReseauHydrauliqueCielOuvert.class, "ID_ELEMENT_RESEAU", "ID_ELEMENT_RESEAU_RESEAU_EAU", true);
        }
    }
}
