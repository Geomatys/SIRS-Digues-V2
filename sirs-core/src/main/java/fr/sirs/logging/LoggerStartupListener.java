/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.logging;

import ch.qos.logback.core.Context;
import fr.sirs.core.SirsCore;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import java.util.logging.Level;
import java.util.prefs.Preferences;

/**
 * This class is used to configure the output of logs in the configuration folder.
 * 
 * @author maximegavens
 */
public class LoggerStartupListener extends LevelChangePropagator {

    @Override
    public void start() {
        if (!isStarted()) {
            try {
                final Context context = getContext();
                final Preferences prefs = Preferences.userNodeForPackage(SirsCore.class);
                final String rootPath = prefs.get("CONFIGURATION_FOLDER_PATH", "none");
                String logPath = "/tmp";
                if (!rootPath.equals("none")) {
                    logPath = rootPath + "/." + SirsCore.NAME;
                }
                context.putProperty("CONF_PATH", logPath.toString());
            } catch (Exception ex) {
                SirsCore.LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                context.putProperty("CONF_PATH", "/tmp");
            }
        }
        super.start();
    }
}