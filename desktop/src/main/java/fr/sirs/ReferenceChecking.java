package fr.sirs;

import fr.sirs.core.model.RefConduiteFermee;
import fr.sirs.core.model.RefConvention;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefDevers;
import fr.sirs.core.model.RefDocumentGrandeEchelle;
import fr.sirs.core.model.RefEcoulement;
import fr.sirs.core.model.RefEvenementHydraulique;
import fr.sirs.core.model.RefFoncitonMaitreOeuvre;
import fr.sirs.core.model.RefFonction;
import fr.sirs.core.model.RefFrequenceEvenementHydraulique;
import fr.sirs.core.model.RefImplantation;
import fr.sirs.core.model.RefLargeurFrancBord;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefMoyenManipBatardeaux;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefNatureBatardeaux;
import fr.sirs.core.model.RefOrientationOuvrage;
import fr.sirs.core.model.RefOrientationPhoto;
import fr.sirs.core.model.RefOrientationVent;
import fr.sirs.core.model.RefOrigineProfilLong;
import fr.sirs.core.model.RefOrigineProfilTravers;
import fr.sirs.core.model.RefOuvrageFranchissement;
import fr.sirs.core.model.RefOuvrageHydrauliqueAssocie;
import fr.sirs.core.model.RefOuvrageParticulier;
import fr.sirs.core.model.RefOuvrageTelecomEnergie;
import fr.sirs.core.model.RefOuvrageVoirie;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefPositionProfilLongSurDigue;
import fr.sirs.core.model.RefPrestation;
import fr.sirs.core.model.RefProfilFrancBord;
import fr.sirs.core.model.RefProprietaire;
import fr.sirs.core.model.RefRapportEtude;
import fr.sirs.core.model.RefReferenceHauteur;
import fr.sirs.core.model.RefReseauHydroCielOuvert;
import fr.sirs.core.model.RefReseauTelecomEnergie;
import fr.sirs.core.model.RefRevetement;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.RefSeuil;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.RefTypeDocument;
import fr.sirs.core.model.RefTypeGlissiere;
import fr.sirs.core.model.RefTypeProfilTravers;
import fr.sirs.core.model.RefTypeTroncon;
import fr.sirs.core.model.RefUrgence;
import fr.sirs.core.model.RefUsageVoie;
import fr.sirs.core.model.RefUtilisationConduite;
import fr.sirs.core.model.RefVoieDigue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ReferenceChecking {

    private static String MODEL_PACKAGE = "fr.sirs.core.model";
    private static String FRANCE_DIGUES_REFERENCES_DIRECTORY = "http://france-digues.moc/references/";

    public static void checkAllReferences() throws IOException {
        
        /*
        Récupération des classes de référence du serveur. Inclut la
        vérification que ces classes existent bien dans l'application.
        */
        final List<Class> serverReferences = getAllServerReferences();
        
        /*
        Récupération des classes de référence de l'application. 
         */
        final List<Class> localReferences = getAllLocalReferences();
        
        /*
        Vérification que les classes de l'application sont toutes recensées sur
        le serveur.
        */
        final List<String> referencesNotFound = new ArrayList<>();
        for (final Class reference : localReferences) {
            if(!serverReferences.contains(reference)){
                referencesNotFound.add(reference.getSimpleName());
            }
        }
        
        if(!referencesNotFound.isEmpty()){
            final StringBuilder information = new StringBuilder("Les références suivantes sont présentes sur localement mais sont inconnues du serveur :");
            for (final String referenceNotFound : referencesNotFound) {
                information.append("\n").append(referenceNotFound);
            }
            information.append("\n\nVotre application n'est peut-être pas à jour. Pour la mettre à jour, veuillez s'il vous plaît contacter l'administrateur France-Digue.");
            new Alert(Alert.AlertType.WARNING, information.toString(), ButtonType.OK).showAndWait();
        }
        
        /*
        On vérifie ensuite l'identité des instances : pour chaque classe de 
        référence locale, pourvu qu'elle soit référencée sur le serveur,
        on va vérifier que les instances sont "identiques" (identité à définir).
        */
        for (final Class reference : localReferences) {
            if(serverReferences.contains(reference)){
                // Provisoirement, on ne vérifie que RefNature
                if (reference == RefNature.class) {
                    checkReferenceClass(reference);
                }
            }
        }
        
        
    }

    /**
     * Check one reference class
     *
     * @param referenceClass
     */
    private static void checkReferenceClass(final Class referenceClass) {
        try {
            final URL referenceURL = referenceURL(FRANCE_DIGUES_REFERENCES_DIRECTORY, referenceClass);
            final File referenceFile = retriveFileFromURL(referenceURL);

            final List<Object> localReferences = Injector.getSession().getRepositoryForClass(referenceClass).getAll();
            final List<Object> fileReferences = readReferenceFile(referenceFile, referenceClass);

            System.out.println(fileReferences);
            
            /*
            On vérifie que toutes les instances locales sont présentes sur le serveur.
            Sinon, on récupère les instances locales non présentes sur le serveur.
             */
            final List<Object> localInstancesNotOnTheServer = new ArrayList<>();
            for (final Object serverReferenceInstance : fileReferences){
                for (final Object localReferenceInstance : localReferences){
                
                }
            }
            
            /*
            On vérifie que toutes les instances du serveur sont présentes localement.
            Sinon, on récupère les instances du serveur non présentes localement.
            */
            final Method getId = referenceClass.getMethod("getId");
            final List<Object> serverInstancesNotLocal = new ArrayList<>();
            for (final Object localReferenceInstance : localReferences){
                try{
                    final Object localId = getId.invoke(localReferenceInstance);
                    for (final Object serverReferenceInstance : fileReferences){
                        try{
                            final Object serverId = getId.invoke(serverReferenceInstance);
                            if(localId instanceof String
                                    && localId.equals(serverId)){
                                if(!localReferenceInstance.equals(serverReferenceInstance)){
                                    System.out.println(localReferenceInstance+"\n"+serverReferenceInstance+"\n");
                                }
                                else{
                                    System.out.println("Instances identiques !");
                                    System.out.println(localReferenceInstance+"\n"+serverReferenceInstance+"\n");
                                }
                            }
                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            SIRS.LOGGER.log(Level.SEVERE, ex.getMessage());
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    SIRS.LOGGER.log(Level.SEVERE, ex.getMessage());
                }
            }
            
            

        } catch (MalformedURLException ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(ReferenceChecking.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ReferenceChecking.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void checkReferenceInstance(final Object referenceToCheck, final List<Object> listOfReferences){
        
    }
    
    
    
    

    private static List<Object> readReferenceFile(final File file, final Class referenceClass) throws FileNotFoundException, IOException {

        if (file == null) {
            return null;
        }

        final BufferedReader bufferdReader = new BufferedReader(new FileReader(file));

        boolean readHeader = true;
        String[] header = null;

        final List<Object> fileReferences = new ArrayList<>();

        // Construction des instances de références présentes dans le fichier
        while (true) {
            final String line = bufferdReader.readLine();

            if (line == null) {
                break;
            }

            final String[] fields = line.split("\\s*,\\s*");
            if (readHeader) {
                header = fields;
                readHeader = false;
            } else {
                try {
                    //                checkReferenceInstance(fields, header);
                    fileReferences.add(buildReferenceInstance(header, fields, referenceClass));

                    /*
                     Il faut :
                     1) Vérifier que toutes les références du serveur (libellé et éventuellement abrégé) sont présentes dans la base locale
                     !!! Cela suppose de pouvoir parser l'ensemble des fichiers du serveur : mettre un fichier d'index ?
                     => si ce n'est pas le cas, signaler quelles sont les références du serveur non disponibles localement;
                     ==> proposition de solution : créer les nouvelles références dans la base locale.
                     2) Vérifier que toutes les références locales sont bien référencées sur le serveur (libellé et éventuellement abrégé)
                     => si ce n'est pas le cas, signaler quelles sont les références locales qui n'existent pas sur le serveur;
                     ==> proposition de solution a) : proposer de supprimer les références locales non référencées
                     ==> proposition de solution b) : proposer de contacter france-digues.
                     3) Question de l'identifiant : Que définir comme identifiant des références (pour le partage, il faut s'assurer de l'identifiant) : proposition : concaténation de la classe et du libellé
                     4) Question de l'interface globale à toutes les références.
                     5)
                     */
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return fileReferences;
    }

//    /**
//     * Tentative d'écriture d'une méthode utilisant la généricité et la
//     * métaprogrammation : échec.
//     *
//     * @param <T>
//     */
//    private static class ReferenceHelper<T> {
//
//        public T buildReferenceInstance(final String[] header, final String[] fields, final T instance) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//
//            for (int i = 0; i < header.length; i++) {
//                final String fieldUpperCase = header[i].substring(0, 1).toUpperCase() + header[i].substring(1);
//                final Class fieldType = instance.getClass().getMethod("get" + fieldUpperCase).getReturnType();
//                final Method setter = instance.getClass().getMethod("set" + fieldUpperCase, fieldType);
//                if (String.class == fieldType) {
//                    setter.invoke(instance, fields[i]);
//                } else if (LocalDateTime.class == fieldType) {
//                    setter.invoke(instance, LocalDateTime.parse(fields[i], DateTimeFormatter.ISO_DATE));
//                }
//            }
//
//            return instance;
//        }
//    }

    private static Object buildReferenceInstance(final String[] header, final String[] fields, final Class referenceClass)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {

        if (header == null || fields == null) {
            return null;
        }

        final Constructor constructor = referenceClass.getConstructor();
        final Object referenceInstance = constructor.newInstance();

        for (int i = 0; i < header.length; i++) {

            final Method getter = referenceClass.getMethod("get" + header[i].substring(0, 1).toUpperCase() + header[i].substring(1));
            final Class type = getter.getReturnType();
            final Method setter = referenceClass.getMethod("set" + header[i].substring(0, 1).toUpperCase() + header[i].substring(1), type);
            if (String.class.equals(type)) {
                if("id".equals(header[i])){
                    setter.invoke(referenceInstance, referenceClass.getSimpleName()+":"+fields[i]);
                }
                else{
                    setter.invoke(referenceInstance, fields[i]);
                }
            } else {

            }
        }

        return referenceInstance;
    }
    
    
    
    

//    private static void checkReferenceInstance(final String[] fields, final String[] header) {
//
//        if (fields == null || header == null || (fields.length != header.length) || header.length == 0) {
//            return;
//        }
//
//    }

    /**
     * Return the file content located at the URL.
     *
     * @param url
     * @return null if the input URL is null
     * @throws IOException
     */
    private static File retriveFileFromURL(final URL url) throws IOException {

        if (url == null) {
            return null;
        }

        final File file = File.createTempFile("tempReference", ".csv");
        final URLConnection connection = url.openConnection();
        try (final InputStream inputStream = connection.getInputStream()) {

            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final FileOutputStream fos = new FileOutputStream(file);
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);

            int r = 0;
            while (true) {
                r = inputStreamReader.read();
                if (r != -1) {
                    outputStreamWriter.write(r);
                } else {
                    break;
                }
            }
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fos.close();
            inputStreamReader.close();
        }
        return file;
    }

//    /**
//     * Get the class for the reference provided by the URL.
//     *
//     * @param referenceURL
//     * @return
//     */
//    private static Class referenceClass(final URL referenceURL) {
//        final Pattern pattern = Pattern.compile("\\/references\\/(.*)\\.csv");
//        final Matcher matcher = pattern.matcher(referenceURL.getFile());
//        if (matcher.matches()) {
//            final String filePattern = matcher.group(1);
//            switch (filePattern) {
//                case "TYPE_NATURE":
//                    return RefNature.class;
//                default:
//                    return null;
//            }
//        } else {
//            return null;
//        }
//    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    /**
     * Build URL for given reference class.
     *
     * @param locationDir
     * @param referenceClass
     * @return
     * @throws MalformedURLException
     */
    private static URL referenceURL(final String locationDir, final Class referenceClass) throws MalformedURLException {
        return new URL(locationDir + referenceClass.getSimpleName() + ".csv");
    }

    /**
     * TODO Retrive all the reference classes of the local system.
     *
     * @return
     */
    private static List<Class> getAllLocalReferences() {
        final List<Class> references = new ArrayList<>();
        references.add(RefConduiteFermee.class);
        references.add(RefConvention.class);
        references.add(RefCote.class);
        references.add(RefDevers.class);
        references.add(RefDocumentGrandeEchelle.class);
        references.add(RefEcoulement.class);
        references.add(RefEvenementHydraulique.class);
        references.add(RefFoncitonMaitreOeuvre.class);
        references.add(RefFonction.class);
        references.add(RefFrequenceEvenementHydraulique.class);
        references.add(RefImplantation.class);
        references.add(RefLargeurFrancBord.class);
        references.add(RefMateriau.class);
        references.add(RefMoyenManipBatardeaux.class);
        references.add(RefNatureBatardeaux.class);
        references.add(RefNature.class);
        references.add(RefOrientationOuvrage.class);
        references.add(RefOrientationPhoto.class);
        references.add(RefOrientationVent.class);
        references.add(RefOrigineProfilLong.class);
        references.add(RefOrigineProfilTravers.class);
        references.add(RefOuvrageFranchissement.class);
        references.add(RefOuvrageHydrauliqueAssocie.class);
        references.add(RefOuvrageParticulier.class);
        references.add(RefOuvrageTelecomEnergie.class);
        references.add(RefOuvrageVoirie.class);
        references.add(RefPosition.class);
        references.add(RefPositionProfilLongSurDigue.class);
        references.add(RefPrestation.class);
        references.add(RefProfilFrancBord.class);
        references.add(RefProprietaire.class);
        references.add(RefRapportEtude.class);
        references.add(RefReferenceHauteur.class);
        references.add(RefReseauHydroCielOuvert.class);
        references.add(RefReseauTelecomEnergie.class);
        references.add(RefRevetement.class);
        references.add(RefRive.class);
        references.add(RefSeuil.class);
        references.add(RefSource.class);
        references.add(RefSystemeReleveProfil.class);
        references.add(RefTypeDesordre.class);
        references.add(RefTypeDocument.class);
        references.add(RefTypeGlissiere.class);
        references.add(RefTypeProfilTravers.class);
        references.add(RefTypeTroncon.class);
        references.add(RefUrgence.class);
        references.add(RefUsageVoie.class);
        references.add(RefUtilisationConduite.class);
        references.add(RefVoieDigue.class);
        return references;
    }

    /**
     * Reference names located on the server references index file are supposed
     * to be the simple name of the model class related to the given reference.
     *
     * @return
     */
    private static List<Class> getAllServerReferences() throws IOException {
        final List<String> names = getAllServerReferenceNames();
        final List<Class> classes = new ArrayList<>();
        final List<String> classesNotFound = new ArrayList<String>();
        for (final String name : names) {
            final String className = MODEL_PACKAGE + "." + name;
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException ex) {
                classesNotFound.add(className);
            }
        }

        if (!classesNotFound.isEmpty()) {
            String information = "Les références suivantes sont présentes sur le serveur mais sont inconnues de l'application :";
            for (final String classeNotFound : classesNotFound) {
                information += ("\n" + classeNotFound);
            }
            information += ("\n\nVotre application n'est peut-être pas à jour. Pour la mettre à jour, veuillez s'il vous plaît contacter l'administrateur France-Digue.");
            new Alert(Alert.AlertType.WARNING, information, ButtonType.OK).showAndWait();
        }

        return classes;
    }

    /**
     * TODO Retrive all the server references (parse the index.csv file).
     *
     * @return
     */
    private static List<String> getAllServerReferenceNames() throws MalformedURLException, IOException {
        final List<String> result;
        final URL indexURL = new URL(FRANCE_DIGUES_REFERENCES_DIRECTORY + "index.csv");
        result = readIndexFile(retriveFileFromURL(indexURL));
        return result;
    }

    private static List<String> readIndexFile(final File file) throws IOException {
        if (file == null) {
            return null;
        }

        final List<String> result = new ArrayList<>();
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        while (true) {
            final String line = bufferedReader.readLine();

            if (line == null) {
                break;
            }

            result.add(line.replaceAll("\\s", ""));

        }
        return result;
    }
}
