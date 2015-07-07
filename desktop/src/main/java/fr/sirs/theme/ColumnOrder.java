
package fr.sirs.theme;

import fr.sirs.core.SirsCore;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Stoque l'ordre des colonnes pour chaque classe du modèle.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ColumnOrder {

    private static final Properties config = new Properties();
    static {
        try{
            final Enumeration<URL> rsr = ClassLoader.getSystemClassLoader().getResources("fr/sirs/theme/ui/columnOrder.properties");
            while(rsr.hasMoreElements()){
                final URL url = rsr.nextElement();
                try (InputStream stream = url.openStream()) {
                    config.load(stream);
                }
            }
        } catch (IOException ex) {
            SirsCore.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private ColumnOrder(){}

    public static Comparator<String> createComparator(String className){
        return new ColumnComparator(className);
    }

    public static List<String> sort(String className, List<String> properties){
        final Comparator<String> cmp = createComparator(className);
        Collections.sort(properties, cmp);
        return properties;
    }

    private static final class ColumnComparator implements Comparator<String>{

        final Map<String,Integer> map = new HashMap<>();

        public ColumnComparator(String className) {
            final String order = config.getProperty(className);
            if(order!=null){
                final String[] parts = order.split(",");
                final Map<String,Integer> map = new HashMap<>();
                for(int i=0;i<parts.length;i++){
                    map.put(parts[i], i);
                }
            }
        }

        @Override
        public int compare(String o1, String o2) {
            final Integer i1 = map.get(o1);
            final Integer i2 = map.get(o2);
            if(i1!=null && i2!=null){
                return Integer.compare(i1, i2);
            }else if(i1!=null){
                return -1;
            }else if(i2!=null){
                return +1;
            }else{
                return o1.compareTo(o2);
            }
        }

    }

}
