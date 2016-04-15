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
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.theme.AbstractTheme.ThemeManager;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.theme.ui.FXVegetationTronconThemePane;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class VegetationTraitementTheme extends AbstractPluginsButtonTheme {
    
    private final static Consumer<ParcelleVegetation> deletor = (ParcelleVegetation themeElement) -> Injector.getSession().getRepositoryForClass(ParcelleVegetation.class).remove(themeElement);
    private final static Predicate<ParcelleVegetation> predicate =  (ParcelleVegetation p) -> {
            final PlanVegetation plan = VegetationSession.INSTANCE.planProperty().get();
            return plan==null || p==null || p.getPlanId()==null || !p.getPlanId().equals(plan.getId());
        };
    final static Function<String, ObservableList<ParcelleVegetation>> extractor = (String linearId) -> {
        final List<ParcelleVegetation> result = ((ParcelleVegetationRepository) Injector.getSession().getRepositoryForClass(ParcelleVegetation.class)).getByLinearId(linearId);
        final ObservableList<ParcelleVegetation> toReturn = FXCollections.observableList(result);
        toReturn.removeIf(predicate);
        return toReturn;
    };

    public VegetationTraitementTheme() {
        super("Description de la végétation", "Description de la végétation", new Image("fr/sirs/plugin/vegetation/vegetation-description.png"));
    }

    @Override
    public Parent createPane() {
        return new FXVegetationTronconThemePane(generateThemeManager());
    }

    public static ThemeManager<ParcelleVegetation> generateThemeManager(){
        final ResourceBundle bundle = ResourceBundle.getBundle(ParcelleVegetation.class.getCanonicalName(), Locale.getDefault(),
                Thread.currentThread().getContextClassLoader());
        
        return new ThemeManager<>(bundle.getString(BUNDLE_KEY_CLASS), "Thème "+bundle.getString(BUNDLE_KEY_CLASS),
                ParcelleVegetation.class, extractor, deletor);
    }
}
