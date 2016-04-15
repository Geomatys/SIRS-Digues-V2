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
 * Verify all importers and repositories are registered into Spring context, and
 * all element types are listed into service loader.
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
        checkSpringComponents(Element.class, pFilter);
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
