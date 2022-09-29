
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.TronconDigue;
import org.apache.sis.util.ArgumentChecks;

/**
 * Class used to group prestations' linked pojotables  in 'categories' as for Desordres.
 * The group should be introduce in .jet files to automatically generate it for each reference with a categoried element types.
 * @author Maxime Gavens (Geomatys)
 */
public class FXPrestationPane extends FXPrestationPaneStub {

    public FXPrestationPane(final Prestation prestation){
        super(prestation);
        final Session session = Injector.getBean(Session.class);
        ui_desordreIds.setContent(() -> {
            // HACK_REDMINE_7605 - TronconLit has Prestation and DesordreLit : best would be to create PrestationLit and adapt all plugin-lit models
            Prestation presta = elementProperty().get();
            if (presta == null) throw new IllegalStateException("The prestation cannot be null");
            String linearId = presta.getLinearId();
            if (linearId == null) throw new IllegalStateException("The linearId of the prestation cannot be null");
            TronconDigue troncon = session.getRepositoryForClass(TronconDigue.class).get(linearId);
            if (troncon == null) throw new IllegalStateException("No element found for id " + linearId);
            if ("TronconLit".equals(troncon.getClass().getSimpleName()))
                desordreIdsTable = new PrestationDesordresPojoTable(elementProperty(), true);
            else desordreIdsTable = new PrestationDesordresPojoTable(elementProperty(), false);
            desordreIdsTable.editableProperty().bind(disableFieldsProperty().not());
            desordreIdsTable.createNewProperty().set(false);
            updateDesordreIdsTable(session, elementProperty.get());
            return desordreIdsTable;
        });
    }
}