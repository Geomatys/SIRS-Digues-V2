package fr.sirs.owc;

import org.geotoolkit.map.MapLayer;
import org.geotoolkit.owc.xml.v10.OfferingType;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface OwcExtension {
    
    String getCode();
    
    MapLayer createLayer(OfferingType offering);
    
    OfferingType createOffering(MapLayer mapLayer);
    
}
