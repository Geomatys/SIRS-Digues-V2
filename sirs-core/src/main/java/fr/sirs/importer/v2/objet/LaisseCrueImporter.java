/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.objet;

import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LaisseCrue;
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
public class LaisseCrueImporter extends AbstractLinker<LaisseCrue, EvenementHydraulique> {

    @Override
    protected Class<LaisseCrue> getElementClass() {
        return LaisseCrue.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.LAISSE_CRUE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_LAISSE_CRUE";
    }

    @Override
    public void bind(EvenementHydraulique holder, Collection<String> targetIds) throws AccessDbImporterException {
        holder.getLaisseCrueIds().addAll(targetIds);
    }

    @Override
    public String getHolderColumn() {
        return "ID_EVENEMENT_HYDRAU";
    }

    @Override
    public Class<EvenementHydraulique> getHolderClass() {
        return EvenementHydraulique.class;
    }

}
