package fr.sirs.importer.objet.link;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Desordre;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.objet.desordre.DesordreImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import fr.sirs.importer.objet.reseau.TypeElementReseauImporter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DesordreElementReseauImporter extends GenericObjetLinker {

    private final ElementReseauImporter elementReseauImporter;
    private final DesordreImporter desordreImporter;
    private final TypeElementReseauImporter typeElementReseauImporter;


    private enum Columns {
        ID_DESORDRE,
        ID_ELEMENT_RESEAU,
        ID_TYPE_ELEMENT_RESEAU,
//        DATE_DERNIERE_MAJ
    };

    @Override
    public String getTableName() {
        return DESORDRE_ELEMENT_RESEAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        final Map<Integer, ObjetReseau> elementsReseaux = elementReseauImporter.getById();
        final Map<Integer, Desordre> desordres = desordreImporter.getById();
        final Map<Integer, Class> classesElementReseaux = typeElementReseauImporter.getTypeReferences();

        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
//            final ObjetReferenceObjet elementReseauDesordre = new ObjetReferenceObjet();

            final Objet elementReseau = elementsReseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            final Class classeElementReseau = classesElementReseaux.get(row.getInt(Columns.ID_TYPE_ELEMENT_RESEAU.toString()));
            final Desordre desordre = desordres.get(row.getInt(Columns.ID_DESORDRE.toString()));

            if(elementReseau!=null && desordre!=null){

                if (elementReseau.getClass().equals(classeElementReseau)) {
                    if (elementReseau instanceof VoieDigue) {
                        desordre.getVoieDigueIds().add(elementReseau.getId());
                    }
                    else if (elementReseau instanceof OuvrageParticulier){
                        desordre.getOuvrageParticulierIds().add(elementReseau.getId());
                    }
                    else if (elementReseau instanceof OuvrageHydrauliqueAssocie){
                        desordre.getOuvrageHydrauliqueAssocieIds().add(elementReseau.getId());
                    }
                    else if (elementReseau instanceof OuvrageTelecomEnergie){
                        desordre.getOuvrageTelecomEnergieIds().add(elementReseau.getId());
                    }
                    else if (elementReseau instanceof OuvrageVoirie){
                        desordre.getOuvrageVoirieIds().add(elementReseau.getId());
                    }
                    else if (elementReseau instanceof ReseauTelecomEnergie){
                        desordre.getReseauTelecomEnergieIds().add(elementReseau.getId());
                    }
                    else if (elementReseau instanceof ReseauHydrauliqueCielOuvert){
                        desordre.getReseauHydrauliqueCielOuvertIds().add(elementReseau.getId());
                    }
                    else if (elementReseau instanceof ReseauHydrauliqueFerme){
                        desordre.getReseauHydrauliqueFermeIds().add(elementReseau.getId());
                    }
                    else {
                        SirsCore.LOGGER.log(Level.FINE, elementReseau.getClass().getSimpleName());
                        throw new AccessDbImporterException("Bad type.");
                    }
                }
                else {
                    throw new AccessDbImporterException("Bad referenced type. Incoherent data.");
                }

                associations.add(new AbstractMap.SimpleEntry<>(elementReseau, desordre));
            }
        }
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
