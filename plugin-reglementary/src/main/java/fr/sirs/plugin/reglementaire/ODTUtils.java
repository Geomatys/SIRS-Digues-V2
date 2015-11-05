
package fr.sirs.plugin.reglementaire;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.util.SirsStringConverter;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.sf.jooreports.templates.DocumentTemplate;
import net.sf.jooreports.templates.DocumentTemplateException;
import net.sf.jooreports.templates.DocumentTemplateFactory;
import org.apache.sis.util.Static;
import org.ektorp.DocumentNotFoundException;

/**
 * Classe utilitaire d'écriture de fichier ODT.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ODTUtils extends Static{

    private static final int IMAGE_WIDTH = 140;
    private static DocumentTemplate DEFAULT_TEMPLATE;

    public static synchronized DocumentTemplate getDefaultTemplate() throws IOException{
        if(DEFAULT_TEMPLATE!=null) return DEFAULT_TEMPLATE;
        final DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
        DEFAULT_TEMPLATE = documentTemplateFactory.getTemplate(ODTUtils.class.getResourceAsStream("/fr/sirs/plugin/reglementaire/defaultTemplate.odt"));
        return DEFAULT_TEMPLATE;
    }

    public static Map toTemplateMap(Object candidate) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        final Class pojoClass = candidate.getClass();
        final HashMap<String, PropertyDescriptor> props = SIRS.listSimpleProperties(pojoClass);
        final LabelMapper labelMapper = LabelMapper.get(pojoClass);
        final SirsStringConverter cvt = new SirsStringConverter();
        final Previews previews = Injector.getSession().getPreviews();

        final List<Map<String,Object>> properties = new ArrayList<>();
        for(Entry<String,PropertyDescriptor> entry : props.entrySet()){
            if(entry.getValue().getReadMethod()!=null){
                Object val = entry.getValue().getReadMethod().invoke(candidate);
                final HashMap map = new HashMap(2);
                map.put("key", labelMapper.mapPropertyName(entry.getKey()));
                if(val instanceof String){
                    try{
                        val = cvt.toString(previews.get((String)val));
                    }catch(DocumentNotFoundException ex){/**pas important*/}
                }
                map.put("value", val==null ? "":val.toString() );
                properties.add(map);
            }
        }

        final Map objectMap = new HashMap();
        objectMap.put("properties", properties);
        objectMap.put("class", candidate.getClass().getSimpleName());

        return objectMap;
    }

    /**
     * Remplissage d'un modèle de rapport pour un objet donné.
     *
     * Template au format JODReport : http://jodreports.sourceforge.net/?q=node/23
     *
     * @param templateFile fichier ODT template
     * @param candidate bean servant au remplissage
     * @param outputFile fichier ODT de sortie
     * @throws IOException
     * @throws TemplateModelException
     * @throws DocumentTemplateException
     */
    public static void generateReport(File templateFile, Object candidate, Path outputFile)
            throws IOException, TemplateModelException, DocumentTemplateException, IllegalAccessException,
            IntrospectionException, InvocationTargetException {
        final DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
        final DocumentTemplate template = documentTemplateFactory.getTemplate(templateFile);
        generateReport(template, candidate, outputFile);
    }

    /**
     * Remplissage d'un modèle de rapport pour un objet donné.
     *
     * Template au format JODReport : http://jodreports.sourceforge.net/?q=node/23
     *
     * @param template template JOOReport
     * @param candidate bean servant au remplissage
     * @param outputFile fichier ODT de sortie
     * @throws IOException
     * @throws TemplateModelException
     * @throws DocumentTemplateException
     */
    public static void generateReport(DocumentTemplate template, Object candidate, Path outputFile)
            throws IOException, TemplateModelException, DocumentTemplateException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {
        if(!(candidate instanceof Map || candidate instanceof TemplateModel)){
            final Class pojoClass = candidate.getClass();
            final HashMap<String, PropertyDescriptor> props = SIRS.listSimpleProperties(pojoClass);
            final SirsStringConverter cvt = new SirsStringConverter();
            final Previews previews = Injector.getSession().getPreviews();

            final Map<String,Object> properties = new HashMap<>();
            for(Entry<String,PropertyDescriptor> entry : props.entrySet()){
                if(entry.getValue().getReadMethod()!=null){
                    Object val = entry.getValue().getReadMethod().invoke(candidate);
                    if(val instanceof String){
                        try{
                            val = cvt.toString(previews.get((String)val));
                        }catch(DocumentNotFoundException ex){/**pas important*/}
                    }
                    properties.put(entry.getKey(), val == null ? "" : val.toString());
                }
            }
            candidate = properties;
            //candidate = BeansWrapper.getDefaultInstance().wrap(candidate);
        }
        template.createDocument(candidate, Files.newOutputStream(outputFile, StandardOpenOption.CREATE_NEW));
    }
}
