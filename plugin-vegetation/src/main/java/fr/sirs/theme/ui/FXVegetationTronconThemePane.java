package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractZoneVegetationRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.isCoherent;
import fr.sirs.plugin.vegetation.VegetationSession;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.TronconTheme;
import fr.sirs.util.SimpleFXEditMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXVegetationTronconThemePane extends FXTronconThemePane {

    public FXVegetationTronconThemePane(TronconTheme.ThemeManager ... groups) {
        super(groups);
    }

    protected class VegetationTronconThemePojoTable extends TronconThemePojoTable<ParcelleVegetation>{

        public VegetationTronconThemePojoTable(TronconTheme.ThemeManager<ParcelleVegetation> group) {
            super(group);
// On n'a plus de colonne d'alerte dans la nouvelle spec car elle n'a plus de sens du fait du panneau d'exploitation.
//            final TableColumn<ParcelleVegetation, ParcelleVegetation> alertColumn = new AlertTableColumn();
//            getTable().getColumns().add((TableColumn) alertColumn);
        }

        @Override
        protected ParcelleVegetation createPojo(){
            final PlanVegetation planActif = VegetationSession.INSTANCE.planProperty().get();

            if(planActif==null){
                final Alert alert = new Alert(Alert.AlertType.WARNING, "La création de parcelles ne s'effectue que dans le plan actif.\n Si vous voulez créer une parcelle, veuillez s'il vous plaît activer auparavant un plan de gestion.", ButtonType.OK);
                alert.setResizable(true);
                alert.showAndWait();
                return null;
            }


            final int dureePlan = planActif.getAnneeFin()-planActif.getAnneeDebut();
            if(dureePlan<0){
                throw new IllegalStateException("La durée du plan "+planActif+" ("+dureePlan+") ne doit pas être négative");
            }
            else{
                final ParcelleVegetation created =  (ParcelleVegetation) repo.create();

                if(created!=null){
                    // Association au troçon sélectionné
                    created.setForeignParentId(getForeignParentId());

                    // Mode auto par défaut
                    created.setModeAuto(true);

                    // Association au plan actif
                    created.setPlanId(planActif.getId());

                    // Initialisation des planifications pour les années du plan.
                    PluginVegetation.initPlanifs(created, dureePlan);
                    
                    repo.add(created);
                    getAllValues().add(created);
                }
                return created;
            }
        }

        @Override
        protected void deletePojos(Element... pojos) {
            // Avant de supprimer les parcelles, il faut supprimer les zones de végétation qu'elles contiennent !

            final Map<Class, List<ZoneVegetation>> indexedZones = new HashMap<>();
            for(final Element pojo : pojos){

                if(pojo instanceof ParcelleVegetation){
                    // 1-Pour cela il faut commencer par les récupérer
                    final List<? extends ZoneVegetation> zones = AbstractZoneVegetationRepository.getAllZoneVegetationByParcelleId(pojo.getId(), session);

                    // 2-Ensuite on les indexe en fonction de leur classe
                    for(final ZoneVegetation zone : zones){
                        if(indexedZones.get(zone.getClass())==null) indexedZones.put(zone.getClass(), new ArrayList<>());
                        indexedZones.get(zone.getClass()).add(zone);
                    }
                }
            }


            // 3-Une fois qu'on a indexé les zones par classe, on peut les supprimer en masse
            for(final Class zoneClass : indexedZones.keySet()){
                session.getRepositoryForClass(zoneClass).executeBulkDelete(indexedZones.get(zoneClass));
            }


            // On peut ensuite supprimer les parcelles.
            super.deletePojos(pojos);
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

    /**
     * Colonne d'alerte lorsque les traitements réalisés sur une parcelle paraissent incohérents avec la planification.
     */
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


    /**
     * Cellule d'alerte lorsque les traitements réalisés sur une parcelle paraissent incohérents avec la planification.
     */
    private static class AlertTableCell extends TableCell<ParcelleVegetation, ParcelleVegetation>{
        @Override
        protected void updateItem(final ParcelleVegetation item, boolean empty){
            super.updateItem(item, empty);

            if(item!=null && item.getPlanId()!=null){

                final Runnable cellUpdater = () -> {
                    final ImageView image = isCoherent(item) ? null : new ImageView(SIRS.ICON_EXCLAMATION_TRIANGLE);
                    Platform.runLater(() -> setGraphic(image));
                };

                Injector.getSession().getTaskManager().submit("Vérification de la cohérence de traitement de la parcelle "+item.getDesignation(), cellUpdater);
            }
            else{
                setGraphic(null);
            }
        }
    }
}
