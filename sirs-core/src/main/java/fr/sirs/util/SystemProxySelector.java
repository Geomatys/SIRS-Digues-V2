/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import com.btr.proxy.search.ProxySearch;
import com.sun.javafx.PlatformUtil;
import fr.sirs.core.SirsCore;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * A proxy selector which uses power of Proxy-vole library to detect proxies defined
 * on underlying system and configure JVM to use them.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class SystemProxySelector extends ProxySelector {
    
        private static final Pattern NO_PROXY = Pattern.compile("(?i)(localhost)|(127.0.0.1)");
        
        private final ProxySelector source;
        
        public SystemProxySelector() {
            ProxySearch ps = ProxySearch.getDefaultProxySearch();
            ps.setPacCacheSettings(32, 300000); // keep at most 32 uri configuration in cache for 5 minutes max.

            if (PlatformUtil.isWindows()) {
                ps.addStrategy(ProxySearch.Strategy.IE);
                ps.addStrategy(ProxySearch.Strategy.WIN);
            } else if (PlatformUtil.isLinux()) {
                ps.addStrategy(ProxySearch.Strategy.GNOME);
                ps.addStrategy(ProxySearch.Strategy.KDE);
            } else {
                ps.addStrategy(ProxySearch.Strategy.OS_DEFAULT);
            }

            ps.addStrategy(ProxySearch.Strategy.FIREFOX);
            ps.addStrategy(ProxySearch.Strategy.JAVA);
            
            source = ps.getProxySelector();
        }
        
        @Override
        public List<Proxy> select(final URI uri) {
            if (source == null || NO_PROXY.matcher(uri.getHost()).matches()) {
                SirsCore.LOGGER.log(Level.FINE, "Local connection : {0}", uri);
                return Collections.singletonList(Proxy.NO_PROXY);
            } else {
                final List<Proxy> proxies = source.select(uri);
                SirsCore.LOGGER.log(Level.FINE, () -> {
                    final StringBuilder builder = new StringBuilder("Connection to ").append(uri);
                    builder.append("\n\tAvailable proxies : ");
                    for (final Proxy p : proxies) {
                        builder.append("\n\t").append(p.toString());
                    }
                    return builder.toString();
                });
                for (final Proxy p : proxies) {
                    System.out.println("    PROXY "+ p);
                }
                return proxies;
            }
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            if (source != null)
                source.connectFailed(uri, sa, ioe);
        }
}
