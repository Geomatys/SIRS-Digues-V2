/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs;

import fr.sirs.core.SirsCore;
import fr.sirs.ui.FXBasemapEditor;
import fr.sirs.util.property.SirsPreferences;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.geotoolkit.storage.coverage.CoverageStore;
import org.apache.http.client.utils.URLEncodedUtils;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.security.DefaultClientSecurity;
import org.geotoolkit.wms.WebMapClient;
import org.geotoolkit.wms.xml.WMSVersion;
import org.geotoolkit.wmts.WebMapTileClient;
import org.geotoolkit.wmts.xml.WMTSVersion;


/**
 *
 * @author maximegavens
 */
public class BasemapImporter {

    private Logger LOGGER = Logger.getLogger(BasemapImporter.class.getName());
    private static final String DEFAULT_BASEMAP_URL = "http://c.tile.stamen.com/toner";

    private String title;
    private CoverageStore store;

    public BasemapImporter() {
        final String storeType = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_CHOICE);

        if (storeType.equals(FXBasemapEditor.WMS_WMTS_CHOICE)) {
            initStoreFromWebMapClientParameter();
        } else if (storeType.equals(FXBasemapEditor.FILE_CHOICE)) {
            initStoreFromLocalFile();
        } else if (storeType.equals(FXBasemapEditor.OTHER_CHOICE)) {
            initStoreFromOtherService();
        } else {
            LOGGER.info("No default basemap choice are already made.");
            initDefaultStore();
        }
    }

    private void initStoreFromWebMapClientParameter() {
        String baseUrl = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_WM_URL);
        String wmType = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_WM_TYPE);

        try {
            baseUrl = getUrlWithoutParameters(baseUrl);
            final URL url = new URL(baseUrl);
            title = url.getHost();
            if (wmType.equals(FXBasemapEditor.WMS111)) {
                store = new WebMapClient(url, DefaultClientSecurity.NO_SECURITY, WMSVersion.v111, SirsCore.GEO_CLIENT_TIMEOUT);
            } else if (wmType.equals(FXBasemapEditor.WMS130)) {
                store = new WebMapClient(url, DefaultClientSecurity.NO_SECURITY, WMSVersion.v130, SirsCore.GEO_CLIENT_TIMEOUT);
            } else if (wmType.equals(FXBasemapEditor.WMTS100)) {
                store = new WebMapTileClient(url, DefaultClientSecurity.NO_SECURITY, WMTSVersion.v100, SirsCore.GEO_CLIENT_TIMEOUT);
            }
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.WARNING, "L'url choisie pour le fond de carte par défaut est malformée", ex);
            initDefaultStore();
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, "L'url choisie pour le fond de carte par défaut est malformée", ex);
            initDefaultStore();
        }
    }

    private void initStoreFromLocalFile() {
        final String baseUrl = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_LOCAL_FILE);
    }
    
    private void initStoreFromOtherService() {
        String baseUrl = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_OTHER_URL);

        try {
            baseUrl = getUrlWithoutParameters(baseUrl);
            final URL url = new URL(baseUrl);
            title = url.getHost();
            store = new OSMTileMapClient(url, null, 18, true);
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.WARNING, "L'url choisie pour le fond de carte par défaut est malformée", ex);
            initDefaultStore();
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, "L'url choisie pour le fond de carte par défaut est malformée", ex);
            initDefaultStore();
        }
    }

    private void initDefaultStore() {
        try {
            title = "stamen";
            //store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);
            //store = new OSMTileMapClient(new URL("http://c.tile.stamen.com/terrain"), null, 18, true);
            //store = new OSMTileMapClient(new URL("http://c.tile.stamen.com/toner"), null, 18, true);
            //store = new OSMTileMapClient(new URL("https://tile.thunderforest.com/cycle"), new ApiSecurity(SIRSAuthenticator.getThunderForestApiKey()), 18, true);
            store = new OSMTileMapClient(new URL(DEFAULT_BASEMAP_URL), null, 18, true);
        } catch (MalformedURLException ex) {
            // Sensé ne jamais se produire
            throw new RuntimeException(ex);
        }
    }

    /**
     * TODO: à utiliser si l'url fourni contient des paramètres.
     * @param url
     */
    private void parseQuery(final String url) throws URISyntaxException {
        List<NameValuePair> parse = URLEncodedUtils.parse(new URI(url), "UTF-8");
        for (NameValuePair nvp: parse) {
            String name = nvp.getName();
            String value = nvp.getValue();
        }
    }

    private String getUrlWithoutParameters(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return new URI(uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                null, // Ignore the query part of the input url
                uri.getFragment()).toString();
    }

    public String getTitle() {
        return title;
    }

    public CoverageStore getStore() {
        return store;
    }
}
