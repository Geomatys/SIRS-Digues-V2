
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.plugin.vegetation.map.CreateParcelleTool;
import java.util.Iterator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;
import org.geotoolkit.gui.javafx.render2d.edition.FXEditAction;
import org.geotoolkit.gui.javafx.render2d.edition.FXToolBox;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.MapBuilder;

/**
 *
 * @author Johann Sorel
 */
public class VegetationToolBar extends ToolBar {

    private static final Image ICON_VEGETATION = new Image("fr/sirs/plugin/vegetation/vegetation.png");
    private static final Image ICON_PARCELLE = new Image("fr/sirs/plugin/vegetation/parcelle.png");

    private Stage dialog = null;

    public VegetationToolBar() {
        getItems().add(new Label("Végétation"));

        final ToggleButton buttonParcelle = new ToggleButton(null, new ImageView(GeotkFX.ICON_EDIT));
        getItems().add(buttonParcelle);

        buttonParcelle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(dialog==null){
                    showEditor();
                }else{
                    dialog.close();
                    dialog = null;
                }
            }
        });


//        final MenuItem menuPeuplement = new MenuItem("Peuplement");
//        final MenuItem menuInvasives = new MenuItem("Invasives");
//        final MenuItem menuArbres = new MenuItem("Arbres exceptionnels");
//        final MenuItem menuStrates = new MenuItem("Strates herbacées");
//
//        final MenuButton buttonPeuplement = new MenuButton(null, new ImageView(ICON_VEGETATION),
//                menuPeuplement, menuInvasives, menuArbres, menuStrates);
//        getItems().add(buttonPeuplement);

    }

    private void showEditor(){
        final FXToolBox toolbox = new FXToolBox(Injector.getSession().getFrame().getMapTab().getMap().getUiMap(), MapBuilder.createEmptyMapLayer());
        toolbox.commitRollbackVisibleProperty().setValue(false);
        toolbox.getToolPerRow().set(6);
        toolbox.getTools().add(CreateParcelleTool.SPI);

        dialog = new Stage();
        dialog.setAlwaysOnTop(true);
        dialog.initModality(Modality.NONE);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle(GeotkFX.getString(FXEditAction.class,"edit"));

        toolbox.setMaxHeight(Double.MAX_VALUE);
        toolbox.setMaxWidth(Double.MAX_VALUE);
        final Iterator<EditionTool.Spi> ite = EditionHelper.getToolSpis();
        while(ite.hasNext()){
            toolbox.getTools().add(ite.next());
        }

        final BorderPane pane = new BorderPane(toolbox);
        pane.setPadding(new Insets(10, 10, 10, 10));

        final Scene scene = new Scene(pane);

        dialog.setOnCloseRequest((WindowEvent evt) -> dialog = null);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.setWidth(350);
        dialog.setHeight(450);
        dialog.show();

    }



}
