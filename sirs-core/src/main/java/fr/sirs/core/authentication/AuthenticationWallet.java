package fr.sirs.core.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import fr.sirs.core.SirsCore;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.geotoolkit.internal.GeotkFX;

/**
 * Registry which contains connection information users has already provided.
 * @author Alexis Manin (Geomatys)
 */
public class AuthenticationWallet {
    
    private static AuthenticationWallet INSTANCE;
    
    private final Path walletPath = SirsCore.CONFIGURATION_PATH.resolve("authWallet.json");
        
    private final ObservableMap<URL, Entry> wallet = FXCollections.observableMap(new HashMap<URL, Entry>());
    private final ReentrantReadWriteLock walletLock = new ReentrantReadWriteLock();
    
    private AuthenticationWallet() throws IOException {
        Path walletPath = SirsCore.CONFIGURATION_PATH.resolve("authWallet.json");
        if (Files.isRegularFile(walletPath)) {
            // TODO check if file is empty, because jackson explode on empty files.
            try (final InputStream walletStream = Files.newInputStream(walletPath, StandardOpenOption.READ)) {
                final ObjectMapper mapper = new ObjectMapper();
                ObjectReader reader = mapper.reader(Entry.class);
                JsonNode root = mapper.readTree(walletStream);
                if (root.isArray()) {
                    Iterator<JsonNode> iterator = root.iterator();
                    while(iterator.hasNext()) {
                        Entry entry = reader.readValue(iterator.next());
                        wallet.put(entry.serviceAddress, entry);
                    }
                }
            }
        } else {
            Files.createFile(walletPath);
        }
        
        /*
         * When cached wallet is modified, we update wallet on file system. We can
         * use only a read lock here, because file is only read at initialization.
         */
        wallet.addListener((MapChangeListener.Change<? extends URL, ? extends Entry> change) -> {
            walletLock.readLock().lock();
            try (final OutputStream walletStream = Files.newOutputStream(walletPath, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                new ObjectMapper().writeValue(walletStream, wallet.values());
            } catch (IOException e) {
                SirsCore.LOGGER.log(Level.WARNING, "Password wallet cannot be updated !", e);
            } finally {
                walletLock.readLock().lock();
            }
        });
    }
    
    public Entry get(final URL service) {
        walletLock.readLock().lock();
        try {
            return wallet.get(service);
        } finally {
            walletLock.readLock().unlock();
        }
    }
    
    public void put(final Entry authenticationInfo) {
        // Check if it doesn't exist already, to avoid useless update.
        walletLock.readLock().lock();
        final Entry existing;
        try {
            existing = wallet.get(authenticationInfo.serviceAddress);
        } finally {
            walletLock.readLock().unlock();
        }
        if (existing == null || !existing.equals(authenticationInfo)) {
            walletLock.writeLock().lock();
            try {
                wallet.put(authenticationInfo.serviceAddress, authenticationInfo);
            } finally {
                walletLock.writeLock().unlock();
            }
        }
    }
    
    public void remove(final Entry entry) {
        walletLock.writeLock().lock();
        try {
            wallet.remove(entry.serviceAddress, entry);
        } finally {
            walletLock.writeLock().unlock();
        }
    }
    
    public void removeForAddress(final InetAddress service) {
        walletLock.writeLock().lock();
        try {
            wallet.remove(service);
        } finally {
            walletLock.writeLock().unlock();
        }
    }
    /**
     *
     * @return Default registered password container, or null if an error occurred while initializing it.
     */
    public static AuthenticationWallet getDefault() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new AuthenticationWallet();
            } catch (IOException e) {
                SirsCore.LOGGER.log(Level.WARNING, "Password wallet cannot be initialized !", e);
                Runnable r = () -> GeotkFX.newExceptionDialog("Impossible d'initialiser le portefeuille de mots de passe.", e).show();
                if (Platform.isFxApplicationThread()) {
                    r.run();
                } else {
                    Platform.runLater(r);
                }
            }
        }
        return INSTANCE;
    }
    
    public static class Entry {
        public URL serviceAddress;
        public String login;
        public String password;
        
        public Entry(){};
        
        public Entry(final URL service, final String login, final String password) {
            serviceAddress = service;
            this.login = login;
            this.password = password;
        }

        @Override
        public int hashCode() {
            return 7 * Objects.hashCode(this.serviceAddress) + Objects.hashCode(this.login);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Entry other = (Entry) obj;
            if (!Objects.equals(this.serviceAddress, other.serviceAddress))
                return false;
            if (!Objects.equals(this.login, other.login))
                return false;
            if (!Objects.equals(this.password, other.password))
                return false;
            return true;
        }
        
        
    }
}
