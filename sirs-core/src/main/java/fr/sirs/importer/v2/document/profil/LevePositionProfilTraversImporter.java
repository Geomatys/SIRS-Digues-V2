package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.LevePositionProfilTravers;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.PROFIL_EN_TRAVERS_TRONCON;
import fr.sirs.importer.v2.AbstractLinker;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class LevePositionProfilTraversImporter extends AbstractLinker<LevePositionProfilTravers, PositionProfilTravers> {

    @Override
    public void bind(PositionProfilTravers holder, Collection<String> targetIds) throws AccessDbImporterException {
        holder.getLevePositionIds().addAll(targetIds);
    }

    @Override
    public String getHolderColumn() {
        return Columns.ID_DOC.name();
    }

    @Override
    public Class<PositionProfilTravers> getHolderClass() {
        return PositionProfilTravers.class;
    }

    private enum Columns {
        ID_DOC,
        ID_PROFIL_EN_TRAVERS_LEVE
    }

    @Override
    protected Class<LevePositionProfilTravers> getElementClass() {
        return LevePositionProfilTravers.class;
    }

    @Override
    public String getTableName() {
        return PROFIL_EN_TRAVERS_TRONCON.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_DOC.name();
    }

}
