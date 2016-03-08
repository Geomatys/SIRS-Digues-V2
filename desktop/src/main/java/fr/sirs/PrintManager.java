package fr.sirs;

import com.sun.javafx.stage.StageHelper;
import static fr.sirs.core.SirsCore.*;
import static fr.sirs.core.SirsCore.DesordreFields.*;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.util.SirsStringConverter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.sf.jasperreports.engine.JRException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.feature.Feature;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PrintManager {

    static Node printButton;

    /**
     * Find first encountered printable element while browsing recursively given
     * nodes. Browsing is done "depth last".
     * @param nodes Nodes to find a printable object into.
     * @return First encountered printable, or null if we cannot find any.
     */
    private static Printable findPrintableChild(final Collection<Node> nodes) {
        if (nodes == null || nodes.isEmpty())
            return null;

        final ArrayList<Node> children = new ArrayList<>();
        for (final Node n : nodes) {
            if (n.isDisabled() || n.isMouseTransparent() || !n.isVisible()) {
                continue;
            }

            if (n instanceof Printable) {
                return (Printable) n;

            } else if (n instanceof Parent) {
                children.addAll(((Parent)n).getChildrenUnmodifiable());
            }
        }

        return findPrintableChild(children);
    }

    private static final ChangeListener<Scene> sceneListener = new ChangeListener<Scene>() {
        @Override
        public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
            if(oldValue!=null){
                oldValue.focusOwnerProperty().removeListener(focusOwnerListener);
            }
            if(newValue!=null){
                newValue.focusOwnerProperty().addListener(focusOwnerListener);
            }
        }
    };

    private static final ChangeListener<Node> focusOwnerListener = new ChangeListener<Node>() {
        @Override
        public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
            if (newValue == null) {
                printable.set(null);
                return;
            }

            // Do not update if user focused print button.
            if (printButton != null && newValue == printButton) {
                return;
            }

            // On regarde si un des enfants est imprimable.
            Printable p = null;
            if (newValue instanceof Printable) {
                p = (Printable) newValue;
            } else if (newValue instanceof Parent) {
                p = findPrintableChild(((Parent) newValue).getChildrenUnmodifiable());
            }

            // Si aucun enfant ne l'est on s'interesse aux parents du noeud selectionné
            if (p == null) {
                Node previous = newValue;
                Parent papa = newValue.getParent();
                while (p ==null && papa != null) {
                    if (papa instanceof Printable) {
                        p = (Printable) papa;
                    } else {
                        // Check other children of current parent.
                        final ObservableList<Node> otherChildren = FXCollections.observableArrayList(papa.getChildrenUnmodifiable());
                        otherChildren.remove(previous);
                        p = findPrintableChild(otherChildren);
                        previous = papa;
                        papa = papa.getParent();
                    }
                }
            }

            printable.set(p);
        }
    };

    private static Stage focusedStage = null;
    private static final ObjectProperty<Printable> printable = new SimpleObjectProperty<>();

    static {
        //on ecoute quel element a le focus pour savoir qui est imprimable
        StageHelper.getStages().addListener(new ListChangeListener<Stage>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Stage> c) {
                while(c.next()){
                    for(Stage s : c.getAddedSubList()){
                        s.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                            if(Boolean.TRUE.equals(newValue)) setFocusedStage(s);
                        });
                    }
                }
            }
        });
        for(Stage s : StageHelper.getStages()){
            if(s.isFocused()){
                setFocusedStage(s);
            }
        }
    }

    private static synchronized void setFocusedStage(Stage stage){
        if(focusedStage!=null){
            focusedStage.sceneProperty().removeListener(sceneListener);
            focusedStage.getScene().focusOwnerProperty().removeListener(focusOwnerListener);
        }
        focusedStage = stage;
        if(focusedStage!=null){
            focusedStage.sceneProperty().addListener(sceneListener);
            focusedStage.getScene().focusOwnerProperty().addListener(focusOwnerListener);
            focusOwnerListener.changed(null, null, focusedStage.getScene().getFocusOwner());
        }
    }

    /**
     * Récuperer l'element imprimable actif.
     *
     * @return Printable
     */
    public static ReadOnlyObjectProperty<Printable> printableProperty() {
        return printable;
    }

    public void printFocusedPrintable() {
        final Printable tmpPrintable = PrintManager.printable.get();
        if(!tmpPrintable.print()){
            final Object candidate = tmpPrintable.getPrintableElements().get();
            print(candidate);
        }
    }

    public final void print(Object candidate){
        List<Element> elementsToPrint = null;
        FeatureCollection featuresToPrint = null;

        if(candidate instanceof Feature){
            featuresToPrint = FeatureStoreUtilities.collection((Feature)candidate);
        }else if(candidate instanceof Element){
            elementsToPrint = new ArrayList<>();
            elementsToPrint.add((Element)candidate);
        }else if(candidate instanceof FeatureCollection){
            featuresToPrint = (FeatureCollection) candidate;
        }else if(candidate instanceof List){
            elementsToPrint = (List)candidate;
        }

        if(elementsToPrint!=null){
            printElements(elementsToPrint);
        } else if(featuresToPrint!=null){
            printFeatures(featuresToPrint);
        }
    }

    private void printFeatures(FeatureCollection featuresToPrint){
        try {
            final List<String> avoidFields = new ArrayList<>();
            avoidFields.add(GEOMETRY_MODE_FIELD);
            final File fileToPrint = PrinterUtilities.print(avoidFields, featuresToPrint);
            SIRS.openFile(fileToPrint);
        } catch (IOException | ParserConfigurationException | SAXException | JRException | TransformerException ex) {
           SIRS.LOGGER.log(Level.WARNING, null, ex);
        }
    }

    private void printElements(List<Element> elementsToPrint){
        final List<String> avoidFields = new ArrayList<>();
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
        avoidFields.add(GEOMETRY_MODE_FIELD);

        for(final Element element : elementsToPrint){
            if(element instanceof TronconDigue){
                if(!avoidFields.contains(BORNE_IDS_REFERENCE)) avoidFields.add(BORNE_IDS_REFERENCE);
            }
        }

        try {
            final File fileToPrint = PrinterUtilities.print(avoidFields, Injector.getSession().getPreviews(), new SirsStringConverter(), elementsToPrint);
            SIRS.openFile(fileToPrint);
        } catch (IOException | ParserConfigurationException | SAXException | JRException | TransformerException e) {
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
            SIRS.openFile(fileToPrint);
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
            SIRS.openFile(fileToPrint);
        } catch (Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, null, ex);
        }
    }
}
