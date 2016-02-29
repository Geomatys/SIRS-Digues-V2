package fr.sirs.couchdb.generator;

import org.eclipse.emf.ecore.EObject;

public class RepositoryHelper extends Helper {
    

    private final String classQualifiedName;

    private boolean customized;

    public RepositoryHelper(EObject eObject, String pack, String modelPackage) {
        super(eObject);
        this.pakage = pack;
        this.classQualifiedName = modelPackage + "." + getClassName();
    }
    
    public boolean hasByLinearView(){
        return EcoreHelper.isA(eClass, OBJET_CLASS_NAME) 
                || EcoreHelper.isA(eClass, ABSTRACT_POSITION_DOCUMENT_CLASS_NAME)
                || PROPRIETE_TRONCON_CLASS_NAME.equals(eClass.getName())
                || GARDE_TRONCON_CLASS_NAME.equals(eClass.getName());
    }
    
    public boolean hasByDocumentView(){
        return EcoreHelper.isA(eClass, ABSTRACT_POSITION_DOCUMENT_ASSOCIABLE_CLASS_NAME);
    }

//    public String getRepositoryInterface() {
//        return repositoryInterface;
//    }
//    
//    public String getRepositoryInterfaceSimpleName() {
//        return repositoryInterfaceSimpleName;
//    }
    
    String getImplements() {
        // No need, Abstract class will do the job (see AbstractSIRSRepository)
//        if(repositoryInterfaceSimpleName!=null)
//            return "implements " + repositoryInterfaceSimpleName + "<" +getClassName()+">";
        return "";
    }
    
    public String getModelQualifiedClassName() {
        return classQualifiedName;
    }

    public void setCustomized(boolean customized) {
        this.customized = customized;
    }

    public boolean isCustomized() {
        return customized;
    }


    public String getRepositoryCompleteClassName() {
        return pakage+"."+getRepositoryClassName();
    }


    public String getRepositoryClassName() {
        if (customized)
            return super.getClassName() + "RepositoryGen";
        return super.getClassName() + "Repository";
    }

}
