package fr.sirs;

import javafx.stage.Stage;

/**
 * Fournit une méthode pour relancer l'application pour un {@link Stage} donné.
 *
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractRestartableStage {
    private final Stage stage;

    public AbstractRestartableStage(final Stage stage) {
        this.stage = stage;
    }

    protected Stage getStage() {
        return stage;
    }

    /**
     * A appeler quand l'application doit être redémarrée.
     */
    protected abstract void restart();
}
