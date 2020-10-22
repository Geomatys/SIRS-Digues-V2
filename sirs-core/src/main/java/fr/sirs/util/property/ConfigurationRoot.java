/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util.property;

import fr.sirs.core.SirsCore;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.lang.IllegalArgumentException;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author maximegavens
 */
public class ConfigurationRoot {

    public static String getRoot() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(SirsCore.class);
            String rootStr = prefs.get("CONFIGURATION_FOLDER_PATH", "none");
            Path rootPath = Paths.get(rootStr);
            if (rootStr.equals("none")) {
                throw new RuntimeException("Behavior unexpected. No value associate with CONFIGURATION_FOLDER_PATH key. This value should be provide when the application is launched.");
            }
            if (!Files.isDirectory(rootPath)) {
                throw new RuntimeException("Behavior unexpected. The path provided is not a directory.");
            }
            return rootStr;
        } catch (SecurityException ex) {
            throw new SecurityException("A security manager refuses access to preferences. " + ex);
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The node for package 'SirsCore.class' has been removed." + ex);
        }
    }
    
    public static void setRootAndMove(String from, String to) throws IOException {
        try {
            ConfigurationRoot.move(from, to);
            Preferences prefs = Preferences.userNodeForPackage(SirsCore.class);
            prefs.put("CONFIGURATION_FOLDER_PATH", to);
        } catch (SecurityException ex) {
            ConfigurationRoot.move(to, from);
            throw new SecurityException("A security manager refuses access to preferences. " + ex);
        } catch (IllegalStateException ex) {
            ConfigurationRoot.move(to, from);
            throw new IllegalStateException("The node for package 'SirsCore.class' has been removed." + ex);
        }
    }
    
    public static void setRoot(String toSet) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(SirsCore.class);
            prefs.put("CONFIGURATION_FOLDER_PATH", toSet);
        } catch (SecurityException ex) {
            throw new SecurityException("A security manager refuses access to preferences. " + ex);
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("The node for package 'SirsCore.class' has been removed." + ex);
        }
    }
    
    public static void move(String from, String to) throws IOException {
        if (from == null || from.isEmpty()) {
            throw new IllegalArgumentException("previousRoot can't be null or empty");
        }
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("newRoot can't be null or empty");
        }
        if (from.equals(to)) return;
        Path p1 = Paths.get(from, "." + SirsCore.NAME);
        Path p2 = Paths.get(to, "." + SirsCore.NAME);
        
        File src = p1.toFile();
	File dest = p2.toFile();
        
        try {
            FileUtils.moveDirectory(src, dest);
        } catch (IOException ex) {
            throw new IOException(ex);
        }
        SirsCore.LOGGER.log(Level.INFO, "Directory " + from + " moved successfully to " + to + ".");
    }
    
    private static void moveDir(Path src, Path dest) throws IOException {
        if (src.toFile().isDirectory()) {
            for (File file : src.toFile().listFiles()) {
                moveDir(file.toPath(), dest.resolve(src.relativize(file.toPath())));
            }
        }
        
        try {
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Source directory: " + src + " doesn't exist.");
        }
    }
    
    public static void clear() throws BackingStoreException {
        try {
            Preferences.userNodeForPackage(SirsCore.class).clear();
        } catch (SecurityException ex) {
            throw new SecurityException("A security manager refuses access to preferences. " + ex);
        }
    }

    public static void flush() throws BackingStoreException {
        try {
            Preferences.userNodeForPackage(SirsCore.class).flush();
        } catch (SecurityException ex) {
            throw new SecurityException("A security manager refuses access to preferences. " + ex);
        }
    }
}
