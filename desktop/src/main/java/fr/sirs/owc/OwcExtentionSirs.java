package fr.sirs.owc;

import fr.sirs.Plugin;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.JAXBElement;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.owc.xml.v10.ContentType;
import org.geotoolkit.owc.xml.v10.OfferingType;
import org.geotoolkit.owc.xml.v10.StyleSetType;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.sld.xml.v110.UserStyle;
import org.geotoolkit.style.MutableStyle;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class OwcExtentionSirs implements OwcExtension {
    
    public static final String CODE = "http://www.france-digues.fr/owc";
    private static final org.geotoolkit.owc.xml.v10.ObjectFactory OWC_FACTORY = new org.geotoolkit.owc.xml.v10.ObjectFactory();

    private static final List<MapItem> mapItems = new ArrayList();
    
    
    
    static {
        final Iterator<Plugin> ite = ServiceRegistry.lookupProviders(Plugin.class);
//            final List<Plugin> candidates = new ArrayList<>();
            while(ite.hasNext()){
                mapItems.addAll(ite.next().getMapItems());
//                candidates.add(ite.next());
            }    
            System.out.println(mapItems);
    }
    
    
    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public MapLayer createLayer(OfferingType offering) {
//        System.out.println(mapItems);
        final String type = fromContent(offering);
        if(type!=null){
            for(final MapItem mapItem : mapItems){
                if(type.equals(mapItem.getName()) 
                        && mapItem instanceof MapLayer){
                    return (MapLayer) mapItem;
                }
                else{
                    System.out.println("Autre item : "+mapItem.getName());
                }
            }
        }
        return null;
    }
    
    private static String fromContent(final OfferingType offering){
        final List<Object> offeringContent = offering.getOperationOrContentOrStyleSet();
        for(final Object o : offeringContent){
            if(o instanceof JAXBElement 
                    && ((JAXBElement)o).getValue() instanceof ContentType){
                return ((ContentType) ((JAXBElement)o).getValue()).getType();
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    @Override
    public OfferingType createOffering(MapLayer mapLayer) {
        final OfferingType offering = OWC_FACTORY.createOfferingType();
        offering.setCode(CODE);
        if(mapLayer.getName()!=null){
            offering.getOperationOrContentOrStyleSet().add(OWC_FACTORY.createOfferingTypeContent(toContent(mapLayer)));
        }
        if(mapLayer.getStyle()!=null){
            offering.getOperationOrContentOrStyleSet().add(OWC_FACTORY.createOfferingTypeStyleSet(toStyleSet(mapLayer.getStyle(), false)));
        }
        if(mapLayer.getSelectionStyle()!=null){
            offering.getOperationOrContentOrStyleSet().add(OWC_FACTORY.createOfferingTypeStyleSet(toStyleSet(mapLayer.getStyle(), true)));
        }
        return offering;
    }
    
    private static StyleSetType toStyleSet(final MutableStyle style, final boolean selection){
        final StyleSetType styleSet = OWC_FACTORY.createStyleSetType();
        styleSet.setDefault(selection);
        
        styleSet.getNameOrTitleOrAbstract().add(OWC_FACTORY.createStyleSetTypeContent(toContent(style)));
        
        return styleSet;
    }
    
    private static ContentType toContent(final MutableStyle style){
        final ContentType content = OWC_FACTORY.createContentType();
        
        StyleXmlIO io = new StyleXmlIO();
        UserStyle jaxbStyle = io.getTransformerXMLv110().visit(style, null);
        content.getContent().add(jaxbStyle);
        return content;
    }
    
    private static ContentType toContent(final MapLayer mapLayer){
        final ContentType content = OWC_FACTORY.createContentType();
        content.setType(mapLayer.getName());
        return content;
    }
    
}
