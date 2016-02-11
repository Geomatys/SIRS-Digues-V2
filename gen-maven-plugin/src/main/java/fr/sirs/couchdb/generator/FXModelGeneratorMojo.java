package fr.sirs.couchdb.generator;

import static fr.sirs.couchdb.generator.Helper.CLASS_ABREGE_BUNDLE_FIELD_NAME;
import static fr.sirs.couchdb.generator.Helper.CLASS_BUNDLE_FIELD_NAME;
import static fr.sirs.couchdb.generator.Helper.CLASS_PLURAL_BUNDLE_FIELD_NAME;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreSwitch;


@Mojo(name = "fxmodel", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresProject = true)
public class FXModelGeneratorMojo extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/fxmodel")
    private File outputDirectory;

    @Parameter(defaultValue = "${project.basedir}/src/main/resources")
    private File srcMainResourceDirectory;

    @Parameter(defaultValue = "${project.build.directory}/fxmodel/fxml")
    private File generatedDirectory;

    @Parameter(required = true)
    private File model;

    @Parameter(required = true)
    private String modelPackage;

    @Parameter(required = true)
    private String repositoryPackage;

    @Parameter(required = true)
    private String repositoryInterface;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Parameter(defaultValue = "false")
    private boolean generateEquals;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());

        final Resource resource = EcoreHelper.loadModel(model);

        try {
            generatePojo(resource, modelPackage, outputDirectory.toPath());
            generateProperties(resource);
            generateRepository(resource, repositoryPackage, modelPackage, outputDirectory.toPath());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private void generateRepository(Resource model, String pakage, String modelPakage, Path src) throws IOException {

        final Path srcMainJavaPackage = EcoreHelper.prepareDest(pakage, Paths.get(project.getBuild().getSourceDirectory()));

        final Generator generator = new Generator() {
            final RepositoryGenerator repositoryGenerator = new RepositoryGenerator();

            @Override
            public String getFileName(EClass eClass) {
                return eClass.getName() + "Repository.java";
            }

            @Override
            public String generate(Object object) {
                return repositoryGenerator.generate(object);
            }

            @Override
            public Helper buildHelper(EClass eClass) {
                return new RepositoryHelper(eClass, pakage, modelPakage, repositoryInterface);
            }

            @Override
            public boolean accept(EClass eClass) {
                if (!Helper.hasDocument(eClass)) {
                    return false;
                }

                return !Files.exists(srcMainJavaPackage.resolve(getFileName(eClass)));
            }
        };

        EcoreHelper.walk(getLog(), src, generator, repositoryPackage, model);
    }

    private void generatePojo(Resource model, String pakage, Path srcDst)
            throws IOException {

        Generator generator = new Generator() {
            FXModelGenerator fxModelGenerator = new FXModelGenerator();

            @Override
            public String getFileName(EClass eClass) {
                return eClass.getName() + ".java";
            }

            @Override
            public String generate(Object object) {
                return fxModelGenerator.generate(object);
            }

            @Override
            public Helper buildHelper(EClass eClass) {
                return new ModelHelper(eClass, pakage, generateEquals);
            }

            @Override
            public boolean accept(EClass eClass) {
                return true;
            }
        };

        EcoreHelper.walk(getLog(), srcDst, generator, pakage, model);
    }

    private String generateClassAccronym(final String simpleName){
        final Pattern pattern = Pattern.compile("(\\p{Upper})\\p{Lower}*");
        final Matcher matcher = pattern.matcher(simpleName);
        String result="";
        while(matcher.find()){
            result+=matcher.group(1);
        }
        return result;
    }

    private void generateProperties(Resource resource) {

        EcoreSwitch<EObject> packageSwitch = new EcoreSwitch<EObject>() {
            @Override
            public EObject caseEPackage(EPackage object) {

                for (EObject eObject : object.eContents()) {
                    this.doSwitch(eObject);
                }

                return super.caseEPackage(object);
            }

            @Override
            public EObject caseEClass(EClass eClass) {
                if (eClass.isInterface())
                    return super.caseEClass(eClass);

                String filename = eClass.getName() + ".properties";

                Path generated = generatedDirectory
                        .toPath()
                        .resolve(
                                FXModelGeneratorMojo.this.modelPackage.replace(
                                        '.', '/')).resolve(filename);

                Path output = srcMainResourceDirectory
                        .toPath()
                        .resolve(
                                FXModelGeneratorMojo.this.modelPackage.replace(
                                        '.', '/')).resolve(filename);

                final Properties properties = new Properties();
                int addedKeys = 0;
                try {
                    Files.createDirectories(generated.getParent());
                    Files.createDirectories(output.getParent());

                    if (Files.isReadable(output)) {
                        try (final BufferedReader existing = Files.newBufferedReader(output)) {
                            properties.load(existing);
                        }
                    }
                } catch (IOException e1) {
                    throw new RuntimeException("Properties file is unreadable : " + output.toString(), e1);
                }

                for (EStructuralFeature attribute : eClass.getEAllStructuralFeatures()) {
                    final String attrName = attribute.getName();
                    if (!properties.containsKey(attrName)) {
                        final String defaultValue = "@"+attrName.replaceAll("_", " ").replaceAll("([a-z])([A-Z])", "$1 $2");
                        properties.put(attrName, defaultValue);
                        addedKeys++;
                    }
                }

                if(!properties.containsKey(CLASS_ABREGE_BUNDLE_FIELD_NAME)){
                    properties.put(CLASS_ABREGE_BUNDLE_FIELD_NAME, generateClassAccronym(eClass.getName()));
                    addedKeys++;
                }

                if(!properties.containsKey(CLASS_BUNDLE_FIELD_NAME)){
                    properties.put(CLASS_BUNDLE_FIELD_NAME, "@"+eClass.getName());
                    addedKeys++;
                }

                if(!properties.containsKey(CLASS_PLURAL_BUNDLE_FIELD_NAME)){
                    properties.put(CLASS_PLURAL_BUNDLE_FIELD_NAME, properties.get(CLASS_BUNDLE_FIELD_NAME)+"s");
                    addedKeys++;
                }

                if (addedKeys > 0) {
                    try (OutputStream destination = Files.newOutputStream(output)) {
                        properties.store(destination, "Added "+addedKeys+" on last update.");
                    } catch (IOException e) {
                        throw new RuntimeException("Properties file cannot be updated : " + output.toString(), e);
                    }
                }
                return super.caseEClass(eClass);
            }
        };

        for (EObject eObject : resource.getContents()) {
            packageSwitch.doSwitch(eObject);
        }

    }

}
