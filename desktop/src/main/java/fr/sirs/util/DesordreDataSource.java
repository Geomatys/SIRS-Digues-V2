package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.VoieDigue;
import static fr.sirs.util.JRDomWriterDesordreSheet.OBSERVATION_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.PHOTO_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.PRESTATION_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.RESEAU_OUVRAGE_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.VOIRIE_TABLE_DATA_SOURCE;
import java.util.ArrayList;
import java.util.List;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DesordreDataSource extends ObjectDataSource<Desordre> {

    public DesordreDataSource(Iterable<Desordre> iterable) {
        super(iterable);
    }

    public DesordreDataSource(final Iterable<Desordre> iterable, final Previews previewLabelRepository){
        super(iterable, previewLabelRepository);
    }
    
    public DesordreDataSource(final Iterable<Desordre> iterable, final Previews previewLabelRepository, final SirsStringConverter stringConverter){
        super(iterable, previewLabelRepository, stringConverter);
    }

    @Override
    public Object getFieldValue(final JRField jrf) throws JRException {

        final String name = jrf.getName();

        if(PHOTO_DATA_SOURCE.equals(name)){
            final List<Photo> photos = new ArrayList<>();
            for(final Observation observation : currentObject.observations){
                if(observation.photos!=null && !observation.photos.isEmpty()){
                    photos.addAll(observation.photos);
                }
            }
            return new ObjectDataSource<>(photos, previewRepository, stringConverter);
        }
        else if(OBSERVATION_TABLE_DATA_SOURCE.equals(name)){
            return new ObjectDataSource<>(currentObject.getObservations(), previewRepository, stringConverter);
        }
        else if(PRESTATION_TABLE_DATA_SOURCE.equals(name)){
            return new ObjectDataSource<>(Injector.getSession().getRepositoryForClass(Prestation.class).get(currentObject.getPrestationIds()), previewRepository, stringConverter);
        }
        else if(RESEAU_OUVRAGE_TABLE_DATA_SOURCE.equals(name)){

            final List<ObjetReseau> reseauOuvrageList = new ArrayList<>();
            final List<List<? extends ObjetReseau>> retrievedLists = new ArrayList();
            retrievedLists.add(Injector.getSession().getRepositoryForClass(EchelleLimnimetrique.class).get(currentObject.getEchelleLimnimetriqueIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageParticulier.class).get(currentObject.getOuvrageParticulierIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauTelecomEnergie.class).get(currentObject.getReseauTelecomEnergieIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageTelecomEnergie.class).get(currentObject.getOuvrageTelecomEnergieIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageHydrauliqueAssocie.class).get(currentObject.getOuvrageHydrauliqueAssocieIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauHydrauliqueCielOuvert.class).get(currentObject.getReseauHydrauliqueCielOuvertIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauHydrauliqueFerme.class).get(currentObject.getReseauHydrauliqueFermeIds()));

            for(final List candidate : retrievedLists){
                if(candidate!=null && !candidate.isEmpty()){
                    reseauOuvrageList.addAll(candidate);
                }
            }

            return new ObjectDataSource<>(reseauOuvrageList, previewRepository, stringConverter);
        }
        else if(VOIRIE_TABLE_DATA_SOURCE.equals(name)){

            final List<ObjetReseau> voirieList = new ArrayList<>();
            final List<List<? extends ObjetReseau>> retrievedLists = new ArrayList();
            retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageVoirie.class).get(currentObject.getOuvrageVoirieIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(VoieDigue.class).get(currentObject.getVoieDigueIds()));

            for(final List candidate : retrievedLists){
                if(candidate!=null && !candidate.isEmpty()){
                    voirieList.addAll(candidate);
                }
            }

            return new ObjectDataSource<>(voirieList, previewRepository, stringConverter);
        }
        else return super.getFieldValue(jrf);
    }
    
}
