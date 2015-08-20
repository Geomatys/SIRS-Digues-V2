package fr.sirs;

import com.sun.javafx.PlatformUtil;
import fr.sirs.core.Repository;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.AbstractPositionDocumentRepository;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.other.FXContactPane;
import fr.sirs.other.FXOrganismePane;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.FXDiguePane;
import fr.sirs.theme.ui.FXElementContainerPane;
import fr.sirs.theme.ui.FXTronconDiguePane;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.property.SirsPreferences;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Constants used for project.
 *
 * @author Johann Sorel
 */
public final class SIRS extends SirsCore {

    public static final CoordinateReferenceSystem CRS_WGS84 = CommonCRS.WGS84.normalizedGeographic();

    public static final Image ICON = new Image(SirsCore.class.getResource("/fr/sirs/icon.png").toString());

    public static final Image ICON_ADD_WHITE    = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PLUS,22,Color.WHITE),null);
    public static final Image ICON_COPY_WHITE   = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_COPY_ALIAS,22,Color.WHITE),null);
    public static final Image ICON_ADD_BLACK    = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PLUS,16,Color.BLACK),null);
    public static final Image ICON_ARROW_RIGHT_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ARROW_RIGHT,16,Color.BLACK),null);
    public static final Image ICON_CLOCK_WHITE  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CLOCK_O,22,Color.WHITE),null);
    public static final Image ICON_SEARCH_WHITE       = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_SEARCH,22,Color.WHITE),null);
    public static final Image ICON_ARCHIVE_WHITE       = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ARCHIVE,22,Color.WHITE),null);
    public static final Image ICON_TRASH_WHITE        = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TRASH_O,22,Color.WHITE),null);
    public static final Image ICON_CROSSHAIR_BLACK= SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CROSSHAIRS,22,Color.BLACK),null);
    public static final Image ICON_CARET_UP_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CARET_UP,16,Color.BLACK),null);
    public static final Image ICON_CARET_DOWN_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CARET_DOWN,16,Color.BLACK),null);
    public static final Image ICON_CARET_LEFT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CARET_LEFT,22,Color.WHITE),null);
    public static final Image ICON_CARET_RIGHT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CARET_RIGHT,22,Color.WHITE),null);
    public static final Image ICON_FILE_WHITE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FILE,22,Color.WHITE),null);
    public static final Image ICON_FILE_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FILE,22,Color.BLACK),null);
    public static final Image ICON_TABLE_WHITE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TABLE,22,Color.WHITE),null);
    public static final Image ICON_UNDO_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_UNDO, 22, Color.BLACK),null);
    public static final Image ICON_INFO_BLACK_16 = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_INFO, 16, Color.BLACK),null);
    public static final Image ICON_EYE_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EYE, 16, Color.BLACK),null);
    public static final Image ICON_COMPASS_WHITE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_COMPASS, 22, Color.WHITE),null);
    public static final Image ICON_EDIT_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FILE_O, 16, Color.BLACK),null);
    public static final Image ICON_PRINT_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PRINT, 16, Color.BLACK),null);
    public static final Image ICON_ROTATE_LEFT_ALIAS = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ROTATE_LEFT_ALIAS, 16, Color.BLACK),null);
    public static final Image ICON_IMPORT_WHITE  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_DOWNLOAD,22,Color.WHITE),null);
    public static final Image ICON_EXPORT_WHITE  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_SAVE_ALIAS,22,Color.WHITE),null);
    public static final Image ICON_VIEWOTHER_WHITE  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_BARS,22,Color.WHITE),null);

    public static final String COLOR_INVALID_ICON = "#aa0000";
    public static final Image ICON_EXCLAMATION_CIRCLE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EXCLAMATION_CIRCLE, 16, Color.decode(COLOR_INVALID_ICON)),null);
    public static final String COLOR_VALID_ICON = "#00aa00";
    public static final Image ICON_CHECK_CIRCLE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CHECK_CIRCLE, 16, Color.decode(COLOR_VALID_ICON)),null);
    public static final String COLOR_WARNING_ICON = "#EEB422";
    public static final Image ICON_EXCLAMATION_TRIANGLE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EXCLAMATION_TRIANGLE, 16, Color.decode(COLOR_WARNING_ICON)),null);
    public static final Image ICON_EXCLAMATION_TRIANGLE_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EXCLAMATION_TRIANGLE, 16, Color.BLACK),null);

    public static final Image ICON_LINK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EXTERNAL_LINK, 16, Color.BLACK),null);
    public static final Image ICON_WARNING = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EXCLAMATION_TRIANGLE, 16, Color.BLACK),null);

    public static final String CSS_PATH = "/fr/sirs/theme.css";
    public static final String CSS_PATH_CALENDAR = "/fr/sirs/calendar.css";

    //--------------------------------------------------------------------------
    // Champs spéciaux des classes utilisés dans le code
    //--------------------------------------------------------------------------

    // Champs de contrôle des dates
    public static final String DATE_DEBUT_FIELD = "date_debut";
    public static final String DATE_FIN_FIELD = "date_fin";
    public static final String DATE_MAJ_FIELD = "dateMaj";

    public static final String COMMENTAIRE_FIELD = "commentaire";
    public static final String GEOMETRY_FIELD = "geometry";

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

    public static final String PASSWORD_ENCRYPT_ALGO="MD5";

    // Méthodes utilisées pour les références
    public static final String REFERENCE_GET_ID = "getId";
    public static final String REFERENCE_SET_DESIGNATION = "setDesignation";

    private static AbstractRestartableStage LAUNCHER;
    public static void setLauncher(AbstractRestartableStage currentWindow) {
        LAUNCHER=currentWindow;
    }
    public static AbstractRestartableStage getLauncher() {
        return LAUNCHER;
    }

    public static Loader LOADER;

    private SIRS(){};

    public static void loadFXML(Parent candidate) {
        final Class modelClass = null;
        loadFXML(candidate, modelClass);
    }

    /**
     * Load FXML document matching input controller. If a model class is given,
     * we'll try to load a bundle for text internationalization.
     * @param candidate The controller object to get FXMl for.
     * @param modelClass A class which will be used for bundle loading.
     */
    public static void loadFXML(Parent candidate, final Class modelClass) {
        ResourceBundle bundle = null;
        if (modelClass != null) {
            try{
                bundle = ResourceBundle.getBundle(modelClass.getName(), Locale.FRENCH,
                        Thread.currentThread().getContextClassLoader());
            }catch(MissingResourceException ex){
                LOGGER.log(Level.INFO, "Missing bundle for : {0}", modelClass.getName());
            }
        }
        loadFXML(candidate, bundle);
    }
    
    public static void loadFXML(Parent candidate, final ResourceBundle bundle) {
        loadFXML(candidate, candidate.getClass(), bundle);
    }

    public static void loadFXML(Parent candidate, final Class fxmlClass, final ResourceBundle bundle) {
        ArgumentChecks.ensureNonNull("JavaFX controller object", candidate);
        final String fxmlpath = "/"+fxmlClass.getName().replace('.', '/')+".fxml";
        final URL resource = fxmlClass.getResource(fxmlpath);
        if (resource == null) {
            throw new RuntimeException("No FXMl document can be found for path : "+fxmlpath);
        }
        final FXMLLoader loader = new FXMLLoader(resource);
        loader.setController(candidate);
        loader.setRoot(candidate);
        //in special environement like osgi or other, we must use the proper class loaders
        //not necessarly the one who loaded the FXMLLoader class
        loader.setClassLoader(fxmlClass.getClassLoader());

        if(bundle!=null) loader.setResources(bundle);

        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        candidate.getStylesheets().add(CSS_PATH);
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

        /* HACK : change all separators, because when we use 2 different system
         * separator in the same time, it produces invalid paths. We also check
         * if path starts with file separator, because unix consider it as system
         * root, and will not resolve image path as relative if we keep it.
         */
        if (PlatformUtil.isWindows()) {
            return docRoot.resolve(relativeReference.replaceAll("/+", "\\\\"));
        } else {
            return docRoot.resolve(relativeReference.replaceFirst("^/+", "").replaceAll("(\\\\[^\\s])+", File.separator));
        }
    }

    /**
     * Reconstruit une liste d'éléments depuis la liste en entrée et le {@link Repository} donné.
     * Si la liste en paramètre est nulle ou vide, une liste vide est renvoyée.
     * Si elle contient des éléments, elle est renvoyée telle quel.
     * Si c'est une liste d'ID, on construit une liste des élements correspondants.
     *
     * @param sourceList La liste depuis laquelle on doit reconstruire la liste des éléments.
     * @param repo Le repository servant à retrouver les éléments depuis leur ID.
     * @return Une liste d'éléments. Peut être vide, mais jamais nulle.
     */
    public static ObservableList<Element> toElementList(final List sourceList, final AbstractSIRSRepository repo) {
        if (sourceList == null) {
            return FXCollections.observableArrayList();

        } else if (!sourceList.isEmpty() && sourceList.get(0) instanceof Element) {
            if (sourceList instanceof ModifiableObservableListBase) {
                return (ObservableList) sourceList;
            } else {
                return FXCollections.observableArrayList(sourceList);
            }
        } else if (repo == null) {
            return FXCollections.observableArrayList();
        } else {
            // Version de récupération "Bulk" : récupération de l'ensemble des documents dont les IDs sont spécifiés en une requête unique.
//            ViewQuery q = new ViewQuery()
//                      .allDocs()
//                      .includeDocs(true)
//                      .keys(sourceList);
//            return FXCollections.observableArrayList(Injector.getSession().getConnector().queryView(q, repo.getModelClass()));

            // Version de récupération "cache" : fes documents sont récupérés un par un à moins qu'ils ne soient dans le cache du repository
            // Restauration de cette version, car la duplication "Bulk" ne passe pas par le repository et duplique donc les instances déjà dans son cache.
            final ObservableList resultList = FXCollections.observableArrayList();
            final Iterator<String> it = sourceList.iterator();
            while (it.hasNext()) {
                resultList.add(repo.get(it.next()));
            }
            return resultList;
        }
    }

    /**
     * Return the AbstractPositionDocumentAssociable linked to one document
     * specified by the given id.
     *
     * @param documentId
     * @return
     * @deprecated use instead AbstractPositionDocumentRepository.getPositionDocumentByDocumentId(documentId, sessionCore)
     */
    @Deprecated
    public static ObservableList<? extends AbstractPositionDocumentAssociable> getPositionDocumentByDocumentId(final String documentId){
        ObservableList<? extends AbstractPositionDocumentAssociable> result = null;
        final Collection<AbstractSIRSRepository> candidateRepos = Injector.getSession().getRepositoriesForClass(AbstractPositionDocumentAssociable.class);
        for(AbstractSIRSRepository candidateRepo : candidateRepos){
            if(candidateRepo instanceof AbstractPositionDocumentRepository){
                result = FXCollections.observableList(((AbstractPositionDocumentRepository) candidateRepo).getByDocumentId(documentId));
                if(!result.isEmpty()) return result; // Si la liste n'est pas vide c'est qu'on a trouvé le bon repo et on sort donc de la boucle en renvoyant la liste.
            }
        }
        /*
        Si aucun repo n'a été trouvé (ce qui est normalement impossible étant
        donné le modèle, on renvoie null. Si des repos ont été trouvés mais qu'
        on arrive tout de même à ce point c'est qu'ils ont tous renvoyé une
        liste vide. Parmi elles, la dernière est renvoyée.
        */
        return result;
    }

    /**
     * Tente de trouver un éditeur d'élément compatible avec l'objet passé en paramètre.
     * @param pojo
     * @return Un éditeur pour l'objet d'entrée, ou null si aucun ne peut être
     * trouvé. L'éditeur aura déjà été initialisé avec l'objet en paramètre.
     */
    public static AbstractFXElementPane generateEditionPane(final Element pojo) {
        final AbstractFXElementPane content;
        if (pojo instanceof Contact) {
            content = new FXContactPane((Contact) pojo);
        } else if (pojo instanceof Organisme) {
            content = new FXOrganismePane((Organisme) pojo);
        } else if (pojo instanceof TronconDigue) {
            content = new FXTronconDiguePane((TronconDigue) pojo);
        } else if (pojo instanceof Digue) {
            content = new FXDiguePane((Digue) pojo);
        } else {
            content = new FXElementContainerPane((Element) pojo);
        }
        return content;
    }

    /**
     * initialize ComboBox items using input list. We also activate completion.
     * @param comboBox The combo box to set value on.
     * @param items The items we want into the ComboBox.
     * @param current the element to select by default.
     */
    public static void initCombo(ComboBox comboBox, final ObservableList items, final Object current) {
        comboBox.setConverter(new SirsStringConverter());
        comboBox.setEditable(true);
        comboBox.setItems(items);
        comboBox.getSelectionModel().select(current);
        ComboBoxCompletion.autocomplete(comboBox);
    }

    public static String hexaMD5(final String toEncrypt){
        StringBuilder sb = new StringBuilder();
        try {
            byte[] encrypted = MessageDigest.getInstance(PASSWORD_ENCRYPT_ALGO).digest(toEncrypt.getBytes());
            for (byte b : encrypted) {
                sb.append(String.format("%02X", b));
            }
        } catch (NoSuchAlgorithmException ex) {
            SIRS.LOGGER.log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    public static String binaryMD5(final String toEncrypt){
        try {
            return new String(MessageDigest.getInstance(PASSWORD_ENCRYPT_ALGO).digest(toEncrypt.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            SIRS.LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }


    /**
     * Convert byte number given in parameter in a human readable string. It tries
     * to fit the best unit. Ex : if you've got a number higher than a thousand,
     * input byte number will be expressed in kB. If you've got more than a million,
     * you've got it as MB
     * @param byteNumber Byte quantity to display
     * @return A string displaying byte number converted in fitting unit, along with unit symbol.
     */
    public static String toReadableSize(final long byteNumber) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        if (byteNumber < 0) {
            return "inconnu";
        } else if (byteNumber < 1e3) {
            return "" + byteNumber + " octets";
        } else if (byteNumber < 1e6) {
            return "" + format.format(byteNumber / 1e3) + " ko";
        } else if (byteNumber < 1e9) {
            return "" + format.format(byteNumber / 1e6) + " Mo";
        } else if (byteNumber < 1e12) {
            return "" + format.format(byteNumber / 1e9) + " Go";
        } else {
            return "" + (byteNumber / 1e12) + " To";
        }
    }

    public static ObservableList view(ObservableList ... listes){
        return new ViewList(listes);
    }

    private static final class ViewList extends ObservableListBase implements ListChangeListener{

        private final ObservableList[] listes;

        public ViewList(ObservableList ... listes) {
            this.listes = listes;

            for(ObservableList lst : listes){
                lst.addListener(this);
            }
        }

        @Override
        public Object get(int index) {
            for(int i=0;i<listes.length;i++){
                int size = listes[i].size();
                if(size<=index){
                    index -= size;
                }else{
                    return listes[i].get(index);
                }
            }
            throw new ArrayIndexOutOfBoundsException(index);
        }

        @Override
        public int size() {
            int size = 0;
            for (ObservableList liste : listes) {
                size += liste.size();
            }
            return size;
        }

        private int getOffset(ObservableList lst){
            int size = 0;
            for (ObservableList liste : listes) {
                if(lst==liste) break;
                size += liste.size();
            }
            return size;
        }

        @Override
        public void onChanged(ListChangeListener.Change c) {
            final int offset = getOffset(c.getList());

            beginChange();
            while (c.next()) {
                if (c.wasPermutated()) {
                    //permutate
                    beginChange();
                    final int[] perms = new int[c.getTo()-c.getFrom()];
                    for (int i = c.getFrom(),k=0; i < c.getTo(); ++i,k++) {
                        perms[k] = c.getPermutation(i);
                    }
                    nextPermutation(offset+c.getFrom(), offset+c.getTo(), perms);
                    endChange();
                } else if (c.wasUpdated()) {
                    //update item
                    beginChange();
                    nextUpdate(offset+c.getFrom());
                    endChange();
                } else {
                    beginChange();
                    if(c.wasUpdated()){
                        throw new UnsupportedOperationException("Update events not supported.");
                    }else if(c.wasAdded()){
                        nextAdd(offset+c.getFrom(), offset+c.getTo());
                    }else if(c.wasRemoved()){
                        nextReplace(offset+c.getFrom(), offset+c.getTo(), c.getRemoved());
                    }
                    endChange();
                }
            }
            endChange();
        }

    }

}
