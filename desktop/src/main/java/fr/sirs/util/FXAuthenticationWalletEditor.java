package fr.sirs.util;

import fr.sirs.SIRS;
import fr.sirs.core.authentication.AuthenticationWallet;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

import static fr.sirs.core.authentication.AuthenticationWallet.Entry;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;

/**
 * An editor to update saved logins.
 *
 * Note : Due to a bug with {@link ListView} component, we've "simulated" a list
 * by stacking grid panes in a VBox. The problem with javafx list views is that
 * it's impossible to make embedded buttons either fireable or capable of retrieving
 * the right item.
 *
 * TODO : Move style rules in a CSS file
 * TODO : Do not reload all list on display update.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class FXAuthenticationWalletEditor extends BorderPane {

    /**
     * Elements in the list will alternate their background by picking one in
     * the following array.
     */
    private static final Background[] LIST_BACKGROUNDS = new Background[] {
        new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)),
        new Background(new BackgroundFill(Color.ALICEBLUE, CornerRadii.EMPTY, Insets.EMPTY))
    };

    /** Wallet to display information from. */
    private final AuthenticationWallet wallet;
    /** Filtered list of wallet entries (filtered using host and port research. */
    private final FilteredList<Entry> entries;

    /** A combo box to search over host name. */
    private final ComboBox<String> hostSearch = new ComboBox();

    /** A combo box to search over available ports. */
    private final ComboBox<Integer> portSearch = new ComboBox();

    private final SimpleObjectProperty<Predicate<Entry>> predicateProperty = new SimpleObjectProperty<>();

    /** Used for list emulation. */
    private final VBox content = new VBox();

    public FXAuthenticationWalletEditor(final AuthenticationWallet wallet) {
        ArgumentChecks.ensureNonNull("Authentication wallet", wallet);
        this.wallet = wallet;
        final ObservableList<Entry> allValues = wallet.values();

        // Prepare filtered list
        entries = allValues.filtered(null);
        entries.predicateProperty().bind(predicateProperty);
        entries.addListener((ListChangeListener.Change<? extends Entry> change) -> updateDisplay());

        // Initialize filters
        allValues.addListener((ListChangeListener.Change<? extends Entry> c) -> {
            ObservableList<String> hosts = FXCollections.observableList(
                    allValues.stream().map(entry -> entry.host).collect(Collectors.toList()));
            hostSearch.setItems(hosts);

            ObservableList<Integer> ports = FXCollections.observableList(
                    allValues.stream().map(entry -> entry.port).collect(Collectors.toList()));
            portSearch.setItems(ports);
        });
        SIRS.initCombo(hostSearch, FXCollections.observableList(allValues.stream().map(entry -> entry.host).collect(Collectors.toList())), null);
        SIRS.initCombo(portSearch, FXCollections.observableList(allValues.stream().map(entry -> entry.port).collect(Collectors.toList())), null);
        portSearch.setConverter(new StringConverter<Integer>() {

            @Override
            public String toString(Integer object) {
                if (object == null) return "";
                else if (object < 0) return "Inconnu";
                else return object.toString();
            }

            @Override
            public Integer fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                else if (string.equalsIgnoreCase("inconnu")) return -1;
                else try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        });

        hostSearch.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            predicateProperty.set(new EntryPredicate(hostSearch.getValue(), portSearch.getValue()));
        });

        portSearch.valueProperty().addListener((ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) -> {
            predicateProperty.set(new EntryPredicate(hostSearch.getValue(), portSearch.getValue()));
        });

        // init search ribbon (header)
        final GridPane header = new GridPane();
        header.add(new Label("Recherche :"), 0, 0, GridPane.REMAINING, 1);
        header.add(new Label("hôte :"), 0, 1);
        header.add(hostSearch, 1, 1);
        header.add(new Label("port :"), 2, 1);
        header.add(portSearch, 3, 1);
        header.setPadding(new Insets(5));
        header.setHgap(5);
        header.setVgap(5);

        setTop(header);

        // init entry list
        content.setFillWidth(true);
        content.setMaxHeight(USE_PREF_SIZE);
        updateDisplay();

        setPadding(new Insets(5));

        final ScrollPane sPane = new ScrollPane(content);
        sPane.setFitToWidth(true);
        sPane.setBorder(Border.EMPTY);
        setCenter(sPane);
        updateDisplay();
    }

    private void updateDisplay() {
        final ArrayList<EntryCell> cells = new ArrayList<>();
        for (int i = 0 ; i < entries.size() ; i++) {
            final EntryCell cell = new EntryCell(entries.get(i));
            cell.setBorder(Border.EMPTY);
            cell.setBackground(LIST_BACKGROUNDS[i%LIST_BACKGROUNDS.length]);
            cells.add(cell);
        }
        content.getChildren().setAll(cells);
    }

    private class EntryCell extends GridPane {

        private final Entry source;

        EntryCell(final Entry source) {
            this.source = source;
            setPadding(new Insets(5));
            getColumnConstraints().addAll(
                    // Label column
                    new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, HPos.LEFT, true),
                    // information column (host and port)
                    new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, HPos.LEFT, true),
                    // Empty column to fill empty space.
                    new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true),
                    // Button "update" column
                    new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, HPos.RIGHT, true)
            );

            add(new Label("hôte :"), 0, 0);
            add(new Label("port :"), 0, 1);
            add(new Label(source.host), 1, 0);
            add(new Label(source.port < 0? "inconnu" : Integer.toString(source.port)), 1, 1);

            final Hyperlink updateButton = new Hyperlink("Mettre à jour");
            updateButton.setOnAction(event -> updateLogin(this.source).ifPresent(entry -> wallet.put(entry)));
            add(updateButton, 3, 0);

            final Hyperlink removeButton = new Hyperlink("Supprimer");
            removeButton.setOnAction(event -> wallet.remove(this.source));
            add(removeButton, 3, 1);
        }
    }


    /**
     * Show a dialog which contains inputs allowing to update login/password associated to given entry.
     * @param source The entry to update. This object will not be modified. A copy is returned at the end of the method.
     * @return A copy off input entry, updated with new login. If user cancelled his input, an empty optional is returned.
     */
    private static Optional<Entry> updateLogin(final Entry source) {
        final TextField userInput = new TextField(source.login);
        final PasswordField passInput = new PasswordField();
        passInput.setText(source.password);

        final GridPane gPane = new GridPane();
        gPane.add(new Label("Login : "), 0, 0);
        gPane.add(userInput, 1, 0);
        gPane.add(new Label("Mot de passe : "), 0, 1);
        gPane.add(passInput, 1, 1);

        final StringBuilder headerText = new StringBuilder().append("Mettre à jour les identifiants de connexion du service");
        headerText.append("\n");
        headerText.append("hôte : ");
        if ("localhost".equals(source.host) || "127.0.0.1".equals(source.host)) {
            headerText.append("Service local");
        } else {
            headerText.append(source.host);
        }

        headerText.append("\n").append("port : ").append(source.port < 0 ? "indéfini" : source.port);
        final Alert question = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.CANCEL, ButtonType.OK);
        question.getDialogPane().setContent(gPane);
        question.setResizable(true);
        question.setTitle("Mise à jour du login");
        question.setHeaderText(headerText.toString());

        Optional<ButtonType> result = question.showAndWait();
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            Entry resultEntry = source.clone();
            resultEntry.login = userInput.getText();
            resultEntry.password = passInput.getText();
            return Optional.of(resultEntry);
        } else {
            return Optional.empty();
        }
    }

    private static class EntryPredicate implements Predicate<Entry> {

        private final Pattern hostPattern;
        private final Integer portRequested;

        public EntryPredicate(final String host, final Integer port) {
            if (host == null || host.isEmpty()) {
                hostPattern = null;
            } else {
                hostPattern = ComboBoxCompletion.buildPattern(host);
            }
            portRequested = port;
        }

        @Override
        public boolean test(Entry t) {
            if (t == null || t.host == null) return false;
            return (hostPattern == null || hostPattern.matcher(t.host).find())
                    && (portRequested == null || portRequested == t.port);
        }
    }
}
