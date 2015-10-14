package fr.sirs;

import static fr.sirs.SIRS.AUTHOR_FIELD;
import static fr.sirs.SIRS.BORNE_IDS_REFERENCE;
import static fr.sirs.SIRS.COUCH_DB_DOCUMENT_FIELD;
import static fr.sirs.SIRS.DATE_MAJ_FIELD;
import static fr.sirs.SIRS.DOCUMENT_ID_FIELD;
import static fr.sirs.SIRS.DesordreFields.ECHELLE_LIMINIMETRIQUE_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.OBSERVATIONS_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.OUVRAGE_HYDRAULIQUE_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.OUVRAGE_PARTICULIER_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.OUVRAGE_TELECOM_ENERGIE_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.OUVRAGE_VOIRIE_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.PRESTATION_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.RESEAU_HYDRAULIQUE_CIEL_OUVERT_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.RESEAU_HYDRAULIQUE_FERME_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.RESEAU_TELECOM_ENERGIE_REFERENCE;
import static fr.sirs.SIRS.DesordreFields.VOIE_DIGUE_REFERENCE;
import static fr.sirs.SIRS.FOREIGN_PARENT_ID_FIELD;
import static fr.sirs.SIRS.GEOMETRY_FIELD;
import static fr.sirs.SIRS.GEOMETRY_MODE;
import static fr.sirs.SIRS.ID_FIELD;
import static fr.sirs.SIRS.LATITUDE_MAX_FIELD;
import static fr.sirs.SIRS.LATITUDE_MIN_FIELD;
import static fr.sirs.SIRS.LONGITUDE_MAX_FIELD;
import static fr.sirs.SIRS.LONGITUDE_MIN_FIELD;
import static fr.sirs.SIRS.PARENT_FIELD;
import static fr.sirs.SIRS.POSITION_DEBUT_FIELD;
import static fr.sirs.SIRS.POSITION_FIN_FIELD;
import static fr.sirs.SIRS.REVISION_FIELD;
import static fr.sirs.SIRS.VALID_FIELD;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.util.SirsStringConverter;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.feature.Feature;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PrintManager {
    
    private List<Element> elementsToPrint = null;
    private FeatureCollection featuresToPrint = null;
    
    public List<? extends Element> getElementsToPrint(){return elementsToPrint;}
    public FeatureCollection getFeaturesToPrint(){return featuresToPrint;}

    public void prepareToPrint(final Element object){
        featuresToPrint = null;
        elementsToPrint = new ArrayList<>();
        elementsToPrint.add(object);
    }

    public void prepareToPrint(final List<Element> objects){
        featuresToPrint = null;
        elementsToPrint = objects;
    }
    
    public void prepareToPrint(final Feature feature){
        elementsToPrint = null;
        featuresToPrint = FeatureStoreUtilities.collection(feature);
    }
    
    public void prepareToPrint(final FeatureCollection featureCollection){
        elementsToPrint = null;
        featuresToPrint = featureCollection;
    }
    
    public final void printFeatures(){
        try {
            final File fileToPrint = PrinterUtilities.print(null, featuresToPrint);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(fileToPrint);
        } catch (Exception ex) {
           SIRS.LOGGER.log(Level.WARNING, null, ex);
        }
    }
    
    public final void printElements(){
        final List avoidFields = new ArrayList<>();
        avoidFields.add(GEOMETRY_FIELD);
        avoidFields.add(DOCUMENT_ID_FIELD);
        avoidFields.add(ID_FIELD);
        avoidFields.add(LONGITUDE_MIN_FIELD);
        avoidFields.add(LONGITUDE_MAX_FIELD);
        avoidFields.add(LATITUDE_MIN_FIELD);
        avoidFields.add(LATITUDE_MAX_FIELD);
        avoidFields.add(FOREIGN_PARENT_ID_FIELD);
        avoidFields.add(REVISION_FIELD);
        avoidFields.add(POSITION_DEBUT_FIELD);
        avoidFields.add(POSITION_FIN_FIELD);
        avoidFields.add(PARENT_FIELD);
        avoidFields.add(COUCH_DB_DOCUMENT_FIELD);
        avoidFields.add(GEOMETRY_MODE);
        
        for(final Element element : elementsToPrint){
            if(element instanceof TronconDigue){
                if(!avoidFields.contains(BORNE_IDS_REFERENCE)) avoidFields.add(BORNE_IDS_REFERENCE);
            }
        }

        try {
            final File fileToPrint = PrinterUtilities.print(avoidFields, Injector.getSession().getPreviews(), new SirsStringConverter(), elementsToPrint);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(fileToPrint);
        } catch (Exception e) {
            SIRS.LOGGER.log(Level.WARNING, null, e);
        }
    }

    /**
     * Génère un rapport PDF des désordres requis.
     *
     * @param desordres
     * @param printPhoto
     * @param printReseauOuvrage
     * @param printVoirie
     */
    public final void printDesordres(final List<Desordre> desordres, final boolean printPhoto, final boolean printReseauOuvrage, final boolean printVoirie) {
        
            final List avoidDesordreFields = new ArrayList<>();
            avoidDesordreFields.add(GEOMETRY_FIELD);
            avoidDesordreFields.add(DOCUMENT_ID_FIELD);
            avoidDesordreFields.add(ID_FIELD);
            avoidDesordreFields.add(LONGITUDE_MIN_FIELD);
            avoidDesordreFields.add(LONGITUDE_MAX_FIELD);
            avoidDesordreFields.add(LATITUDE_MIN_FIELD);
            avoidDesordreFields.add(LATITUDE_MAX_FIELD);
            avoidDesordreFields.add(FOREIGN_PARENT_ID_FIELD);
            avoidDesordreFields.add(REVISION_FIELD);
            avoidDesordreFields.add(PARENT_FIELD);
            avoidDesordreFields.add(COUCH_DB_DOCUMENT_FIELD);
            avoidDesordreFields.add(OBSERVATIONS_REFERENCE);
            
            avoidDesordreFields.add(ECHELLE_LIMINIMETRIQUE_REFERENCE);
            avoidDesordreFields.add(OUVRAGE_PARTICULIER_REFERENCE);
            avoidDesordreFields.add(RESEAU_TELECOM_ENERGIE_REFERENCE);
            avoidDesordreFields.add(OUVRAGE_TELECOM_ENERGIE_REFERENCE);
            avoidDesordreFields.add(OUVRAGE_HYDRAULIQUE_REFERENCE);
            avoidDesordreFields.add(RESEAU_HYDRAULIQUE_FERME_REFERENCE);
            avoidDesordreFields.add(RESEAU_HYDRAULIQUE_CIEL_OUVERT_REFERENCE);
            avoidDesordreFields.add(OUVRAGE_VOIRIE_REFERENCE);
            avoidDesordreFields.add(VOIE_DIGUE_REFERENCE);
            avoidDesordreFields.add(PRESTATION_REFERENCE);
            
            avoidDesordreFields.add(VALID_FIELD);
            avoidDesordreFields.add(AUTHOR_FIELD);
            avoidDesordreFields.add(DATE_MAJ_FIELD);
            
            final List<String> observationFields = new ArrayList<>();
            observationFields.add("designation");
            observationFields.add("urgenceId");
            observationFields.add("observateurId");
            observationFields.add("nombreDesordres");
            observationFields.add("date");
            observationFields.add("evolution");
            observationFields.add("suite");
            
            final List<String> prestationFields = new ArrayList<>();
            prestationFields.add("designation");
            prestationFields.add("libelle");
            prestationFields.add("typePrestationId");
            prestationFields.add("coutMetre");
            prestationFields.add("marcheId");
            prestationFields.add("realisationInterne");
            prestationFields.add("date_debut");
            prestationFields.add("date_fin");
            prestationFields.add("commentaire");
            
            final List<String> reseauFields = new ArrayList<>();
            reseauFields.add("designation");
            reseauFields.add("libelle");
            reseauFields.add("date_debut");
            reseauFields.add("date_fin");
            reseauFields.add("commentaire");
            
        try{  
            final File fileToPrint = PrinterUtilities.printDisorders(
                    avoidDesordreFields, 
                    observationFields, 
                    prestationFields, 
                    reseauFields,
                    Injector.getSession().getPreviews(), 
                    new SirsStringConverter(), 
                    desordres, printPhoto, printReseauOuvrage, printVoirie);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(fileToPrint);
        } catch (Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     * Génère un rapport PDF des désordres requis.
     *
     * @param reseauxFermes
     * @param printPhoto
     * @param printReseauOuvrage
     */
    public final void printReseaux(final List<ReseauHydrauliqueFerme> reseauxFermes, final boolean printPhoto, final boolean printReseauOuvrage) {

            final List avoidDesordreFields = new ArrayList<>();
            avoidDesordreFields.add(GEOMETRY_FIELD);
            avoidDesordreFields.add(DOCUMENT_ID_FIELD);
            avoidDesordreFields.add(ID_FIELD);
            avoidDesordreFields.add(LONGITUDE_MIN_FIELD);
            avoidDesordreFields.add(LONGITUDE_MAX_FIELD);
            avoidDesordreFields.add(LATITUDE_MIN_FIELD);
            avoidDesordreFields.add(LATITUDE_MAX_FIELD);
            avoidDesordreFields.add(FOREIGN_PARENT_ID_FIELD);
            avoidDesordreFields.add(REVISION_FIELD);
            avoidDesordreFields.add(PARENT_FIELD);
            avoidDesordreFields.add(COUCH_DB_DOCUMENT_FIELD);
            avoidDesordreFields.add(OBSERVATIONS_REFERENCE);

            avoidDesordreFields.add(ECHELLE_LIMINIMETRIQUE_REFERENCE);
            avoidDesordreFields.add(OUVRAGE_PARTICULIER_REFERENCE);
            avoidDesordreFields.add(RESEAU_TELECOM_ENERGIE_REFERENCE);
            avoidDesordreFields.add(OUVRAGE_TELECOM_ENERGIE_REFERENCE);
            avoidDesordreFields.add(OUVRAGE_HYDRAULIQUE_REFERENCE);
            avoidDesordreFields.add(RESEAU_HYDRAULIQUE_FERME_REFERENCE);
            avoidDesordreFields.add(RESEAU_HYDRAULIQUE_CIEL_OUVERT_REFERENCE);
            avoidDesordreFields.add(OUVRAGE_VOIRIE_REFERENCE);
            avoidDesordreFields.add(VOIE_DIGUE_REFERENCE);
            avoidDesordreFields.add(PRESTATION_REFERENCE);

            avoidDesordreFields.add(VALID_FIELD);
            avoidDesordreFields.add(AUTHOR_FIELD);
            avoidDesordreFields.add(DATE_MAJ_FIELD);

            final List<String> observationFields = new ArrayList<>();
            observationFields.add("designation");
            observationFields.add("urgenceId");
            observationFields.add("observateurId");
            observationFields.add("nombreDesordres");
            observationFields.add("date");
            observationFields.add("evolution");
            observationFields.add("suite");

            final List<String> reseauFields = new ArrayList<>();
            reseauFields.add("designation");
            reseauFields.add("libelle");
            reseauFields.add("date_debut");
            reseauFields.add("date_fin");
            reseauFields.add("commentaire");

        try{
            final File fileToPrint = PrinterUtilities.printReseauFerme(
                    avoidDesordreFields,
                    observationFields,
                    reseauFields,
                    Injector.getSession().getPreviews(),
                    new SirsStringConverter(),
                    reseauxFermes, printPhoto, printReseauOuvrage);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(fileToPrint);
        } catch (Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, null, ex);
        }
    }
    
}
