
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
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;

/**
 * Session de vegetation.
 * Contient le plan de gestion en cours.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class VegetationSession {

    public static final VegetationSession INSTANCE = new VegetationSession();
    public static final String ETAT_PLANIFIE_TRAITE = "Planifié / Traité";
    public static final String ETAT_PLANIFIE_NONTRAITE = "Planifié / Non traité";
    public static final String ETAT_NONPLANIFIE_TRAITE = "Non planifié / Traité";
    public static final String ETAT_NONPLANIFIE_NONTRAITE = "Non planifié / Non traité";
    public static final String ETAT_PLANIFIE_FUTUR = "Planifié (futur)";
    public static final String ETAT_NONPLANIFIE_FUTUR = "Non planifié (futur)";

    private final ObjectProperty<PlanVegetation> planProperty = new SimpleObjectProperty<>();

    private final AbstractSIRSRepository<PlanVegetation> planRepo;
    private final ArbreVegetationRepository arbreRepo;
    private final HerbaceeVegetationRepository herbaceeRepo;
    private final InvasiveVegetationRepository invasiveRepo;
    private final ParcelleVegetationRepository parcelleRepo;
    private final PeuplementVegetationRepository peuplementRepo;

    private final MapItem vegetationGroup;

    private VegetationSession(){
        final Session session = Injector.getSession();
        planRepo = session.getRepositoryForClass(PlanVegetation.class);
        arbreRepo = (ArbreVegetationRepository)session.getRepositoryForClass(ArbreVegetation.class);
        herbaceeRepo = (HerbaceeVegetationRepository)session.getRepositoryForClass(HerbaceeVegetation.class);
        invasiveRepo = (InvasiveVegetationRepository)session.getRepositoryForClass(InvasiveVegetation.class);
        parcelleRepo = (ParcelleVegetationRepository)session.getRepositoryForClass(ParcelleVegetation.class);
        peuplementRepo = (PeuplementVegetationRepository)session.getRepositoryForClass(PeuplementVegetation.class);

        vegetationGroup = MapBuilder.createItem();
        vegetationGroup.setName("Végétation");
        vegetationGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
    }

    public MapItem getVegetationGroup() {
        return vegetationGroup;
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
     * String en fonction de l'etat des traitements.
     * Voir constantes : ETAT_X
     * 
     * @param parcelle
     * @param planifie
     * @param year
     * @return 
     */
    public static String getParcelleEtat(ParcelleVegetation parcelle, boolean planifie, int year){
        final int thisYear = LocalDate.now().getYear();

        boolean done = false;
        for(ParcelleTraitementVegetation traitement : parcelle.getTraitements()){
            if(traitement.getDate().getYear() == year){
                done = true;
            }
        }

        if(year>thisYear){
            //pas de couleur pour les années futurs
            return planifie ? ETAT_PLANIFIE_FUTUR : ETAT_NONPLANIFIE_FUTUR;
        }

        if(done){
            if(planifie){
                return ETAT_PLANIFIE_TRAITE;
            }else{
                return ETAT_NONPLANIFIE_TRAITE;
            }
        }else{
            if(planifie){
                return ETAT_PLANIFIE_NONTRAITE;
            }else{
                return ETAT_NONPLANIFIE_NONTRAITE;
            }
        }
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
    public static Color getParcelleEtatColor(ParcelleVegetation parcelle, boolean planifie, int year){
        final String state = getParcelleEtat(parcelle, planifie, year);
        switch(state){
            case ETAT_PLANIFIE_TRAITE : return Color.GREEN;
            case ETAT_PLANIFIE_NONTRAITE : return Color.RED;
            case ETAT_NONPLANIFIE_TRAITE : return Color.ORANGE;
            case ETAT_NONPLANIFIE_NONTRAITE : return null;
            default : return null;
        }
    }

}
