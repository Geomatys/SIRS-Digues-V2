package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractZoneVegetationRepository;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.ParcelleTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.RefFrequenceTraitementVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.TronconTheme;
import fr.sirs.util.SimpleFXEditMode;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.elasticsearch.common.joda.time.LocalDate;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXVegetationTronconThemePane extends FXTronconThemePane {

    public FXVegetationTronconThemePane(TronconTheme.ThemeManager ... groups) {
        super(groups);
    }

    protected class VegetationTronconThemePojoTable<T extends AvecForeignParent> extends TronconThemePojoTable<T>{

        public VegetationTronconThemePojoTable(TronconTheme.ThemeManager<T> group) {
            super(group);

            final TableColumn<ParcelleVegetation, ParcelleVegetation> alertColumn = new AlertTableColumn();
            getTable().getColumns().add((TableColumn) alertColumn);
        }
    }

    @Override
    protected Parent createContent(AbstractTheme.ThemeManager manager) {
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);

        final VegetationTronconThemePojoTable table = new VegetationTronconThemePojoTable(manager);
        table.setDeletor(manager.getDeletor());
        table.editableProperty().bind(editMode.editionState());
        table.foreignParentProperty().bindBidirectional(linearIdProperty());

        return new BorderPane(table, topPane, null, null, null);
    }

    private static class AlertTableColumn extends TableColumn<ParcelleVegetation, ParcelleVegetation> {

        public AlertTableColumn(){
            setGraphic(new ImageView(SIRS.ICON_EXCLAMATION_TRIANGLE_BLACK));
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setCellValueFactory((TableColumn.CellDataFeatures<ParcelleVegetation, ParcelleVegetation> param) -> new SimpleObjectProperty(param.getValue()));
            setCellFactory((TableColumn<ParcelleVegetation, ParcelleVegetation> param) -> new AlertTableCell());
        }
    }


    private static class AlertTableCell extends TableCell<ParcelleVegetation, ParcelleVegetation>{
        @Override
        protected void updateItem(final ParcelleVegetation item, boolean empty){
            super.updateItem(item, empty);

            if(item!=null && item.getPlanId()!=null){

                final Runnable cellUpdater = () -> {

                    // On initialise la plus courte fréquence à la durée du plan
                    final PlanVegetation plan = Injector.getSession().getRepositoryForClass(PlanVegetation.class).get(item.getPlanId());
                    int plusCourteFrequence = plan.getAnneFin()-plan.getAnneDebut();

                    // On récupère toutes les fréquences de traitement de la parcelle
                    final List<String> frequenceIds = new ArrayList<>();
                    final ObservableList<? extends ZoneVegetation> zones = AbstractZoneVegetationRepository.getAllZoneVegetationByParcelleId(item.getId(), Injector.getSession());
                    for(final ZoneVegetation zone : zones){
                        if(zone.getTraitement()!=null && !zone.getTraitement().getHorsGestion()){
                            final String frequenceId = zone.getTraitement().getFrequenceId();
                            if(frequenceId!=null) frequenceIds.add(frequenceId);
                        }
                    }

                    // Si on a récupéré des identifiants de fréquences, il faut obtenir les fréquences elles-mêmes !
                    if(!frequenceIds.isEmpty()){
                        final List<RefFrequenceTraitementVegetation> frequences = Injector.getSession().getRepositoryForClass(RefFrequenceTraitementVegetation.class).get(frequenceIds);
                        for(final RefFrequenceTraitementVegetation frequence : frequences){
                            final int f = frequence.getFrequence();
                            if(f>0 && f<plusCourteFrequence) plusCourteFrequence=f;
                        }
                    }

                    /*
                    On a maintenant la plus courte fréquence de
                    traitement touvée sur toutes les zones de la
                    parcelle.

                    Il faut d'autre part examiner les traitements
                    qui ont eu lieu sur la parcelle.

                    Si l'année courante moint l'année de l'un de ces
                    traitements est inférieure à la fréquence la
                    plus courte qui a été trouvée, c'est que le
                    dernier traitement ayant eu lieu sur la parcelle
                    remonte à moins longtemps que la fréquence
                    minimale. On peut alors arrêter le parcours des
                    traitements car la parcelle est a priori
                    cohérente.

                    Si a l'issue du parcours des traitements on n'a
                    pas trouvé de traitement ayant eu lieu depuis un
                    intervalle de temps inférieur à la fréquence
                    minimale, il faut alors lancer une alerte.
                    */
                    final int anneeCourante = LocalDate.now().getYear();
                    boolean coherent = false;// On part de l'a priori d'une parcelle incohérente
                    for(final ParcelleTraitementVegetation traitement : item.getTraitements()){
                        if(traitement.getDate()!=null){
                            final int anneeTraitement = traitement.getDate().getYear();
                            if(anneeCourante-anneeTraitement<plusCourteFrequence){
                                coherent = true; break;
                            }
                        }
                    }

                    final ImageView image = coherent ? null : new ImageView(SIRS.ICON_EXCLAMATION_TRIANGLE);
                    Platform.runLater(() -> setGraphic(image));
                };

                Injector.getSession().getTaskManager().submit("Vérification de la cohérence de traitement de la parcelle "+item.getDesignation(), cellUpdater);
            }
        }
    }
}
