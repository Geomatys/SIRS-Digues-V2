/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.dao;

import fr.symadrem.sirs.core.model.Digue;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface DigueDAO extends DAO {
    
    public List<Digue> retrieveDigues() throws SQLException;
    
}
