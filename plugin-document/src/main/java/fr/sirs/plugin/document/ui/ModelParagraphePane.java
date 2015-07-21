package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.RapportModeleDocumentRepository;
import fr.sirs.core.model.RapportModeleDocument;
import fr.sirs.core.model.RapportSectionDocument;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

import java.awt.Color;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class ModelParagraphePane extends BorderPane {
    public static final Image ICON_TRASH = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TRASH_O, 16, Color.BLACK), null);

    @FXML private Label uiSectionNameLbl;

    @FXML private Button uiDeleteSectionBtn;

    @FXML private TextField uiSectionTitleTxtField;

    private final RapportSectionDocument section;
    private final RapportModeleDocument model;

    public ModelParagraphePane(final RapportModeleDocument model, final RapportSectionDocument section, final int index) {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);

        this.model = model;
        this.section = section;

        uiSectionNameLbl.setText("Paragraphe "+ index);
        uiDeleteSectionBtn.setGraphic(new ImageView(ICON_TRASH));
        uiSectionTitleTxtField.textProperty().bindBidirectional(section.libelleProperty());
    }

    @FXML
    private void deleteSection() {
        model.getSections().remove(section);
        Injector.getBean(RapportModeleDocumentRepository.class).update(model);
    }
}
