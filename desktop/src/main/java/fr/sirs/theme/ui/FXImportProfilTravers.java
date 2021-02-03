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
import fr.sirs.util.SirsStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
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

    protected final LinkedHashMap<String, String> ehMap;

    @FXML protected ComboBox<String> uiPT;
    @FXML protected ComboBox<String> uiAttCote;
    @FXML protected ComboBox<String> uiAttDebit;
    @FXML protected ComboBox<String> uiAttVitesse;
    @FXML protected ComboBox<String> uiEH;


    public FXImportProfilTravers(final PojoTable pojoTable) {
        SIRS.loadFXML(this);

        uiPaneConfig.setDisable(true);
        uiTable.setEditable(false);
        uiTable.setLoadAll(true);
        uiCrushingCheck.setSelected(false);

        this.pojoTable = pojoTable;

        SirsStringConverter ssc = new SirsStringConverter();
        List<EvenementHydraulique> eh = pojoTable.session.getRepositoryForClass(EvenementHydraulique.class).getAll();
        eh.sort(Comparator.comparingInt(e -> Integer.parseInt(e.getDesignation())));
        ehMap = eh.stream().collect(Collectors.toMap(ssc::toString, EvenementHydraulique::getId, (e1, e2) -> { throw new AssertionError("keys should be unique"); }, LinkedHashMap::new));
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

        try {
            if(url.toLowerCase().endsWith(".shp")){
                store = new ShapefileFeatureStore(file.toURI(), "no namespace");
                uiPaneConfig.setDisable(true);
            }else if (url.toLowerCase().endsWith(".txt") || url.toLowerCase().endsWith(".csv")) {
                final char separator = (uiSeparator.getText().isEmpty()) ? ';' : uiSeparator.getText().charAt(0);
                store = new CSVFeatureStore(file, "no namespace", separator);
                uiPaneConfig.setDisable(false);
            } else {
                final Alert alert = new Alert(Alert.AlertType.ERROR, "Le fichier sélectionné n'est pas un shape, csv ou txt", ButtonType.OK);
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
            final ObservableList<String> properties = FXCollections
                    .observableArrayList(getPropertiesFromFeatures(col)
                            .stream().map(p -> p.getName().tip().toString()).collect(Collectors.toList()));

            uiPT.setItems(FXCollections.observableArrayList(properties));
            uiEH.setItems(FXCollections.observableArrayList(ehMap.keySet()));

            properties.add(0, "");
            uiAttCote.setItems(properties);
            uiAttDebit.setItems(properties);
            uiAttVitesse.setItems(properties);

            if(properties.size() >= 2){
                uiPT.getSelectionModel().clearAndSelect(0);
                uiAttCote.getSelectionModel().clearAndSelect(0);
                uiAttDebit.getSelectionModel().clearAndSelect(0);
                uiAttVitesse.getSelectionModel().clearAndSelect(0);
                uiEH.getSelectionModel().clearAndSelect(0);
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

    protected void saveSelectionProfilTravers() {
        final ObservableList<Feature> features = selectionProperty;
        final fr.sirs.Session sirsSession = Injector.getSession();
        final ProfilTraversRepository ptRepo = (ProfilTraversRepository) sirsSession.getRepositoryForClass(ProfilTravers.class);
        final Set<String> storePtDesignation = new HashSet<>();

        // check integrity
        for (final Feature feature : features) {

            // retrieves designation
            final String toCheck = String.valueOf(feature.getPropertyValue(uiPT.getValue()));

            // check doublon
            if (storePtDesignation.contains(toCheck)) {
                alert("La désignation " + toCheck + " a été trouvé deux fois.");
                return;
            } else {
                storePtDesignation.add(toCheck);
            }

            // check existence
            final List<ProfilTravers> ptList = ptRepo.getByDesignation(toCheck);
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
            // element creator
            ElementCreator elementCreator = sirsSession.getElementCreator();

            // is user select crushed option rewriting existing parametres hydrauliques
            boolean crushed = uiCrushingCheck.isSelected();

            // retrieve evenement hydraulique id selected
            final String ehId = ehMap.get(uiEH.getValue());

            // start collecting
            for (final Feature feature : features) {
                // retrieve current ProfilTravers
                final String currentDesignation = String.valueOf(feature.getPropertyValue(uiPT.getValue()));
                final ProfilTravers currentPt = ptRepo.getByDesignation(currentDesignation).get(0);

                // Update ProfilTravers
                boolean find = false;
                for (ParametreHydrauliqueProfilTravers ph: currentPt.getParametresHydrauliques()) {
                    if (ehId.equals(ph.getEvenementHydrauliqueId())) {
                        // update parameter
                        if (crushed) setParameters(ph, feature);
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    // add parameter
                    final ParametreHydrauliqueProfilTravers newPh = elementCreator.createElement(ParametreHydrauliqueProfilTravers.class, true);

                    newPh.setEvenementHydrauliqueId(ehId);
                    newPh.setAuthor(sirsSession.getUtilisateur() == null? null : sirsSession.getUtilisateur().getId());
                    newPh.setValid(sirsSession.createValidDocuments().get());
                    setParameters(newPh, feature);

                    currentPt.parametresHydrauliques.add(newPh);
                }
                ptRepo.update(currentPt);
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

    private void setParameters(final ParametreHydrauliqueProfilTravers ph, Feature feature) {
        if (!uiAttVitesse.getValue().isEmpty()) {
            ph.setVitessePointe(Float.parseFloat(String.valueOf(feature.getPropertyValue(uiAttVitesse.getValue()))));
        }
        if (!uiAttCote.getValue().isEmpty()) {
            ph.setCoteEau(Float.parseFloat(String.valueOf(feature.getPropertyValue(uiAttCote.getValue()))));
        }
        if (!uiAttDebit.getValue().isEmpty()) {
            ph.setDebitPointe(Float.parseFloat(String.valueOf(feature.getPropertyValue(uiAttDebit.getValue()))));
        }
    }

    private boolean checkPaneConfig() {
        // check ProfilTravers col
        if (uiPT.getValue().isEmpty()) {
            alert("Vous devez attribuer une colonne au désignation des profils en travers.");
            return false;
        }
        // check at least one measure selected
        if (uiAttCote.getValue().isEmpty() && uiAttDebit.getValue().isEmpty() && uiAttVitesse.getValue().isEmpty()) {
            alert("Vous devez renseigner au moins une colonnes pour les valeurs de debit, cote ou vitesse.");
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
