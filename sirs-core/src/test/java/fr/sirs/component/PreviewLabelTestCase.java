package fr.sirs.component;

import org.junit.Test;

import fr.sirs.core.CouchDBTestCase;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.PreviewLabelRepository;
import fr.sirs.core.model.BorneDigue;

public class PreviewLabelTestCase extends CouchDBTestCase {

    @Test
    public void test() {

        PreviewLabelRepository previewLabelRepository = new PreviewLabelRepository(
                connector);
        
        BorneDigueRepository borneDigueRepository = new BorneDigueRepository(connector);

        for(BorneDigue borneDigue: borneDigueRepository.getAll()) {
            
        String label =  previewLabelRepository.getPreview(borneDigue.getId());
        System.out.println(label);
        }
    }

}
