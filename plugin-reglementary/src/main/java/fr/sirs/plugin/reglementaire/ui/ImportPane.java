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
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.util.DatePickerConverter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;

/**
 *
 * @author guilhem
 */
public class ImportPane extends GridPane {

    @FXML
    private Button chooseFileButton;

    @FXML
    protected TextField fileField;

    @FXML
    protected CheckBox isSyntheseTable;

    @FXML
    protected DatePicker horodatageDate;


    public ImportPane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);

        DatePickerConverter.register(horodatageDate);
    }

    @FXML
    public void chooseFileButton(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            fileField.setText(file.getPath());
        }
    }
}
