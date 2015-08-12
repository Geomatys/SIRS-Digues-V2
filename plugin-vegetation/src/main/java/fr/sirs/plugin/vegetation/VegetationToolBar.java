
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.plugin.vegetation.map.CreateArbreTool;
import fr.sirs.plugin.vegetation.map.CreateHerbaceTool;
import fr.sirs.plugin.vegetation.map.CreateInvasiveTool;
import fr.sirs.plugin.vegetation.map.CreateParcelleTool;
import fr.sirs.plugin.vegetation.map.CreatePeuplementTool;
import fr.sirs.plugin.vegetation.map.EditVegetationTool;
import java.util.Iterator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
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
 * @author Johann Sorel (Geomatys)
 */
public class VegetationToolBar extends ToolBar {

    private Stage dialog = null;
    private Stage dialogRecherche = null;

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

        final Button recherche = new Button("Recherche");
        recherche.setOnAction(this::showSearchDialog);
        getItems().add(recherche);

    }

    private boolean checkPlan(){
        //on vérifie qu'il y a une plan de gestion actif
        PlanVegetation plan = VegetationSession.INSTANCE.planProperty().get();
        if(plan==null){
            final Dialog dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setContentText("Veuillez activer un plan de gestion avant de commencer l'édition.");
            dialog.showAndWait();
            return false;
        }
        return true;
    }

    private void showSearchDialog(ActionEvent act){
        if(!checkPlan()) return;
        
        dialogRecherche = new Stage();
        dialogRecherche.setAlwaysOnTop(true);
        dialogRecherche.initModality(Modality.NONE);
        dialogRecherche.initStyle(StageStyle.UTILITY);
        dialogRecherche.setTitle(GeotkFX.getString(FXEditAction.class,"Végétation"));

        final FXPlanLayerPane pan = new FXPlanLayerPane();

        final BorderPane pane = new BorderPane(pan);
        pane.setPadding(new Insets(10, 10, 10, 10));

        final Scene scene = new Scene(pane);

        dialogRecherche.setOnCloseRequest((WindowEvent evt) -> dialogRecherche = null);
        dialogRecherche.setScene(scene);
        dialogRecherche.setResizable(true);
        dialogRecherche.setWidth(350);
        dialogRecherche.setHeight(450);
        dialogRecherche.show();

    }

    private void showEditor(){
        if(!checkPlan()) return;

        final FXToolBox toolbox = new FXToolBox(Injector.getSession().getFrame().getMapTab().getMap().getUiMap(), MapBuilder.createEmptyMapLayer());
        toolbox.commitRollbackVisibleProperty().setValue(false);
        toolbox.getToolPerRow().set(6);
        toolbox.getTools().add(CreateParcelleTool.SPI);
        toolbox.getTools().add(CreatePeuplementTool.SPI);
        toolbox.getTools().add(CreateInvasiveTool.SPI);
        toolbox.getTools().add(CreateHerbaceTool.SPI);
        toolbox.getTools().add(CreateArbreTool.SPI);
        toolbox.getTools().add(EditVegetationTool.SPI);

        dialog = new Stage();
        dialog.setAlwaysOnTop(true);
        dialog.initModality(Modality.NONE);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle(GeotkFX.getString(FXEditAction.class,"Végétation"));

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
