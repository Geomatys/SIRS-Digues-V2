package fr.sirs;

import fr.sirs.core.SirsCore;
import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXAboutPane extends BorderPane {
    
    private static final String FRANCE_DIGUES_URL = "http://sirs-digues.info";
    
    @FXML
    private Hyperlink uiHyperLink;

    @FXML
    private Label uiVersionLabel;
    
    public FXAboutPane() {
        SIRS.loadFXML(this);
        uiVersionLabel.setText(SIRS.getVersion());
        uiHyperLink.setUnderline(true);
        uiHyperLink.setOnAction((ActionEvent event) -> {
            if (Desktop.isDesktopSupported()) {
                new Thread(() -> {
                    try {
                        Desktop.getDesktop().browse(new URI(FRANCE_DIGUES_URL));
                    } catch (Exception ex) {
                        SIRS.LOGGER.log(Level.WARNING, null, ex);
                    }
                }).start();
            } else {
                final WebView webView = new WebView();
                final Stage infoStage = new Stage();
                infoStage.getIcons().add(SirsCore.ICON);
                infoStage.setTitle("Site communautaire");
                infoStage.setScene(new Scene(webView));
                infoStage.show();
                webView.getEngine().load(FRANCE_DIGUES_URL);
            }
        });
    }

}
