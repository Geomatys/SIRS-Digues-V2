package fr.sirs.component;

import org.junit.Test;

import fr.sirs.core.CouchDBTestCase;
import fr.sirs.core.component.PreviewLabelRepository;

public class PreviewLabelTestCase extends CouchDBTestCase {

    @Test
    public void test() {

        PreviewLabelRepository previewLabelRepository = new PreviewLabelRepository(
                connector);

        String label =  previewLabelRepository.findById("1f4f8c701109dc4e42c433aa18000b03");
        System.out.println(label);
    }

}
