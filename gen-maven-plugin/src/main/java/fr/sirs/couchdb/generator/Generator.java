package fr.sirs.couchdb.generator;


import org.eclipse.emf.ecore.EClass;

public interface Generator {

    String generate(Object object);

    Helper buildHelper(EClass eClass);

    String getFileName(EClass eClass);

    boolean accept(EClass eClass);

    default boolean acceptAsStub(EClass eClass){
        return false;
    };

    default String getStubFileName(EClass eClass){
        return "";
    };
}
