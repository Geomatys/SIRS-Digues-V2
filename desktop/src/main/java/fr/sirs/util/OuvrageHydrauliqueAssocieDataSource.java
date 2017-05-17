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
import static fr.sirs.core.SirsCore.DIGUE_ID_FIELD;
import fr.sirs.core.component.DesordreRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.ObservationOuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.util.JRDomWriterOuvrageAssocieSheet.RESEAU_FERME_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterOuvrageAssocieSheet.DESORDRE_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterOuvrageAssocieSheet.OBSERVATION_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterOuvrageAssocieSheet.PHOTO_DATA_SOURCE;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.collections.ObservableList;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 * Source de données de remplissage des fiches de réseaux hydrauliques fermés.
 *
 * @author Samuel Andrés (Geomatys)
 */
public class OuvrageHydrauliqueAssocieDataSource extends ObjectDataSource<OuvrageHydrauliqueAssocie> {
    
    /**
     * Groupe par désignation de désordre et classe par date décroissante à l'intérieur de chaque groupe.
     * Inutilisé (était utilisé pour ordonner les lignes du tableau des observations/désordres).
     */
    static final Comparator<JRDesordreTableRow> DESORDRE_RESEAU_COMPARATOR = (JRDesordreTableRow d1, JRDesordreTableRow d2)->{
        final LocalDate date1 = d1.getObservationDate();
        final String des1 = d1.getDesordreDesignation();
        final LocalDate date2 = d2.getObservationDate();
        final String des2 = d2.getDesordreDesignation();
        if(des1==null && des2==null) return 0;
        else if(des1==null || des2==null) return (des1==null) ? -2 : 2; // On regroupe par désignation de désordre.
        else {
            final int compareTo = des1.compareTo(des2);
            if (compareTo!=0) return compareTo;
            else if(date1==null && date2==null) return 0;
            else if(date1==null || date2==null) return (date1==null) ? 1 : -1;
            else return -date1.compareTo(date2); // Par date décroissante : la plus récente en tête.
        }
    };
    
    /**
     * Compare par date de "désordre" (lesquels sont en réalité des hybrides entre des désordres et des observations.
     * Demande SYM-1496, commentaire de Jordan Perrin du 21 mars 2017.
     */
    static final Comparator<JRDesordreTableRow> DESORDRE_RESEAU_DATE_COMPARATOR = (JRDesordreTableRow d1, JRDesordreTableRow d2)->{
        final LocalDate date1 = d1.getObservationDate();
        final LocalDate date2 = d2.getObservationDate();
        if(date1==null && date2==null) return 0;
        else if(date1==null || date2==null) return (date1==null) ? 1 : -1;
        else return -date1.compareTo(date2); // Par date décroissante : la plus récente en tête.
    };

    public OuvrageHydrauliqueAssocieDataSource(Iterable<OuvrageHydrauliqueAssocie> iterable) {
        super(iterable);
    }

    public OuvrageHydrauliqueAssocieDataSource(final Iterable<OuvrageHydrauliqueAssocie> iterable, final Previews previewLabelRepository){
        super(iterable, previewLabelRepository);
    }

    public OuvrageHydrauliqueAssocieDataSource(final Iterable<OuvrageHydrauliqueAssocie> iterable, final Previews previewLabelRepository, final SirsStringConverter stringConverter){
        super(iterable, previewLabelRepository, stringConverter);
    }

    @Override
    public Object getFieldValue(final JRField jrf) throws JRException {

        final String name = jrf.getName();

        if(DIGUE_ID_FIELD.equals(name)){
            if(currentObject!=null&&currentObject.getLinearId()!=null){
                final TronconDigue troncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).get(currentObject.getLinearId());
                if(troncon!=null&&troncon.getDigueId()!=null){
                    return parsePropertyValue(troncon.getDigueId(), Digue.class, String.class);
                }
            }
            return null;
        }
        else if(PHOTO_DATA_SOURCE.equals(name)){
            final List<Photo> photos = new ArrayList<>();
            for(final ObservationOuvrageHydrauliqueAssocie observation : currentObject.getObservations()){
                if(observation.getPhotos()!=null && !observation.getPhotos().isEmpty()){
                    photos.addAll(observation.getPhotos());
                }
            }
            if(currentObject.getPhotos()!=null && !currentObject.getPhotos().isEmpty()){
                photos.addAll(currentObject.getPhotos());
            }
            photos.sort(PHOTO_COMPARATOR);
            return new ObjectDataSource<>(photos, previewRepository, stringConverter);
        }
        else if(OBSERVATION_TABLE_DATA_SOURCE.equals(name)){
            final ObservableList<ObservationOuvrageHydrauliqueAssocie> observations = currentObject.getObservations();
            observations.sort(OBSERVATION_COMPARATOR);
            return new ObjectDataSource<>(observations, previewRepository, stringConverter);
        }
        else if(RESEAU_FERME_TABLE_DATA_SOURCE.equals(name)){

            final List<ReseauHydrauliqueFerme> reseauOuvrageList = Injector.getSession().getRepositoryForClass(ReseauHydrauliqueFerme.class).get(currentObject.getReseauHydrauliqueFermeIds());
            
            reseauOuvrageList.sort(ELEMENT_COMPARATOR);
            return new ObjectDataSource<>(reseauOuvrageList, previewRepository, stringConverter);
        }
        else if(DESORDRE_TABLE_DATA_SOURCE.equals(name)){
            final DesordreRepository desordreRepository = (DesordreRepository) Injector.getSession().getRepositoryForClass(Desordre.class);
            final List<Desordre> all = desordreRepository.getAll();
            final List<JRDesordreTableRow> desordreRows = new ArrayList<>();
            for(final Desordre des : all){
                if(des.getOuvrageHydrauliqueAssocieIds().contains(currentObject.getId())){
                    final List<Observation> observations = des.getObservations();
                    for(final Observation obs : observations){
                        desordreRows.add(new JRDesordreTableRow(obs.getDate(), des.getDesignation(), obs.getDesignation(), obs.getUrgenceId(), des.getCommentaire()));
                    }
                }
            }
            desordreRows.sort(DESORDRE_RESEAU_DATE_COMPARATOR);
            return new ObjectDataSource<>(desordreRows, previewRepository, stringConverter);
        }
        else return super.getFieldValue(jrf);
    }

}
