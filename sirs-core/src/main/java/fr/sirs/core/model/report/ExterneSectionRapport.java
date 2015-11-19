package fr.sirs.core.model.report;

import com.sun.javafx.PlatformUtil;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.util.odt.ODTUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A section composed of an unique external file (can be an image, a PDF or
 * another ODT document).
 *
 * Note : {@link #requeteIdProperty() } is not used here, because section generation
 * is independent from any element.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class ExterneSectionRapport extends AbstractSectionRapport implements SIRSFileReference {
    /**
     * Path to external document to reference.
     */
    private final StringProperty  chemin = new SimpleStringProperty();
    public StringProperty cheminProperty() {
       return chemin;
    }

    @Override
    public String getChemin() {
        return chemin.get();
    }

    @Override
    public void setChemin(String ref) {
        chemin.set(ref);
    }

    @Override
    public Element copy() {
        final ExterneSectionRapport rapport = ElementCreator.createAnonymValidElement(ExterneSectionRapport.class);
        super.copy(rapport);

        rapport.setChemin(getChemin());

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
    protected void printSection(PrintContext context) throws Exception {
        /* First, we'll analyze input path to determine where target document is
         * located. First, we will use the same strategy as other documents
         * refered by SIRS : We'll concatenate local path with defined root one.
         * If we're not succcessful, we'll try to interpret local path as an
         * absolute one.
         */
        final String strPath = getChemin();
        if (strPath == null || strPath.isEmpty()) {
            return;
        }

        Path path;
        Exception suppressed = null;
        try {
            path = SirsCore.getDocumentAbsolutePath(strPath);
            if (!Files.isRegularFile(path))
                path = null;
        } catch (Exception e) {
            path = null;
            suppressed = e;
        }

        if (path == null) {
            // Replace separators from another OS.
            if (PlatformUtil.isWindows()) {
                path = Paths.get(strPath.replaceAll("/+", "\\\\"));
            } else {
                path = Paths.get(strPath.replaceAll("\\\\+", File.separator));
            }
        }

        if (!Files.isReadable(path)) {
            final IllegalStateException ex = new IllegalStateException("No readable file can be found using path "+ strPath);
            if (suppressed != null) {
                ex.addSuppressed(suppressed);
            }
            throw ex;
        }

        ODTUtils.append(context.target, path);
    }
}
