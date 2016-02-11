package fr.sirs.couchdb.generator;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class SQLModelHelper extends ModelHelper {

private static final Set<String> geometryClassNames = new HashSet<String>();
    
    {
        geometryClassNames.add("Geometry");
        geometryClassNames.add("Point");
    }
    
    private int sridCount;

    public SQLModelHelper(EObject eObject, String pack, boolean generateEquals) {
        super(eObject, pack, generateEquals);
        
        
        for (EAttribute att : eClass.getEAllAttributes()) {
            if(geometryClassNames.contains(getClassName(att)))
                sridCount++;
        }

    }

    public boolean hasSrid() {

        return sridCount > 0;
    }

    public int getSridCount() {
        return sridCount;
    }

    public String getSQLType(EStructuralFeature att) {
        String className = getClassName(att);
        switch (className) {
        case "java.util.Date":
        case "LocalDateTime":
            return "Timestamp";
        case "Geometry":
        case "Point":
            if ("TronconDigue".equals(className(eClass.getName())))
                return "LINESTRING CHECK ST_SRID(\\\"" + att.getName()
                        + "\\\") = ?";
            else
                return "POINT CHECK ST_SRID(\\\"" + att.getName() + "\\\") = ?";
        case "java.lang.String":
            return "TEXT";
        case "java.lang.Boolean":
        case "boolean":
            return "BOOL";
        case "java.lang.Integer":
        case "int":
            return "INTEGER";
        case "java.lang.Float":
        case "float":
            return "FLOAT";
        case "java.lang.Double":
        case "double":
            return "DOUBLE";
        default:
            break;
        }
        return "TEXT";
    }

    public String getSQLParam(EStructuralFeature att) {
        String className = getClassName(att);
        switch (className) {
        case "java.util.Date":
            return "Timestamp";
        case "Geometry":
        case "Point":
        case "java.lang.String":
            return "String";
        case "java.lang.Boolean":
        case "boolean":
            return "Boolean";
        case "java.lang.Integer":
        case "int":
            return "Int";
        case "java.lang.Float":
        case "float":
            return "Float";
        case "java.lang.Double":
        case "double":
            return "Double";
        default:
            break;
        }
        return "Object";
    }

    public String sqlGetter(EStructuralFeature esf) {

        switch (getClassName(esf)) {
        case "java.util.Date":
            if(esf.getEAnnotation(ANNOTATION_LOCAL_DATE_TIME)!=null)
                return getter(esf) + "().toInstant(ZoneOffset.UTC).toEpochMilli()";
            else if(esf.getEAnnotation(ANNOTATION_LOCAL_DATE)!=null)
                return getter(esf) + "().atTime(LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC).toEpochMilli()";
            else // DEFAULT: LocalDateTime 
                return getter(esf) + "().toInstant(ZoneOffset.UTC).toEpochMilli()";
        case "Point":
        case "Geometry":
            return getter(esf) + "().toText()";
        default:
            return getter(esf) + "()";
        }
    }

    public boolean isGeometric(EStructuralFeature att) {
        return geometryClassNames.contains(getClassName(att));
    }
}
