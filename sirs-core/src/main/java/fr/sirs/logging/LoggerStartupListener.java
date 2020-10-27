/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.logging;

import ch.qos.logback.core.Context;
import java.nio.file.Path;
import fr.sirs.core.SirsCore;
import ch.qos.logback.classic.jul.LevelChangePropagator;

/**
 * This class is used to redirect the path of logs folder into the configuration folder.
 * 
 * @author maximegavens
 */
public class LoggerStartupListener extends LevelChangePropagator {

    @Override
    public void start() {
        if (!isStarted()) {
            final Path logPath = SirsCore.CONFIGURATION_PATH;

            if (logPath == null) return;

            final Context context = getContext();
            context.putProperty("CONF_PATH", logPath.toString());
        }
        super.start();
    }
}
