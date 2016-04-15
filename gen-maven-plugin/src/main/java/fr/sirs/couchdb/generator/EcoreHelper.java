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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreSwitch;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

public class EcoreHelper {

    static void walk(Log log, Path srcDst, Generator generator, String pakage, Resource model) throws IOException {

        final Path dest = EcoreHelper.prepareDest(pakage, srcDst);

        final EcoreSwitch<EObject> packageSwitch = new EcoreSwitch<EObject>() {
            
            @Override
            public EObject caseEPackage(EPackage object) {

                for (final EObject eObject : object.eContents()) {
                    this.doSwitch(eObject);
                }
                return super.caseEPackage(object);
            }

            @Override
            public EObject caseEClass(EClass eClass) {

                final boolean generateMain = generator.accept(eClass);
                final boolean generateStub = generator.acceptAsStub(eClass);
                if (generateMain || generateStub) {
                    final Path classFile;
                    if(generateMain) classFile = dest.resolve(generator.getFileName(eClass));
                    else classFile = dest.resolve(generator.getStubFileName(eClass));
                    log.debug(classFile.toString());

                    try (final BufferedWriter writer = Files.newBufferedWriter(classFile)) {
                        writer.write(generator.generate(generator.buildHelper(eClass)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                return super.caseEClass(eClass);
            }
        };

        for (final EObject eObject : model.getContents()) {
            packageSwitch.doSwitch(eObject);
        }
    }

    static Path prepareDest(String pakage, Path srcDst) throws IOException {

        final Path dest = srcDst.resolve(pakage.replace('.', '/'));
        Files.createDirectories(dest);
        return dest;
    }

    static Resource loadModel(File model) {
        
        // Create a resource set.
        final ResourceSet resourceSet = new ResourceSetImpl();

        // Register the default resource factory -- only needed for stand-alone!
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("ecore", new EcoreResourceFactoryImpl());

        // Get the URI of the model file.
        final URI fileURI = URI.createFileURI(model.getAbsolutePath());
        
        // load the resource and resolve the proxies
        final Resource resource = resourceSet.createResource(fileURI);

        try {
            resource.load(null);
        } catch (IOException ex) {
            Logger.getLogger(EcoreHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        EcoreUtil.resolveAll(resourceSet);
        
        return resource;
    }

    static boolean isA(EClass eClass, String className) {
        if (className.equals(eClass.getName())) {
            return false;
        }
        for (final EClass superEclass : eClass.getEAllSuperTypes()) {
            if (className.equals(superEclass.getName())) {
                return true;
            }
        }
        return false;
    }
}
