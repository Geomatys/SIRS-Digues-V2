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
package fr.sirs.util;

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.CorePlugin;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.*;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.DefaultFont;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.geometry.Envelope;
import org.opengis.style.*;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;

/**
 * <p>This class provides utilities for two purposes:</p>
 * <ul>
 * <li>increasing the @{@link Symbolizer} size prior printing the Fiches</li>
 * <li>hide the @{@link TronconDigue} archived prior printing the Fiches</li>
 * <li>restoring the @{@link Symbolizer} size after printing the Fiches</li>
 * <li>restore the @{@link TronconDigue} archived prior printing the Fiches</li>
 * </ul>
 * @author Estelle Idée (Geomatys)
 */
public class LocationInsertUtilities {

    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;

    /**
     *  Modify the SelectionStyle size on a specific layer
     * @param multiplier how much the symbols size should be modified by
     * @param e to get the layer that should be modified
     * @return the original style prior modification - to be used to restore the layer back to normal after printing
     */
    public static MutableStyle modifySelectionSymbolSize(double multiplier, Element e) {
        MapLayer layer = CorePlugin.getMapLayerForElement(e);
        //create new style to replace the old one
        Map<MapLayer, MutableStyle> result = new HashMap<>();
        modifyLayerSymbolSize(layer, multiplier, result, true);
        return result.get(layer);
    }

    /**
     * Modify the Style size on visible layers of the context
     * Method used prior printing Fiches
     *
     * @param multiplier how much the symbols size should be modified by
     * @return Map of @{@link MutableStyle} of the modified layers prior modification : to be used to restore the layers back to normal after printing
     */
    public static Map<MapLayer, MutableStyle> modifySymbolSize(double multiplier) {
        final List<MapLayer> layers = Injector.getSession().getFrame().getMapTab().getMap().getUiMap().getContainer().getContext().layers();
        Map<MapLayer, MutableStyle> result = new HashMap<>();

        for (MapLayer layer : layers) {
            if (layer.isVisible()) {
                modifyLayerSymbolSize(layer, multiplier, result, false);
            }
        }
        return result;
    }

    /**
     * Modify a layer style or selectionStyle by modifying the size of @{@link PointSymbolizer},
     * @{@link LineSymbolizer} and @{@link TextSymbolizer}
     *
     * @param layer that will be modified
     * @param multiplier how much the symbols size should be modified by
     * @param result Map of @{@link MutableStyle} of the modified layers prior modification : to be used to restore the layers back to normal after printing
     * @param selectionStyle to modify the layer style or selectionStyle
     */
    public static void modifyLayerSymbolSize(MapLayer layer, double multiplier, Map<MapLayer, MutableStyle> result, boolean selectionStyle) {
        //create new style to replace the old one
        final MutableFeatureTypeStyle newFts = SF.featureTypeStyle();

        MutableStyle currentStyle = selectionStyle ? layer.getSelectionStyle() : layer.getStyle();
        if (currentStyle != null && currentStyle.featureTypeStyles() != null && !currentStyle.featureTypeStyles().isEmpty()) {
            boolean styleModified;
            Symbolizer sym;
            Symbolizer[] copiedSymbolizers;
            for (final FeatureTypeStyle ftStyle : currentStyle.featureTypeStyles()) {
                styleModified = false;
                result.put(layer, currentStyle);
                if (ftStyle != null && ftStyle.rules() != null && !ftStyle.rules().isEmpty()) {
                    for (int ri = 0; ri < ftStyle.rules().size(); ri++) {
                        final Rule r = ftStyle.rules().get(ri);
                        if (r.symbolizers() != null && !r.symbolizers().isEmpty()) {
                            copiedSymbolizers = r.symbolizers().toArray(new Symbolizer[r.symbolizers().size()]);

                            if (r.symbolizers() != null && !r.symbolizers().isEmpty()) {
                                for (int i = 0; i < r.symbolizers().size(); i++) {
                                    sym = r.symbolizers().get(i);

                                    if (sym instanceof PointSymbolizer) {
                                        styleModified = true;
                                        copiedSymbolizers[i] = increaseSizePointSymbolizer((PointSymbolizer) sym, multiplier);
                                    }
                                    if (sym instanceof LineSymbolizer) {
                                        styleModified = true;
                                        copiedSymbolizers[i] = increaseSizeLineSymbolizer((LineSymbolizer) sym, multiplier);

                                    } else if (sym instanceof TextSymbolizer) {
                                        styleModified = true;
                                        copiedSymbolizers[i] = increaseSizeTextSymbolizer((TextSymbolizer) sym, multiplier);
                                    }
                                }
                            }
                            newFts.rules().add(SF.rule(copiedSymbolizers));
                        }
                    }
                }
                // if the layer's style has not been modified, it's removed from the returned Map
                if (!styleModified) {
                    result.remove(layer);
                }
            }
        }
        final MutableStyle style = SF.style();
        style.featureTypeStyles().add(newFts);

        if (selectionStyle) layer.setSelectionStyle(style);
        else layer.setStyle(style);
    }

    /**
     * create a copy of a @{@link PointSymbolizer} after modifying its graphic size
     *
     * @param sym @{@link PointSymbolizer} to copy
     * @param multiplier how much to multiply the graphic size by
     * @return the created @{@link PointSymbolizer} with new size
     */
    public static PointSymbolizer increaseSizePointSymbolizer(PointSymbolizer sym, double multiplier) {
        Graphic tmpGraphic = sym.getGraphic();
        tmpGraphic = SF.graphic(tmpGraphic.graphicalSymbols(),
                tmpGraphic.getOpacity(),
                FF.literal(Double.parseDouble(tmpGraphic.getSize().toString()) * multiplier),
                tmpGraphic.getRotation(),
                tmpGraphic.getAnchorPoint(),
                tmpGraphic.getDisplacement());
        return SF.pointSymbolizer(sym.getName(), sym.getGeometry(), sym.getDescription(), sym.getUnitOfMeasure(), tmpGraphic);
    }

    /**
     * create a copy of a @{@link LineSymbolizer} after modifying its line width
     *
     * @param sym @{@link LineSymbolizer} to copy
     * @param multiplier how much to multiply the line width by
     * @return the created @{@link LineSymbolizer} with new width
     */
    public static LineSymbolizer increaseSizeLineSymbolizer(LineSymbolizer sym, double multiplier) {
        Stroke tmpStroke = sym.getStroke();
        tmpStroke = SF.stroke(tmpStroke.getColor(),
                tmpStroke.getOpacity(),
                FF.literal(Double.parseDouble(tmpStroke.getWidth().toString()) * multiplier),
                tmpStroke.getLineJoin(),
                tmpStroke.getLineCap(),
                tmpStroke.getDashArray(),
                tmpStroke.getDashOffset());
        return SF.lineSymbolizer(sym.getName(), sym.getGeometry(), sym.getDescription(), sym.getUnitOfMeasure(), tmpStroke, sym.getPerpendicularOffset());
    }

    /**
     * create a copy of a @{@link TextSymbolizer} after modifying its font size
     *
     * @param sym @{@link TextSymbolizer} to copy
     * @param multiplier how much to multiply the font size by
     * @return the created @{@link TextSymbolizer} with new font size
     */
    public static TextSymbolizer increaseSizeTextSymbolizer(TextSymbolizer sym, double multiplier) {
        Font font = sym.getFont();
        TextSymbolizer tmpText = SF.textSymbolizer(sym.getFill(),
                new DefaultFont(font.getFamily(),
                        font.getStyle(),
                        font.getWeight(),
                        FF.literal(Double.parseDouble(font.getSize().toString()) * multiplier)),
                SF.halo(sym.getHalo().getFill(),
                        FF.literal(Double.parseDouble(sym.getHalo().getRadius().toString()) * multiplier)),
                FF.property("libelle"),
                sym.getLabelPlacement(), null);

        return tmpText;
    }

    /**
     * Restore the context layers style
     *
     * @param backUpStyles map with the layers' styles to be restored
     * @return
     */
    public static void changeBackLayersSymbolSize(Map<MapLayer, MutableStyle> backUpStyles) {
        final List<MapLayer> layers = Injector.getSession().getFrame().getMapTab().getMap().getUiMap().getContainer().getContext().layers();
        MutableStyle toRestore;
        for (MapLayer layer : layers) {
            toRestore = backUpStyles.get(layer);
            if (toRestore != null) layer.setStyle(toRestore);
        }
    }

    /**
     * Restore the SelectionStyle for an @{@link Element} layer
     * @param e element for which the layer should be modified
     * @param backUpSelectionStyle the selectionStyle to be restored
     */
    public static void changeBackSelectionSymbolSize(Element e, MutableStyle backUpSelectionStyle) {
        MapLayer layer = CorePlugin.getMapLayerForElement(e);
        layer.setSelectionStyle(backUpSelectionStyle);
    }

}