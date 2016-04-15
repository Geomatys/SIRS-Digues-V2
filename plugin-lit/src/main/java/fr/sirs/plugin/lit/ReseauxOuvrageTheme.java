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
package fr.sirs.plugin.lit;

import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.plugin.lit.ui.AbstractDescriptionPane;
import fr.sirs.plugin.lit.util.TabContent;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ReseauxOuvrageTheme extends AbstractDescriptionTheme {

    public ReseauxOuvrageTheme() {
        super("Réseaux et ouvrages", "Réseaux et ouvrages");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("Réseau hydrau. fermé", "Tableau des réseaux hydrauliques fermés", ReseauHydrauliqueFerme.class));
        content.add(new TabContent("Ouvrage hydrau. associé", "Tableau des ouvrages hydrauliques associés", OuvrageHydrauliqueAssocie.class));
        content.add(new TabContent("Réseau télécom/énergie", "Tableau des réseaux télécom/énérgie", ReseauTelecomEnergie.class));
        content.add(new TabContent("Ouvrage télécom/énergie", "Tableau des ouvrages télécom/énérgie", OuvrageTelecomEnergie.class));
        content.add(new TabContent("Réseau hydrau. ciel ouvert", "Tableau des réseaux hydrauliques a ciel ouvert", ReseauHydrauliqueCielOuvert.class));
        content.add(new TabContent("Ouvrage part.", "Tableau des ouvrages particulier", OuvrageParticulier.class));
        content.add(new TabContent("Echelles limnimétriques", "Tableau des échelles limnimétriques", EchelleLimnimetrique.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
