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
 *
 * @author maximegavens
 */
public class LoggerStartupListener extends LevelChangePropagator {

    @Override
    public void start() {
        if (!isStarted()) {
            Path logPath = SirsCore.CONFIGURATION_PATH;

            Context context = getContext();
            context.putProperty("CONF_PATH", logPath.toString());
        }
        super.start();
    }
}
