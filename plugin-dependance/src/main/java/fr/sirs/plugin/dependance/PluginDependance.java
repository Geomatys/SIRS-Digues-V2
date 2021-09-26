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

import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AireStockageDependance;
import fr.sirs.core.model.AmenagementHydraulique;
import fr.sirs.core.model.AutreDependance;
import fr.sirs.core.model.CheminAccesDependance;
import fr.sirs.core.model.DesordreDependance;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.OuvrageVoirieDependance;
import fr.sirs.core.model.TraitAmenagementHydraulique;
import fr.sirs.core.model.PrestationAmenagementHydraulique;
import fr.sirs.core.model.StructureAmenagementHydraulique;
import fr.sirs.core.model.OuvrageAssocieAmenagementHydraulique;
import fr.sirs.core.model.OrganeProtectionCollective;
import fr.sirs.map.FXMapPane;
import fr.sirs.plugin.dependance.map.DependanceToolBar;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;

/**
 * Plugin correspondant au module dépendance.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Maxime Gavens (Geomatys)
 */
public class PluginDependance extends Plugin {
    private static final String NAME = "plugin-dependance";
    private static final String TITLE = "Module dépendance";

    //doit avoir la meme valeur que dans le fichier Berge.properties classPlural
    public static final String LAYER_AH_NAME = "Aménagements hydrauliques";
    public static final String LAYER_TRAIT_NAME = "Traits d'aménagement hydraulique";

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
        loadingMessage.set("module dépendance");
        themes.add(new DependancesTheme());
        themes.add(new DescriptionDependanceAHTheme());
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
        return null;
    }

    @Override
    public List<ToolBar> getMapToolBars(final FXMapPane mapPane) {
        return Collections.singletonList(new DependanceToolBar(mapPane.getUiMap()));
    }

    @Override
    public List<MapItem> getMapItems() {
        final List<MapItem> items = new ArrayList<>();
        final MapItem depGroup = MapBuilder.createItem();

        depGroup.setName("Dépendances");
        depGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
        ouvrageVoirieLayer = buildMapLayerDependance(OuvrageVoirieDependance.class, LabelMapper.get(OuvrageVoirieDependance.class).mapClassName());
        traitAmenagementLayer = buildMapLayerDependance(TraitAmenagementHydraulique.class, LAYER_TRAIT_NAME);
        cheminLayer = buildMapLayerDependance(CheminAccesDependance.class, LabelMapper.get(CheminAccesDependance.class).mapClassName());
        autreLayer = buildMapLayerDependance(AutreDependance.class, LabelMapper.get(AutreDependance.class).mapClassName());
        aireLayer = buildMapLayerDependance(AireStockageDependance.class, LabelMapper.get(AireStockageDependance.class).mapClassName());
        amenagementLayer = buildMapLayerDependance(AmenagementHydraulique.class, LAYER_AH_NAME);
        desordreLayer = buildMapLayerDependance(DesordreDependance.class, LabelMapper.get(DesordreDependance.class).mapClassName());
        prestationLayer = buildMapLayerDependance(PrestationAmenagementHydraulique.class, LabelMapper.get(PrestationAmenagementHydraulique.class).mapClassName());
        structureLayer = buildMapLayerDependance(StructureAmenagementHydraulique.class, LabelMapper.get(StructureAmenagementHydraulique.class).mapClassName());
        ouvrageAssocieLayer = buildMapLayerDependance(OuvrageAssocieAmenagementHydraulique.class, LabelMapper.get(OuvrageAssocieAmenagementHydraulique.class).mapClassName());
        organeProtectionLayer = buildMapLayerDependance(OrganeProtectionCollective.class, LabelMapper.get(OrganeProtectionCollective.class).mapClassName());
        depGroup.items().add(ouvrageVoirieLayer);
        depGroup.items().add(traitAmenagementLayer);
        depGroup.items().add(cheminLayer);
        depGroup.items().add(autreLayer);
        depGroup.items().add(aireLayer);
        depGroup.items().add(amenagementLayer);
        depGroup.items().add(desordreLayer);
        depGroup.items().add(prestationLayer);
        depGroup.items().add(structureLayer);
        depGroup.items().add(ouvrageAssocieLayer);
        depGroup.items().add(organeProtectionLayer);
        items.add(depGroup);
        return items;
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

    private FeatureMapLayer buildMapLayerDependance(final Class clazz, final String name) {
        try {
            final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(clazz);
            final StructBeanSupplier supplier = new StructBeanSupplier(clazz, repo::getAll);
            final BeanStore store = new BeanStore(supplier);
            final FeatureMapLayer layer = MapBuilder.createFeatureLayer(store.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(store.getNames().iterator().next())));
            layer.setName(name);
            layer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            return layer;
        } catch(Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }
    }
}
