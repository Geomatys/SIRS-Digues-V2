/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs;

import fr.sirs.core.SirsCore;
import fr.sirs.ui.FXBasemapEditor;
import fr.sirs.util.property.SirsPreferences;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.geotoolkit.storage.coverage.CoverageStore;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.security.DefaultClientSecurity;
import org.geotoolkit.storage.DataStoreFactory;
import org.geotoolkit.storage.DataStores;
import org.geotoolkit.storage.coverage.CoverageStoreFactory;
import org.geotoolkit.utility.parameter.ParametersExt;
import org.geotoolkit.wms.WebMapClient;
import org.geotoolkit.wms.xml.WMSVersion;
import org.geotoolkit.wmts.WebMapTileClient;
import org.geotoolkit.wmts.xml.WMTSVersion;
import org.opengis.parameter.ParameterValueGroup;


/**
 * Provided the title and the store containing the basemap layer defined into the basemap preferences.
 *
 * @author maximegavens
 */
public class BasemapImporter {

    private Logger LOGGER = Logger.getLogger(BasemapImporter.class.getName());
    private static final String DEFAULT_BASEMAP_URL = "http://c.tile.stamen.com/toner";

    private String title;
    private CoverageStore store;

    public BasemapImporter() throws URISyntaxException, MalformedURLException, DataStoreException {
        final String storeType = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_CHOICE);

        if (storeType.equals(FXBasemapEditor.WMS_WMTS_CHOICE)) {
            initStoreFromWebMapClientParameter();
        } else if (storeType.equals(FXBasemapEditor.FILE_CHOICE)) {
            initStoreFromLocalFile();
        } else if (storeType.equals(FXBasemapEditor.OSM_TILE_CHOICE)) {
            initStoreFromOsmTileMapClient();
        } else {
            LOGGER.info("No default basemap choice are already made.");
            initDefaultStore();
        }
    }

    private void initStoreFromWebMapClientParameter() throws URISyntaxException, MalformedURLException {
        String baseUrl = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_WM_URL);
        String wmType = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_WM_TYPE);

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
    }

    private void initStoreFromLocalFile() throws DataStoreException {
        String filePath = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_LOCAL_FILE);
        final String fileType = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_FILE_TYPE);

        final File f = new File(filePath);
        DataStoreFactory factory;
        if (FXBasemapEditor.COVERAGE_FILE_TYPE.equals(fileType)) {
            factory = DataStores.getFactoryById("coverage-file");
        } else if (FXBasemapEditor.HEXAGON_TYPE.equals(fileType)) {
            factory = DataStores.getFactoryById("hexagon");
        } else {
            throw new IllegalArgumentException("Type de fichier local non géré pour le chargement du fond de carte par défaut.");
        }
        if(factory instanceof CoverageStoreFactory) {
            CoverageStoreFactory coverageFactory = (CoverageStoreFactory) factory;
            final ParameterValueGroup params = coverageFactory.getParametersDescriptor().createValue();
            ParametersExt.getOrCreateValue(params, "path").setValue(f.toURI());
            title = f.getName();
            store = (CoverageStore) coverageFactory.create(params);
        }
    }

    private void initStoreFromOsmTileMapClient() throws URISyntaxException, MalformedURLException {
        String baseUrl = SirsPreferences.INSTANCE.getPropertySafeOrDefault(SirsPreferences.PROPERTIES.BASEMAP_OSM_TILE_URL);

        baseUrl = getUrlWithoutParameters(baseUrl);
        final URL url = new URL(baseUrl);
        title = url.getHost();
        store = new OSMTileMapClient(url, null, 18, true);
    }

    private void initDefaultStore() throws MalformedURLException {
        title = "stamen";
        //store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);
        //store = new OSMTileMapClient(new URL("http://c.tile.stamen.com/terrain"), null, 18, true);
        //store = new OSMTileMapClient(new URL("http://c.tile.stamen.com/toner"), null, 18, true);
        //store = new OSMTileMapClient(new URL("https://tile.thunderforest.com/cycle"), new ApiSecurity(SIRSAuthenticator.getThunderForestApiKey()), 18, true);
        store = new OSMTileMapClient(new URL(DEFAULT_BASEMAP_URL), null, 18, true);
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
