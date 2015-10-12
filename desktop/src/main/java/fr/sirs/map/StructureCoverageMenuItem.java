
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
import org.geotoolkit.coverage.amended.AmendedCoverageReference;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.contexttree.TreeMenuItem;
import org.geotoolkit.gui.javafx.layer.FXCoverageDecoratorPane;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.storage.coverage.CoverageReference;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class StructureCoverageMenuItem extends TreeMenuItem {

    private static final Image ICON = SwingFXUtils.toFXImage(
            IconBuilder.createImage(FontAwesomeIcons.ICON_COG, 16, FontAwesomeIcons.DEFAULT_COLOR), null);

    private WeakReference<TreeItem> itemRef;

    public StructureCoverageMenuItem() {

        menuItem = new CoverageStructureMenuItem();
        menuItem.setGraphic(new ImageView(ICON));
    }

    @Override
    public MenuItem init(List<? extends TreeItem> selection) {
        boolean valid = uniqueAndType(selection,CoverageMapLayer.class);
        if(valid && selection.get(0).getParent()!=null){
            //test if it's not a application layer
            final TreeItem treeItem = selection.get(0);
            final CoverageMapLayer layer = (CoverageMapLayer) treeItem.getValue();
            final CoverageReference cref = layer.getCoverageReference();
            if(!(cref instanceof AmendedCoverageReference)){
                return null;
            }

            itemRef = new WeakReference<>(selection.get(0));
            return menuItem;
        }
        return null;
    }


    private class CoverageStructureMenuItem extends MenuItem {

        public CoverageStructureMenuItem() {
            super("Géoréférencement");

            setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent event) {
                    if(itemRef == null) return;
                    final TreeItem treeItem = itemRef.get();
                    if(treeItem == null) return;
                    final CoverageMapLayer layer = (CoverageMapLayer) treeItem.getValue();
                    final AmendedCoverageReference cref = (AmendedCoverageReference) layer.getCoverageReference();
                    FXCoverageDecoratorPane pane = new FXCoverageDecoratorPane(cref);

                    final Stage dialog = new Stage();
                    dialog.getIcons().add(SIRS.ICON);
                    dialog.setTitle("Géoréférencement");
                    dialog.setResizable(true);
                    dialog.initModality(Modality.NONE);

                    final Button cancelBtn = new Button("Fermer");
                    cancelBtn.setCancelButton(true);

                    final ButtonBar bbar = new ButtonBar();
                    bbar.setPadding(new Insets(5, 5, 5, 5));
                    bbar.getButtons().addAll(cancelBtn);

                    final BorderPane dialogContent = new BorderPane();
                    dialogContent.setCenter(pane);
                    dialogContent.setBottom(bbar);
                    dialog.setScene(new Scene(dialogContent));
                    dialog.sizeToScene();

                    cancelBtn.setOnAction((ActionEvent e) -> {
                        dialog.close();
                    });

                    dialog.show();

                }
            });
        }

    }

}
