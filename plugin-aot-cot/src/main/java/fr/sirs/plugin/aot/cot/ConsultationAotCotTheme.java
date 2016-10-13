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
package fr.sirs.plugin.aot.cot;

import fr.sirs.PrintManager;
import fr.sirs.Printable;
import fr.sirs.core.model.AotCotAssociable;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.PositionConvention;
import static fr.sirs.plugin.aot.cot.PluginAotCot.getConventionTableForObjet;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.data.FeatureCollection;

/**
 * Bouton de suivi d'AOT / COT.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public final class ConsultationAotCotTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            ConsultationAotCotTheme.class.getResourceAsStream("images/aot-objAssocies.png"));
    public ConsultationAotCotTheme() {
        super("Consultation AOT/COT", "Consultation AOT/COT", BUTTON_IMAGE);
    }
    
    private Element toConsultFromMap = null;

    public void setObjetToConsultFromMap(final Element candidate){
        toConsultFromMap = candidate;
    }

    /**
     * AOT/COT consultation must not be cached.
     * @return
     */
    @Override
    public boolean isCached(){return false;}

    @Override
    public Parent createPane() {
        final BorderPane borderPane;

        // On commence par vérifier qu'aucun objet n'est sélectionné par la carte pour traitement par ce thème.
        if(toConsultFromMap!=null){
            borderPane = new BorderPane();
            borderPane.setCenter(getConventionTableForObjet(toConsultFromMap));
            // Réinitialisation de l'objet courant à consulter.
            toConsultFromMap=null;
        }

        // Sinon on recherche l'objet courant sélectionné dans l'interface qui est également l'élément sélectionné pour impression.
        else{
            final Printable printable = PrintManager.printableProperty().get();
            final List<Element> elements = new ArrayList<>();
            if(printable!=null){
                Object printableElements = printable.getPrintableElements().get();
                if(printableElements instanceof Element){
                    elements.add((Element)printableElements);
                }else if(printableElements instanceof List && !(printableElements instanceof FeatureCollection)){
                    elements.addAll((List)printableElements);
                }
            }

            if(elements.size()==1 &&
                    (elements.get(0) instanceof AotCotAssociable
                    || elements.get(0) instanceof Objet
                    || elements.get(0) instanceof PositionConvention)){
                borderPane = new BorderPane();
                borderPane.setCenter(getConventionTableForObjet(elements.get(0)));
            } else {
                final String msg;
                borderPane = null;
                if( elements ==null || elements.isEmpty()){
                    msg="Aucun élément sélectionné.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un objet.";
                } else if (elements.size()>1){
                    msg="Plusieurs éléments ont été sélectionnés.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un objet sans ambigüité.";
                } else if (!(elements.get(0) instanceof Objet)  && !(elements.get(0) instanceof AotCotAssociable)){
                    msg="L'élément sélectionné n'est pas associable à une convention.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un objet.";
                } else { // Normalement ce cas ne doit jamais se présenter car toutes les possibilités ont été épuisées.
                    msg=null;
                }

                if(msg!=null){
                    final Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.CLOSE);
                    alert.setResizable(true);
                    alert.showAndWait();
                }
            }
        }

        return borderPane;
    }
}
