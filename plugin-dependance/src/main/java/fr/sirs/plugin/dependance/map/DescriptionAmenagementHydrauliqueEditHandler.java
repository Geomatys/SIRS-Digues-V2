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
package fr.sirs.plugin.dependance.map;

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.core.model.DescriptionAmenagementHydraulique;
import fr.sirs.core.model.DesordreDependance;
import fr.sirs.core.model.OrganeProtectionCollective;
import fr.sirs.core.model.OuvrageAssocieAmenagementHydraulique;
import fr.sirs.core.model.PrestationAmenagementHydraulique;
import fr.sirs.core.model.StructureAmenagementHydraulique;
import fr.sirs.map.AbstractSIRSEditHandler;
import fr.sirs.map.SIRSEditMouseListen;
import fr.sirs.plugin.dependance.PluginDependance;
import fr.sirs.util.SirsStringConverter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 * Contrôle les actions possibles pour le bouton d'édition et de modification de dépendances
 * sur la carte.
 *
 * Note : appelé dans l'application Sirs depuis la fiche du {@link DescriptionAmenagementHydraulique}.
 * Pourrait être regroupé avec {@link DesordreCreateHandler}.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @author Maxime Gavens (Geomatys)
 */
public class DescriptionAmenagementHydrauliqueEditHandler extends AbstractSIRSEditHandler {

    private SIRSEditMouseListen mouseInputListener;

    public DescriptionAmenagementHydrauliqueEditHandler() {
        super(DescriptionAmenagementHydraulique.class);
    }
    
    public DescriptionAmenagementHydrauliqueEditHandler(final DescriptionAmenagementHydraulique description) {
        this();

        if (description instanceof DesordreDependance) {
            objetClass = DesordreDependance.class;
            editedObjet = (DesordreDependance) description;
        } else if (description instanceof StructureAmenagementHydraulique) {
            objetClass = StructureAmenagementHydraulique.class;
            editedObjet = (StructureAmenagementHydraulique) description;
        } else if (description instanceof OuvrageAssocieAmenagementHydraulique) {
            objetClass = OuvrageAssocieAmenagementHydraulique.class;
            editedObjet = (OuvrageAssocieAmenagementHydraulique) description;
        } else if (description instanceof PrestationAmenagementHydraulique) {
            objetClass = PrestationAmenagementHydraulique.class;
            editedObjet = (PrestationAmenagementHydraulique) description;
        } else if (description instanceof OrganeProtectionCollective) {
            objetClass = OrganeProtectionCollective.class;
            editedObjet = (OrganeProtectionCollective) description;
        } else {
            throw new IllegalArgumentException("Not supported subclass of DescriptionAmenagementHydraulique.");
        }

        if (description.getGeometry() != null) {
            editGeometry.geometry.set((Geometry)description.getGeometry().clone());
            geomLayer.getGeometries().setAll(editGeometry.geometry.get());
        }
        mouseInputListener = new SIRSEditMouseListen(this, true);
    }

    @Override
    public SIRSEditMouseListen getMouseInputListener() {
        return mouseInputListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void install(FXMap component) {
        super.install(component);

        if (editedObjet instanceof DesordreDependance) {
            objetLayer = PluginDependance.getDesordreLayer();
        } else if (editedObjet instanceof StructureAmenagementHydraulique) {
            objetLayer = PluginDependance.getStructureLayer();
        } else if (editedObjet instanceof OuvrageAssocieAmenagementHydraulique) {
            objetLayer = PluginDependance.getOuvrageAssocieLayer();
        } else if (editedObjet instanceof PrestationAmenagementHydraulique) {
            objetLayer = PluginDependance.getPrestationLayer();
        } else if (editedObjet instanceof OrganeProtectionCollective) {
            objetLayer = PluginDependance.getOrganeProtectionLayer();
        } else {
            throw new IllegalArgumentException("Récupération de la couche, non supportée pour l'objet: " + (new SirsStringConverter()).toString(editedObjet));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean uninstall(FXMap component) {
        if (editGeometry.geometry.get() == null) {
            super.uninstall(component);
            return true;
        }

        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la fin du mode édition ? Les modifications non sauvegardées seront perdues.",
                ButtonType.YES,ButtonType.NO);
        if (ButtonType.YES.equals(alert.showAndWait().get())) {
            super.uninstall(component);
            return true;
        }

        return false;
    }
}
