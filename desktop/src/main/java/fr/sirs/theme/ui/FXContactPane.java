
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Organisme;
import fr.sirs.ui.Growl;
import fr.sirs.util.ReferenceTableCell;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXContactPane extends AbstractFXElementPane<Contact> {

    @FXML private TextField uiNom;
    @FXML private TextField uiPrenom;
    @FXML private TextField uiService;
    @FXML private TextField uiFonction;
    @FXML private TextField uiTelephone;
    @FXML private TextField uiMobile;
    @FXML private TextField uiFax;
    @FXML private TextField uiEmail;

    @FXML private TextField uiAdresse;
    @FXML private TextField uiCodePostal;
    @FXML private TextField uiCommune;

    @FXML private Tab uiOrganismeTab;

    private final PojoTable organismeTable;

    private final AbstractSIRSRepository<Contact> contactRepository;
    private final AbstractSIRSRepository<Organisme> orgRepository;

    final ObservableList<Element> orgsOfContact = FXCollections.observableArrayList();

    final HashSet<Organisme> modifiedOrgs = new HashSet<>();

    final Session session = Injector.getSession();

    /**
     *
     * @param contact
     */
    public FXContactPane(Contact contact) {
        SIRS.loadFXML(this);

        this.contactRepository = session.getRepositoryForClass(Contact.class);
        this.orgRepository = session.getRepositoryForClass(Organisme.class);

        uiNom.disableProperty().bind(disableFieldsProperty());
        uiPrenom.disableProperty().bind(disableFieldsProperty());
        uiService.disableProperty().bind(disableFieldsProperty());
        uiFonction.disableProperty().bind(disableFieldsProperty());
        uiTelephone.disableProperty().bind(disableFieldsProperty());
        uiMobile.disableProperty().bind(disableFieldsProperty());
        uiFax.disableProperty().bind(disableFieldsProperty());
        uiEmail.disableProperty().bind(disableFieldsProperty());
        uiAdresse.disableProperty().bind(disableFieldsProperty());
        uiCodePostal.disableProperty().bind(disableFieldsProperty());
        uiCommune.disableProperty().bind(disableFieldsProperty());

        organismeTable = new ContactOrganismeTable();
        uiOrganismeTab.setContent(organismeTable);
        // /!\ If you remove "and" condition, you must add null check in below ContactOrganismeTable.
        organismeTable.editableProperty().bind(disableFieldsProperty().not().and(elementProperty.isNotNull()));

        orgsOfContact.addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                // We check only removed elements, because new ones do not
                // have an attached organism.
                while (c.next()) {
                    final Iterator<ContactOrganisme> it = c.getRemoved().iterator();
                    while (it.hasNext()) {
                        final ContactOrganisme co = it.next();
                        if (co.getParent() != null) {
                            modifiedOrgs.add((Organisme) co.getParent());
                        }
                    }
                }
            }
        });

        elementProperty.addListener(this::initFields);
        setElement(contact);
    }

    private void initFields(ObservableValue<? extends Contact> observable, Contact oldValue, Contact newValue) {

        if (oldValue != null) {
            uiNom.textProperty().unbindBidirectional(oldValue.nomProperty());
            uiPrenom.textProperty().unbindBidirectional(oldValue.prenomProperty());
            uiService.textProperty().unbindBidirectional(oldValue.serviceProperty());
            uiFonction.textProperty().unbindBidirectional(oldValue.fonctionProperty());
            uiTelephone.textProperty().unbindBidirectional(oldValue.telephoneProperty());
            uiMobile.textProperty().unbindBidirectional(oldValue.mobileProperty());
            uiFax.textProperty().unbindBidirectional(oldValue.faxProperty());
            uiEmail.textProperty().unbindBidirectional(oldValue.emailProperty());
            uiAdresse.textProperty().unbindBidirectional(oldValue.adresseProperty());
            uiCodePostal.textProperty().unbindBidirectional(oldValue.codePostalProperty());
            uiCommune.textProperty().unbindBidirectional(oldValue.communeProperty());
        }

        if (newValue == null) return;

        orgsOfContact.clear();
        modifiedOrgs.clear();

        uiNom.textProperty().bindBidirectional(newValue.nomProperty());
        uiPrenom.textProperty().bindBidirectional(newValue.prenomProperty());
        uiService.textProperty().bindBidirectional(newValue.serviceProperty());
        uiFonction.textProperty().bindBidirectional(newValue.fonctionProperty());
        uiTelephone.textProperty().bindBidirectional(newValue.telephoneProperty());
        uiMobile.textProperty().bindBidirectional(newValue.mobileProperty());
        uiFax.textProperty().bindBidirectional(newValue.faxProperty());
        uiEmail.textProperty().bindBidirectional(newValue.emailProperty());
        uiAdresse.textProperty().bindBidirectional(newValue.adresseProperty());
        uiCodePostal.textProperty().bindBidirectional(newValue.codePostalProperty());
        uiCommune.textProperty().bindBidirectional(newValue.communeProperty());

        // Retrieve all organisms current newValue is / was part of.
        if (newValue.getId() != null) {
            for (final Organisme org : orgRepository.getAllStreaming()) {
                orgsOfContact.addAll(org.contactOrganisme.filtered((ContactOrganisme co) -> {
                    return newValue.getId().equals(co.getContactId());
                }));
            }
        }
    }

    @Override
    public void preSave() {
        try {
            for (final Element org : orgsOfContact) {
                orgRepository.update((Organisme) org.getParent());
            }
            modifiedOrgs.clear();
        } catch (Exception e) {
            final Growl growlError = new Growl(Growl.Type.ERROR, "Erreur survenue pendant la mise à jour des organismes.");
            growlError.showAndFade();
            GeotkFX.newExceptionDialog("L'élément ne peut être sauvegardé.", e).show();
            SIRS.LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Table listant les rattachement du contact courant aux organismes connus.
     * Aucune opération de sauvegarde n'est effectuée ici, elles seront appliquées
     * lors de la sauvegarde globale du panneau.
     */
    private final class ContactOrganismeTable extends PojoTable {

        public ContactOrganismeTable() {
            super(ContactOrganisme.class, "Liste des organismes");
            editableProperty().bind(uiFicheMode.selectedProperty());

            final TableColumn<Element, String> organismeColumn = new TableColumn<>("Organisme");
            organismeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Element, String>, ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Element, String> param) {
                    final ContactOrganisme co = (ContactOrganisme) param.getValue();
                    if(co!=null && co.getParent()!=null) return new SimpleStringProperty(co.getParent().getId());
                    return null;
                }
            });
            organismeColumn.setCellFactory((TableColumn<Element, String> param) -> new ReferenceTableCell<>(Organisme.class));
            getColumns().add(organismeColumn);

            setTableItems(() -> orgsOfContact);
        }

        @Override
        protected void editPojo(Object pojo) {
            final ContactOrganisme co = (ContactOrganisme) pojo;
            co.contactIdProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                if (!elementProperty.get().getId().equals(newValue)) {
                    orgsOfContact.remove(co);
                }
            });
            super.editPojo(pojo);
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            if (event.getRowValue() instanceof ContactOrganisme) {
                final ContactOrganisme co = (ContactOrganisme) event.getRowValue();
                if (co.getParent() != null) {
                    modifiedOrgs.add((Organisme)co.getParent());
                }
            }
        }

        @Override
        protected void deletePojos(Element... pojos) {
            final List<Organisme> modifiedOrganisms = new ArrayList<>();
            for(final Element pojo : pojos){
                // Si l'utilisateur est un externe, il faut qu'il soit l'auteur de
                // l'élément et que celui-ci soit invalide, sinon, on court-circuite
                // la suppression.
                if(authoriseElementDeletion(pojo)) {
                    final Organisme org = (Organisme) pojo.getParent();
                    org.removeChild(pojo);
                    if(!modifiedOrganisms.contains(org)) modifiedOrganisms.add(org);
                    orgsOfContact.remove(pojo);
                }
            }

            session.getRepositoryForClass(Organisme.class).executeBulk(modifiedOrganisms);
        }

        @Override
        protected ContactOrganisme createPojo() {
            final ContactOrganisme co = Injector.getSession().getElementCreator().createElement(ContactOrganisme.class);
            co.getId(); // Necessary to initialize element Id
            co.setValid(!session.needValidationProperty().get());
            co.setAuthor(session.getUtilisateur() == null? null : session.getUtilisateur().getId());
            co.setContactId(elementProperty.get().getId());
            final Organisme org = session.getRepositoryForClass(Organisme.class).getOne();
            co.setDocumentId(org.getId());
            org.addChild(co);
            co.setDateDebutIntervenant(LocalDate.now());
            orgsOfContact.add(co);
            return co;
        }
    }

}
