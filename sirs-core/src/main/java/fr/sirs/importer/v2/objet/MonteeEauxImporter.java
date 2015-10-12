package fr.sirs.importer.v2.objet;

import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.MonteeEaux;
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
public class MonteeEauxImporter extends AbstractLinker<MonteeEaux, EvenementHydraulique>{

    @Override
    public void bind(EvenementHydraulique holder, Collection<String> targetIds) throws AccessDbImporterException {
        holder.getMonteeEauxIds().addAll(targetIds);
    }

    @Override
    public String getHolderColumn() {
        return "ID_EVENEMENT_HYDRAU";
    }

    @Override
    public Class<MonteeEaux> getElementClass() {
        return MonteeEaux.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.MONTEE_DES_EAUX.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_MONTEE_DES_EAUX";
    }

    @Override
    public Class<EvenementHydraulique> getHolderClass() {
        return EvenementHydraulique.class;
    }

}
