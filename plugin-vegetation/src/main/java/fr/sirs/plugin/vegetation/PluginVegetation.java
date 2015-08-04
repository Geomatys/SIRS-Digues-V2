package fr.sirs.plugin.vegetation;

import fr.sirs.Plugin;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.model.ArbreVegetation;
import fr.sirs.core.model.HerbaceeVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.core.model.sql.VegetationSqlHelper;
import fr.sirs.plugin.vegetation.map.CreateParcelleTool;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import fr.sirs.map.FXMapPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.sld.xml.Specification;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.MutableStyle;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginVegetation extends Plugin {
    private static final String NAME = "plugin-vegetation";
    private static final String TITLE = "Module végétation";

    private final VegetationToolBar toolbar = new VegetationToolBar();

    public PluginVegetation() {
        name = NAME;
        loadingMessage.set("module végétation");
        themes.add(new ButtonExampleTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();

        //on force le chargement
        final CreateParcelleTool.Spi spi = CreateParcelleTool.SPI;
        spi.getTitle().toString();
        spi.getAbstract().toString();

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
        return Collections.singletonList(toolbar);
    }

    @Override
    public SQLHelper getSQLHelper() {
        return VegetationSqlHelper.getInstance();
    }

    @Override
    public List<MapItem> getMapItems() {
        final List<MapItem> items = new ArrayList<>();
        final MapItem vegetationGroup = MapBuilder.createItem();
        vegetationGroup.setName("Végétation");
        items.add(vegetationGroup);

        try{
            //parcelles
            final StructBeanSupplier parcelleSupplier = new StructBeanSupplier(ParcelleVegetation.class, () -> getSession().getRepositoryForClass(ParcelleVegetation.class).getAll());
            final BeanStore parcelleStore = new BeanStore(parcelleSupplier);
            final MapLayer parcelleLayer = MapBuilder.createFeatureLayer(parcelleStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(parcelleStore.getNames().iterator().next())));
            parcelleLayer.setName("Parcelles");
            parcelleLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,parcelleLayer);

            try{
                final MutableStyle style = new StyleXmlIO().readStyle(Thread.currentThread().getContextClassLoader()
                        .getResource("/fr/sirs/plugin/vegetation/style/parcelle.xml"), Specification.SymbologyEncoding.V_1_1_0);
                parcelleLayer.setStyle(style);
            }catch(Exception ex){
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }

            //strates herbacée
            final StructBeanSupplier herbeSupplier = new StructBeanSupplier(HerbaceeVegetation.class, () -> getSession().getRepositoryForClass(HerbaceeVegetation.class).getAll());
            final BeanStore herbeStore = new BeanStore(herbeSupplier);
            final MapLayer herbeLayer = MapBuilder.createFeatureLayer(herbeStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(herbeStore.getNames().iterator().next())));
            herbeLayer.setName("Strates herbacée");
            herbeLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,herbeLayer);

            //arbres
            final StructBeanSupplier arbreSupplier = new StructBeanSupplier(ArbreVegetation.class, () -> getSession().getRepositoryForClass(ArbreVegetation.class).getAll());
            final BeanStore arbreStore = new BeanStore(arbreSupplier);
            final MapLayer arbreLayer = MapBuilder.createFeatureLayer(arbreStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(arbreStore.getNames().iterator().next())));
            arbreLayer.setName("Arbres exceptionnels");
            arbreLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,arbreLayer);

            //peuplements
            final MapItem peuplementGroup = MapBuilder.createItem();
            peuplementGroup.setName("Peuplements");
            vegetationGroup.items().add(0,peuplementGroup);

            //invasives
            final MapItem invasivesGroup = MapBuilder.createItem();
            invasivesGroup.setName("Invasives");
            vegetationGroup.items().add(0,invasivesGroup);

            //traitements
            final MapItem traitementGroup = MapBuilder.createItem();
            traitementGroup.setName("Traitements");
            vegetationGroup.items().add(0,traitementGroup);

        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        return items;
    }
    
}
