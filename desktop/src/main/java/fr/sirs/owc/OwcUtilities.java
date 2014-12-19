package fr.sirs.owc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.xml.MarshallerPool;
import org.geotoolkit.georss.xml.v100.WhereType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.owc.xml.v10.ContentType;
//import org.geotoolkit.owc.xml.v10.ContentType;
import org.geotoolkit.owc.xml.v10.OfferingType;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.opengis.util.FactoryException;
//import org.w3._2005.atom.ContentType;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;
import org.w3._2005.atom.LinkType;
import org.w3._2005.atom.TextType;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class OwcUtilities {
    
    private static final org.w3._2005.atom.ObjectFactory ATOM_FACTORY = new org.w3._2005.atom.ObjectFactory();
    private static final org.geotoolkit.owc.xml.v10.ObjectFactory OWC_FACTORY = new org.geotoolkit.owc.xml.v10.ObjectFactory();
    private static final org.geotoolkit.georss.xml.v100.ObjectFactory GEORSS_FACTORY = new org.geotoolkit.georss.xml.v100.ObjectFactory();
    private static final OwcExtension OWC_EXTENSION = new OwcExtentionSirs();
    
    public static void toOwc(final OutputStream outputStream, final MapContext mapContext) 
            throws JAXBException, FileNotFoundException, FactoryException{
        
        final JAXBContext jaxbCtxt = JAXBContext.newInstance(
                "org.geotoolkit.owc.xml.v10"
                        + ":org.w3._2005.atom"
                        + ":org.geotoolkit.georss.xml.v100"
                        + ":org.geotoolkit.gml.xml.v311"
                        + ":org.geotoolkit.sld.xml.v110"
                        + ":org.apache.sis.internal.jaxb.geometry"
                        + ":org.geotoolkit.wms.xml.v130");
        final MarshallerPool pool = new MarshallerPool(jaxbCtxt, null);
        final Marshaller marsh = pool.acquireMarshaller();
        marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marsh.marshal(toFeed(mapContext), outputStream);
        pool.recycle(marsh);
    }
    
    private static FeedType toFeed(final MapContext mapContext) throws FactoryException{
        final FeedType feed = ATOM_FACTORY.createFeedType();
        
        final LinkType link = ATOM_FACTORY.createLinkType();
        link.setRel("profile");
        link.setHref("http://www.opengis.net/spec/owc-atom/1.0/req/core");
        link.setTitle("SIRS context file OWC compliant.");
        feed.getAuthorOrCategoryOrContributor().add(ATOM_FACTORY.createFeedTypeLink(link));
        
        final TextType title = ATOM_FACTORY.createTextType();
        title.getContent().add("SIRS context file.");
        feed.getAuthorOrCategoryOrContributor().add(ATOM_FACTORY.createFeedTypeTitle(title));
        
        final Integer epsg = IdentifiedObjects.lookupEpsgCode(mapContext.getCoordinateReferenceSystem(), true);
        
        final WhereType where = GEORSS_FACTORY.createWhereType();
        final DirectPositionType lowerCorner = new DirectPositionType(mapContext.getAreaOfInterest().getLowerCorner());
        final DirectPositionType upperCorner = new DirectPositionType(mapContext.getAreaOfInterest().getUpperCorner());
        final EnvelopeType envelopeType = new EnvelopeType(null, 
                lowerCorner, upperCorner, "EPSG:"+epsg);
        envelopeType.setSrsDimension(2);
        where.setEnvelope(envelopeType);
        feed.getAuthorOrCategoryOrContributor().add(GEORSS_FACTORY.createWhere(where));
        
        for(final MapItem mapItem : mapContext.items()){
            feed.getAuthorOrCategoryOrContributor().add(ATOM_FACTORY.createFeedTypeEntry(toEntry(mapItem)));
        }
        return feed;
    }
    
    private static EntryType toEntry(final MapItem mapItem){
        
        final EntryType entry = ATOM_FACTORY.createEntryType();
        if(mapItem instanceof MapLayer){
            entry.getAuthorOrCategoryOrContent().add(OWC_FACTORY.createOffering(toOffering((MapLayer) mapItem)));
        }
        else{
            entry.getAuthorOrCategoryOrContent().add(OWC_FACTORY.createOfferingTypeContent(toContent(mapItem)));
        }
        return entry;
    }
    
    private static OfferingType toOffering(final MapLayer mapLayer){
        
        return OWC_EXTENSION.createOffering(mapLayer);
    }
    
    private static ContentType toContent(final MapItem mapItem){
        final ContentType content = OWC_FACTORY.createContentType();
        content.setType(mapItem.getName());
        for(final MapItem item : mapItem.items()){
            content.getContent().add(ATOM_FACTORY.createEntry(toEntry(item)));
        }
        return content;
    }
    

    
    ////////////////////////////////////////////////////////////////////////////
    
    
    public static MapContext fromOwc(final File file) throws JAXBException{
        
        
        final JAXBContext jaxbCtxt = JAXBContext.newInstance(
                "org.geotoolkit.owc.xml.v10"
                        + ":org.w3._2005.atom"
                        + ":org.geotoolkit.georss.xml.v100"
                        + ":org.geotoolkit.gml.xml.v311"
                        + ":org.geotoolkit.sld.xml.v110"
                        + ":org.apache.sis.internal.jaxb.geometry"
                        + ":org.geotoolkit.wms.xml.v130");
        final MarshallerPool pool = new MarshallerPool(jaxbCtxt, null);
        final Unmarshaller unmarsh = pool.acquireUnmarshaller();
        JAXBElement feed = (JAXBElement) unmarsh.unmarshal(file);
        pool.recycle(unmarsh);
        
        return fromFeed((FeedType) feed.getValue());
    }
    
    private static MapContext fromFeed(final FeedType feed){
        final MapContext mapContext = MapBuilder.createContext();
        
        final List<Object> feedContent = feed.getAuthorOrCategoryOrContributor();
        
        for(final Object o : feedContent){
            if(o instanceof JAXBElement){
                final Object feedContentValue = ((JAXBElement)o).getValue();
                if(feedContentValue instanceof LinkType){
                    final LinkType link = (LinkType) feedContentValue;
                    link.getRel();// "profile" : rien
                    link.getHref();// "http://www.opengis.net/spec/owc-atom/1.0/req/core" : rien
                    link.getTitle();// "SIRS context file OWC compliant." : rien
                }
                else if(feedContentValue instanceof TextType){
                    final TextType title = (TextType) feedContentValue;
                    title.getContent();// "SIRS context file." : Rien
                }
                else if(feedContentValue instanceof WhereType){
                    final WhereType where = (WhereType) feedContentValue;
                    final EnvelopeType envelopeType = where.getEnvelope();
                    
                    mapContext.setAreaOfInterest(envelopeType);
//                    System.out.println(mapContext.getAreaOfInterest().getLowerCorner());
//                    System.out.println(mapContext.getAreaOfInterest().getUpperCorner());
                }
                else if(feedContentValue instanceof EntryType){
                    final EntryType entry = (EntryType) feedContentValue;
                    mapContext.items().add(fromEntry(entry));
                }
//                System.out.println(feedContentValue);
            }
        }
        
        return mapContext;
    }
        
    private static MapItem fromEntry(final EntryType entry){
        final List<Object> entryContent = entry.getAuthorOrCategoryOrContent();
        MapItem mapItem=null;
        if(entryContent.size()==1 && entryContent.get(0) instanceof JAXBElement){
            if((((JAXBElement) entryContent.get(0)).getValue()) instanceof OfferingType){
               mapItem = fromOffering(((OfferingType) ((JAXBElement) entryContent.get(0)).getValue()));
            } else if((((JAXBElement) entryContent.get(0)).getValue()) instanceof ContentType){
               mapItem = fromContent(((ContentType) ((JAXBElement) entryContent.get(0)).getValue()));
            }
        }
        return mapItem;
    }
    
    private static MapLayer fromOffering(final OfferingType offering){
        return OWC_EXTENSION.createLayer(offering);
    }
    
    private static MapItem fromContent(final ContentType content){
        final List<Object> contentContent = content.getContent();
        final MapItem mapItem = MapBuilder.createItem();
        if(content.getType()!=null) mapItem.setName(content.getType());
        for(final Object o : contentContent){
            System.out.println(o.getClass().getCanonicalName());
            mapItem.items().add(fromEntry((EntryType) ((JAXBElement) o).getValue()));
        }
        return mapItem;
    }
}
