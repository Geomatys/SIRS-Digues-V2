
package fr.sym.store;

import fr.sym.Session;
import java.util.Collections;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.geotoolkit.data.AbstractFeatureStoreFactory;
import static org.geotoolkit.data.AbstractFeatureStoreFactory.NAMESPACE;
import static org.geotoolkit.data.AbstractFeatureStoreFactory.createFixedIdentifier;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.storage.DataType;
import org.geotoolkit.storage.DefaultFactoryMetadata;
import org.geotoolkit.storage.FactoryMetadata;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class SymadremStoreFactory extends AbstractFeatureStoreFactory{
    
    /** factory identification **/
    public static final String NAME = "symadrem-couchdb";
    public static final DefaultServiceIdentification IDENTIFICATION;
    static {
        IDENTIFICATION = new DefaultServiceIdentification();
        final Identifier id = new DefaultIdentifier(NAME);
        final DefaultCitation citation = new DefaultCitation(NAME);
        citation.setIdentifiers(Collections.singleton(id));
        IDENTIFICATION.setCitation(citation);
    }
    
    public static final ParameterDescriptor<String> IDENTIFIER = createFixedIdentifier(NAME);
    
    public static final ParameterDescriptor<Session> SESSION = createDescriptor("session",
                    new SimpleInternationalString("session"),
                    new SimpleInternationalString("session"),
                    Session.class,null,null,null,null,null,true);
    
    public static final ParameterDescriptor<CoordinateReferenceSystem> CRS = createDescriptor("crs",
                    new SimpleInternationalString("crs"),
                    new SimpleInternationalString("crs"),
                    CoordinateReferenceSystem.class,null,null,null,null,null,true);
    
    public static final ParameterDescriptorGroup PARAMETERS_DESCRIPTOR =
            new DefaultParameterDescriptorGroup("SymadremParameters",
                IDENTIFIER,NAMESPACE,SESSION,CRS);
    
    
    @Override
    public Identification getIdentification() {
        return IDENTIFICATION;
    }
    
    @Override
    public ParameterDescriptorGroup getParametersDescriptor() {
        return PARAMETERS_DESCRIPTOR;
    }

    @Override
    public FeatureStore open(ParameterValueGroup params) throws DataStoreException {
        checkCanProcessWithError(params);
        return new SymadremStore(params);
    }

    @Override
    public FeatureStore create(ParameterValueGroup params) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public FactoryMetadata getMetadata() {
        return new DefaultFactoryMetadata(DataType.VECTOR, true);
    }
    
}
