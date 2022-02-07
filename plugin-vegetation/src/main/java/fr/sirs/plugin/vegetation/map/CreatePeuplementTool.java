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
package fr.sirs.plugin.vegetation.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefTypePeuplementVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.DEFAULT_PEUPLEMENT_VEGETATION_TYPE;
import fr.sirs.theme.ui.FXPositionableExplicitMode;
import fr.sirs.util.ResourceInternationalString;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CreatePeuplementTool extends CreateVegetationPolygonTool<PeuplementVegetation>{

    public static final Spi SPI = new Spi();

    private final ComboBox<Preview> comboTypeVegetation = new ComboBox();

    public static final class Spi extends AbstractEditionToolSpi{

        public Spi() {
            super("CreatePeuplement",
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreatePeuplementTool.title",CreatePeuplementTool.class.getClassLoader()),
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreatePeuplementTool.abstract",CreatePeuplementTool.class.getClassLoader()),
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
    };

    public CreatePeuplementTool(FXMap map) {
        super(map,SPI, PeuplementVegetation.class);

        final Previews previewRepository = Injector.getSession().getPreviews();
        SIRS.initCombo(comboTypeVegetation, FXCollections.observableList(
            previewRepository.getByClass(RefTypePeuplementVegetation.class)),
            previewRepository.get(DEFAULT_PEUPLEMENT_VEGETATION_TYPE));

        final Label label = new Label("Type de végétation :");
        label.getStyleClass().add("label-header");

        final VBox center = (VBox) wizard.getCenter();
        center.getChildren().add(4, label);
        center.getChildren().add(5, comboTypeVegetation);

        // Override save action to include vegetation type
        end.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //on sauvegarde
                vegetation.setGeometryMode(FXPositionableExplicitMode.MODE);
                vegetation.setValid(true);
                vegetation.setForeignParentId(parcelle.getDocumentId());
                vegetation.setTypeVegetationId(comboTypeVegetation.getSelectionModel().getSelectedItem().getElementId());
                final AbstractSIRSRepository vegetationRepo = Injector.getSession().getRepositoryForClass(vegetationClass);
                vegetationRepo.add(vegetation);
                startGeometry();
            }
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
        if(parcelle!=null && parcelle.getId()!=null){
            candidate.setParcelleId(parcelle.getId());
            PluginVegetation.paramTraitement(PeuplementVegetation.class, candidate, DEFAULT_PEUPLEMENT_VEGETATION_TYPE);
        }

        return candidate;
    }

}
