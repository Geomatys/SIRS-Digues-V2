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
package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Organisme;
import static fr.sirs.importer.DbImporter.cleanNullString;
import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class OrganismeMapperSpi extends GenericMapperSpi<Organisme> {

    private enum Columns {
        RAISON_SOCIALE,
        STATUT_JURIDIQUE,
        ADRESSE_L1_ORG,
        ADRESSE_L2_ORG,
        ADRESSE_L3_ORG,
        ADRESSE_CODE_POSTAL_ORG,
        ADRESSE_NOM_COMMUNE_ORG,
        TEL_ORG,
        MAIL_ORG,
        FAX_ORG
    }

    private final HashMap<String, String> bindings;
    public OrganismeMapperSpi() throws IntrospectionException {
        super(Organisme.class);

        bindings = new HashMap<>(9);
        bindings.put(Columns.RAISON_SOCIALE.name(), "nom");
        bindings.put(Columns.STATUT_JURIDIQUE.name(), "statutJuridique");
        bindings.put(Columns.ADRESSE_CODE_POSTAL_ORG.name(), "codePostal");
        bindings.put(Columns.ADRESSE_NOM_COMMUNE_ORG.name(), "commune");
        bindings.put(Columns.TEL_ORG.name(), "telephone");
        bindings.put(Columns.MAIL_ORG.name(), "email");
        bindings.put(Columns.FAX_ORG.name(), "fax");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

    @Override
    protected Collection<BiConsumer<Row, Organisme>> getExtraBindings() {
        return Collections.singleton((input, output) -> {
            output.setAdresse(cleanNullString(input.getString(Columns.ADRESSE_L1_ORG.name()))
                    + cleanNullString(input.getString(Columns.ADRESSE_L2_ORG.name()))
                    + cleanNullString(input.getString(Columns.ADRESSE_L3_ORG.name())));
        });
    }
}
