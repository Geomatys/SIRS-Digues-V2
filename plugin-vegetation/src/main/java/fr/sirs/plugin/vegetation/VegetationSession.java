
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ArbreVegetationRepository;
import fr.sirs.core.component.HerbaceeVegetationRepository;
import fr.sirs.core.component.InvasiveVegetationRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.component.PeuplementVegetationRepository;
import fr.sirs.core.model.ArbreVegetation;
import fr.sirs.core.model.HerbaceeVegetation;
import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.ParcelleTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.ZoneVegetation;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

/**
 * Session de vegetation.
 * Contient le plan de gestion en cours.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class VegetationSession {

    public static final VegetationSession INSTANCE = new VegetationSession();

    private final ObjectProperty<PlanVegetation> planProperty = new SimpleObjectProperty<>();

    private final AbstractSIRSRepository<PlanVegetation> planRepo;
    private final ArbreVegetationRepository arbreRepo;
    private final HerbaceeVegetationRepository herbaceeRepo;
    private final InvasiveVegetationRepository invasiveRepo;
    private final ParcelleVegetationRepository parcelleRepo;
    private final PeuplementVegetationRepository peuplementRepo;


    private VegetationSession(){
        final Session session = Injector.getSession();
        planRepo = session.getRepositoryForClass(PlanVegetation.class);
        arbreRepo = (ArbreVegetationRepository)session.getRepositoryForClass(ArbreVegetation.class);
        herbaceeRepo = (HerbaceeVegetationRepository)session.getRepositoryForClass(HerbaceeVegetation.class);
        invasiveRepo = (InvasiveVegetationRepository)session.getRepositoryForClass(InvasiveVegetation.class);
        parcelleRepo = (ParcelleVegetationRepository)session.getRepositoryForClass(ParcelleVegetation.class);
        peuplementRepo = (PeuplementVegetationRepository)session.getRepositoryForClass(PeuplementVegetation.class);

    }

    public AbstractSIRSRepository<PlanVegetation> getPlanRepository() {
        return planRepo;
    }

    public ArbreVegetationRepository getArbreRepo() {
        return arbreRepo;
    }

    public HerbaceeVegetationRepository getHerbaceeRepo() {
        return herbaceeRepo;
    }

    public InvasiveVegetationRepository getInvasiveRepo() {
        return invasiveRepo;
    }

    public ParcelleVegetationRepository getParcelleRepo() {
        return parcelleRepo;
    }

    public PeuplementVegetationRepository getPeuplementRepo() {
        return peuplementRepo;
    }

    public List<HerbaceeVegetation> getHerbaceeByPlan(String planId){
        //herbaceeRepo.getByParcelleId(planId);
        return null;
    }

    /**
     * Plan de gestion actif.
     * 
     * @return
     */
    public ObjectProperty<PlanVegetation> planProperty() {
        return planProperty;
    }

    /**
     * Couleur en fonction de l'etat des traitements.
     * Void : Non planifié, Non traité
     * Orange : Non planifié, traité
     * Rouge : Planifié, Non traité
     * Vert : Planifié, traité
     * 
     * @param parcelle
     * @param planifie
     * @param year
     * @return
     */
    public static Color getColor(ParcelleVegetation parcelle, boolean planifie, int year){
        final int thisYear = LocalDate.now().getYear();

        boolean done = false;
        for(ParcelleTraitementVegetation traitement : parcelle.getTraitements()){
            if(traitement.getDate().getYear() == year){
                done = true;
            }
        }

        if(year>thisYear){
            //pas de couleur pour les années futurs
            return null;
        }

        if(done){
            if(planifie){
                return Color.GREEN;
            }else{
                return Color.ORANGE;
            }
        }else{
            if(planifie){
                return Color.RED;
            }else{
                return null;
            }
        }
    }

//    public List<ZoneVegetation> getAllZonesVegetation(){
//        
//    }
//
//    public List<ZoneVegetation> getAllZonesVegetation(){
//        
//    }
}
