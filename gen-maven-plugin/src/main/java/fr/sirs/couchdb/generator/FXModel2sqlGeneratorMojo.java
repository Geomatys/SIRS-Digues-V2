package fr.sirs.couchdb.generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
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

@Mojo(name = "fxmodel2sql", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresProject = true)
public class FXModel2sqlGeneratorMojo extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/fxmodel2sql")
    private File outputDirectory;

    @Parameter(required = true)
    private File model;

    @Parameter(required = true)
    private String modelPackage;

    @Parameter(defaultValue = "SQLHelper")
    private String helperName;

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
            generatePojo2SQL(resource, modelPackage, outputDirectory.toPath());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
    }

    private void generatePojo2SQL(Resource model, String pakage, Path srcDst)
            throws IOException {

        final List<String> generated = new ArrayList<>();
        final List<String> couchDbDocuments = new ArrayList<>();

        final Generator generator = new Generator() {

            final FXModel2sqlGenerator fxModelGenerator = new FXModel2sqlGenerator();

            @Override
            public String getFileName(EClass eClass) {
                final String className = eClass.getName();
                generated.add(className);
                couchDbDocuments.add(className);
                return className + "2sql" + ".java";
            }

            @Override
            public String generate(Object object) {
                if (object instanceof Helper) {
                    return fxModelGenerator.generate(object);
                } else {
                    throw new RuntimeException("Unexpected parameter !");
                }
            }

            @Override
            public Helper buildHelper(EClass eClass) {
                return new SQLModelHelper(eClass, pakage + ".sql", generateEquals);
            }

            @Override
            public boolean accept(EClass eClass) {
                return true;
            }
        };
        EcoreHelper.walk(getLog(), srcDst, generator, pakage + ".sql", model);

        final StringBuilder builder = new StringBuilder("package ");
        builder.append(pakage).append(".sql;\n");

        builder.append("\nimport ").append(pakage).append(".*;");
        builder.append("\nimport java.sql.*;")
                .append(System.lineSeparator())
                .append("import org.springframework.stereotype.Component;");

        builder.append("\n\n")
                .append("@Component")
                .append(System.lineSeparator())
                .append("public class ").append(helperName).append(" implements SQLHelper {\n\n");

        builder.append("    private ").append(helperName).append("(){}\n\n");

        /*
        Création des tables.
        */
        builder.append("    @Override\n    public void createTables(Connection conn, int srid) throws SQLException {\n");
        for (final String sqlClass : generated) {
            builder.append("\n        ").append(sqlClass).append("2sql").append(".createTable(conn, srid);");
        }
        builder.append("    }\n\n");

        /*
        Création des clefs étrangères
        */
        builder.append("    @Override\n    public void addForeignKeys(Connection conn) throws SQLException {\n");
        for (final String sqlClass : generated) {
            builder.append("\n        ").append(sqlClass).append("2sql").append(".addForeignKeys(conn);");
        }
        builder.append("    }\n\n");

        /*
        Insertion des tuples
        */
        builder.append("    @Override\n    public boolean insertElement(Connection conn, Element element)  {\n");
        builder.append("    boolean result = true;\n");
        builder.append("    try {\n");
        for (final String sqlClass : couchDbDocuments) {
            builder.append("\n        if(element instanceof ").append(sqlClass).append("){\n");
            builder.append("             result &= ").append(sqlClass).append("2sql.insert(conn, ").append("(").append(sqlClass).append(") element);\n");
            builder.append("        }");
        }
        builder.append("\n    } catch(SQLException e){e.printStackTrace();}\n");
        builder.append("    return result;\n");
        builder.append("    }\n\n");

        /*
        Mise à jour
        */
        builder.append("    @Override\n    public boolean updateElement(Connection conn, Element element)  {\n");
        builder.append("    boolean result = true;\n");
        builder.append("    try {\n");
        for (final String sqlClass : couchDbDocuments) {
            builder.append("\n        if(element instanceof " + sqlClass).append("){\n");
            builder.append("             result &= ").append(sqlClass).append("2sql.delete(conn, ").append("(").append(sqlClass).append(") element);\n");
            builder.append("             result &= ").append(sqlClass).append("2sql.insert(conn, ").append("(").append(sqlClass).append(") element);\n");
            builder.append("        }");
        }
        builder.append("\n    } catch(SQLException e){e.printStackTrace();}\n");
        builder.append("    return result;\n");
        builder.append("    }\n");

        builder.append("}\n");

        try(final InputStream is = new ByteArrayInputStream(builder.toString().getBytes())){
            Files.copy(is, srcDst.resolve(pakage.replace('.', '/')).resolve("sql/"+helperName+".java"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
