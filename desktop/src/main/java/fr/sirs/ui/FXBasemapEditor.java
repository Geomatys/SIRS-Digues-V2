/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.ui;

import fr.sirs.SIRS;
import fr.sirs.util.SaveableConfiguration;
import fr.sirs.util.property.SirsPreferences;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.gui.javafx.util.FXDirectoryTextField;

/**
 *
 * @author maximegavens
 */
public class FXBasemapEditor extends BorderPane implements SaveableConfiguration {

    public final static String WMS_WMTS_CHOICE = "wms/wmts";
    public final static String OTHER_CHOICE = "autre";
    public final static String FILE_CHOICE = "ficher";
    public final static String WMS111 = "WMS - 1.1.1";
    public final static String WMS130 = "WMS - 1.3.0";
    public final static String WMTS100 = "WMTS - 1.0.0";

    @FXML private RadioButton uiRadioButtonWM;
    @FXML private RadioButton uiRadioButtonOther;
    @FXML private RadioButton uiRadioButtonLocalFile;

    @FXML private ChoiceBox uiChoiceService;

    @FXML private TextField uiBasemapUrlWM;
    @FXML private TextField uiBasemapUrlOther;
    @FXML private FXDirectoryTextField uiBasemapFile;

    public FXBasemapEditor() {
        SIRS.loadFXML(this);
        initField();
    }

    private void initField() {
        final String previousWMUrl = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_WM_URL);
        final String previousOtherUrl = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_OTHER_URL);
        final String previousLocalFile = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_LOCAL_FILE);
        final String previousChoice = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_CHOICE);
        final String previousWMType = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_WM_TYPE);

        uiBasemapUrlWM.setText(previousWMUrl);
        uiBasemapUrlOther.setText(previousOtherUrl);
        uiBasemapFile.setText(previousLocalFile);

        ToggleGroup group = new ToggleGroup();
        uiRadioButtonWM.setToggleGroup(group);
        uiRadioButtonOther.setToggleGroup(group);
        uiRadioButtonLocalFile.setToggleGroup(group);

        if (WMS_WMTS_CHOICE.equals(previousChoice)) {
            uiRadioButtonWM.selectedProperty().setValue(true);
            //uiCheckBoxOther.selectedProperty().setValue(false);
            //uiCheckBoxLocalFile.selectedProperty().setValue(false);
        } else if (OTHER_CHOICE.equals(previousChoice)) {
            //uiCheckBoxWM.selectedProperty().setValue(false);
            uiRadioButtonOther.selectedProperty().setValue(true);
            //uiCheckBoxLocalFile.selectedProperty().setValue(false);
        } else if (FILE_CHOICE.equals(previousChoice)) {
            //uiCheckBoxWM.selectedProperty().setValue(false);
            //uiCheckBoxOther.selectedProperty().setValue(false);
            uiRadioButtonLocalFile.selectedProperty().setValue(true);
        } else {
            throw new RuntimeException("Expected behaviour");
        }

        uiChoiceService.setItems(FXCollections.observableList(Arrays.asList(new String[]{WMS111, WMS130, WMTS100})));
        uiChoiceService.getSelectionModel().select(previousWMType);
    }

    @Override
    public void save() throws Exception {
        final Map<SirsPreferences.PROPERTIES, String> properties = new HashMap<>();
        properties.put(SirsPreferences.PROPERTIES.BASEMAP_WM_URL, uiBasemapUrlWM.getText());
        properties.put(SirsPreferences.PROPERTIES.BASEMAP_OTHER_URL, uiBasemapUrlOther.getText());
        properties.put(SirsPreferences.PROPERTIES.BASEMAP_LOCAL_FILE, uiRadioButtonLocalFile.getText());
        properties.put(SirsPreferences.PROPERTIES.BASEMAP_WM_TYPE, (String) uiChoiceService.getSelectionModel().getSelectedItem());
        if (uiRadioButtonWM.isSelected()) {
            properties.put(SirsPreferences.PROPERTIES.BASEMAP_CHOICE, WMS_WMTS_CHOICE);
        } else if (uiRadioButtonOther.isSelected()) {
            properties.put(SirsPreferences.PROPERTIES.BASEMAP_CHOICE, OTHER_CHOICE);
        } else if (uiRadioButtonLocalFile.isSelected()) {
            properties.put(SirsPreferences.PROPERTIES.BASEMAP_CHOICE, FILE_CHOICE);
        }
        SirsPreferences.INSTANCE.store(properties);
    }

    @Override
    public String getTitle() {
        return "Fond de carte par défaut";
    }
}
