/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
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
