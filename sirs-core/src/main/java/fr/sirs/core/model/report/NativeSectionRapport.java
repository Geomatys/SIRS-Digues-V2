package fr.sirs.core.model.report;

import fr.sirs.core.model.Element;

/**
 * An implementation of section report which tries to acquire hard-coded impression
 * templates to allow their use in reports.
 *
 * TODO : find a way to get available impression types.
 *
 * @author Alexis Manin (Geomatys)
 */
public class NativeSectionRapport extends AbstractSectionRapport {

    @Override
    public Element copy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        else return null;
    }

}
