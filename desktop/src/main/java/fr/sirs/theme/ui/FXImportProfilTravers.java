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
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.model.*;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import fr.sirs.ui.Growl;
import fr.sirs.util.property.SirsPreferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

    @FXML protected CheckBox uiCrushingCheck;

    protected FeatureStore store;

    protected final ObservableList<Feature> selectionProperty = FXCollections.observableArrayList();
    protected final PojoTable pojoTable;

    protected final Map<String, String> ehDesignationToId;

    protected final Map<String, Map<String, String>> checkMap;
    protected String designationCol;


    public FXImportProfilTravers(final PojoTable pojoTable) {
        SIRS.loadFXML(this);

        uiPaneConfig.setDisable(true);
        uiTable.setEditable(false);
        uiTable.setLoadAll(true);
        uiCrushingCheck.setSelected(true);

        this.pojoTable = pojoTable;

        List<EvenementHydraulique> eh = this.pojoTable.session.getRepositoryForClass(EvenementHydraulique.class).getAll();
        ehDesignationToId = eh.stream().collect(Collectors.toMap(EvenementHydraulique::getDesignation, EvenementHydraulique::getId));

        checkMap = new HashMap<>();
    }

    @FXML
    protected void openFileChooser(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final File prevPath = getPreviousPath();
        if (prevPath != null) {
            fileChooser.setInitialDirectory(prevPath);
        }
        final File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file!=null) {
            setPreviousPath(file.getParentFile());
            uiPath.setText(file.getAbsolutePath());
        }
    }

    private static File getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(FXImportProfilTravers.class);
        final String str = prefs.get("path", null);
        if (str != null) {
            final File file = new File(str);
            if (file.isDirectory()) {
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

        selectionProperty.removeAll(selectionProperty);

        // init control panel
        uiPaneConfig.setDisable(true);
        uiPaneConfig.getChildren().clear();
        uiPaneConfig.add(new Label("Colonnes"), 0, 0);
        uiPaneConfig.add(new Label("Types de paramètres hydrauliques"), 1, 0);
        uiPaneConfig.add(new Label("Évènements hydrauliques"), 2, 0);

        try {
            if (url.toLowerCase().endsWith(".txt") || url.toLowerCase().endsWith(".csv")) {
                final char separator = (uiSeparator.getText().isEmpty()) ? ';' : uiSeparator.getText().charAt(0);
                store = new CSVFeatureStore(file, "no namespace", separator);
                uiPaneConfig.setDisable(false);
            } else {
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
            final List<String> ehDesignation = this.ehDesignationToId.keySet().stream()
                    .sorted(Comparator.comparingInt(Integer::parseInt)).collect(toList());
            ehDesignation.add(0, "");
            final ObservableList<String> ehObs = FXCollections.observableArrayList(ehDesignation);
            final ObservableList<String> measureObs = FXCollections.observableArrayList("designation", "debit", "cote", "vitesse");

            for (int i = 0; i < properties.size(); i++) {
                Label label = new Label(properties.get(i).getName().toString());
                ComboBox<String> measure = new ComboBox<>();
                ComboBox<String> eh = new ComboBox<>();

                label.setAlignment(Pos.CENTER);

                measure.setItems(measureObs);
                eh.setItems(ehObs);

                if (!properties.isEmpty()) {
                    measure.getSelectionModel().clearAndSelect(0);
                    eh.getSelectionModel().clearAndSelect(0);
                }

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
                    if (!FeatureMapLayer.SELECTION_FILTER_PROPERTY.equals(evt.getPropertyName())) return;

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

        } catch (Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            final Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
        }
    }

    protected void saveSelectionProfilTravers(){
        final ObservableList<Feature> features = selectionProperty;
        final ProfilTraversRepository repo = (ProfilTraversRepository) this.pojoTable.session.getRepositoryForClass(ProfilTravers.class);
        final Set<ProfilTravers> toUpdate = new HashSet<>();
        final Set<String> storePtDesignation = new HashSet<>();

        // check integrity
        for (final Feature feature : features) {

            // retrieves designation
            final String toCheck = String.valueOf(feature.getPropertyValue(this.designationCol));

            // check doublon
            if (storePtDesignation.contains(toCheck)) {
                alert("La désignation " + toCheck + " a été trouvé deux fois.");
                return;
            } else {
                storePtDesignation.add(toCheck);
            }

            // check existence
            final List<ProfilTravers> ptList = repo.getByDesignation(toCheck);
            if (ptList.size() == 0) {
                error("Le profil en travers " + toCheck + " n'existe pas.");
                return;
            }

            // check only one ProfilTravers found
            if (ptList.size() >= 2) {
                error("La désignation " + toCheck + " correspond à plusieurs profils en travers stockés en base.");
                return;
            }
        }

        // Confirm save popup
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la sauvegarde ?",
                ButtonType.NO, ButtonType.YES);
        alert.setResizable(true);
        final ButtonType res = alert.showAndWait().get();
        if (ButtonType.NO == res) {
            return;
        }

        try {
            // retrieve values
            for (final Feature feature : features) {
                // designation
                final String currentDesignation = String.valueOf(feature.getPropertyValue(this.designationCol));
                final List<ProfilTravers> ptList = repo.getByDesignation(currentDesignation);
                final ProfilTravers currentPt = ptList.get(0);

                // get param values
                for (Map.Entry<String, Map<String, String>> entry: checkMap.entrySet()) {
                    for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
                        String col = entry2.getValue();
                        String val = String.valueOf(feature.getPropertyValue(col));
                        entry2.setValue(val);
                    }
                }

                // Update ProfilTravers
                completeProfilTravers(currentPt, checkMap);
                repo.update(currentPt);
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

    private void completeProfilTravers(final ProfilTravers pt, final Map<String, Map<String, String>> EHtoPH) {
        if (uiCrushingCheck.isSelected()) {
            for (Map.Entry<String, Map<String, String>> entry: EHtoPH.entrySet()) {
                boolean find = false;
                for (ParametreHydrauliqueProfilTravers ph: pt.getParametresHydrauliques()) {
                    final String ehId = ehDesignationToId.get(entry.getKey());
                    if (ehId.equals(ph.getEvenementHydrauliqueId())) {
                        setParameters(ph, entry.getValue());
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    addParametreHydraulique(pt, entry);
                }
            }
        } else {
            for (Map.Entry<String, Map<String, String>> entry: EHtoPH.entrySet()) {
                boolean find = false;
                for (ParametreHydrauliqueProfilTravers ph: pt.getParametresHydrauliques()) {
                    final String ehId = ehDesignationToId.get(entry.getKey());
                    if (ehId.equals(ph.getEvenementHydrauliqueId())) {
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    addParametreHydraulique(pt, entry);
                }
            }
        }
    }

    private void addParametreHydraulique(final ProfilTravers pt, final Map.Entry<String, Map<String, String>> newParams) {
        final fr.sirs.Session sirsSession = Injector.getSession();
        final ParametreHydrauliqueProfilTravers newPh = sirsSession.getElementCreator().createElement(ParametreHydrauliqueProfilTravers.class, true);

        newPh.setEvenementHydrauliqueId(this.ehDesignationToId.get(newParams.getKey()));
        newPh.setAuthor(sirsSession.getUtilisateur() == null? null : sirsSession.getUtilisateur().getId());
        newPh.setValid(sirsSession.createValidDocuments().get());

        setParameters(newPh, newParams.getValue());
        pt.parametresHydrauliques.add(newPh);
    }

    private void setParameters(final ParametreHydrauliqueProfilTravers ph, final Map<String, String> newParams) {
        for (Map.Entry<String, String> entry: newParams.entrySet()) {
            switch (entry.getKey()) {
                case "debit":
                    ph.setDebitPointe(Float.valueOf(entry.getValue()));
                    break;
                case "cote":
                    ph.setVitessePointe(Float.valueOf(entry.getValue()));
                    break;
                case "vitesse":
                    ph.setCoteEau(Float.valueOf(entry.getValue()));
                    break;
                default:
                    throw new RuntimeException("Unexpected behaviour: " + entry.getKey() + " unknown.");
            }
        }
    }

    private boolean checkPaneConfig() {
        ObservableList<Node> childrens = this.uiPaneConfig.getChildren();
        checkMap.clear();
        designationCol = null;

        // check auto increment preference true
        final String propertyAutoIncrementStr = SirsPreferences.INSTANCE.getProperty(SirsPreferences.PROPERTIES.DESIGNATION_AUTO_INCREMENT);
        if (Boolean.FALSE.equals(Boolean.valueOf(propertyAutoIncrementStr))) {
            alert("Vous devez renseigner une colonne pour les désignations des paramètres hydrauliques ou activer l'auto incrémentation.");
            return false;
        }

        // check configuration panel
        for (int i = 3; i < childrens.size(); i += 3) {
            final String col = ((Label) childrens.get(i)).getText();
            final String mes = ((ComboBox<String>) childrens.get(i + 1)).getValue();
            final String ehd = ((ComboBox<String>) childrens.get(i + 2)).getValue();

            if (mes.equals("designation")) {
                // Vérification de l'unicité de la colonne désignation des Profil en travers
                if (designationCol != null) {
                    alert("Une seule colonne doit référencer la désignation des profils en travers.");
                    return false;
                } else {
                    designationCol = col;
                }
            } else if (ehd.isEmpty()) {
                alert(String.format("Le paramètre hydraulique %s (colonne:%s) n'est associée à aucun évènement hydraulique.", mes, col));
                return false;
            } else {
                // Vérification de non doublon des paramètres hydrauliques pour un même évènement hydraulique
                if (!checkMap.containsKey(ehd)) checkMap.put(ehd, new HashMap<>());
                if (checkMap.get(ehd).containsKey(mes)) {
                    alert(String.format("Le paramètre hydraulique %s est associé deux fois à l'évènement hydraulique %s", mes, ehd));
                    return false;
                } else {
                    checkMap.get(ehd).put(mes, col);
                }
            }
        }

        // Check ProfilTravers designation found
        if (designationCol == null) {
            alert("Aucune colonne ne référence les désignations des profils en travers");
            return false;
        }
        return true;
    }

    private void alert(final String msg) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setResizable(true);
        alert.show();
    }

    private void error(final String msg) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setResizable(true);
        alert.show();
    }
}
