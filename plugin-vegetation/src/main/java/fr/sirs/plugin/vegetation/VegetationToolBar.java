
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;
import org.geotoolkit.gui.javafx.render2d.edition.FXToolBox;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.MapBuilder;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class VegetationToolBar extends ToolBar {

    private static final String LEFT = "buttongroup-left";
    private static final String CENTER = "buttongroup-center";
    private static final String RIGHT = "buttongroup-right";
    
    private Stage dialog = null;
    private Stage dialogRecherche = null;

    public VegetationToolBar() {
        getStylesheets().add("/org/geotoolkit/gui/javafx/buttonbar.css");
        getItems().add(new Label("Végétation"));

        final Button buttonParcelle = new Button(null, new ImageView(GeotkFX.ICON_EDIT));
        buttonParcelle.disableProperty().bind(Injector.getSession().geometryEditionProperty().not());
        buttonParcelle.setTooltip(new Tooltip("Outil d'édition de végétation"));
        buttonParcelle.getStyleClass().add(LEFT);
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

        final Button recherche = new Button(null, new ImageView(SwingFXUtils.toFXImage(IconBuilder.createImage(
                FontAwesomeIcons.ICON_GEARS_ALIAS,16,FontAwesomeIcons.DEFAULT_COLOR),null)));
        recherche.setTooltip(new Tooltip("Analyse de la végétation"));
        recherche.setOnAction(this::showSearchDialog);
        recherche.getStyleClass().add(RIGHT);

        getItems().add(new HBox(buttonParcelle,recherche));
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
        dialogRecherche.setTitle("Végétation");

        final FXPlanLayerPane pan = new FXPlanLayerPane();

        final BorderPane pane = new BorderPane(pan);
        pane.setPadding(new Insets(10, 10, 10, 10));

        final Scene scene = new Scene(pane);

        dialogRecherche.setOnCloseRequest((WindowEvent evt) -> dialogRecherche = null);
        dialogRecherche.setScene(scene);
        dialogRecherche.setResizable(true);
        dialogRecherche.setWidth(300);
        dialogRecherche.setHeight(300);
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
        dialog.initModality(Modality.NONE);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Végétation");

        toolbox.setMaxHeight(Double.MAX_VALUE);
        toolbox.setMaxWidth(Double.MAX_VALUE);
        final Iterator<EditionTool.Spi> ite = EditionHelper.getToolSpis();
        while(ite.hasNext()){
            toolbox.getTools().add(ite.next());
        }

        final FXBordeRPane pane = new FXBordeRPane(toolbox);

        pane.setPadding(new Insets(10, 10, 10, 10));

        final Scene scene = new Scene(pane);

        dialog.setOnCloseRequest((WindowEvent evt) -> dialog = null);
        dialog.setScene(scene);
        dialog.setResizable(true);

        //resize pane if too small
        pane.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                final double minWidth = pane.computeMinWidth(dialog.getHeight()) +30;
                final double minHeight = pane.computeMinHeight(dialog.getWidth()) +30;
                if(dialog.getHeight()<minHeight){
                    dialog.setHeight(minHeight);
                }
                if(dialog.getWidth()<minWidth){
                    dialog.setWidth(minWidth);
                }
            }
        });

        dialog.show();
        dialog.requestFocus();
    }

    private static class FXBordeRPane extends BorderPane{

        public FXBordeRPane() {
        }

        public FXBordeRPane(Node center) {
            super(center);
        }
        
        @Override
        public  double computeMinWidth(double height) {
            return super.computeMinWidth(height);
        }

        @Override
        public double computeMinHeight(double width) {
            return super.computeMinHeight(width);
        }
    }


}
