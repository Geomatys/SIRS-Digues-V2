package fr.sirs.couchdb.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
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

    /**
     * Analyze current class to find creation scripts for all join tables implying it.
     *
     * Note : Returned scripts uses "CREATE TABLE IF NOT EXISTS", to avoid errors if another
     * table implied in the join table to create have already done the job.
     * @return All creation scripts of the join tables implying current class.
     */
    public List<String> getJoinTableCreationScripts() {
        final ArrayList<String> scripts = new ArrayList<>();
        final String classKey = Helper.lcFirst(className).concat("Id");
        String refClassName, refKey, joinTableName;
        StringBuilder scriptBuilder;
        for (final EReference ref : getAllMultipleReferences()) {
            if (!ref.isContainment()) {
                refClassName = getClassName(ref);
                /*
                 * Force join table name to be the concatenation of the 2 pointed tables,
                 * conatenated using natural sorting on their name (ensure only one join
                 * table will be created if the same process is launched on the two tables.
                 */
                if (refClassName.compareTo(className) < 0) {
                    joinTableName = refClassName.concat(className);
                } else {
                    joinTableName = className.concat(refClassName);
                }

                refKey = Helper.lcFirst(refClassName).concat("Id");

                scriptBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                        .append(joinTableName)
                        .append(" (")
                            .append(refKey).append(", ")
                            .append(classKey).append(", ")
                            .append("PRIMARY KEY (").append(refKey).append(", ").append(classKey).append(")").append(", ")
                            .append("FOREIGN KEY (").append(refKey).append(") REFERENCES \\\"").append(className).append("\\\"(\\\"id\\\")").append(", ")
                            .append("FOREIGN KEY (").append(classKey).append(") REFERENCES \\\"").append(refClassName).append("\\\"(\\\"id\\\")")
                        .append(") ");

                scripts.add(scriptBuilder.toString());
            }
        }

        return scripts;
    }

    /**
     *
     * @param ref Reference to find a join table to insert reference into.
     * @return Insert statement to use if given reference is part of a join table.
     * Statement first parameter will be reference id, second will be current object id
     * Return null otherwise.
     */
    public String getJoinInsert(final EReference ref) {
        if (ref.isContainment() || !ref.isMany()) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();

        final String refClassName = getClassName(ref);
        final String refKey = Helper.lcFirst(refClassName).concat("Id");
        final String classKey = Helper.lcFirst(className).concat("Id");
        final String joinTableName;
        if (refClassName.compareTo(className) < 0) {
                    joinTableName = refClassName.concat(className);
                } else {
                    joinTableName = className.concat(refClassName);
                }

        return new StringBuilder("INSERT INTO ")
                .append(joinTableName).append(" (").append(refKey).append(", ").append(classKey)
                .append(") values (?, ?)").toString();
    }
}
