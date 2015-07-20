import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.PluginInfo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import org.apache.sis.util.ObjectConverters;

final Path targetDir =  basedir.toPath().resolve("target");
if (!Files.isDirectory(targetDir)) {
    Files.createDirectory(targetDir);
}
final File target = targetDir.resolve(project.getArtifactId()+".json").toFile();

final PluginInfo info = new PluginInfo();
info.setName(project.getArtifactId());
info.setDescription(project.getDescription());

final String[] version = project.getVersion().split("[^\\d]+");

String vMajor = properties.getProperty("plugin.version.major");
String vMinor = properties.getProperty("plugin.version.major");

if (vMajor == null || vMajor.isEmpty()) {
	if (version.length > 0) {
		vMajor = version[0];
	} else {
		vMajor = "0";
	}
}
info.setVersionMajor(Integer.parseInt(vMajor));

if (vMinor == null || vMinor.isEmpty()) {
	if (version.length > 1) {
		vMinor = version[1];
	} else {
		vMinor = "1";
	}
}
info.setVersionMinor(Integer.parseInt(vMinor));

final String downloadURL = properties.getProperty("plugin.download.url");
if (downloadURL != null) {
	try {
		URL dlURL = ObjectConverters.convert(downloadURL, URL.class);
		info.setDownloadURL(dlURL.toExternalForm());
	} catch (Exception e) {
		log.error("Property \"plugin.download.url\" is not a valid URL."
			+ "\nFound value : "+downloadURL
			+ "Error : "+e.getMessage());
	}
}

new ObjectMapper().writeValue(target, info);
