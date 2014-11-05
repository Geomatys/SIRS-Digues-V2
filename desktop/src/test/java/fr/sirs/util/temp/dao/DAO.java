/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util.temp.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface DAO {
    
    public void initConnection() throws SQLException;

    public Connection getConnection();

    public void close() throws SQLException;

    public void executePreparedQuery() throws SQLException;

    public void prepareCall(String sqlStatement) throws SQLException;

    public void executeQuery(String sqlStatement) throws SQLException;

    public int foundRows() throws SQLException;
}
