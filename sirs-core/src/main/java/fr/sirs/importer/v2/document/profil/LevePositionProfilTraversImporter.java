package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.LevePositionProfilTravers;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.PROFIL_EN_TRAVERS_TRONCON;
import fr.sirs.importer.v2.AbstractLinker;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class LevePositionProfilTraversImporter extends AbstractLinker<LevePositionProfilTravers, PositionProfilTravers> {

    @Override
    public void bind(PositionProfilTravers holder, String targetId) throws AccessDbImporterException {
        holder.getLevePositionIds().add(targetId);
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
