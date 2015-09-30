package fr.sirs.importer.v2.event;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.Meteo;
import fr.sirs.core.model.RefOrientationVent;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.SimpleUpdater;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class MeteoImporter extends SimpleUpdater<Meteo, EvenementHydraulique> {

    private AbstractImporter<RefOrientationVent> typeOrientationVentImporter;

    @Override
    protected Class<Meteo> getElementClass() {
        return Meteo.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_EVENEMENT_HYDRAU.name();
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_EVENEMENT_HYDRAU.name();
    }

    @Override
    public void put(EvenementHydraulique container, Meteo toPut) {
        container.meteos.add(toPut);
    }

    @Override
    public Class<EvenementHydraulique> getDocumentClass() {
        return EvenementHydraulique.class;
    }

    private enum Columns{
        ID_EVENEMENT_HYDRAU,
        VITESSE_VENT,
        ID_TYPE_ORIENTATION_VENT,
        PRESSION_ATMOSPHERIQUE
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
        return METEO.toString();
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        typeOrientationVentImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        typeOrientationVentImporter = context.importers.get(RefOrientationVent.class);
    }

    @Override
    public Meteo importRow(Row row, Meteo meteo) throws IOException, AccessDbImporterException {
        meteo = super.importRow(row, meteo);

        final Double vitesseVent = row.getDouble(Columns.VITESSE_VENT.toString());
        if (vitesseVent != null) {
            meteo.setVitesseVent(vitesseVent.floatValue());
        }

        final Integer typeOrientation = row.getInt(Columns.ID_TYPE_ORIENTATION_VENT.toString());
        if (typeOrientation != null) {
            meteo.setTypeOrientationVentId(typeOrientationVentImporter.getImportedId(typeOrientation));
        }

        final Double pression = row.getDouble(Columns.PRESSION_ATMOSPHERIQUE.toString());
        if (pression != null) {
            meteo.setPression(pression.floatValue());
        }

        return meteo;
    }
}
