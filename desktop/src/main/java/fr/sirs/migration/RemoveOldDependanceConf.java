/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.migration;

import fr.sirs.core.ModuleDescription;
import fr.sirs.core.SirsCore;
import static fr.sirs.core.SirsCore.INFO_DOCUMENT_ID;
import fr.sirs.core.SirsDBInfo;
import java.util.Map;
import javafx.concurrent.Task;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author maximegavens
 */
public class RemoveOldDependanceConf extends Task {
    final CouchDbConnector connector;

    public RemoveOldDependanceConf(final CouchDbConnector connector) {
        ArgumentChecks.ensureNonNull("Database connector", connector);
        this.connector = connector;
    }

    @Override
    protected Object call() throws Exception {
        try  {
            final SirsDBInfo info = connector.get(SirsDBInfo.class, INFO_DOCUMENT_ID);
            final Map<String, ModuleDescription> moduleDescriptions = info.getModuleDescriptions();

            if (moduleDescriptions.containsKey("plugin-dependance")) {
                updateTitle("Suppréssion de l'ancienne configuration du plugin dépendance");
                moduleDescriptions.remove("plugin-dependance");
                info.addModuleDescriptions(moduleDescriptions);
                updateMessage("Mise à jour de la base de données");
                connector.update(info);
            }
        } catch (Exception ex) {
            SirsCore.LOGGER.warning("Something goes wrong during RemoveOldDependanceConf Upgrade: " + ex);
        }
        return true;
    }
}
