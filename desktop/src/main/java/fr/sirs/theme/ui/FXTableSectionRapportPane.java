package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.SQLQuery;
import fr.sirs.core.model.report.TableSectionRapport;
import fr.sirs.query.FXSearchPane;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.ektorp.DbAccessException;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXTableSectionRapportPane extends AbstractFXElementPane<TableSectionRapport> {

    @FXML private TextField uiTitle;
    @FXML private Label uiQueryTitle;

    private final ObjectProperty<SQLQuery> queryProperty = new SimpleObjectProperty<>();

    public FXTableSectionRapportPane() {
        super();
        SIRS.loadFXML(this);

        elementProperty.addListener(this::elementChanged);
        queryProperty.addListener(this::queryChanged);

        disableProperty().bind(disableFieldsProperty());
    }

    public FXTableSectionRapportPane(final TableSectionRapport rapport) {
        this();
        setElement(rapport);
    }

    private void elementChanged(ObservableValue<? extends TableSectionRapport> obs, TableSectionRapport oldValue, TableSectionRapport newValue) {
        if (oldValue != null) {
            uiTitle.textProperty().unbindBidirectional(oldValue.libelleProperty());
        }
        if (newValue != null) {
            uiTitle.textProperty().bindBidirectional(newValue.libelleProperty());
            if (newValue.getRequeteId() != null) {
                try {
                    queryProperty.set(Injector.getSession().getRepositoryForClass(SQLQuery.class).get(newValue.getRequeteId()));
                } catch (DbAccessException e) {
                    queryProperty.set(null);
                }
            }
        } else {
            uiTitle.setText(null);
            queryProperty.set(null);
        }
    }

    private void queryChanged(ObservableValue<? extends SQLQuery> obs, SQLQuery oldValue, SQLQuery newValue) {
        if (newValue == null)
            uiQueryTitle.setText("N/A");
        else
            uiQueryTitle.setText(newValue.getLibelle());
    }

    @Override
    public void preSave() throws Exception {
        if (queryProperty.get() != null) {
            elementProperty.get().setRequeteId(queryProperty.get().getId());
        } else {
            elementProperty.get().setRequeteId(null);
        }
    }

    @FXML
    private void chooseQuery(ActionEvent event) {
        final Optional<SQLQuery> query = FXSearchPane.chooseSQLQuery(Injector.getSession().getRepositoryForClass(SQLQuery.class).getAll());
        if (query.isPresent())
            queryProperty.set(query.get());
    }

    @FXML
    private void deleteQuery(ActionEvent event) {
        queryProperty.set(null);
    }
}
