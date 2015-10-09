
package fr.sirs.map;

import fr.sirs.SIRS;
import java.lang.ref.WeakReference;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.contexttree.TreeMenuItem;
import org.geotoolkit.gui.javafx.layer.FXFeatureTypePane;
import org.geotoolkit.map.FeatureMapLayer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class StructureMenuItem extends TreeMenuItem {

    private static final Image ICON = SwingFXUtils.toFXImage(
            IconBuilder.createImage(FontAwesomeIcons.ICON_COG, 16, FontAwesomeIcons.DEFAULT_COLOR), null);

    private WeakReference<TreeItem> itemRef;

    public StructureMenuItem() {

        menuItem = new FeatureStructureMenuItem();
        menuItem.setGraphic(new ImageView(ICON));
    }

    @Override
    public MenuItem init(List<? extends TreeItem> selection) {
        boolean valid = uniqueAndType(selection,FeatureMapLayer.class);
        if(valid && selection.get(0).getParent()!=null){
            itemRef = new WeakReference<>(selection.get(0));
            return menuItem;
        }
        return null;
    }


    private class FeatureStructureMenuItem extends MenuItem {

        public FeatureStructureMenuItem() {
            super("Géoréférencement");

            setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent event) {
                    if(itemRef == null) return;
                    final TreeItem treeItem = itemRef.get();
                    if(treeItem == null) return;
                    final FeatureMapLayer layer = (FeatureMapLayer) treeItem.getValue();
                    FXFeatureTypePane pane = new FXFeatureTypePane();
                    pane.init(layer);


                    final Stage dialog = new Stage();
                    dialog.getIcons().add(SIRS.ICON);
                    dialog.setTitle("Géoréférencement");
                    dialog.setResizable(true);
                    dialog.initModality(Modality.NONE);
                    dialog.initOwner(null);

                    final Button cancelBtn = new Button("Fermer");
                    cancelBtn.setCancelButton(true);

                    final ButtonBar bbar = new ButtonBar();
                    bbar.setPadding(new Insets(5, 5, 5, 5));
                    bbar.getButtons().addAll(cancelBtn);

                    final BorderPane dialogContent = new BorderPane();
                    dialogContent.setCenter(pane);
                    dialogContent.setBottom(bbar);
                    dialog.setScene(new Scene(dialogContent));

                    cancelBtn.setOnAction((ActionEvent e) -> {
                        dialog.close();
                    });

                    dialog.show();

                }
            });
        }

    }

}
