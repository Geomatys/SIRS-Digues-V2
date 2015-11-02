package fr.sirs;

import fr.sirs.core.model.Element;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ElementModifier;
import fr.sirs.importer.v2.Linker;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.ektorp.support.CouchDbRepositorySupport;
import org.junit.Test;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class RegistrationTest extends ServiceRegistrationTestBase {

    @Test
    public void RepositoryTest() throws Exception {
        final Pattern pat = Pattern.compile("fr\\.sirs.*");
        final Predicate<Package> pFilter = p -> pat.matcher(p.getName()).matches();
        checkSpringComponents(CouchDbRepositorySupport.class, pFilter);
    }

    @Test
    public void ElementTest() throws Exception {
        final Pattern pat = Pattern.compile("fr\\.sirs.*");
        final Predicate<Package> pFilter = p -> pat.matcher(p.getName()).matches();
        checkServiceLoading(Element.class, pFilter);
    }

    @Test
    public void ImportTest() throws Exception {
        final Pattern pat = Pattern.compile("fr\\.sirs\\.importer.*");
        final Predicate<Package> pFilter = p -> pat.matcher(p.getName()).matches();

        checkSpringComponents(AbstractImporter.class, pFilter);
        checkSpringComponents(Linker.class, pFilter);
        checkSpringComponents(MapperSpi.class, pFilter);
        checkSpringComponents(ElementModifier.class, pFilter);
    }
}
