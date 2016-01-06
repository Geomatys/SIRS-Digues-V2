package fr.sirs.core.model.report;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.util.odt.ODTUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterators;
import org.geotoolkit.data.FeatureIterator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Used for printing brut table reports.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TableSectionRapport extends AbstractSectionRapport {

    @Override
    public Element copy() {
        final TableSectionRapport rapport = ElementCreator.createAnonymValidElement(TableSectionRapport.class);
        super.copy(rapport);
        return rapport;
    }

    @Override
    public boolean removeChild(Element toRemove) {
        return false;
    }

    @Override
    public boolean addChild(Element toAdd) {
        return false;
    }

    @Override
    public Element getChildById(String toSearch) {
        if (toSearch != null && toSearch.equals(getId()))
            return this;
        return null;
    }

    @Override
    protected void printSection(final PrintContext ctx) throws Exception {
        final List<String> properties = ctx.propertyNames == null? null : new ArrayList<>(ctx.propertyNames);
        if (ctx.elements != null) {
            ODTUtils.appendTable(ctx.target, Spliterators.iterator(ctx.elements.spliterator()), properties);
        } else if (ctx.filterValues != null) {
            try (final FeatureIterator it = ctx.filterValues.iterator()) {
                ODTUtils.appendTable(ctx.target, it, properties);
            }
        }
    }
}
