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
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.core.component.DesordreRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.ObservationReseauHydrauliqueFerme;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.StationPompage;
import static fr.sirs.util.JRDomWriterReseauFermeSheet.DESORDRE_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterReseauFermeSheet.OBSERVATION_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterReseauFermeSheet.PHOTO_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterReseauFermeSheet.RESEAU_OUVRAGE_TABLE_DATA_SOURCE;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 * Source de données de remplissage des fiches de réseaux hydrauliques fermés.
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ReseauHydrauliqueFermeDataSource extends ObjectDataSource<ReseauHydrauliqueFerme> {

    public ReseauHydrauliqueFermeDataSource(Iterable<ReseauHydrauliqueFerme> iterable) {
        super(iterable);
    }

    public ReseauHydrauliqueFermeDataSource(final Iterable<ReseauHydrauliqueFerme> iterable, final Previews previewLabelRepository){
        super(iterable, previewLabelRepository);
    }

    public ReseauHydrauliqueFermeDataSource(final Iterable<ReseauHydrauliqueFerme> iterable, final Previews previewLabelRepository, final SirsStringConverter stringConverter){
        super(iterable, previewLabelRepository, stringConverter);
    }

    @Override
    public Object getFieldValue(final JRField jrf) throws JRException {

        final String name = jrf.getName();

        if(PHOTO_DATA_SOURCE.equals(name)){
            final List<Photo> photos = new ArrayList<>();
            for(final ObservationReseauHydrauliqueFerme observation : currentObject.getObservations()){
                if(observation.getPhotos()!=null && !observation.getPhotos().isEmpty()){
                    photos.addAll(observation.getPhotos());
                }
            }
            if(currentObject.getPhotos()!=null && !currentObject.getPhotos().isEmpty()){
                photos.addAll(currentObject.getPhotos());
            }
            return new ObjectDataSource<>(photos, previewRepository, stringConverter);
        }
        else if(OBSERVATION_TABLE_DATA_SOURCE.equals(name)){
            final ObservableList<ObservationReseauHydrauliqueFerme> observations = currentObject.getObservations();
            observations.sort(OBSERVATION_COMPARATOR);
            return new ObjectDataSource<>(observations, previewRepository, stringConverter);
        }
        else if(RESEAU_OUVRAGE_TABLE_DATA_SOURCE.equals(name)){
            
            final List<List<? extends ObjetReseau>> retrievedLists = new ArrayList();
            retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageHydrauliqueAssocie.class).get(currentObject.getOuvrageHydrauliqueAssocieIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauHydrauliqueCielOuvert.class).get(currentObject.getReseauHydrauliqueCielOuvertIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(StationPompage.class).get(currentObject.getStationPompageIds()));

            final List<ObjetReseau> reseauOuvrageList = new ArrayList<>();
            for(final List<? extends ObjetReseau> candidate : retrievedLists){
                if(candidate!=null && !candidate.isEmpty()){
                    reseauOuvrageList.addAll(candidate);
                }
            }
            return new ObjectDataSource<>(reseauOuvrageList, previewRepository, stringConverter);
        }
        else if(DESORDRE_TABLE_DATA_SOURCE.equals(name)){
            final DesordreRepository desordreRepository = (DesordreRepository) Injector.getSession().getRepositoryForClass(Desordre.class);
            final List<Desordre> all = desordreRepository.getAll();
            final List<JRDesordreTableRow> desordreRows = new ArrayList<>();
            for(final Desordre des : all){
                if(des.getReseauHydrauliqueFermeIds().contains(currentObject.getId())){
                    final List<Observation> observations = des.getObservations();
                    for(final Observation obs : observations){
                        desordreRows.add(new JRDesordreTableRow(obs.getDate(), obs.getDesignation(), des.getDesignation(), obs.getUrgenceId(), des.getCommentaire()));
                    }
                }
            }
            return new ObjectDataSource<>(desordreRows, previewRepository, stringConverter);
        }
        else return super.getFieldValue(jrf);
    }

}
