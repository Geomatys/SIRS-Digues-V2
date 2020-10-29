/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.logging;

import ch.qos.logback.core.rolling.RollingFileAppender;

/**
 *
 * @author maximegavens
 */
public class LoggerFileAppender extends RollingFileAppender {
    
    private static LoggerFileAppender INSTANCE = new LoggerFileAppender();
    
    public static LoggerFileAppender getInstance() {
        return INSTANCE;
    }
}
