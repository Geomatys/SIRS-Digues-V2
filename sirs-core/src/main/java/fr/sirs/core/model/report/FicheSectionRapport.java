package fr.sirs.core.model.report;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.util.odt.ODTUtils;
import fr.sirs.util.property.Reference;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.odftoolkit.simple.TextDocument;

/**
 * Detailed element printing.
 *
 * @author Alexis Manin (Geomatys)
 */
public class FicheSectionRapport extends AbstractSectionRapport {

    private final SimpleIntegerProperty nbPhotos = new SimpleIntegerProperty(0);

    /**
     *
     * @return The number of photos to print for a given element.
     */
    public IntegerProperty nbPhotosProperty() {
        return nbPhotos;
    }

    public int getNbPhotos() {
        return nbPhotos.get();
    }

    public void setNbPhotos(int newValue) {
        nbPhotos.set(newValue);
    }

    private final SimpleStringProperty modeleElementId = new SimpleStringProperty();

    /**
     *
     * @return ODT template used to print each element given in this section.
     */
    @Reference(ref=ModeleElement.class)
    public StringProperty ModeleElementIdProperty() {
        return modeleElementId;
    }

    public String getModeleElementId() {
        return modeleElementId.get();
    }

    public void setModeleElementId(final String newValue) {
        modeleElementId.set(newValue);
    }

    @Override
    public Element copy() {
        final FicheSectionRapport rapport = ElementCreator.createAnonymValidElement(FicheSectionRapport.class);
        super.copy(rapport);

        rapport.setNbPhotos(getNbPhotos());
        rapport.setModeleElementId(getModeleElementId());

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
    public void printSection(final PrintContext ctx) throws Exception {
        if (modeleElementId.get() == null)
            throw new IllegalStateException("No model set for printing !");

        final SessionCore session = InjectorCore.getBean(SessionCore.class);
        ModeleElement model = session.getRepositoryForClass(ModeleElement.class).get(modeleElementId.get());
        byte[] odt = model.getOdt();
        if (odt == null || odt.length <= 0) {
            throw new IllegalStateException("No ODT template available.");
        }

        try (final ByteArrayInputStream stream = new ByteArrayInputStream(odt);
                TextDocument doc = TextDocument.loadDocument(stream)) {
            final Iterator<Element> it = ctx.elements.iterator();
            while (it.hasNext()) {
                ODTUtils.fillTemplate(doc, it.next());
                ctx.target.insertContentFromDocumentBefore(doc, ctx.endParagraph, true);
            }
        }
    }
}
