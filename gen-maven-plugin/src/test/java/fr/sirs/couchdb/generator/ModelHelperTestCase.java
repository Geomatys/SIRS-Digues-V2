package fr.sirs.couchdb.generator;

import java.io.File;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreSwitch;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ModelHelperTestCase {

	@Test
	public void test() {
		Resource loadModel = EcoreHelper.loadModel(new File("/Users/cheleb/projects/geomatys/symadrem/sirs-core/model/sirs.ecore"));
		
		EcoreSwitch<EObject> ecoreSwitch = new EcoreSwitch<EObject>() {
			@Override
			public EObject caseEClass(EClass object) {
				System.out.println(object.getName());
				object.isInterface();
				if("Objet".equals(object.getName())) {
				    boolean instance = object.isSuperTypeOf(object);
				    System.out.println(instance);
				    
				    
				}
				return super.caseEClass(object);
			}
			
			
			@Override
			public EObject caseEPackage(EPackage object) {
				for(EObject eObject: object.eContents()) {
					this.doSwitch(eObject);
				}
				return super.caseEPackage(object);
			}
		};
		
		
		
		for(EObject eObject: loadModel.getContents()) {
			ecoreSwitch.doSwitch(eObject);
		}
		
	}

}
