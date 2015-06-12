
package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.logging.Level;
import javafx.concurrent.Task;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArraysExt;
import org.ektorp.DocumentNotFoundException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreRuntimeException;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.data.FileFeatureStoreFactory;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.data.memory.WrapFeatureCollection;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.feature.Attribute;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.feature.Property;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.internal.Loggers;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.storage.FactoryMetadata;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.opengis.geometry.Geometry;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ExportTask extends Task<Boolean> {
    private final FeatureMapLayer layer;
    private final FileFeatureStoreFactory factory;
    private final File folder;

    public ExportTask(FeatureMapLayer layer, File folder, FileFeatureStoreFactory factory) {
        updateTitle("Export de " + layer.getName());
        this.folder = folder;
        this.factory = factory;
        this.layer = layer;
    }

    @Override
    protected Boolean call() throws Exception {
        try {
            final FeatureCollection baseCol = new FillCoordCollection(layer.getCollection());
            final FeatureType baseType = baseCol.getFeatureType();
            final FactoryMetadata metadata = factory.getMetadata();
            final Class<Geometry>[] supportedGeometryTypes = metadata.supportedGeometryTypes();
            //detect if we need one or multiple types.
            final FeatureCollection[] cols;
            if (baseType.getGeometryDescriptor()==null || ArraysExt.contains(supportedGeometryTypes, baseType.getGeometryDescriptor().getType().getBinding())) {
                cols = new FeatureCollection[]{baseCol};
            } else {
                //split the feature collection in sub geometry types
                cols = FeatureStoreUtilities.decomposeByGeometryType(baseCol, supportedGeometryTypes);
            }
            //transforme each collection
            //replace ids by libellé when possible
            for (int i = 0; i < cols.length; i++) {
                final Previews previews = Injector.getSession().getPreviews();
                cols[i] = new WrapFeatureCollection(cols[i]) {
                    @Override
                    protected Feature modify(Feature ftr) throws FeatureStoreRuntimeException {
                        if(ftr instanceof BeanFeature){
                            //do not modify the real bean
                            ftr = FeatureUtilities.copy(ftr);
                        }

                        for (Property p : ftr.getProperties()) {
                            if (p instanceof Attribute) {
                                final Class<?> binding = p.getType().getBinding();
                                if (String.class.isAssignableFrom(binding)) {
                                    final String value = String.valueOf(p.getValue());
                                    try {
                                        final Preview lbl = previews.get(value);
                                        if (lbl != null) {
                                            p.setValue(lbl.getLibelle());
                                        }
                                    } catch (DocumentNotFoundException e) {
                                        SirsCore.LOGGER.log(Level.FINEST, "No document found for id : {0}", value);
                                    }
                                }
                            }
                        }
                        return ftr;
                    }
                };
            }
            for (FeatureCollection col : cols) {
                if (col.isEmpty()) {
                    continue;
                }
                final FeatureType inType = col.getFeatureType();
                final String inTypeName = inType.getName().getLocalPart();
                //output file path
                File file = new File(folder, inTypeName + factory.getFileExtensions()[0]);
                //if file exist, add date aside it
                if (file.exists()) {
                    //generate name + time
                    String name = inTypeName + " " + TemporalUtilities.toISO8601(new Date()) + factory.getFileExtensions()[0];
                    name = name.replace(':', '_');
                    file = new File(folder, name);
                    //it should not exist, but delete it if there is one in case
                    file.delete();
                }
                //create output store
                final FeatureStore store = factory.createDataStore(file.toURI().toURL());
                //delete feature types
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
            done();
        } catch (MalformedURLException | DataStoreException ex) {
            Loggers.DATA.log(Level.WARNING, ex.getMessage(), ex);
            setException(ex);
            return false;
        }
        return true;
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
