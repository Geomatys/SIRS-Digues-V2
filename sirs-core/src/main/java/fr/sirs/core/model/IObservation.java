

package fr.sirs.core.model;

import fr.sirs.util.property.Reference;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public interface IObservation extends  Element, AvecDateMaj {

    StringProperty evolutionProperty();

    StringProperty suiteProperty();

    StringProperty suiteApporterIdProperty();

    ObjectProperty<LocalDate> dateProperty();


    String getEvolution();


    void setEvolution(String evolution);

    String getSuite();


    void setSuite(String suite);

    @Reference(ref=RefSuiteApporter.class)
    String getSuiteApporterId();

    void setSuiteApporterId(String suiteApporterId);

    ObservableList<? extends AbstractPhoto> getPhotos();

    LocalDate getDate();

    void setDate(LocalDate date);

}
