package fr.sirs.util;

import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.model.Element;
import fr.sirs.ui.Growl;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Window;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Watch CouchDB changes for deleted elements. Any time an element is deleted,
 * we analyze graph scene to ensure all related editors are closed.
 *
 * JIRA : <a href="http://jira.geomatys.com/browse/SYM-1445">SYM-1445</a>
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class RemovalWatcher implements DocumentListener {

    @Autowired
    private Session session;

    @PostConstruct
    void init() {
        try {
            final DocumentChangeEmiter emiter = session.getApplicationContext().getBean(DocumentChangeEmiter.class);
            emiter.addListener(this);
        } catch (NoSuchBeanDefinitionException e) {
            SIRS.LOGGER.log(Level.FINE, "No document change emiter registered.", e);
        }
    }

    @Override
    public void documentCreated(Map<Class, List<Element>> added) {
        // Don't care
    }

    @Override
    public void documentChanged(Map<Class, List<Element>> changed) {
        // nope
    }

    @Override
    public void documentDeleted(Set<String> deleted) {
        final Set<FXFreeTab> editors = session.findEditors(deleted);
        Platform.runLater(() -> {
            final AtomicBoolean closed = new AtomicBoolean(false);
            final Predicate<TabPane> notifier = input -> {
                closed.set(true);
                return true;
            };
            for (final FXFreeTab tab : editors) {
                removeFromParent(tab)
                        .filter(notifier) // HACK : use filter as notifier, to activate message to user once any tab has been closed.
                        .ifPresent(this::closeIfEmpty);
            }

            if (closed.get())
                new Growl(
                        Growl.Type.WARNING,
                        "Des fiches ont été fermées :"
                                .concat(System.lineSeparator())
                                .concat(" les données correspondantes ont été supprimées de la base de données.")
                ).showAndFade();
        });
    }

    /**
     * Try to remove the given {@link Tab} from its parent {@link TabPane}. If
     * input is contained in a tab pane, and we succeed removing it, We return
     * the affected parent.
     *
     * @param toRemove the tab to get out of its pane.
     * @return The pane containing the tab, if any.
     */
    private Optional<TabPane> removeFromParent(final Tab toRemove) {
        final TabPane tp = toRemove.getTabPane();
        if (tp != null && tp.getTabs().remove(toRemove))
            return Optional.of(tp);
        return Optional.empty();
    }

    /**
     * Test if the window containing given panel has no other node in it. I.e :
     * There's no other node in one of its parents. If the test succeeds, we
     * actually try to close the target window.
     *
     * @param startPane Panel to close containing window for.
     */
    private void closeIfEmpty(final Parent startPane) {
        // Check that the given pane is contained in a window.
        final Window toClose;
        final Scene scene = startPane.getScene();
        if (scene == null || (toClose = scene.getWindow()) == null)
            return;

        // If given pane is scene root, proceed to the closing immediately.
        Parent p = startPane.getParent();
        if (p == null) {
            toClose.hide();
        } else {
            // Otherwise, we ensure there's no other content before doing so.
            while (p.getParent() != null && p.getChildrenUnmodifiable().size() <= 1) {
                p = p.getParent();
            }

            if (p.getParent() == null && p.getChildrenUnmodifiable().size() <= 1)
                toClose.hide();
        }
    }
}
