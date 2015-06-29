package fr.sirs.theme.ui;

import fr.sirs.core.model.AvecForeignParent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public class ForeignParentPojoTable<T extends AvecForeignParent> extends PojoTable {
    
    /** The element to set as foreignParent for any created element using {@linkplain #createPojo() }. 
     On the contrary to the parent, the foreing parent purpose is not to contain the created pojo, 
     * but nor to reference it as an owner element. the foreignParent id is only refenced by the table elements.*/
    protected final StringProperty foreignParentIdProperty = new SimpleStringProperty();
    
    /**
     * Définit l'élément en paramètre comme principal référent de tout élément créé via cette table.
     * 
     * @param foreignParentId L'id de l'élément qui doit devenir le parent référencé de tout objet créé via 
     * la PojoTable.
     */
    public void setForeignParentId(final String foreignParentId) {
        foreignParentIdProperty.set(foreignParentId);
    }
    
    /**
     * 
     * @return L'id de l'élément référencé par tout élément créé via 
     * cette table.
     */
    public String getForeignParentId() {
        return foreignParentIdProperty.get();
    }
        
    /**
     * 
     * @return La propriété contenant l'id de l'élément à référencer par
     *  tout élément créé via cette table. Jamais nulle, mais peut-être vide.
     */
    public StringProperty foreignParentProperty() {
        return foreignParentIdProperty;
    }

    public ForeignParentPojoTable(Class<T> pojoClass, String title) {
        super(pojoClass, title);
    }
    
    @Override
    protected T createPojo() {
        T created = null;
        if (repo != null) {
            created = (T) repo.create();
        } 
        else if (pojoClass != null) {
            try {
                created = (T) session.getElementCreator().createElement(pojoClass);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if(created!=null){
            created.setForeignParentId(getForeignParentId());
            repo.add(created);
            uiTable.getItems().add(created);
        }
        return created;
    }
}
