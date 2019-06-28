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
package fr.sirs.core.authentication;

import fr.sirs.core.SirsCore;
import fr.sirs.core.authentication.AuthenticationWallet.Entry;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * An authenticator which will prompt a JavaFX dialog to query password from user.
 *
 * @author Alexis Manin (Geomatys)
 */
public class SIRSAuthenticator extends Authenticator {

    private final AuthenticationWallet wallet = AuthenticationWallet.getDefault();

    /**
     * Keep reference of checked entries, because if login information is wrong,
     * we'll know it and will prompt user.
     */
    private static final HashMap<String, Entry> ENTRIES_TO_CHECK = new HashMap<>();
    private static boolean alreadyChecked=false;

    @Override
    protected synchronized PasswordAuthentication getPasswordAuthentication() {
        // First, we retrieve target service information and check its integrity.
        String host = getRequestingHost();
        int port = getRequestingPort();
        final URL url = getRequestingURL();
        if (host == null || host.isEmpty()) {
            if (url == null)
                throw new IllegalStateException("Neither host nor valid URL has been provided for authentication check");
            else
                host = url.getHost();
        }

        if (port < 0 && url != null) {
            port = (url.getPort() < 0)? url.getDefaultPort() : url.getPort();
        }

        String serviceId = AuthenticationWallet.toServiceId(host, port);
        AuthenticationWallet.Entry entry = wallet == null? null : wallet.get(host, port);

        /*
         * HACK : Apache HttpClient (used by Ektorp) will call this method on
         * each query, which means we cannot determine if it is performing a
         * fail&retry. As java.net methods give us the query URL, we can adopt
         * different behavior for thee two components.
         */
        final boolean fromApache = (getRequestingURL() == null);

        SirsCore.LOGGER.log(Level.FINE, "CREDENTIAL QUERY FROM "+ (fromApache? "APACHE" : "JAVA.NET"));

//        
        if (( (entry != null) && ((!alreadyChecked) || ENTRIES_TO_CHECK.get(serviceId) == null) )
            && !(((ENTRIES_TO_CHECK.get(serviceId)!=null) && (ENTRIES_TO_CHECK.get(serviceId).equals(entry))))){
//           
        // We've got login from wallet, and it has not been rejected yet.
//        if ( (entry != null) && (fromApache || ENTRIES_TO_CHECK.get(serviceId) == null) && (!alreadyChecked)) {
//              if (!fromApache) {
                
                if (!alreadyChecked) {
                    ENTRIES_TO_CHECK.put(serviceId, entry);
                    alreadyChecked=true;
                }
                return new PasswordAuthentication(entry.login, (entry.password == null) ? new char[0] : entry.password.toCharArray());

        // New or invalid entry case.
        } else {
            
            Map.Entry<String, String> login = askForLogin(entry == null? null : entry.login, entry == null? null : entry.password);
            if (login == null || login.getKey() == null) {
                return null;
            } else {
                entry = new AuthenticationWallet.Entry(host, port, login.getKey(), login.getValue());
                ENTRIES_TO_CHECK.put(serviceId, entry);
                if (wallet != null) {
                    wallet.put(entry);
                }
                return new PasswordAuthentication(login.getKey(), login.getValue() == null? new char[0] : login.getValue().toCharArray());
            }
        }
    }
    
    public static void cleanEntriesToCheck(){
        ENTRIES_TO_CHECK.clear();
    }

    /**
     * Display a dialog to ask user a login and password to allow connection to queried service.
     * @param defaultUser Default login to show in login input. Can be null.
     * @param defaultPass default password to fill password input with. Can be null.
     * @return An entry whose key is login and value is password typed by user. Null if user cancelled dialog.
     */
    public Map.Entry<String, String> askForLogin(final String defaultUser, final String defaultPass) {
        final Task<Map.Entry<String, String>> askLogin = new Task() {

            @Override
            protected Object call() throws Exception {
                final TextField userInput = new TextField(defaultUser);
                final PasswordField passInput = new PasswordField();
                passInput.setText(defaultPass);

                final GridPane gPane = new GridPane();
                gPane.add(new Label("Login : "), 0, 0);
                gPane.add(userInput, 1, 0);
                gPane.add(new Label("Mot de passe : "), 0, 1);
                gPane.add(passInput, 1, 1);

                final StringBuilder headerText = new StringBuilder().append("Identifiants requis pour ");
                if (RequestorType.PROXY.equals(getRequestorType())) {
                    headerText.append("le Proxy ");
                } else if (getRequestingPort() == 5984) {
                    headerText.append("CouchDB");
                } else {
                    headerText.append("le service");
                }
                headerText.append(" :\n");
                final String host = getRequestingHost();
                if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
                    headerText.append("Service local");
                } else {
                    headerText.append(getRequestingSite());
                }
                if (getRequestingPort() >= 0) {
                    headerText.append(", port ").append(getRequestingPort());
                }

                final Alert question = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.CANCEL, ButtonType.OK);
                question.getDialogPane().setContent(gPane);
                question.setResizable(true);
                question.setTitle("Authentification requise");
                question.setHeaderText(headerText.toString());

                Optional<ButtonType> result = question.showAndWait();
                if (result.isPresent()) {
                    if (result.get().equals(ButtonType.OK)) {
                        return new AbstractMap.SimpleEntry<>(userInput.getText(), passInput.getText());
                    } else if (result.get().equals(ButtonType.CANCEL)) {
                        System.exit(0);
                    }
                }
                return null;
            }
        };

        if (Platform.isFxApplicationThread()) {
            askLogin.run();
        } else {
            Platform.runLater(askLogin);
        }

        try {
            return askLogin.get();
        } catch (InterruptedException | ExecutionException ex) {
            SirsCore.LOGGER.log(Level.WARNING, "Authentication prompt failed for service : "+ getRequestingSite().toString(), ex);
            return null;
        }
    }
}
