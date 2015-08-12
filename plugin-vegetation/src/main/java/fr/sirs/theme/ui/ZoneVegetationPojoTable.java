package fr.sirs.theme.ui;

import fr.sirs.core.model.ZoneVegetation;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ZoneVegetationPojoTable extends ListeningPojoTable<String> {

    public ZoneVegetationPojoTable(String title) {
        super(ZoneVegetation.class, title);
    }
    
}
