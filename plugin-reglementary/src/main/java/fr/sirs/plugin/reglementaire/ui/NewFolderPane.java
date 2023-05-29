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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 *
 * @author guilhem
 */
public class NewFolderPane extends GridPane {

    public static final String IN_CURRENT_FOLDER = " Dans le dossier sélectionné";
//    public static final String IN_ALL_FOLDER     = " Dans tous les dossiers de SE, digues et tronçons";
    public static final String IN_SE_FOLDER      = " Uniquement dans les systèmes d'endiguements";
//    public static final String IN_DG_FOLDER      = " Uniquement dans les digues";
//    public static final String IN_TR_FOLDER      = " Uniquement dans les tronçons";

    @FXML
    public TextField folderNameField;

    @FXML
    public ComboBox<String> locCombo;

    public NewFolderPane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);

        final ObservableList prop = FXCollections.observableArrayList();
        prop.add(IN_CURRENT_FOLDER);
//        prop.add(IN_ALL_FOLDER);
        prop.add(IN_SE_FOLDER);
//        prop.add(IN_DG_FOLDER);
//        prop.add(IN_TR_FOLDER);
        locCombo.setItems(prop);
        locCombo.getSelectionModel().selectFirst();
    }
}
