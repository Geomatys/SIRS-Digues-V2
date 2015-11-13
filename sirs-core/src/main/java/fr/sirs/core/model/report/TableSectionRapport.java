package fr.sirs.core.model.report;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.util.odt.ODTUtils;
import java.util.ArrayList;
import java.util.Spliterators;
import org.odftoolkit.odfdom.dom.element.text.TextSectionElement;
import org.odftoolkit.simple.text.Section;

/**
 * Used for printing brut table reports.
 *
 * @author Alexis Manin (Geomatys)
 */
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
        final TextSectionElement element = new TextSectionElement(ctx.target.getContentDom());
        ctx.target.insertOdfElement(ctx.endParagraph.getOdfElement(), ctx.target, element, true);
        ODTUtils.appendTable(
                Section.getInstance(element),
                Spliterators.iterator(ctx.elements.spliterator()), 
                ctx.propertyNames == null? null : new ArrayList<>(ctx.propertyNames));
    }
}
