/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.Positionable;
import fr.sirs.theme.ui.FXPositionablePane;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArraysExt;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FeatureStoreRuntimeException;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.data.FileFeatureStoreFactory;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.data.memory.WrapFeatureCollection;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.contexttree.TreeMenuItem;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.internal.Loggers;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.storage.FactoryMetadata;
import org.opengis.geometry.Geometry;

/**
 * Export selected layer in the context tree.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class ExportMenu extends TreeMenuItem {

    private static final Image ICON = SwingFXUtils.toFXImage(
            IconBuilder.createImage(FontAwesomeIcons.ICON_DOWNLOAD, 16, FontAwesomeIcons.DEFAULT_COLOR), null);
    
    private final Map<FileChooser.ExtensionFilter, FileFeatureStoreFactory> index = new HashMap<>();
    private WeakReference<TreeItem> itemRef;
    
    public ExportMenu() {
        
        menuItem = new Menu(GeotkFX.getString(org.geotoolkit.gui.javafx.contexttree.menu.ExportItem.class,"export"));
        menuItem.setGraphic(new ImageView(ICON));
        
        //select file factories which support writing
        final Set<FileFeatureStoreFactory> factories = FeatureStoreFinder.getAvailableFactories(FileFeatureStoreFactory.class);
        for(final FileFeatureStoreFactory factory : factories){
            final FactoryMetadata metadata = factory.getMetadata();
            if(metadata.supportStoreCreation() 
                    && metadata.supportStoreWriting() 
                    && metadata.supportedGeometryTypes().length>0){
                final String[] exts = factory.getFileExtensions();
                final String name = factory.getDisplayName().toString();
                final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(name, exts);
                index.put(filter, factory);
                
                ((Menu)menuItem).getItems().add(new ExportMenuItem(factory));
            }
        }
        
    }

    @Override
    public MenuItem init(List<? extends TreeItem> selection) {
        boolean valid = uniqueAndType(selection,FeatureMapLayer.class) && !index.isEmpty();
        if(valid && selection.get(0).getParent()!=null){
            itemRef = new WeakReference<>(selection.get(0));
            return menuItem;
        }
        return null;
    }
    
    private class ExportMenuItem extends MenuItem {

        public ExportMenuItem(FileFeatureStoreFactory factory) {
            super(factory.getDisplayName().toString());
            
            setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent event) {
                    if(itemRef == null) return;                
                    final TreeItem treeItem = itemRef.get();
                    if(treeItem == null) return;
                    
                    final FeatureMapLayer layer = (FeatureMapLayer) treeItem.getValue();

                    final DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle(GeotkFX.getString(org.geotoolkit.gui.javafx.contexttree.menu.ExportItem.class, "folder"));
                    
                    final File folder = chooser.showDialog(null);

                    if(folder!=null){                    
                        try {
                            final FeatureCollection baseCol = new FillCoordCollection(layer.getCollection());
                            final FeatureType baseType = baseCol.getFeatureType();

                            final FactoryMetadata metadata = factory.getMetadata();
                            final Class<Geometry>[] supportedGeometryTypes = metadata.supportedGeometryTypes();

                            //detect if we need one or multiple types.
                            final FeatureCollection[] cols;
                            if(ArraysExt.contains(supportedGeometryTypes,baseType.getGeometryDescriptor().getType().getBinding()) ){
                                cols = new FeatureCollection[]{baseCol};
                            }else{
                                //split the feature collection in sub geometry types
                                cols = FeatureStoreUtilities.decomposeByGeometryType(baseCol, supportedGeometryTypes);
                            }

                            for(FeatureCollection col : cols){

                                final FeatureType inType = col.getFeatureType();
                                final String inTypeName = inType.getName().getLocalPart();
                                
                                //output file path
                                final File file= new File(folder, inTypeName+factory.getFileExtensions()[0]);

                                //create output store
                                final FeatureStore store = factory.createDataStore(file.toURI().toURL());

                                //create output type
                                store.createFeatureType(inType.getName(), inType);
                                final FeatureType outType = store.getFeatureType(inTypeName);
                                final Name outName = outType.getName();

                                //write datas
                                final Session session = store.createSession(false);
                                session.addFeatures(outName, col);

                                //close store
                                store.close();
                            }

                        } catch (MalformedURLException | DataStoreException ex) {
                            Loggers.DATA.log(Level.WARNING, ex.getMessage(),ex);
                            final Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
                            alert.showAndWait();
                        }

                    }
                }
            });
            
        }
        
    }
    
    private static class FillCoordCollection extends WrapFeatureCollection{
        
        public FillCoordCollection(FeatureCollection originalFC) {
            super(originalFC);
        }

        @Override
        protected Feature modify(Feature feature) throws FeatureStoreRuntimeException {
            final Object baseBean = feature.getUserData().get(BeanFeature.KEY_BEAN);
            
            if(baseBean instanceof Positionable){
                final Positionable pos = (Positionable) baseBean;
                final TronconUtils.PosInfo info = new TronconUtils.PosInfo(pos,Injector.getSession());
                
                try{
                    //on calcul les informations au besoin
                    feature.setPropertyValue("positionDebut", info.getGeoPointStart());
                    feature.setPropertyValue("positionFin", info.getGeoPointEnd());
                    final TronconUtils.PosSR possr = info.getForSR();
                    feature.setPropertyValue("systemeRepId", possr.srid);
                    feature.setPropertyValue("borneDebutId", possr.borneStartId);
                    feature.setPropertyValue("borne_debut_distance", (float)possr.distanceStartBorne);
//                    feature.setPropertyValue("positionFin", possr.startAval);
                    feature.setPropertyValue("borneFinId", possr.borneEndId);
                    feature.setPropertyValue("borne_fin_distance", (float)possr.distanceEndBorne);
//                    feature.setPropertyValue("positionFin", possr.endAval);
                }catch(Exception ex){
                    SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                }
            }
            
            return feature;
        }
        
    }
    
}
