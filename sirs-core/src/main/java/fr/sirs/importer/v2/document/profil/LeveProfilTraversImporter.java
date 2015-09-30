package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractLinker;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class LeveProfilTraversImporter extends AbstractLinker<LeveProfilTravers, ProfilTravers> {

    @Override
    public Class<ProfilTravers> getHolderClass() {
        return ProfilTravers.class;
    }

    @Override
    public void bind(ProfilTravers holder, Collection<String> targetIds) throws AccessDbImporterException {
        holder.getLeveIds().addAll(targetIds);
    }

    @Override
    public String getHolderColumn() {
        return Columns.ID_PROFIL_EN_TRAVERS.name();
    }

    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_PROFIL_EN_TRAVERS
    }

    @Override
    protected Class<LeveProfilTravers> getElementClass() {
        return LeveProfilTravers.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_TRAVERS_DESCRIPTION.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_PROFIL_EN_TRAVERS_LEVE.name();
    }
}
