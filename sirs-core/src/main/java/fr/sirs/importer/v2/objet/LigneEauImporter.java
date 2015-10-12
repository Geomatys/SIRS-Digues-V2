/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.objet;

import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LigneEau;
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
public class LigneEauImporter extends AbstractLinker<LigneEau, EvenementHydraulique> {

    @Override
    public Class<LigneEau> getElementClass() {
        return LigneEau.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.LIGNE_EAU.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_LIGNE_EAU";
    }

    @Override
    public void bind(EvenementHydraulique holder, Collection<String> targetIds) throws AccessDbImporterException {
        holder.getLigneEauIds().addAll(targetIds);
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
