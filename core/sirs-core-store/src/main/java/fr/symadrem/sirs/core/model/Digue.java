
package fr.symadrem.sirs.core.model;

import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
public class Digue  extends CouchDbDocument  {


    private static final String type = "Digue";
    
    public String getType() {
        return type;
    }
    
    void setType(String type){
    }
    
        
    public StringProperty  label = new SimpleStringProperty();
        
    public StringProperty  comment = new SimpleStringProperty();
    
 
    //
    // References
    //
    private Set<String> tronconsIds;
    
  
  
      
    public String getLabel(){
    	return this.label.get();
    }
    
    public void setLabel(String label){
    	this.label.set(label);
    }
        
    public String getComment(){
    	return this.comment.get();
    }
    
    public void setComment(String comment){
    	this.comment.set(comment);
    }
    

  
  
    
    public Set<String> getTronconsIds(){
    	return this.tronconsIds;
    }
    
    public void setTronconsIds(Set<String> tronconsIds){
    	this.tronconsIds = tronconsIds;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Digue ");
      builder.append("label: ");
      builder.append(label.get());
      builder.append(", ");
      builder.append("comment: ");
      builder.append(comment.get());
      return builder.toString();
  }


}

