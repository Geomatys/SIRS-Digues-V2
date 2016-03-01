package fr.sirs.core.model;

import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T> Type of photograph associated to this object.
 */
public interface AvecPhotos<T extends AbstractPhoto> {
    
    List<T> getPhotos();

    void setPhotos(List<T> photos);
}
