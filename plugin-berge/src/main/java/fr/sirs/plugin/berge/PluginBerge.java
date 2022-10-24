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
package fr.sirs.plugin.berge;

import fr.sirs.CorePlugin;
import static fr.sirs.CorePlugin.buildLayers;
import static fr.sirs.CorePlugin.createDefaultSelectionStyle;
import static fr.sirs.CorePlugin.createTronconSelectionStyle;
import fr.sirs.Plugin;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.*;
import fr.sirs.map.FXMapPane;
import fr.sirs.plugin.berge.map.BergeToolBar;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import org.apache.sis.measure.Units;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import static org.geotoolkit.style.StyleConstants.DEFAULT_DESCRIPTION;
import static org.geotoolkit.style.StyleConstants.LITERAL_ONE_FLOAT;
import static org.geotoolkit.style.StyleConstants.LITERAL_ZERO_FLOAT;
import static org.geotoolkit.style.StyleConstants.STROKE_CAP_SQUARE;
import static org.geotoolkit.style.StyleConstants.STROKE_JOIN_BEVEL;
import org.opengis.filter.FilterFactory2;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Stroke;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginBerge extends Plugin {
    private static final String NAME = "plugin-berge";
    private static final String TITLE = "Module berge";

    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;
    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;

    public PluginBerge() {
        name = NAME;
        loadingMessage.set("module berge");
        themes.add(new SuiviBergeTheme());
        themes.add(new StructuresDescriptionTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
    }

    @Override
    public CharSequence getTitle() {
        return TITLE;
    }

    @Override
    public Image getImage() {
        // TODO: choisir une image pour ce plugin
        return null;
    }

    @Override
    public List<ToolBar> getMapToolBars(final FXMapPane mapPane) {
        return Collections.singletonList(new BergeToolBar(mapPane.getUiMap()));
    }

    //doit avoir la meme valeur que dans le fichier Berge.properties classPlural
    public static final String LAYER_BERGE_NAME = "Berges";
    public static final String LAYER_TRAIT_NAME = "Traits de berge";

    final Color[] colors = new Color[]{
                Color.BLACK,
                Color.BLUE,
                Color.CYAN,
                Color.RED,
                Color.DARK_GRAY,
                Color.GREEN,
                Color.MAGENTA,
                Color.ORANGE,
                Color.PINK,
                Color.RED
            };

    private final HashMap<Class, BeanFeatureSupplier> suppliers = new HashMap<>();

    private synchronized void loadDataSuppliers() {
        suppliers.clear();

        final Function<Class<? extends Element>, StructBeanSupplier> getDefaultSupplierForClass = (Class<? extends Element> c) ->{
            return new StructBeanSupplier(c, () -> getSession().getRepositoryForClass(c).getAllStreaming());
        };

        suppliers.put(Berge.class, getDefaultSupplierForClass.apply(Berge.class));
        suppliers.put(TraitBerge.class, getDefaultSupplierForClass.apply(TraitBerge.class));

        suppliers.put(SommetBerge.class, getDefaultSupplierForClass.apply(SommetBerge.class));
        suppliers.put(TalusBerge.class, getDefaultSupplierForClass.apply(TalusBerge.class));
        suppliers.put(PiedBerge.class, getDefaultSupplierForClass.apply(PiedBerge.class));
        suppliers.put(EpiBerge.class, getDefaultSupplierForClass.apply(EpiBerge.class));
        suppliers.put(OuvrageRevancheBerge.class, getDefaultSupplierForClass.apply(OuvrageRevancheBerge.class));
//        suppliers.put(FondationBerge.class, getDefaultSupplierForClass.apply(FondationBerge.class));
//        suppliers.put(TalusRisbermeBerge.class, getDefaultSupplierForClass.apply(TalusRisbermeBerge.class));
//        suppliers.put(CreteBerge.class, getDefaultSupplierForClass.apply(CreteBerge.class));
        }


    @Override
    public List<MapItem> getMapItems() {
        loadDataSuppliers();
        try {
            final MapItem container = MapBuilder.createItem();
            container.setName("Module berges");

            final BeanStore bergeStore = new BeanStore(suppliers.get(Berge.class));
            final BeanStore traitStore = new BeanStore(suppliers.get(TraitBerge.class));

            container.items().addAll(CorePlugin.buildLayers(bergeStore, LAYER_BERGE_NAME,
                    createBergeStyle(), createTronconSelectionStyle(false),true));
            container.items().addAll(CorePlugin.buildLayers(traitStore, LAYER_TRAIT_NAME,
                    createTraitBergeStyle(), createTronconSelectionStyle(false),true));

            final Map<String, String> nameMap = new HashMap<>();
            for(Class elementClass : suppliers.keySet()) {
                final LabelMapper mapper = LabelMapper.get(elementClass);
                nameMap.put(elementClass.getSimpleName(), mapper.mapClassName());
            }

            final MapItem desordreLayer = MapBuilder.createItem();
            desordreLayer.setName("Désordres (Berge)");
            final BeanStore desordreStore = new BeanStore(suppliers.get(DesordreBerge.class));
            desordreLayer.items().addAll(buildLayers(desordreStore, nameMap, colors, createDefaultSelectionStyle(), false));
            desordreLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            container.items().add(desordreLayer);

            final MapItem structLayer = MapBuilder.createItem();
            structLayer.setName("Structure (berge)");
            final BeanStore otherStore = new BeanStore(suppliers.get(PiedBerge.class),
                    suppliers.get(SommetBerge.class),
                    suppliers.get(TalusBerge.class),
                    suppliers.get(PiedBerge.class),
                    suppliers.get(EpiBerge.class),
                    suppliers.get(OuvrageRevancheBerge.class));
//                    suppliers.get(FondationBerge.class),
//                    suppliers.get(TalusRisbermeBerge.class),
//                    suppliers.get(CreteBerge.class))
            structLayer.items().addAll(buildLayers(otherStore, nameMap, colors, createDefaultSelectionStyle(), false));
            structLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            container.items().add(structLayer);

            // Réseaux de voirie
            final BeanStore rvStore = new BeanStore(
                    suppliers.get(VoieAcces.class),
                    suppliers.get(OuvrageFranchissement.class),
                    suppliers.get(OuvertureBatardable.class),
                    suppliers.get(VoieDigue.class),
                    suppliers.get(OuvrageVoirie.class));
            final MapItem rvLayer = MapBuilder.createItem();
            rvLayer.setName("Réseaux de voirie");
            rvLayer.items().addAll( buildLayers(rvStore, nameMap, colors, createDefaultSelectionStyle(),false) );
            rvLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            container.items().add(rvLayer);

            // Réseaux et ouvrages
            final BeanStore roStore = new BeanStore(
                    suppliers.get(StationPompage.class),
                    suppliers.get(ReseauHydrauliqueFerme.class),
                    suppliers.get(OuvrageHydrauliqueAssocie.class),
                    suppliers.get(ReseauTelecomEnergie.class),
                    suppliers.get(OuvrageTelecomEnergie.class),
                    suppliers.get(ReseauHydrauliqueCielOuvert.class),
                    suppliers.get(OuvrageParticulier.class),
                    suppliers.get(EchelleLimnimetrique.class));
            final MapItem roLayer = MapBuilder.createItem();
            roLayer.setName("Réseaux et ouvrages");
            roLayer.items().addAll( buildLayers(roStore, nameMap, colors, createDefaultSelectionStyle(),false) );
            roLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            container.items().add(roLayer);

            // Prestations
            final BeanStore prestaStore = new BeanStore(suppliers.get(Prestation.class));
            final MapItem prestaLayer = MapBuilder.createItem();
            prestaLayer.setName("Prestations");
            prestaLayer.items().addAll( buildLayers(prestaStore, nameMap, colors, createDefaultSelectionStyle(),false) );
            prestaLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            container.items().add(prestaLayer);

            return Collections.singletonList(container);
        } catch (Exception e) {
            throw new SirsCoreRuntimeException(e);
        }
    }

    public static MutableStyle createBergeStyle() throws CQLException, URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(Color.BLACK),LITERAL_ONE_FLOAT,FF.literal(9),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,Units.POINT,stroke1,LITERAL_ONE_FLOAT);

        final Stroke stroke2 = SF.stroke(SF.literal(new Color(230, 179, 77)),LITERAL_ONE_FLOAT,FF.literal(7),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line2 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,Units.POINT,stroke2,LITERAL_ONE_FLOAT);

        final Stroke stroke3 = SF.stroke(SF.literal(Color.BLACK),LITERAL_ONE_FLOAT,FF.literal(1),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line3 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,Units.POINT,stroke3,LITERAL_ONE_FLOAT);

        return SF.style(line1,line2,line3);
    }

    public static MutableStyle createTraitBergeStyle() throws CQLException, URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(new Color(255,204,128)),LITERAL_ONE_FLOAT,FF.literal(1.5),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,Units.POINT,stroke1,LITERAL_ONE_FLOAT);

        return SF.style(line1);
    }

    @Override
    public Optional<Image> getModelImage() throws IOException {
        final Image image;

        try (final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("fr/sirs/bergeModel.png")) {
            image = new Image(in);
        }
        return Optional.of(image);
    }

}