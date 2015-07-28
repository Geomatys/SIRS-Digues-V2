package fr.sirs.plugin.aot.cot;

import static fr.sirs.CorePlugin.createDefaultSelectionStyle;
import static fr.sirs.CorePlugin.createDefaultStyle;
import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.DATE_DEBUT_FIELD;
import static fr.sirs.SIRS.DATE_FIN_FIELD;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import static fr.sirs.core.ModuleDescription.getLayerDescription;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ConventionRepository;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.PositionConvention;
import fr.sirs.core.model.sql.AotCotSqlHelper;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.ui.AlertItem;
import fr.sirs.ui.AlertManager;
import fr.sirs.util.SirsStringConverter;
import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.RandomStyleBuilder;
import org.opengis.util.GenericName;

/**
 * Plugin correspondant au module AOT COT.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginAotCot extends Plugin {
    private static final String NAME = "plugin-aot-cot";
    private static final String TITLE = "Module AOT COT";
    
    private final ConsultationAotCotTheme consultationAotCotTheme;

    public PluginAotCot() {
        name = NAME;
        loadingMessage.set("module AOT COT");
        themes.add(new SuiviAotCotTheme());
        consultationAotCotTheme = new ConsultationAotCotTheme();
        themes.add(consultationAotCotTheme);
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
        loadDataSuppliers();
        showAlerts();
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
    
    private final HashMap<Class, BeanFeatureSupplier> suppliers = new HashMap<>();
    
    private synchronized void loadDataSuppliers() {
        suppliers.clear();
        suppliers.put(PositionConvention.class, new StructBeanSupplier(PositionConvention.class, () -> getSession().getRepositoryForClass(PositionConvention.class).getAll()));
    }
    
    @Override
    public void afterImport() throws Exception {
        if (suppliers.isEmpty()) {
            loadDataSuppliers();
        }
        
        // getLayerDescription itère sur les éléments des FeatureCollections des 
        // couches, ce qui a pour effet de créer les vues.
        for(final MapItem item : getMapItems()) getLayerDescription(item);
    }
    
    
    @Override
    public List<MapItem> getMapItems() {
        final List<MapItem> items = new ArrayList<>();
        try{
            // Positionnement des conventions
            final BeanStore documentsStore = new BeanStore(suppliers.get(PositionConvention.class));
            final MapItem documentsLayer = MapBuilder.createItem();
            documentsLayer.setName(TITLE);
            documentsLayer.items().addAll(buildLayers(documentsStore, createDefaultSelectionStyle(),false) );
            documentsLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            items.add(documentsLayer); 
        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        return items;
    }
    
    private List<MapLayer> buildLayers(BeanStore store, MutableStyle selectionStyle, boolean visible) throws DataStoreException{
        final List<MapLayer> layers = new ArrayList<>();
        final org.geotoolkit.data.session.Session symSession = store.createSession(false);
        int i=0;
        for(GenericName name : store.getNames()){
            final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
            if(col.getFeatureType()!=null){
                final MutableStyle baseStyle = createDefaultStyle(Color.ORANGE);
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

                fml.setName(LabelMapper.get(Convention.class).mapClassName());

                if(selectionStyle!=null) fml.setSelectionStyle(selectionStyle);

                layers.add(fml);
                i++;
            }
        }
        return layers;
    }

    @Override
    public List<MenuItem> getMapActions(Object obj) {
        final List<MenuItem> lst = new ArrayList<>();
        
        if(obj instanceof ObjetReseau) {
            lst.add(new ViewFormReseauItem((ObjetReseau) obj));
        }
        return lst;
    }

    @Override
    public SQLHelper getSQLHelper() {
        return AotCotSqlHelper.getInstance();
    }
    
    private class ViewFormReseauItem extends MenuItem {

        public ViewFormReseauItem(ObjetReseau candidate) {
            setText("Consulter les conventions de "+getSession().generateElementTitle(candidate));
            setOnAction((ActionEvent event) -> {
                consultationAotCotTheme.setReseauToConsultFromMap(candidate);
                getSession().getFrame().addTab(getSession().getOrCreateThemeTab(consultationAotCotTheme));
            });
        }
    }
    
    /**
     * Inutilisé pour le moment
     */
    private class ViewFormObjectOfPositionConventionItem extends MenuItem {

        public ViewFormObjectOfPositionConventionItem(PositionConvention candidate, ObjetReseau reseau) {
            setText("Consulter l'objet de la convention "+getSession().generateElementTitle(candidate));

            setOnAction((ActionEvent event) -> {
                getSession().showEditionTab(reseau);
            });
        }
    }
    
    public static PojoTable getConventionsForReseau(final ObjetReseau reseau){
        final ConventionRepository conventionRepo = (ConventionRepository) Injector.getSession().getRepositoryForClass(Convention.class);
                
        final List<Convention> conventionsLiees = conventionRepo.getByReseau(reseau);

        final PojoTable table = new PojoTable(Convention.class, "Conventions de l'élément de réseau "+reseau.getDesignation());
        table.setTableItems(() -> (ObservableList) FXCollections.observableList(conventionsLiees));
        table.editableProperty().set(false);
        table.fichableProperty().set(false);
        return table;
    }
    
    /**
     * Récupère les alertes à afficher pour l'utilisateur, selon les dates fournies dans les obligations réglementaires
     * et la fréquence de rappel.
     */
    public static void showAlerts() {
        final List<AlertItem> alerts = new ArrayList<>();

        final AbstractSIRSRepository<Convention> orr = Injector.getSession().getRepositoryForClass(Convention.class);
        final List<Convention> obligations = orr.getAll();
        if (obligations.isEmpty()) {
            AlertManager.getInstance().addAlerts(alerts);
            return;
        }

        for (final Convention obligation : obligations) {
            if (obligation.getDate_fin()== null) {
                continue;
            }

            final StringBuilder sb = new StringBuilder();
            sb.append(new SirsStringConverter().toString(obligation));
            
//            if (obligation.getDate_fin().compareTo(LocalDate.now())<0)
//                alerts.add(new AlertItem(sb.toString(), obligation.getDate_fin(), AlertItem.AlertItemLevel.HIGHT));
//            else 
                if(obligation.getDate_fin().minusMonths(6).compareTo(LocalDate.now())<0
                        && obligation.getDate_fin().compareTo(LocalDate.now())>=0) // On ne veut pas d'alerte pour les conventions dont la date de fin est déjà dépassée
                alerts.add(new AlertItem(sb.toString(), obligation.getDate_fin()));
//            else 
//                alerts.add(new AlertItem(sb.toString(), obligation.getDate_fin(), AlertItem.AlertItemLevel.INFORMATION));
        }

        AlertManager.getInstance().addAlerts(alerts);
    }
}

