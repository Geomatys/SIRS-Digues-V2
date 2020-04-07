/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.map;

import fr.sirs.CorePlugin;
import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.apache.sis.util.ArgumentChecks;
import static org.elasticsearch.search.aggregations.support.format.ValueParser.DateMath.mapper;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.style.visitor.ListingColorVisitor;
import org.opengis.style.Style;

/**
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
public class MapItemViewRealPositionColumn extends TreeTableColumn <MapItem, Boolean> {

    private static final Image ICON_REAL_POSITION_VISIBLE   = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CROSSHAIRS, 16, Color.DARK_GRAY),null);
    private static final Image ICON_REAL_POSITION_UNVISIBLE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CROSSHAIRS, 16, Color.LIGHT_GRAY),null);

    private static final Tooltip REAL_POSITION_VISIBLE_TOOLTIP   = new Tooltip("Afficher les positions réelles de début et de fin");
    private static final Tooltip REAL_POSITION_UNVISIBLE_TOOLTIP = new Tooltip("Afficher uniquement les géométries");



    public MapItemViewRealPositionColumn() {
        setEditable(true);
        setPrefWidth(26);
        setMinWidth(26);
        setMaxWidth(26);


        setCellValueFactory((TreeTableColumn.CellDataFeatures<MapItem, Boolean> param) -> {
            return new SimpleBooleanProperty(Boolean.FALSE);
        });

        setCellFactory((param) -> new ViewRealPositionCell());
    }

    private static final class ViewRealPositionCell extends TreeTableCell<MapItem, Boolean> {

        /**
         * Image view contained in the cell.
         */
        private final ImageView cellContent = new ImageView();
        private final SimpleBooleanProperty isRealPositionVisible = new SimpleBooleanProperty(false);


        public ViewRealPositionCell() {
//            setFont(FXUtilities.FONTAWESOME);
            setOnMouseClicked(this::mouseClicked);
            itemProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {

                setGraphic(null);
                if (newValue==null || !newValue) { //Should never be null
                    setTooltip(REAL_POSITION_UNVISIBLE_TOOLTIP);
                    cellContent.setImage(ICON_REAL_POSITION_UNVISIBLE);
                } else { //Should never be null
                    setTooltip(REAL_POSITION_VISIBLE_TOOLTIP);
                    cellContent.setImage(ICON_REAL_POSITION_VISIBLE);
                }
                setGraphic(cellContent);
            });
            itemProperty().setValue(Boolean.FALSE);
        }

        private void mouseClicked(MouseEvent event){
            if(isEditing() && itemProperty().get() != null) {
                final MapItem mitem = getTreeTableRow().getItem();
                if (mitem != null) {
                    final boolean newValue = !itemProperty().get();
                    setRealPositionVisible(mitem, newValue);
                    itemProperty().set(newValue);
                }
            }
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                cellContent.setImage(null);
            } else {
                cellContent.setImage(item? ICON_REAL_POSITION_VISIBLE : ICON_REAL_POSITION_UNVISIBLE);
            }
        }
    }

    private static void setRealPositionVisible(final MapItem target, final Boolean visible) {
        if (target instanceof MapLayer) {
            final MapLayer maplayer = (MapLayer) target;
            final List<MutableFeatureTypeStyle> fts = maplayer.getStyle().featureTypeStyles();

            if (fts != null) {

                final Stream<Color> colors = tryFinfColorFromPrevious(maplayer.getStyle());
                final Color color = colors.filter(c -> !(c.equals(Color.BLACK) || c.equals(Color.WHITE))).findAny().orElse(Color.BLACK);

//                final MutableRule start = CorePlugin.createExactPositinRule("start", SirsCore.POSITION_DEBUT_FIELD, Color.red, StyleConstants.MARK_TRIANGLE);
//                final MutableRule end   = CorePlugin.createExactPositinRule("end", SirsCore.POSITION_FIN_FIELD, Color.red, StyleConstants.MARK_TRIANGLE);
                if(visible) {
                    maplayer.setStyle(CorePlugin.createDefaultStyle(color, null, true));
//                    applyRule(fts, start);
//                    applyRule(fts, end);
                } else {

                    maplayer.setStyle(CorePlugin.createDefaultStyle(color, null, false));
//                    removeRule(fts, "start");
//                    removeRule(fts, "end");
                }

            }
            maplayer.setSelectionStyle(CorePlugin.createDefaultSelectionStyle(visible));

        } else {
            for (final MapItem child : target.items()) {
                setRealPositionVisible(child, visible);
            }
        }
    }

//    private static Color tryFinfColorFromPrevious(final List<MutableFeatureTypeStyle> ftsList) {
    private static Stream<Color> tryFinfColorFromPrevious(final Style style) {
        ArgumentChecks.ensureNonNull("MutableFeatureTypeStyles", style);

        final ListingColorVisitor visitor = new ListingColorVisitor();
        style.accept(visitor, null);
        final Set<Integer> colors = visitor.getColors();
        if (colors == null) {
            return Stream.empty();
        } else {
            return colors.stream().map(integer -> new Color(integer)); //We don't take into account the alpha component to improve comparisaon with white and black colors
        }


//        ftsList.stream().flatMap(fts -> fts.rules().stream())
//                .flatMap(rule -> rule.symbolizers().stream())
//                .map(symbolizer-> sgetStroke())
//                .map(stroke -> )
//                GO2Utilities.STYLE_FACTORY.lineSymbolizer(stroke, geometryPropertyName);
//                GO2Utilities.FILTER_FACTORY.

    }


//
//    private static void applyRule(final List<MutableFeatureTypeStyle> fts, final MutableRule rule) {
//        ArgumentChecks.ensureNonNull("Rule", rule);
//
//        fts.stream().map(ftsl -> ftsl.rules())
//                .filter(rules -> !rules.contains(rule))
//                .forEach(rules -> rules.add(rule));
//
//    }
//
//    private static void removeRule(final List<MutableFeatureTypeStyle> fts, final String ruleName) {
//        ArgumentChecks.ensureNonNull("Rule Name", ruleName);
//
//        fts.stream().map(ftsl -> ftsl.rules())
//                .forEach(rules -> rules.removeIf(rule -> ruleName.equals(rule.getName())));
//
//    }



//        private static boolean isRealPositionVisible(final MapItem item) {
//            if (item instanceof ViewRealPositionCell) {
//                return ((MapLayer)item).getStyle().featureTypeStyles().contains(item)
//            } else {
//                for (MapItem child : item.items()) {
//                    if (isRealPositionVisible(child)) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }

}
