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
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.StreamingIterable;
import fr.sirs.util.javafx.FloatSpinnerValueFactory;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.ArrayList;
import org.geotoolkit.util.collection.CloseableIterator;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXDesordreDependancePane extends AbstractFXElementPane<DesordreDependance> {

    protected final Previews previewRepository;
    protected LabelMapper labelMapper;

    @FXML private FXValidityPeriodPane uiValidityPeriod;
    @FXML private FXPositionDependancePane uiPosition;

    // Propriétés de DesordreDependance
    @FXML protected TextField ui_lieuDit;
    @FXML protected TextArea ui_commentaire;
    @FXML protected ComboBox ui_dependanceId;
    @FXML protected Button ui_dependanceId_link;
    @FXML protected Tab ui_observations;
    protected final PojoTable observationsTable;
    @FXML protected Tab ui_evenementHydrauliqueIds;
    protected final ListeningPojoTable evenementHydrauliqueIdsTable;
    @FXML protected Spinner ui_cote;
    @FXML protected ComboBox ui_sourceId;
    @FXML protected Button ui_sourceId_link;
    @FXML protected ComboBox ui_positionId;
    @FXML protected Button ui_positionId_link;
    @FXML protected ComboBox ui_categorieDesordreId;
    @FXML protected Button ui_categorieDesordreId_link;
    @FXML protected ComboBox ui_typeDesordreId;
    @FXML protected Button ui_typeDesordreId_link;
    @FXML protected FXFreeTab ui_ouvrageAssocieIds;
    protected ListeningPojoTable ouvrageAssocieIdsTable;
    @FXML protected FXFreeTab ui_prestationIds;
    protected ListeningPojoTable prestationIdsTable;
    @FXML protected FXFreeTab ui_articleIds;
    protected ListeningPojoTable articleIdsTable;

    // Propriétés de AvecGeometrie
    // Propriétés de DescriptionAmenagementHydraulique
    @FXML protected ComboBox ui_amenagementHydrauliqueId;
    @FXML protected Button ui_amenagementHydrauliqueId_link;

    /**
     * Constructor. Initialize part of the UI which will not require update when
     * element edited change.
     */
    protected FXDesordreDependancePane() {
        SIRS.loadFXML(this, DesordreDependance.class);
        final Session session = Injector.getSession();
        previewRepository = session.getPreviews();
        elementProperty().addListener(this::initFields);

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

        /*
        * Disabling rules.
        */
        ui_lieuDit.disableProperty().bind(disableFieldsProperty());
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        ui_dependanceId.disableProperty().bind(disableFieldsProperty());
        ui_dependanceId_link.disableProperty().bind(ui_dependanceId.getSelectionModel().selectedItemProperty().isNull());
        ui_dependanceId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_dependanceId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_dependanceId.getSelectionModel().getSelectedItem()));
        observationsTable = new PojoTable(ObservationDependance.class, null, elementProperty());
        observationsTable.editableProperty().bind(disableFieldsProperty().not());
        ui_observations.setContent(observationsTable);
        ui_observations.setClosable(false);
        evenementHydrauliqueIdsTable = new ListeningPojoTable(EvenementHydraulique.class, null, elementProperty());
        evenementHydrauliqueIdsTable.editableProperty().bind(disableFieldsProperty().not());
        evenementHydrauliqueIdsTable.createNewProperty().set(false);
        ui_evenementHydrauliqueIds.setContent(evenementHydrauliqueIdsTable);
        ui_evenementHydrauliqueIds.setClosable(false);
        ui_cote.disableProperty().bind(disableFieldsProperty());
        ui_cote.setEditable(true);
        ui_cote.setValueFactory(new FloatSpinnerValueFactory(0, Float.MAX_VALUE));
        ui_sourceId.disableProperty().bind(disableFieldsProperty());
        ui_sourceId_link.setVisible(false);
        ui_positionId.disableProperty().bind(disableFieldsProperty());
        ui_positionId_link.setVisible(false);
        ui_categorieDesordreId.disableProperty().bind(disableFieldsProperty());
        ui_categorieDesordreId_link.setVisible(false);
        ui_typeDesordreId.disableProperty().bind(disableFieldsProperty());
        ui_typeDesordreId_link.setVisible(false);
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());
        uiPosition.dependanceProperty().bind(elementProperty);

        ui_ouvrageAssocieIds.setContent(() -> {
            ouvrageAssocieIdsTable = new ListeningPojoTable(OuvrageAssocieAmenagementHydraulique.class, null, elementProperty());
            ouvrageAssocieIdsTable.editableProperty().bind(disableFieldsProperty().not());
            ouvrageAssocieIdsTable.createNewProperty().set(false);
            updateOuvrageAssocieIdsTable(session, elementProperty.get());
            return ouvrageAssocieIdsTable;
        });
        ui_ouvrageAssocieIds.setClosable(false);

        ui_prestationIds.setContent(() -> {
            prestationIdsTable = new ListeningPojoTable(Prestation.class, null, elementProperty());
            prestationIdsTable.editableProperty().bind(disableFieldsProperty().not());
            prestationIdsTable.createNewProperty().set(false);
            updatePrestationIdsTable(session, elementProperty.get());
            return prestationIdsTable;
        });
        ui_prestationIds.setClosable(false);

        ui_articleIds.setContent(() -> {
            articleIdsTable = new ListeningPojoTable(ArticleJournal.class, null, elementProperty());
            articleIdsTable.editableProperty().bind(disableFieldsProperty().not());
            articleIdsTable.createNewProperty().set(false);
            updateArticleIdsTable(session, elementProperty.get());
            return articleIdsTable;
        });
        ui_articleIds.setClosable(false);
        ui_amenagementHydrauliqueId.disableProperty().bind(disableFieldsProperty());
        ui_amenagementHydrauliqueId_link.disableProperty().bind(ui_amenagementHydrauliqueId.getSelectionModel().selectedItemProperty().isNull());
        ui_amenagementHydrauliqueId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_amenagementHydrauliqueId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_amenagementHydrauliqueId.getSelectionModel().getSelectedItem()));
    }

    public FXDesordreDependancePane(final DesordreDependance desordreDependance){
        this();
        this.elementProperty().set(desordreDependance);
    }

    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends DesordreDependance > observableElement, DesordreDependance oldElement, DesordreDependance newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
            // Propriétés de DesordreDependance
            ui_lieuDit.textProperty().unbindBidirectional(oldElement.lieuDitProperty());
            ui_lieuDit.setText(null);
            ui_commentaire.textProperty().unbindBidirectional(oldElement.commentaireProperty());
            ui_commentaire.setText(null);
            ui_cote.getValueFactory().valueProperty().unbindBidirectional(oldElement.coteProperty());
            ui_cote.getValueFactory().setValue(0);
            // Propriétés de AvecGeometrie
        }

        final Session session = Injector.getBean(Session.class);

        if (newElement == null) {
            ui_lieuDit.setText(null);
            ui_commentaire.setText(null);
            ui_dependanceId.setItems(null);
            ui_sourceId.setItems(null);
            ui_positionId.setItems(null);
            ui_categorieDesordreId.setItems(null);
            ui_typeDesordreId.setItems(null);
            ui_amenagementHydrauliqueId.setItems(null);
        } else {
            /*
            * Bind control properties to Element ones.
            */
            // Propriétés de DesordreDependance
            // * lieuDit
            ui_lieuDit.textProperty().bindBidirectional(newElement.lieuDitProperty());
            // * commentaire
            ui_commentaire.textProperty().bindBidirectional(newElement.commentaireProperty());
            List<Preview> abstractDependances = previewRepository.getByClass(AbstractDependance.class);
            abstractDependances.removeIf(p -> "fr.sirs.core.model.AmenagementHydraulique".equals(p.getElementClass()));
            SIRS.initCombo(ui_dependanceId, FXCollections.observableList(abstractDependances), newElement.getDependanceId() == null ? null : previewRepository.get(newElement.getDependanceId()));
            // * cote
            ui_cote.getValueFactory().valueProperty().bindBidirectional(newElement.coteProperty());
            final AbstractSIRSRepository<RefSource> sourceIdRepo = session.getRepositoryForClass(RefSource.class);
            SIRS.initCombo(ui_sourceId, SIRS.observableList(sourceIdRepo.getAll()), newElement.getSourceId() == null? null : sourceIdRepo.get(newElement.getSourceId()));
            final AbstractSIRSRepository<RefPosition> positionIdRepo = session.getRepositoryForClass(RefPosition.class);
            SIRS.initCombo(ui_positionId, SIRS.observableList(positionIdRepo.getAll()), newElement.getPositionId() == null? null : positionIdRepo.get(newElement.getPositionId()));
            final AbstractSIRSRepository<RefCategorieDesordre> categorieDesordreIdRepo = session.getRepositoryForClass(RefCategorieDesordre.class);
            SIRS.initCombo(ui_categorieDesordreId, SIRS.observableList(categorieDesordreIdRepo.getAll()), newElement.getCategorieDesordreId() == null? null : categorieDesordreIdRepo.get(newElement.getCategorieDesordreId()));
            final AbstractSIRSRepository<RefTypeDesordre> typeDesordreIdRepo = session.getRepositoryForClass(RefTypeDesordre.class);
            SIRS.initCombo(ui_typeDesordreId, SIRS.observableList(typeDesordreIdRepo.getAll()), newElement.getTypeDesordreId() == null? null : typeDesordreIdRepo.get(newElement.getTypeDesordreId()));
            // Propriétés de AvecGeometrie
            // Propriétés de AvecSettableGeometrie
            // Propriétés de DescriptionAmenagementHydraulique
            final Preview linearPreview = newElement.getAmenagementHydrauliqueId() == null ? null : previewRepository.get(newElement.getAmenagementHydrauliqueId());
            SIRS.initCombo(ui_amenagementHydrauliqueId, SIRS.observableList(
                    previewRepository.getByClass(linearPreview == null ? AmenagementHydraulique.class : linearPreview.getJavaClassOr(AmenagementHydraulique.class))).sorted(), linearPreview);
        }

        updateObservationsTable(session, newElement);
        updateEvenementHydrauliqueIdsTable(session, newElement);
        updateOuvrageAssocieIdsTable(session, newElement);
        updatePrestationIdsTable(session, newElement);
        updateArticleIdsTable(session, newElement);
    }

    protected void updateObservationsTable(final Session session, final DesordreDependance newElement) {
        if (observationsTable == null)
            return;

        if (newElement == null) {
            observationsTable.setTableItems(null);
        } else {
            observationsTable.setParentElement(newElement);
            observationsTable.setTableItems(()-> (ObservableList) newElement.getObservations());
        }
    }

    protected void updateEvenementHydrauliqueIdsTable(final Session session, final DesordreDependance newElement) {
        if (evenementHydrauliqueIdsTable == null)
            return;

        if (newElement == null) {
            evenementHydrauliqueIdsTable.setTableItems(null);
        } else {
            evenementHydrauliqueIdsTable.setParentElement(null);
            final AbstractSIRSRepository<EvenementHydraulique> evenementHydrauliqueIdsRepo = session.getRepositoryForClass(EvenementHydraulique.class);
            evenementHydrauliqueIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getEvenementHydrauliqueIds(), evenementHydrauliqueIdsRepo));
            evenementHydrauliqueIdsTable.setObservableListToListen(newElement.getEvenementHydrauliqueIds());
        }
    }

    protected void updateOuvrageAssocieIdsTable(final Session session, final DesordreDependance newElement) {
        if (ouvrageAssocieIdsTable == null)
            return;

        if (newElement == null) {
            ouvrageAssocieIdsTable.setTableItems(null);
        } else {
            ouvrageAssocieIdsTable.setParentElement(null);
            final AbstractSIRSRepository<OuvrageAssocieAmenagementHydraulique> ouvrageAssocieIdsRepo = session.getRepositoryForClass(OuvrageAssocieAmenagementHydraulique.class);
            ouvrageAssocieIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getOuvrageAssocieIds(), ouvrageAssocieIdsRepo));
            ouvrageAssocieIdsTable.setObservableListToListen(newElement.getOuvrageAssocieIds());
        }
    }

    protected void updatePrestationIdsTable(final Session session, final DesordreDependance newElement) {
        if (prestationIdsTable == null)
            return;

        if (newElement == null) {
            prestationIdsTable.setTableItems(null);
        } else {
            prestationIdsTable.setParentElement(null);
            final AbstractSIRSRepository<Prestation> prestationIdsRepo = session.getRepositoryForClass(Prestation.class);
            prestationIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getPrestationIds(), prestationIdsRepo));
            prestationIdsTable.setObservableListToListen(newElement.getPrestationIds());
        }
    }

    protected void updateArticleIdsTable(final Session session, final DesordreDependance newElement) {
        if (articleIdsTable == null)
            return;

        if (newElement == null) {
            articleIdsTable.setTableItems(null);
        } else {
            articleIdsTable.setParentElement(null);
            final AbstractSIRSRepository<ArticleJournal> articleIdsRepo = session.getRepositoryForClass(ArticleJournal.class);
            articleIdsTable.setTableItems(()-> SIRS.toElementList(newElement.getArticleIds(), articleIdsRepo));
            articleIdsTable.setObservableListToListen(newElement.getArticleIds());
        }
    }

    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final DesordreDependance element = (DesordreDependance) elementProperty().get();

        Object cbValue;
        cbValue = ui_dependanceId.getValue();
        if (cbValue instanceof Preview) {
            element.setDependanceId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setDependanceId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setDependanceId(null);
        }
        cbValue = ui_sourceId.getValue();
        if (cbValue instanceof Preview) {
            element.setSourceId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setSourceId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setSourceId(null);
        }
        cbValue = ui_positionId.getValue();
        if (cbValue instanceof Preview) {
            element.setPositionId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setPositionId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setPositionId(null);
        }
        cbValue = ui_categorieDesordreId.getValue();
        if (cbValue instanceof Preview) {
            element.setCategorieDesordreId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setCategorieDesordreId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setCategorieDesordreId(null);
        }
        cbValue = ui_typeDesordreId.getValue();
        if (cbValue instanceof Preview) {
            element.setTypeDesordreId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setTypeDesordreId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setTypeDesordreId(null);
        }
        // Manage opposite references for EvenementHydraulique...
        if (evenementHydrauliqueIdsTable != null) {
            final List<String> currentEvenementHydrauliqueIdsList = new ArrayList<>();
            for(final Element elt : evenementHydrauliqueIdsTable.getAllValues()){
                final EvenementHydraulique evenementHydraulique = (EvenementHydraulique) elt;
                currentEvenementHydrauliqueIdsList.add(evenementHydraulique.getId());
            }
            element.setEvenementHydrauliqueIds(currentEvenementHydrauliqueIdsList);
        }
        if (ouvrageAssocieIdsTable != null) {
            /*
            * En cas de reference opposee on se prepare a stocker les objets
            * "opposes" pour les mettre � jour.
            */
            final List<OuvrageAssocieAmenagementHydraulique> currentOuvrageAssocieAmenagementHydrauliqueList = new ArrayList<>();
            // Manage opposite references for OuvrageAssocieAmenagementHydraulique...
            /*
            * Si on est sur une reference principale, on a besoin du depot pour
            * supprimer reellement les elements que l'on va retirer du tableau.
            * Si on a une reference opposee, on a besoin du depot pour mettre a jour
            * les objets qui referencent l'objet courant en sens contraire.
            */
            final AbstractSIRSRepository<OuvrageAssocieAmenagementHydraulique> ouvrageAssocieAmenagementHydrauliqueRepository = session.getRepositoryForClass(OuvrageAssocieAmenagementHydraulique.class);
            final List<String> currentOuvrageAssocieAmenagementHydrauliqueIdsList = new ArrayList<>();
            for(final Element elt : ouvrageAssocieIdsTable.getAllValues()){
                final OuvrageAssocieAmenagementHydraulique ouvrageAssocieAmenagementHydraulique = (OuvrageAssocieAmenagementHydraulique) elt;
                currentOuvrageAssocieAmenagementHydrauliqueIdsList.add(ouvrageAssocieAmenagementHydraulique.getId());
                currentOuvrageAssocieAmenagementHydrauliqueList.add(ouvrageAssocieAmenagementHydraulique);

                // Addition
                if(!ouvrageAssocieAmenagementHydraulique.getDesordreDependanceAssocieIds().contains(element.getId())){
                    ouvrageAssocieAmenagementHydraulique.getDesordreDependanceAssocieIds().add(element.getId());
                }
            }
            ouvrageAssocieAmenagementHydrauliqueRepository.executeBulk(currentOuvrageAssocieAmenagementHydrauliqueList);
            element.setOuvrageAssocieIds(currentOuvrageAssocieAmenagementHydrauliqueIdsList);

            // Deletion
            final StreamingIterable<OuvrageAssocieAmenagementHydraulique> listOuvrageAssocieAmenagementHydraulique = ouvrageAssocieAmenagementHydrauliqueRepository.getAllStreaming();
            try (final CloseableIterator<OuvrageAssocieAmenagementHydraulique> it = listOuvrageAssocieAmenagementHydraulique.iterator()) {
                while (it.hasNext()) {
                    final OuvrageAssocieAmenagementHydraulique i = it.next();
                    if(i.getDesordreDependanceAssocieIds().contains(element.getId())
                            || element.getOuvrageAssocieIds().contains(i.getId())){
                        if(!ouvrageAssocieIdsTable.getAllValues().contains(i)){
                            element.getOuvrageAssocieIds().remove(i.getId()); //Normalement inutile du fait du  clear avant les op�rations d'ajout
                            i.getDesordreDependanceAssocieIds().remove(element.getId());
                            ouvrageAssocieAmenagementHydrauliqueRepository.update(i);
                        }
                    }
                }
            }
        }
        if (prestationIdsTable != null) {
            // Manage opposite references for Prestation...
            final List<String> currentPrestationIdsList = new ArrayList<>();
            for(final Element elt : prestationIdsTable.getAllValues()){
                final Prestation prestation = (Prestation) elt;
                currentPrestationIdsList.add(prestation.getId());
            }
            element.setPrestationIds(currentPrestationIdsList);
        }
        if (articleIdsTable != null) {
            // Manage opposite references for ArticleJournal...
            final List<String> currentArticleJournalIdsList = new ArrayList<>();
            for(final Element elt : articleIdsTable.getAllValues()){
                final ArticleJournal articleJournal = (ArticleJournal) elt;
                currentArticleJournalIdsList.add(articleJournal.getId());
            }
            element.setArticleIds(currentArticleJournalIdsList);
        }
        cbValue = ui_amenagementHydrauliqueId.getValue();
        if (cbValue instanceof Preview) {
            element.setAmenagementHydrauliqueId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setAmenagementHydrauliqueId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setAmenagementHydrauliqueId(null);
        }
    }
}
