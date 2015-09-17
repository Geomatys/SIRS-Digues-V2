package fr.sirs.importer.v2.event;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.RefEvenementHydraulique;
import fr.sirs.core.model.RefFrequenceEvenementHydraulique;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class EvenementHydrauliqueImporter extends AbstractImporter<EvenementHydraulique> {

    private AbstractImporter<RefEvenementHydraulique> typeEvenementHydrauliqueImporter;
    private AbstractImporter<RefFrequenceEvenementHydraulique> typeFrequenceEvenementHydrauliqueImporter;

    private enum Columns{
        ID_EVENEMENT_HYDRAU,
        NOM_EVENEMENT_HYDRAU,
        ID_TYPE_EVENEMENT_HYDRAU,
        ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
        VITESSE_MOYENNE,
        DEBIT_MOYEN,
        NOM_MODELEUR_HYDRAU
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected Class<EvenementHydraulique> getDocumentClass() {
        return EvenementHydraulique.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_EVENEMENT_HYDRAU.name();
    }

    @Override
    public EvenementHydraulique importRow(Row row, EvenementHydraulique evenement) throws IOException, AccessDbImporterException {
        evenement.setLibelle(row.getString(Columns.NOM_EVENEMENT_HYDRAU.toString()));

        final Double vitesse = row.getDouble(Columns.VITESSE_MOYENNE.toString());
        if (vitesse != null) {
            evenement.setVitesseMoy(vitesse.floatValue());
        }

        final Double debit = row.getDouble(Columns.DEBIT_MOYEN.toString());
        if (debit != null) {
            evenement.setDebitMoy(debit.floatValue());
        }

        evenement.setModeleurHydraulique(row.getString(Columns.NOM_MODELEUR_HYDRAU.toString()));

        final Integer typeEvenement = row.getInt(Columns.ID_TYPE_EVENEMENT_HYDRAU.toString());
        if (typeEvenement != null) {
            evenement.setTypeEvenementHydrauliqueId(typeEvenementHydrauliqueImporter.getImportedId(typeEvenement));
        }

        final Integer typeFrequence = row.getInt(Columns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString());
        if (typeFrequence != null) {
            evenement.setFrequenceEvenementHydrauliqueId(typeFrequenceEvenementHydrauliqueImporter.getImportedId(typeFrequence));
        }

        return evenement;
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        typeEvenementHydrauliqueImporter = null;
        typeFrequenceEvenementHydrauliqueImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        typeEvenementHydrauliqueImporter = context.importers.get(RefEvenementHydraulique.class);
        typeFrequenceEvenementHydrauliqueImporter = context.importers.get(RefFrequenceEvenementHydraulique.class);
    }
}
