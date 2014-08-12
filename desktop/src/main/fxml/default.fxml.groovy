<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import java.lang.*?>
<?import javafx.scene.layout.*?>

<fx:root hgap="10.0" vgap="10.0" maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308" minHeight="-Infinity" minWidth="-Infinity" type="GridPane" xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
  <columnConstraints>
    <ColumnConstraints hgrow="SOMETIMES" minWidth="10.0" />
    <ColumnConstraints hgrow="SOMETIMES" maxWidth="1.7976931348623157E308" minWidth="10.0" />
  </columnConstraints>
  <rowConstraints>
    <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
    <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
    <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
  </rowConstraints>
   <children><% for(int i = 0; i < param.fields.size(); i++) { field =  param.fields.get(i);
    control = "TextField";
     Class<?> declaringClass = field.getType();
     if(declaringClass==Boolean.class || declaringClass == boolean.class) {
        control = "CheckBox";
     } else if(declaringClass==Calendar.class){
        control = "DatePicker";
     } 
    %>
      <Label fx:id="${field.name}Label" <% if(i>0){ %>  GridPane.rowIndex="<%= i %>" <% } %> text="%${param.classSimpleName}.${field.name}" />      
      <${control} fx:id="${field.name}${control}" <% if(i>0){ %>  GridPane.rowIndex="${i}" <% } %> GridPane.columnIndex="1" /><% } %>
   </children>
</fx:root>