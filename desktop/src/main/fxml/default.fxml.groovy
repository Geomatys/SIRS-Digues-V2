<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import java.lang.*?>
<?import javafx.scene.layout.*?>

<GridPane maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="400.0" prefWidth="600.0" xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
  <columnConstraints>
    <ColumnConstraints hgrow="SOMETIMES" minWidth="10.0" prefWidth="100.0" />
    <ColumnConstraints hgrow="SOMETIMES" minWidth="10.0" prefWidth="100.0" />
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
      <Label fx:id="${field.name}Label" <% if(i>0){ %>  GridPane.rowIndex="<%= i %>" <% } %> text="${field.name}" />      
      <${control} fx:id="${field.name}${control}" <% if(i>0){ %>  GridPane.rowIndex="${i}" <% } %> GridPane.columnIndex="1" /><% } %>
   </children>
</GridPane>
