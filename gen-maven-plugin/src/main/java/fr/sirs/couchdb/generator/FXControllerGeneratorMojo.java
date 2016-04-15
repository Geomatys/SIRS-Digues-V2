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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;

@Mojo(name = "fxcontroller", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresProject = true)
public class FXControllerGeneratorMojo extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/fx-controller")
    private File outputDirectory;

    @Parameter(required = true)
    private File model;

    @Parameter(required = true)
    private String packageName;

    private String mainSrcJava;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        mainSrcJava = project.getBuild().getSourceDirectory();
        
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());

        final Resource resource = EcoreHelper.loadModel(model);

        try {
            generateController(resource, packageName, outputDirectory.toPath());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void generateController(Resource resource, String packageName, Path srcDst) throws IOException {

        final Generator generator = new Generator() {

            final FXMLControllerGenerator fxmlControllerGenerator = new FXMLControllerGenerator();

            @Override
            public String getFileName(EClass eClass) {
                return "FX" + eClass.getName() + "Pane.java";
            }

            @Override
            public String getStubFileName(EClass eClass) {
                return "FX" + eClass.getName() + "PaneStub.java";
            }

            @Override
            public String generate(Object object) {
                return fxmlControllerGenerator.generate(object);
            }

            @Override
            public Helper buildHelper(EClass eClass) {
                return new ModelHelper(eClass, packageName, false, isStub(eClass));
            }

            private boolean isStub(EClass eClass){
                final String classFile = getFileName(eClass);
                final Path path = Paths.get(mainSrcJava, packageName.replaceAll("\\.", "/"), classFile);
                return Files.exists(path) && !Helper.isReferenceType(eClass);
            }

            @Override
            public boolean accept(EClass eClass) {
                final String classFile = getFileName(eClass);
                final Path path = Paths.get(mainSrcJava, packageName.replaceAll("\\.", "/"), classFile);
                if(Files.exists(path)) {
                    getLog().info(classFile + " is overriden");
                    return false;
                }
                else return !Helper.isReferenceType(eClass);
            }

            @Override
            public boolean acceptAsStub(EClass eClass){
                final String classFile = getFileName(eClass);
                if(isStub(eClass)) {
                    getLog().info("Write stub for " + classFile);
                    return true;
                }
                else return false;
            }
        };
        EcoreHelper.walk(getLog(), srcDst, generator, packageName, resource);
    }
}
