/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import com.sun.javafx.PlatformUtil;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SystemProxySelector {
    
        private static final Pattern NO_PROXY = Pattern.compile("(?i)(localhost)|(127.0.0.1)");
        
        private final ProxySelector source;
        
        public SystemProxySelector() {
            ProxySearch ps = ProxySearch.getDefaultProxySearch();
            ps.setPacCacheSettings(32, 300000); // keep at most 32 uri configuration in cache for 5 minutes max.

            if (PlatformUtil.isWindows()) {
                ps.addStrategy(ProxySearch.Strategy.IE);
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
        public List<Proxy> select(URI uri) {
            if (source == null || NO_PROXY.matcher(uri.getHost()).matches()) {
                return Collections.singletonList(Proxy.NO_PROXY);
            } else {
                return source.select(uri);
            }
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            if (source != null)
                source.connectFailed(uri, sa, ioe);
        }
}
