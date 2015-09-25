/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.objet;import org.springframework.stereotype.Component;

import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.mapper.AbstractMapper;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public abstract class AbstractObjetMapper<T extends Objet> extends AbstractMapper<T> {

    protected final AbstractImporter<RefPosition> RefPositionImporter;
    protected final AbstractImporter<RefSource> RefSourceImporter;

    protected AbstractObjetMapper(Table table) {
        super(table);
        RefPositionImporter = context.importers.get(RefPosition.class);
        RefSourceImporter = context.importers.get(RefSource.class);
    }

}
