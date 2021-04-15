
package fr.sirs.theme.ui;

import fr.sirs.core.model.Prestation;

/**
 * Class used to group prestations' linked pojotables  in 'categories' as for Desordres. 
 * The group should be introduce in .jet files to automatically generate it for each reference with a categoried element types.
 * @author Maxime Gavens (Geomatys)
 */
public class FXPrestationPane extends FXPrestationPaneStub {

    public FXPrestationPane(final Prestation prestation){
        super(prestation);
    }
}
