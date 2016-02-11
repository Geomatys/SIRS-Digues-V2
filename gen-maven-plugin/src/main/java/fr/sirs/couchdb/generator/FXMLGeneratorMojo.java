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

@Mojo(name = "fxml", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresProject = true)
public class FXMLGeneratorMojo extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/generated-resources/fxml")
    private File outputDirectory;


    
    @Parameter(required = true)
    private File model;

    @Parameter(required = true)
    private String packageName;

    public File getOutputDirectory() {
        return outputDirectory;
    }
    
    
    private String mainResrcJava;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        mainResrcJava = project.getBuild().getSourceDirectory().replace("/java","/resources");
        
        org.apache.maven.model.Resource res = new org.apache.maven.model.Resource();
        res.setDirectory(outputDirectory.getAbsolutePath());
        project.addResource(res);
        

        final Resource resource = EcoreHelper.loadModel(model);

        try {
            generateFxml(resource, outputDirectory.toPath());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    private void generateFxml(Resource model, Path srcDst) throws IOException {

        final Generator generator = new Generator() {
            final FXMLGenerator fxmlGenerator = new FXMLGenerator();

            @Override
            public String getFileName(EClass eClass) {
                return "FX" + eClass.getName() + "Pane.fxml";
            }

            @Override
            public String getStubFileName(EClass eClass) {
                return "FX" + eClass.getName() + "PaneStub.fxml";
            }

            @Override
            public String generate(Object object) {
                // TODO Auto-generated method stub
                return fxmlGenerator.generate(object);
            }

            @Override
            public Helper buildHelper(EClass eClass) {
                return new ModelHelper(eClass, packageName, false);
            }

            @Override
            public boolean accept(EClass eClass) {
                String classFile = getFileName(eClass);
                Path path = Paths.get(mainResrcJava, packageName.replaceAll("\\.", "/"), classFile);
                if(Files.exists(path)) {
                    getLog().info(classFile + " is overriden");
                    return false;
                }
                else return !Helper.isReferenceType(eClass);
            }

            @Override
            public boolean acceptAsStub(EClass eClass) {
                String classFile = getFileName(eClass);
                Path path = Paths.get(mainResrcJava, packageName.replaceAll("\\.", "/"), classFile);
                if(Files.exists(path) && !Helper.isReferenceType(eClass)) {
                    getLog().info("Write stub for " + classFile);
                    return true;
                }
                else return false;
            }
        };
        EcoreHelper.walk(getLog(), srcDst, generator, packageName, model);
    }
}