package fr.sirs.plugin.carto;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.map.FXDataImportPane;
import fr.sirs.map.FXMapTab;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import java.awt.Color;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;

/**
 * Ajout de donnée.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class AddLayerTheme extends AbstractPluginsButtonTheme {
    public static final Image ICON_ADD = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PLUS,28,new Color(74, 123, 165)),null);

    public AddLayerTheme() {
        super("Ajout de donnée", "Ajout de donnée", ICON_ADD);
    }

    @Override
    public Parent createPane() {
        final Session session = Injector.getSession();
        final FXMapTab mapTab = session.getFrame().getMapTab();
        mapTab.show();

        final FXMap map = mapTab.getMap().getUiMap();

        final Stage importStage = new Stage();
        importStage.getIcons().add(SIRS.ICON);
        importStage.setTitle("Importer une donnée");
        importStage.setResizable(true);
        importStage.initModality(Modality.NONE);
        importStage.initOwner(map.getScene() != null? map.getScene().getWindow() : null);

        importStage.setMaxWidth(Double.MAX_VALUE);
        importStage.sizeToScene();

        final FXDataImportPane importPane = new FXDataImportPane();
        importPane.configurationProperty().addListener((observable, oldValue, newValue) -> importStage.sizeToScene());
        importStage.setScene(new Scene(importPane));
        // We listen on import panel to be noticed when new layers are added.
        importPane.mapLayers.addListener(new ListChangeListener<MapLayer>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends MapLayer> c) {
                if (map.getContainer() != null && map.getContainer().getContext() != null) {
                    List<MapItem> items = map.getContainer().getContext().items();
                    while (c.next()) {
                        if (c.wasAdded()) {
                            for (final MapLayer layer : c.getAddedSubList()) {
                                items.add(layer);
                            }
                        }
                    }
                }
                importStage.hide();
            }
        });
        importStage.show();
        importStage.requestFocus();

        return null;
    }
    
}