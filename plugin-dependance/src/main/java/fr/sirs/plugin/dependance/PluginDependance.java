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
package fr.sirs.plugin.dependance;

import static fr.sirs.CorePlugin.createDefaultSelectionStyle;
import static fr.sirs.CorePlugin.createDefaultStyle;
import fr.sirs.Plugin;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import static fr.sirs.core.SirsCore.DATE_DEBUT_FIELD;
import static fr.sirs.core.SirsCore.DATE_FIN_FIELD;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.AireStockageDependance;
import fr.sirs.core.model.AmenagementHydraulique;
import fr.sirs.core.model.AutreDependance;
import fr.sirs.core.model.CheminAccesDependance;
import fr.sirs.core.model.DesordreDependance;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.OuvrageVoirieDependance;
import fr.sirs.core.model.TraitAmenagementHydraulique;
import fr.sirs.core.model.PrestationAmenagementHydraulique;
import fr.sirs.core.model.StructureAmenagementHydraulique;
import fr.sirs.core.model.OuvrageAssocieAmenagementHydraulique;
import fr.sirs.core.model.OrganeProtectionCollective;
import fr.sirs.map.FXMapPane;
import fr.sirs.plugin.dependance.map.DependanceToolBar;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.RandomStyleBuilder;
import org.opengis.util.GenericName;

/**
 * Plugin correspondant au module dépendance et AH.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Maxime Gavens (Geomatys)
 */
public class PluginDependance extends Plugin {
    private static final String NAME = "plugin-dependance-ah";
    private static final String TITLE = "Module dépendance et AH";

    //doit avoir la meme valeur que dans le fichier Berge.properties classPlural
    public static final String LAYER_AH_NAME = "Aménagements hydrauliques";
    public static final String LAYER_TRAIT_NAME = "Traits d'aménagement hydraulique";

    private final HashMap<Class, BeanFeatureSupplier> suppliers = new HashMap<>();

    private static FeatureMapLayer aireLayer;
    private static FeatureMapLayer autreLayer;
    private static FeatureMapLayer cheminLayer;
    private static FeatureMapLayer ouvrageVoirieLayer;
    private static FeatureMapLayer amenagementLayer;
    private static FeatureMapLayer traitAmenagementLayer;

    private static FeatureMapLayer desordreLayer;
    private static FeatureMapLayer prestationLayer;
    private static FeatureMapLayer structureLayer;
    private static FeatureMapLayer ouvrageAssocieLayer;
    private static FeatureMapLayer organeProtectionLayer;

    public PluginDependance() {
        name = NAME;
        loadingMessage.set("module dépendance et AH");
        themes.add(new DependancesTheme());
        themes.add(new DescriptionDependanceAHTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
        loadDataSuppliers();
    }

    @Override
    public CharSequence getTitle() {
        return TITLE;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public List<ToolBar> getMapToolBars(final FXMapPane mapPane) {
        return Collections.singletonList(new DependanceToolBar(mapPane.getUiMap()));
    }

    @Override
    public List<MapItem> getMapItems() {
        try {
            // Layer colors
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
                Color.LIGHT_GRAY,
                Color.YELLOW,
                Color.GRAY
            };

            // Layer name map
            final Map<String, String> nameMap = new HashMap<>();
            for(Class elementClass : suppliers.keySet()) {
                final LabelMapper mapper = LabelMapper.get(elementClass);
                nameMap.put(elementClass.getSimpleName(), mapper.mapClassName());
            }
            // Replace name of specific layer
            nameMap.put(AmenagementHydraulique.class.getSimpleName(), LAYER_AH_NAME);
            nameMap.put(TraitAmenagementHydraulique.class.getSimpleName(), LAYER_TRAIT_NAME);

            /// Build item
            final MapItem depGroup = MapBuilder.createItem();
            depGroup.setName("Dépendances et AH");
            final BeanStore otherStore = new BeanStore(
                    suppliers.get(OuvrageVoirieDependance.class),
                    suppliers.get(TraitAmenagementHydraulique.class),
                    suppliers.get(CheminAccesDependance.class),
                    suppliers.get(AutreDependance.class),
                    suppliers.get(AireStockageDependance.class),
                    suppliers.get(AmenagementHydraulique.class),
                    suppliers.get(DesordreDependance.class),
                    suppliers.get(PrestationAmenagementHydraulique.class),
                    suppliers.get(StructureAmenagementHydraulique.class),
                    suppliers.get(OuvrageAssocieAmenagementHydraulique.class),
                    suppliers.get(OrganeProtectionCollective.class)
            );
            depGroup.items().addAll(buildLayers(otherStore, nameMap, colors, createDefaultSelectionStyle(), false));
            depGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            return Collections.singletonList(depGroup);
        } catch (DataStoreException ex) {
            throw new SirsCoreRuntimeException(ex);
        }
    }

    public static FeatureMapLayer getAireLayer() {
        return aireLayer;
    }

    public static FeatureMapLayer getAutreLayer() {
        return autreLayer;
    }

    public static FeatureMapLayer getCheminLayer() {
        return cheminLayer;
    }

    public static FeatureMapLayer getOuvrageVoirieLayer() {
        return ouvrageVoirieLayer;
    }

    public static FeatureMapLayer getDesordreLayer() {
        return desordreLayer;
    }

    public static FeatureMapLayer getAmenagementLayer() {
        return amenagementLayer;
    }

    public static FeatureMapLayer getPrestationLayer() {
        return prestationLayer;
    }

    public static FeatureMapLayer getStructureLayer() {
        return structureLayer;
    }

    public static FeatureMapLayer getOuvrageAssocieLayer() {
        return ouvrageAssocieLayer;
    }

    public static FeatureMapLayer getOrganeProtectionLayer() {
        return organeProtectionLayer;
    }

    @Override
    public Optional<Image> getModelImage() throws IOException {
        final Image image;

        try (final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("fr/sirs/dependanceModel.png")) {
            image = new Image(in);
        }
        return Optional.of(image);
    }

    private synchronized void loadDataSuppliers() {
        final Set<Class> toLoad = new HashSet<>(Arrays.asList(
                OuvrageVoirieDependance.class,
                TraitAmenagementHydraulique.class,
                CheminAccesDependance.class,
                AutreDependance.class,
                AireStockageDependance.class,
                AmenagementHydraulique.class,
                DesordreDependance.class,
                PrestationAmenagementHydraulique.class,
                StructureAmenagementHydraulique.class,
                OuvrageAssocieAmenagementHydraulique.class,
                OrganeProtectionCollective.class
        ));
        final Function<Class<? extends Element>, StructBeanSupplier> getDefaultSupplierForClass = (Class<? extends Element> c) ->{
            return new StructBeanSupplier(c, () -> getSession().getRepositoryForClass(c).getAllStreaming());
        };

        suppliers.clear();
        toLoad.forEach(clazz -> suppliers.put(clazz, getDefaultSupplierForClass.apply(clazz)));
    }

    private void attributeLayer(final FeatureMapLayer l, final String name) {
        if (OuvrageVoirieDependance.class.getSimpleName().equals(name)) {
            ouvrageVoirieLayer = l;
        } else if (TraitAmenagementHydraulique.class.getSimpleName().equals(name)) {
            traitAmenagementLayer = l;
        } else if (CheminAccesDependance.class.getSimpleName().equals(name)) {
            cheminLayer = l;
        } else if (AutreDependance.class.getSimpleName().equals(name)) {
            autreLayer = l;
        } else if (AireStockageDependance.class.getSimpleName().equals(name)) {
            aireLayer = l;
        } else if (AmenagementHydraulique.class.getSimpleName().equals(name)) {
            amenagementLayer = l;
        } else if (DesordreDependance.class.getSimpleName().equals(name)) {
            desordreLayer = l;
        } else if (PrestationAmenagementHydraulique.class.getSimpleName().equals(name)) {
            prestationLayer = l;
        } else if (StructureAmenagementHydraulique.class.getSimpleName().equals(name)) {
            structureLayer = l;
        } else if (OuvrageAssocieAmenagementHydraulique.class.getSimpleName().equals(name)) {
            ouvrageAssocieLayer = l;
        } else if (OrganeProtectionCollective.class.getSimpleName().equals(name)) {
            organeProtectionLayer = l;
        }
    }

    private List<MapLayer> buildLayers(BeanStore store, Map<String,String> nameMap, Color[] colors, MutableStyle selectionStyle, boolean visible) throws DataStoreException{
        final List<MapLayer> layers = new ArrayList<>();
        final org.geotoolkit.data.session.Session symSession = store.createSession(false);
        int i = 0;

        for(GenericName name : store.getNames()){
            final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
            final MutableStyle baseStyle = createDefaultStyle(colors[i % colors.length]);
            final MutableStyle style = (baseStyle==null) ? RandomStyleBuilder.createRandomVectorStyle(col.getFeatureType()) : baseStyle;
            final FeatureMapLayer fml = MapBuilder.createFeatureLayer(col, style);

            if(col.getFeatureType().getDescriptor(DATE_DEBUT_FIELD)!=null && col.getFeatureType().getDescriptor(DATE_FIN_FIELD)!=null){
                final FeatureMapLayer.DimensionDef datefilter = new FeatureMapLayer.DimensionDef(
                        CommonCRS.Temporal.JAVA.crs(),
                        GO2Utilities.FILTER_FACTORY.property(DATE_DEBUT_FIELD),
                        GO2Utilities.FILTER_FACTORY.property(DATE_FIN_FIELD)
                );
                fml.getExtraDimensions().add(datefilter);
            }
            fml.setVisible(visible);
            fml.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);

            final String str = nameMap.get(name.tip().toString());
            fml.setName(str!=null ? str : name.tip().toString());

            if(selectionStyle!=null) fml.setSelectionStyle(selectionStyle);

            layers.add(fml);
            i++;
            this.attributeLayer(fml, name.tip().toString());
        }
        return layers;
    }
}
