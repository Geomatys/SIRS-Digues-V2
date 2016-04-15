/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.util;

import fr.sirs.SIRS;
import fr.sirs.core.authentication.AuthenticationWallet;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

import static fr.sirs.core.authentication.AuthenticationWallet.Entry;
import java.util.ArrayList;
import java.util.HashSet;
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
import javafx.scene.layout.Border;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
public class FXAuthenticationWalletEditor extends BorderPane implements SaveableConfiguration {

    /**
     * Elements in the list will alternate their background by picking one in
     * the following array.
     */
    public static final String[] LIST_CLASSES = new String[] {"list-even", "list-odd"};

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

    /**
     * Keep reference of entries to remove on save action.
     */
    private final HashSet<Entry> toRemove = new HashSet<>();
    /**
     * Keep reference of entries to put / update on save action.
     */
    private final HashSet<Entry> toAdd = new HashSet<>();

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
            cell.getStyleClass().add(LIST_CLASSES[i%LIST_CLASSES.length]);
            cells.add(cell);
        }
        content.getChildren().setAll(cells);
    }

    /**
     * Save modified entries by removing trashed entries, then updating / adding
     * the modified / created ones.
     */
    @Override
    public void save() {
        for (final Entry e : toRemove) {
            wallet.remove(e);
        }

        for (final Entry e : toAdd) {
            wallet.put(e);
        }
    }

    @Override
    public String getTitle() {
        return "Trousseau de connexion";
    }

    /**
     * Display of a single authentication entry.
     */
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
            updateButton.setOnAction(event -> updateLogin(this.source).ifPresent(entry -> {toRemove.remove(entry); toAdd.add(entry);}));
            add(updateButton, 3, 0);

            final Hyperlink removeButton = new Hyperlink("Supprimer");
            removeButton.setOnAction(event -> {toAdd.remove(this.source); toRemove.add(this.source);});
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

    /**
     * A predicate used for filtering visible entries when user types in search
     * fields.
     */
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
