package fr.sirs.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javafx.PlatformUtil;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.util.property.Internal;
import fr.sirs.util.property.SirsPreferences;
import java.awt.Desktop;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.geotoolkit.gui.javafx.util.TaskManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import javax.sql.DataSource;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.util.ArgumentChecks;

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.internal.io.Installation;
import org.geotoolkit.internal.referencing.CRSUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.factory.epsg.EpsgInstaller;
import org.geotoolkit.referencing.operation.transform.NTv2Transform;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;


public class SirsCore {

    /**
     * An useful variable which specify how many meters an object length should be
     * to be considered as a linear object.
     */
    public static final double LINE_MIN_LENGTH = 1.0;

    public static final String PASSWORD_ENCRYPT_ALGO="MD5";

    public static final String INFO_DOCUMENT_ID = "$sirs";
    public static final Logger LOGGER = Logging.getLogger("fr.sirs");
    public static final String NAME = "sirs";

    public static final String MODEL_PACKAGE="fr.sirs.core.model";
    public static final String COMPONENT_PACKAGE="fr.sirs.core.component";

    public static final String SPRING_CONTEXT = "classpath:/fr/sirs/spring/application-context.xml";

    public static final Path CONFIGURATION_PATH;
    static {
        Path tmpPath = Paths.get(System.getProperty("user.home"), "."+NAME);
        if (!Files.isDirectory(tmpPath)) {
            try {
                Files.createDirectory(tmpPath);
            } catch (IOException ex) {
                try {
                    tmpPath = Files.createTempDirectory(NAME);
                } catch (IOException ex1) {
                    ex.addSuppressed(ex1);
                    throw new ExceptionInInitializerError(ex);
                }
            }
        }
        CONFIGURATION_PATH = tmpPath;
    }

    public static final Path DATABASE_PATH = CONFIGURATION_PATH.resolve("database");

    public static final Path H2_PATH = CONFIGURATION_PATH.resolve("h2");

    public static final Path PLUGINS_PATH = CONFIGURATION_PATH.resolve("plugins");

    public static final Path EPSG_PATH = CONFIGURATION_PATH.resolve("EPSG");

    public static final Path ELASTIC_SEARCH_PATH = CONFIGURATION_PATH.resolve("elasticSearch");

    public static final Path LOCAL_QUERIES_PATH = CONFIGURATION_PATH.resolve("queries.properties");

    public static final Path IMPORT_ERROR_DIR = CONFIGURATION_PATH.resolve("importErrors");

    //--------------------------------------------------------------------------
    // Champs spéciaux des classes utilisés dans le code
    //--------------------------------------------------------------------------
    public static final String NEW_FIELD = "new";

    // Champs de contrôle des dates
    public static final String DATE_DEBUT_FIELD = "date_debut";
    public static final String DATE_FIN_FIELD = "date_fin";
    public static final String DATE_MAJ_FIELD = "dateMaj";
    public static final String BORNE_DEBUT_AVAL = "borne_debut_aval";
    public static final String BORNE_FIN_AVAL = "borne_fin_aval";

    public static final String COMMENTAIRE_FIELD = "commentaire";
    public static final String GEOMETRY_FIELD = "geometry";
    public static final String GEOMETRY_MODE_FIELD = "geometryMode";

    public static final String DESIGNATION_FIELD = "designation";
    public static final String VALID_FIELD = "valid";
    public static final String AUTHOR_FIELD = "author";

    public static final String DOCUMENT_ID_FIELD = "documentId";
    public static final String ID_FIELD = "id";
    public static final String REVISION_FIELD = "revision";
    public static final String PARENT_FIELD = "parent";
    public static final String COUCH_DB_DOCUMENT_FIELD = "couchDBDocument";

    public static final String LINEAR_ID_FIELD = "linearId";
    public static final String FOREIGN_PARENT_ID_FIELD = "foreignParentId";

    public static final String PR_DEBUT_FIELD = "prDebut";
    public static final String PR_FIN_FIELD = "prFin";
    public static final String POSITION_DEBUT_FIELD = "positionDebut";
    public static final String POSITION_FIN_FIELD = "positionFin";
    public static final String LONGITUDE_MIN_FIELD = "longitudeMin";
    public static final String LONGITUDE_MAX_FIELD = "longitudeMax";
    public static final String LATITUDE_MIN_FIELD = "latitudeMin";
    public static final String LATITUDE_MAX_FIELD = "latitudeMax";

    public static final String SIRSDOCUMENT_REFERENCE = "sirsdocument";
    public static final String BORNE_IDS_REFERENCE = "borneIds";

    //--------------------------------------------------------------------------
    // Champs particuliers aux désordres
    //--------------------------------------------------------------------------
    public static class DesordreFields {
        // Observations des désordres
        public static final String OBSERVATIONS_REFERENCE = "observations";

        // Photos des observations
        public static final String PHOTOS_OBSERVATION_REFERENCE = "photos";

        // Réseaux et ouvrages
        public static final String ECHELLE_LIMINIMETRIQUE_REFERENCE = "echelleLimnimetriqueIds";
        public static final String OUVRAGE_PARTICULIER_REFERENCE = "ouvrageParticulierIds";
        public static final String RESEAU_TELECOM_ENERGIE_REFERENCE = "reseauTelecomEnergieIds";
        public static final String OUVRAGE_TELECOM_ENERGIE_REFERENCE = "ouvrageTelecomEnergieIds";
        public static final String OUVRAGE_HYDRAULIQUE_REFERENCE = "ouvrageHydrauliqueAssocieIds";
        public static final String RESEAU_HYDRAULIQUE_FERME_REFERENCE = "reseauHydrauliqueFermeIds";
        public static final String RESEAU_HYDRAULIQUE_CIEL_OUVERT_REFERENCE = "reseauHydrauliqueCielOuvertIds";

        // Voiries
        public static final String OUVRAGE_VOIRIE_REFERENCE = "ouvrageVoirieIds";
        public static final String VOIE_DIGUE_REFERENCE = "voieDigueIds";

        // Prestations
        public static final String PRESTATION_REFERENCE = "prestationIds";


        public static final String ARTICLE_REFERENCE = "articleIds";
    }

    // Champs spéciaux des ResourceBundles
    public static final String BUNDLE_KEY_CLASS = "class";
    public static final String BUNDLE_KEY_CLASS_ABREGE = "classAbrege";

    // Bundle des previews
    public static final String PREVIEW_BUNDLE_KEY_DOC_ID = "docId";
    public static final String PREVIEW_BUNDLE_KEY_DOC_CLASS = "docClass";
    public static final String PREVIEW_BUNDLE_KEY_ELEMENT_ID = "elementId";
    public static final String PREVIEW_BUNDLE_KEY_ELEMENT_CLASS = "elementClass";
    public static final String PREVIEW_BUNDLE_KEY_LIBELLE = "libelle";
    public static final String PREVIEW_BUNDLE_KEY_DESIGNATION = "designation";
    public static final String PREVIEW_BUNDLE_KEY_AUTHOR = "author";

    // Méthodes utilisées pour les références
    public static final String REFERENCE_GET_ID = "getId";
    public static final String REFERENCE_SET_DESIGNATION = "setDesignation";

    /**
     * User directory root folder.
     *
     * @return {user.home}/.sirs
     */
    public static String getConfigPath(){
        return CONFIGURATION_PATH.toString();
    }

    public static File getConfigFolder(){
        return CONFIGURATION_PATH.toFile();
    }

    public static Path getDatabaseFolder(){
        return DATABASE_PATH;
    }

    /**
     * Initialise la base EPSG et la grille NTV2 utilisée par l'application. Si
     * elles n'existent pas, elles seront créées. Dans tous les cas, on force le
     * chargement de la base EPSG dans le système, ce qui permet de lever les
     * potentiels problèmes au démarrage.
     * Si la création de la bdd EPSG rate, on renvoie une exception, car aucun
     * réferencement spatial ne peut être effecctué sans elle. En revanche, la
     * grille NTV2 n'est utile que pour des besoins de précision. Si son installation
     * rate, on n'afficche juste un message d'avertissement.
     * @throws FactoryException
     * @throws IOException
     */
    public static void initEpsgDB() throws FactoryException, IOException {
        // create a database in user directory
        Files.createDirectories(SirsCore.EPSG_PATH);

        // Geotoolkit startup. We specify that we don't want to use Java preferences,
        // because it does not work on most systems anyway.
        final Properties noJavaPrefs = new Properties();
        noJavaPrefs.put("platform", "server");
        Setup.initialize(noJavaPrefs);
        final String url = "jdbc:hsqldb:file:" + SirsCore.EPSG_PATH.toString()+"/db";

        final DataSource ds = new DefaultDataSource(url);
        Hints.putSystemDefault(Hints.EPSG_DATA_SOURCE, ds);

        final EpsgInstaller installer = new EpsgInstaller();
        installer.setDatabase(url);
        if (!installer.exists()) {
            installer.call();
        }

        // work in lazy mode, do your best for lenient datum shift
        Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

        // force loading epsg
        CRS.decode("EPSG:3395");

        // On tente d'installer la grille NTV2 pour améliorer la précision du géo-réferencement.
        final File directory = Installation.NTv2.directory(true);
        if (!new File(directory, NTv2Transform.RGF93).isFile()) {
            directory.mkdirs();
            final File out = new File(directory, NTv2Transform.RGF93);
            try {
                out.createNewFile();
                IOUtilities.copy(SirsCore.class.getResourceAsStream("/fr/sirs/ntv2/ntf_r93.gsb"), new FileOutputStream(out));
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "NTV2 data for RGF93 cannot be installed.", ex);
                GeotkFX.newExceptionDialog("La grille de transformation NTV2 ne peut être installée. Des erreurs de reprojection pourrait apparaître au sein de l'application.", ex).show();
            }
        }
    }

    public static String getVersion() {
        return SirsCore.class.getPackage().getImplementationVersion();
    }

    public static Path getDocumentRootPath() throws InvalidPathException {
        String rootStr = SirsPreferences.INSTANCE.getProperty(SirsPreferences.PROPERTIES.DOCUMENT_ROOT);
       return Paths.get(rootStr);
    }

    /**
     *
     * @param relativeReference Un chemin relatif dénotant une référence dans un {@link Element}
     * @return Un chemin absolu vers la réference passée en paramètre.
     * @throws IllegalStateException Si la propriété {@link SirsPreferences.PROPERTIES#DOCUMENT_ROOT} est inexistante ou ne dénote pas un chemin valide.
     * Dans ce cas, il est FORTEMENT conseillé d'attraper l'exception, et de proposer à l'utilisateur de vérifier la valeur de cette propriété dans les
     * préférences de l'application.
     * @throws InvalidPathException Si il est impossible de construire un chemin valide avec le paramètre d'entrée.
     *
     * Note : les deux exceptions ci-dessus ne sont pas lancées dans le cas où le
     * chemin créé dénote un fichier inexistant. Elles sont invoquées uniquement
     * si les chemins sont incorrects syntaxiquement.
     */
    public static Path getDocumentAbsolutePath(final String relativeReference) throws IllegalStateException, InvalidPathException {
        ArgumentChecks.ensureNonEmpty("Document relative path", relativeReference);
        final Path docRoot;
        try {
            docRoot = getDocumentRootPath();
        } catch (InvalidPathException e) {
            throw new IllegalStateException("La preference " + SirsPreferences.PROPERTIES.DOCUMENT_ROOT.name()
                    + "ne dénote pas un chemin valide. Vous pouvez vérifier sa valeur "
                    + "depuis les préférences de l'application (Fichier > Preferences).", e);
        }

        if (PlatformUtil.isWindows()) {
            return docRoot.resolve(relativeReference.replaceAll("/+", "\\\\") // On remplace les séparateurs issus d'un autre système.
                    .replaceFirst("^\\\\+", "")); // On enlève tout séparateur en début de chaîne qui tendrait à signifier que le chemin n'est pas relatif.
        } else {
            return docRoot.resolve(relativeReference.replaceAll("\\\\+", File.separator) // On remplace les séparateurs issus d'un autre système.
                    .replaceFirst("^"+File.separator+"+", "")); // On enlève tout séparateur en début de chaîne qui tendrait à signifier que le chemin n'est pas relatif.
        }
    }

    /**
     *
     * @return the application task manager, designed to start users tasks in a
     * separate thread pool.
     */
    public static TaskManager getTaskManager() {
        return TaskManager.INSTANCE;
    }

    /**
     * Try to expand a little an envelope. Main purpose is to ensure we won't
     * have an envelope which is merely a point.
     * @param input The input to expand.
     * @return An expanded envelope. If we cannot analyze CRS or it's unit on
     * horizontal axis, the same envelope is returned.
     */
    public static Envelope pseudoBuffer(final Envelope input) {
        double additionalDistance = 0.01;
        if (input.getCoordinateReferenceSystem() != null) {
            CoordinateReferenceSystem crs = input.getCoordinateReferenceSystem();
            int firstAxis = CRSUtilities.firstHorizontalAxis(crs);

            if (firstAxis >=0) {
                Unit unit = crs.getCoordinateSystem().getAxis(firstAxis).getUnit();
                if (unit != null && SI.METRE.isCompatible(unit)) {
                    additionalDistance = SI.METRE.getConverterTo(unit).convert(1);
                }

                final GeneralEnvelope result = new GeneralEnvelope(input);
                result.setRange(firstAxis,
                        result.getLower(firstAxis)-additionalDistance,
                        result.getUpper(firstAxis)+additionalDistance);
                final int secondAxis = firstAxis +1;
                result.setRange(secondAxis,
                        result.getLower(secondAxis)-additionalDistance,
                        result.getUpper(secondAxis)+additionalDistance);
                return result;
            }
        }
        return input;
    }

    private static final Class[] SUPPORTED_TYPES = new Class[]{
        Boolean.class,
        String.class,
        Number.class,
        boolean.class,
        int.class,
        float.class,
        double.class,
        LocalDateTime.class,
        LocalDate.class,
        Point.class
    };

    /**
     * Récupération des attributs simple pour affichage dans les tables.
     *
     * @param clazz
     * @return liste des propriétés simples
     */
    public static LinkedHashMap<String, PropertyDescriptor> listSimpleProperties(Class clazz) throws IntrospectionException {
        final LinkedHashMap<String, PropertyDescriptor> properties = new LinkedHashMap<>();
        for (java.beans.PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
            final Method m = pd.getReadMethod();

            if (m == null || m.getAnnotation(Internal.class) != null) {
                continue;
            }

            final Class propClass = m.getReturnType();
            if (propClass.isEnum()) {
                properties.put(pd.getName(), pd);
            } else
                for (Class c : SUPPORTED_TYPES) {
                    if (c.isAssignableFrom(propClass)) {
                        properties.put(pd.getName(), pd);
                        break;
                    }
                }
        }
        return properties;
    }

    /**
     * Utility method which will check for application updates. The check is
     * done in an asynchronous task, to avoid freeze of the application while
     * searching for updates. If an update has been found, result of the task
     * will be information about upgrade, including update package download URL.
     *
     * @return The task which has been submitted for update check.
     */
    public static Task<UpdateInfo> checkUpdate() {
        return TaskManager.INSTANCE.submit("Vérification des mises à jour", () -> {
            String localVersion = getVersion();
            if (localVersion == null || localVersion.isEmpty())
                throw new IllegalStateException("La version locale de l'application est illisible !");
            final String updateAddr = SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.UPDATE_CORE_URL);
            try {
                final URL updateURL = new URL(updateAddr);
                Map config = new ObjectMapper().readValue(updateURL, Map.class);
                final Object distantVersion = config.get("version");
                if (distantVersion instanceof String && localVersion.compareTo((String) distantVersion) < 0) {
                    final Object packageUrl = config.get("url");
                    if (packageUrl instanceof String) {
                        return new UpdateInfo(localVersion, (String) distantVersion, new URL((String) packageUrl));
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot access update service : " + updateAddr, e);
            }
            return null;
        });
    }

    /**
     * Try to open a system browser to visit given address. If system browser cannot
     * open input URL, we'll try with a web view.
     * @param toBrowse The address of the page to display. If null, the method does nothing.
     */
    public static void browseURL(final URL toBrowse) {
        browseURL(toBrowse, null);
    }

    /**
     * Try to open a system browser to visit given address. If system browser cannot
     * open input URL, we'll try with a web view. A new stage will be opened for
     * the web view, and input title will be set on the stage.
     *
     * @param toBrowse The address of the page to display. If null, the method does nothing.
     * @param title A title set to displayed web view. If null, no title will be set.
     */
    public static void browseURL(final URL toBrowse, final String title) {
        browseURL(toBrowse, title, false);
    }


    /**
     * Try to open a system browser to visit given address. If system browser cannot
     * open input URL, we'll try with a web view. A new stage will be opened for
     * the web view, and input title will be set on the stage.
     *
     * @param toBrowse The address of the page to display. If null, the method does nothing.
     * @param title A title set to displayed web view. If null, no title will be set.
     * @param showAndWait If true, and a webview is opened, application stays locked while its opened. If false,
     * webview will be launched in a non-modal window.
     */
    public static void browseURL(final URL toBrowse, final String title, final boolean showAndWait) {
        if (toBrowse == null)
            return;
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(toBrowse.toURI());
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Cannot browse following URL with system browser : "+toBrowse, ex);
                }
            }).start();
        } else {
            final WebView webView = new WebView();
            final Stage infoStage = new Stage();
            infoStage.getIcons().add(new Image(SirsCore.class.getResource("/fr/sirs/icon.png").toString()));
            if (title != null)
                infoStage.setTitle(title);
            infoStage.setScene(new Scene(webView));
            webView.getEngine().load(toBrowse.toExternalForm());
            if (showAndWait)
                infoStage.showAndWait();
            else
                infoStage.show();
        }
    }

        public static String hexaMD5(final String toEncrypt){
        StringBuilder sb = new StringBuilder();
        try {
            byte[] encrypted = MessageDigest.getInstance(PASSWORD_ENCRYPT_ALGO).digest(toEncrypt.getBytes());
            for (byte b : encrypted) {
                sb.append(String.format("%02X", b));
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new SirsCoreRuntimeException("Cannot hash input password !", ex);
        }
        return sb.toString();
    }

    public static String binaryMD5(final String toEncrypt){
        try {
            return new String(MessageDigest.getInstance(PASSWORD_ENCRYPT_ALGO).digest(toEncrypt.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            throw new SirsCoreRuntimeException("Cannot hash input password !", ex);
        }
    }

    /**
     * Simple structure to store information about an update.
     * TODO : add changelog
     */
    public static final class UpdateInfo {
        /** Version of currently installed application. */
        public final String localVersion;
        /** Version of found update. */
        public final String distantVersion;
        /** Download URL for update package. */
        public final URL updateURL;

        public UpdateInfo(String localVersion, String distantVersion, URL updateURL) {
            this.localVersion = localVersion;
            this.distantVersion = distantVersion;
            this.updateURL = updateURL;
        }
    }
}
