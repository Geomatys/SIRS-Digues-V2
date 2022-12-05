/**
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2016, FRANCE-DIGUES,
 * <p>
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.plugin.vegetation.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefTypePeuplementVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import fr.sirs.theme.ui.FXPositionableExplicitMode;
import fr.sirs.util.ResourceInternationalString;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;

import static fr.sirs.plugin.vegetation.PluginVegetation.DEFAULT_PEUPLEMENT_VEGETATION_TYPE;
import static fr.sirs.plugin.vegetation.map.EditVegetationUtils.generateHeaderLabel;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CreatePeuplementTool extends CreateVegetationPolygonTool<PeuplementVegetation> {

    public static final Spi SPI = new Spi();

    //Add editable fields ticket redmine 7741
    private final ComboBox<Preview> comboTypeVegetation = new ComboBox<>();

    private final Spinner<Double> densiteSpinner = new Spinner<>();
    private final Spinner<Double> hauteurSpinner = new Spinner<>();
    private final Spinner<Double> diametreSpinner = new Spinner<>();

    public static final class Spi extends AbstractEditionToolSpi {

        public Spi() {
            super("CreatePeuplement",
                    new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                            "fr.sirs.plugin.vegetation.map.CreatePeuplementTool.title", CreatePeuplementTool.class.getClassLoader()),
                    new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                            "fr.sirs.plugin.vegetation.map.CreatePeuplementTool.abstract", CreatePeuplementTool.class.getClassLoader()),
                    new Image("fr/sirs/plugin/vegetation/peuplement.png"));
        }

        @Override
        public boolean canHandle(Object candidate) {
            return true;
        }

        @Override
        public EditionTool create(FXMap map, Object layer) {
            return new CreatePeuplementTool(map);
        }
    }

    public CreatePeuplementTool(FXMap map) {
        super(map, SPI, PeuplementVegetation.class);

        final Previews previewRepository = Injector.getSession().getPreviews();
        SIRS.initCombo(comboTypeVegetation, FXCollections.observableList(
                        previewRepository.getByClass(RefTypePeuplementVegetation.class)),
                previewRepository.get(DEFAULT_PEUPLEMENT_VEGETATION_TYPE));


        densiteSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0));
        hauteurSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0));
        diametreSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0));

        final GridPane gridPane = new GridPane();
        gridPane.setHgap(2);
        gridPane.setVgap(4);


        gridPane.add(generateHeaderLabel("Type de végétation :"),0,0);
        gridPane.add(comboTypeVegetation,1,0);
        gridPane.add(generateHeaderLabel("Densité : "),0,1);
        gridPane.add(densiteSpinner,1,1);
        gridPane.add(generateHeaderLabel("Hauteur : "),0,2);
        gridPane.add(hauteurSpinner,1,2);
        gridPane.add(generateHeaderLabel("Diamètre : "),0,3);
        gridPane.add(diametreSpinner,1,3);

        final VBox center = (VBox) wizard.getCenter();
        center.getChildren().add(4,gridPane);

        // Override save action to include vegetation type
        end.setOnAction(event -> {
            //on sauvegarde
            vegetation.setGeometryMode(FXPositionableExplicitMode.MODE);
            vegetation.setValid(true);
            vegetation.setForeignParentId(parcelle.getDocumentId());
            vegetation.setTypeVegetationId(comboTypeVegetation.getSelectionModel().getSelectedItem().getElementId());
            final Double densite = densiteSpinner.getValue();
            final Double hauteur = hauteurSpinner.getValue();
            final Double diametre = diametreSpinner.getValue();
            if (densite != null) vegetation.setDensite(densite);
            if (hauteur != null) vegetation.setHauteur(hauteur);
            if (diametre != null) vegetation.setDiametre(diametre);
            final AbstractSIRSRepository vegetationRepo = Injector.getSession().getRepositoryForClass(vegetationClass);
            vegetationRepo.add(vegetation);
            startGeometry();
        });
    }

    @Override
    protected PeuplementVegetation newVegetation() {
        final PeuplementVegetation candidate = super.newVegetation();

        //classement indéfini
        candidate.setTypeVegetationId(DEFAULT_PEUPLEMENT_VEGETATION_TYPE);

        /*
        Si on peut, on paramètre le traitement qui a été associé dans super.newVegetation();
        Il est nécessaire pour cela d'associer un identifiant de parcelle à la zone de végétation.
        */
        if (parcelle != null && parcelle.getId() != null) {
            candidate.setParcelleId(parcelle.getId());
            PluginVegetation.paramTraitement(PeuplementVegetation.class, candidate, DEFAULT_PEUPLEMENT_VEGETATION_TYPE);
        }

        return candidate;
    }

}
