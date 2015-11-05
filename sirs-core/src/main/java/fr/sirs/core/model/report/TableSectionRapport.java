package fr.sirs.core.model.report;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import java.util.Iterator;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;

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
    protected void printSection(TextDocument target, Paragraph sectionStart, Paragraph sectionEnd, Iterator<Element> dataIt) throws Exception {
        Table table = target.addTable();
        //table.appendColumn()
    }
}
