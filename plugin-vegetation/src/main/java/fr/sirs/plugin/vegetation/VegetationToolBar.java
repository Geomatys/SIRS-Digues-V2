
package fr.sirs.plugin.vegetation;

import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Johann Sorel
 */
public class VegetationToolBar extends ToolBar {

    private static final Image ICON_VEGETATION = new Image("fr/sirs/plugin/vegetation/vegetation.png");
    private static final Image ICON_PARCELLE = new Image("fr/sirs/plugin/vegetation/parcelle.png");

    public VegetationToolBar() {
        getItems().add(new Label("Végétation"));

        final ToggleButton buttonParcelle = new ToggleButton(null, new ImageView(ICON_PARCELLE));
        getItems().add(buttonParcelle);

        final MenuItem menuPeuplement = new MenuItem("Peuplement");
        final MenuItem menuInvasives = new MenuItem("Invasives");
        final MenuItem menuArbres = new MenuItem("Arbres exceptionnels");
        final MenuItem menuStrates = new MenuItem("Strates herbacées");

        final MenuButton buttonPeuplement = new MenuButton(null, new ImageView(ICON_VEGETATION),
                menuPeuplement, menuInvasives, menuArbres, menuStrates);
        getItems().add(buttonPeuplement);

    }



}
