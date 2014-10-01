package fr.sym.util;

import fr.sym.Plugins;
import fr.sym.Session;
import fr.sym.Theme;
import fr.sym.digue.Injector;
import fr.sym.digue.dto.Dam;
import fr.sym.digue.dto.DamSystem;
import fr.sym.digue.dto.Section;
import fr.symadrem.sirs.core.model.Digue;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class WrapTreeItem extends TreeItem {

    private boolean loaded = false;

    @Autowired
    private Session session;

    public WrapTreeItem(Object candidate) {
        super(candidate);
        Injector.injectDependencies(this);
    }

    @Override
    public boolean isLeaf() {
        return getValue() instanceof Theme;
    }

    @Override
    public synchronized ObservableList getChildren() {
        if (!loaded) {
            loaded = true;
            final Object obj = getValue();
            List candidates = new ArrayList();

            if (obj instanceof Digue) {
                candidates = session.getChildren((Digue) obj);
            } else if (obj instanceof DamSystem) {
                candidates = session.getChildren((DamSystem) obj);
            } else if (obj instanceof Dam) {
                candidates = session.getChildren((Dam) obj);
            } else if (obj instanceof Section) {
                final Theme[] themes = Plugins.getThemes();
                for (Theme theme : themes) {
                    candidates.add(theme);
                }
            }

            for (Object candidate : candidates) {
                super.getChildren().add(new WrapTreeItem(candidate));
            }
        }

        return super.getChildren();
    }

}
