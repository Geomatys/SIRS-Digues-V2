/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.*;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import fr.sirs.ui.Growl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.csv.CSVFeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.gui.javafx.layer.FXFeatureTable;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.LayerListener;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.util.collection.CollectionChangeEvent;
import org.opengis.feature.PropertyType;
import org.opengis.filter.Id;
import org.opengis.util.GenericName;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class FXImportProfilTravers extends BorderPane {

    @FXML protected TextField uiPath;
    @FXML protected TextField uiSeparator;

    @FXML protected FXFeatureTable uiTable;

    @FXML protected GridPane uiPaneConfig;

    protected FeatureStore store;

    protected final ObservableList<Feature> selectionProperty = FXCollections.observableArrayList();
    protected final PojoTable pojoTable;

    protected final List<ProfilTravers> ptList;
    protected final Map<String, String> ehDesignationAndId;


    public FXImportProfilTravers(final PojoTable pojoTable) {
        SIRS.loadFXML(this);

        uiPaneConfig.setDisable(true);
        uiTable.setEditable(false);
        uiTable.setLoadAll(true);

        this.pojoTable = pojoTable;

        ptList = this.pojoTable.repo.getAll();
        List<EvenementHydraulique> eh = this.pojoTable.session.getRepositoryForClass(EvenementHydraulique.class).getAll();
        ehDesignationAndId = eh.stream().collect(Collectors.toMap(EvenementHydraulique::getDesignation, EvenementHydraulique::getId));
    }

    @FXML
    protected void openFileChooser(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final File prevPath = getPreviousPath();
        if (prevPath != null) {
            fileChooser.setInitialDirectory(prevPath);
        }
        final File file = fileChooser.showOpenDialog(getScene().getWindow());
        if(file!=null){
            setPreviousPath(file.getParentFile());
            uiPath.setText(file.getAbsolutePath());
        }
    }

    private static File getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(FXImportProfilTravers.class);
        final String str = prefs.get("path", null);
        if(str!=null){
            final File file = new File(str);
            if(file.isDirectory()){
                return file;
            }
        }
        return null;
    }

    private static void setPreviousPath(final File path) {
        final Preferences prefs = Preferences.userNodeForPackage(FXImportProfilTravers.class);
        prefs.put("path", path.getAbsolutePath());
    }

    protected ObservableList<PropertyType> getPropertiesFromFeatures(final FeatureCollection col) {
        return FXCollections
                .observableArrayList((Collection<PropertyType>) col.getFeatureType().getProperties(true))
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()));
    }

    @FXML
    void importSelection(ActionEvent event) {
        if (!checkPaneConfig()) return;
        saveSelectionProfilTravers();
    }

    @FXML
    void openFeatureStore(ActionEvent event) {
        final String url = uiPath.getText();
        final File file = new File(uiPath.getText());

        uiPaneConfig.setDisable(true);

        selectionProperty.removeAll(selectionProperty);

        try{
            if(url.toLowerCase().endsWith(".txt") || url.toLowerCase().endsWith(".csv")){
                final char separator = (uiSeparator.getText().isEmpty()) ? ';' : uiSeparator.getText().charAt(0);
                store = new CSVFeatureStore(file, "no namespace", separator);
                uiPaneConfig.setDisable(false);
            }else{
                final Alert alert = new Alert(Alert.AlertType.ERROR, "Le fichier sélectionné n'est pas un csv ou txt", ButtonType.OK);
                alert.setResizable(true);
                alert.showAndWait();
                return;
            }

            final Session session = store.createSession(true);
            final GenericName typeName = store.getNames().iterator().next();
            final FeatureCollection col = session.getFeatureCollection(QueryBuilder.all(typeName));
            final FeatureMapLayer layer = MapBuilder.createFeatureLayer(col, RandomStyleBuilder.createDefaultVectorStyle(col.getFeatureType()));
            uiTable.init(layer);

            // liste des propriétés
            final ObservableList<PropertyType> properties = getPropertiesFromFeatures(col);
            final List<String> ehDesignation = this.ehDesignationAndId.keySet().stream()
                    .sorted(Comparator.comparingInt(Integer::parseInt)).collect(toList());
            ehDesignation.add(0, "");
            final ObservableList<String> ehObs = FXCollections.observableArrayList(ehDesignation);
            final ObservableList<String> measureObs = FXCollections.observableArrayList("designation", "debit", "vitesse", "cote");

            List<ComboBox<String>> measureTypes = new ArrayList<>();
            List<ComboBox<String>> evenementHydrauliques = new ArrayList<>();

            for (int i = 0; i < properties.size(); i++) {
                Label label = new Label(properties.get(i).getName().toString());
                ComboBox<String> measure = new ComboBox<>();
                ComboBox<String> eh = new ComboBox<>();

                measure.setItems(measureObs);
                eh.setItems(ehObs);

                if (!properties.isEmpty()) {
                    measure.getSelectionModel().clearAndSelect(0);
                    eh.getSelectionModel().clearAndSelect(0);
                }

                measureTypes.add(measure);
                evenementHydrauliques.add(eh);

                uiPaneConfig.add(label, 0, i+1);
                uiPaneConfig.add(measure, 1, i+1);
                uiPaneConfig.add(eh, 2, i+1);
            }

            // on ecoute la selection
            layer.addLayerListener(new LayerListener() {
                @Override
                public void styleChange(MapLayer source, EventObject event) {}
                @Override
                public void itemChange(CollectionChangeEvent<MapItem> event) {}
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if(!FeatureMapLayer.SELECTION_FILTER_PROPERTY.equals(evt.getPropertyName())) return;

                    selectionProperty.removeAll(selectionProperty);
                    final Id filter = layer.getSelectionFilter();
                    try {
                        final FeatureCollection selection = layer.getCollection().subCollection(QueryBuilder.filtered(typeName, filter));
                        final FeatureIterator iterator = selection.iterator();
                        while(iterator.hasNext()){
                            selectionProperty.add(iterator.next());
                        }
                        iterator.close();
                    } catch (DataStoreException ex) {
                        SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                    }
                }
            });

        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            final Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

    }

    protected void saveSelectionProfilTravers(){
        final ObservableList<Feature> features = selectionProperty;
        final fr.sirs.Session sirsSession = Injector.getSession();
        final AbstractSIRSRepository<ProfilTravers> repo = this.pojoTable.repo;
        final Set<ProfilTravers> toUpdate = new HashSet<>();
        final Set<String> storePtDesignation = new HashSet<>();

        for(final Feature feature : features){
            ProfilTravers currentPt = null;
            Map<String, ParametreHydrauliqueProfilTravers> EHtoPH = new HashMap<>();
            ObservableList<Node> childrens = this.uiPaneConfig.getChildren();

            for (int i = 3; i < childrens.size(); i += 3) {
                final String col = ((Label) childrens.get(i)).getText();
                final String mes = ((ComboBox<String>) childrens.get(i+1)).getValue();
                final String ehd = ((ComboBox<String>) childrens.get(i+2)).getValue();

                if (mes.equals("designation")) {
                    final String currentDesignation = String.valueOf(feature.getPropertyValue(col));
                    ProfilTravers ptFound = getProfilTraversByDesignation(currentDesignation);
                    if (ptFound == null) {
                        alert("La désignation " + currentDesignation + " n'a pas été trouvée.");
                        return;
                    }
                    // check doublon
                    if (storePtDesignation.contains(currentDesignation)) {
                        alert("La désignation " + currentDesignation + " a été trouvée deux fois.");
                        return;
                    } else {
                        storePtDesignation.add(currentDesignation);
                    }
                    currentPt = ptFound;
                } else {
                    if (!EHtoPH.containsKey(ehd)) {
                        ParametreHydrauliqueProfilTravers tmp = sirsSession.getElementCreator().createElement(ParametreHydrauliqueProfilTravers.class, true);
                        tmp.setEvenementHydrauliqueId(this.ehDesignationAndId.get(ehd));
                        EHtoPH.put(ehd, tmp);
                    }
                    switch (mes) {
                        case "debit":
                            EHtoPH.get(ehd).setDebitPointe(Float.valueOf(String.valueOf(feature.getPropertyValue(col))));
                            break;
                        case "vitesse":
                            EHtoPH.get(ehd).setVitessePointe(Float.valueOf(String.valueOf(feature.getPropertyValue(col))));
                            break;
                        case "cote":
                            EHtoPH.get(ehd).setCoteEau(Float.valueOf(String.valueOf(feature.getPropertyValue(col))));
                            break;
                        default:
                            throw new RuntimeException("Unexpected behaviour: the measurement values should be designation, debit, vitesse or cote");
                    }
                }
            }
            
            // Complete ProfilTravers found with Author attribute and Valid attribute
            for (ParametreHydrauliqueProfilTravers val: EHtoPH.values()) {
                val.setAuthor(sirsSession.getUtilisateur() == null? null : sirsSession.getUtilisateur().getId());
                val.setValid(sirsSession.createValidDocuments().get());
            } 

            // Ensure current ProfilTravers is non null
            if (currentPt == null) throw new RuntimeException("Unexpected behaviour");

            // Update ProfilTravers
            currentPt.parametresHydrauliques.addAll(EHtoPH.values());
            toUpdate.add(currentPt);
        }

        // Confirm save popup
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la sauvegarde ?",
                ButtonType.NO, ButtonType.YES);
        alert.setResizable(true);
        final ButtonType res = alert.showAndWait().get();
        if (ButtonType.YES == res) {
            try {
                for (ProfilTravers toUp: toUpdate) {
                    repo.update(toUp);
                }

                final Growl growlInfo = new Growl(Growl.Type.INFO, "Enregistrement effectué.");
                growlInfo.showAndFade();
            } catch (Exception e) {
                final Growl growlError = new Growl(Growl.Type.ERROR, "Erreur survenue pendant l'enregistrement.");
                growlError.showAndFade();
                GeotkFX.newExceptionDialog("L'élément ne peut être sauvegardé.", e).show();
                SIRS.LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private boolean checkPaneConfig() {
        ObservableList<Node> childrens = this.uiPaneConfig.getChildren();
        Map<String, List<String>> checkMap = new HashMap<>();
        boolean designationFound = false;

        for (int i = 3; i < childrens.size(); i += 3) {
            final String col = ((Label) childrens.get(i)).getText();
            final String mes = ((ComboBox<String>) childrens.get(i + 1)).getValue();
            final String ehd = ((ComboBox<String>) childrens.get(i + 2)).getValue();

            if (mes.equals("designation")) {
                // Vérification de l'unicité de la colonne désignation
                if (designationFound) {
                    alert("Une seule colonne doit référencer la désignation des profils en travers.");
                    return false;
                } else {
                    designationFound = true;
                }
            } else if (ehd.isEmpty()) {
                alert(String.format("Le paramètre hydraulique %s (colonne:%s) n'est associée à aucun évènement hydraulique.", mes, col));
                return false;
            } else {
                // Vérification de non doublon des paramètres hydrauliques pour un même évènement hydraulique
                if (!checkMap.containsKey(ehd)) checkMap.put(ehd, new ArrayList<>());
                if (checkMap.get(ehd).contains(mes)) {
                    alert(String.format("Le paramètre hydraulique %s est associé deux fois à l'évènement hydraulique %s", mes, ehd));
                    return false;
                } else {
                    checkMap.get(ehd).add(mes);
                }
            }
        }
        return true;
    }

    private ProfilTravers getProfilTraversByDesignation(String designation) {
        return this.ptList.stream().filter(pt -> designation.equals(pt.getDesignation())).findAny().orElse(null);
    }

    private void alert(final String msg) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setResizable(true);
        alert.show();
    }
}
