/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util;

import fr.sym.digue.Injector;
import fr.sym.util.dao.DigueDAO;
import fr.sym.util.dao.postgres.DigueDAOPostgres;
import fr.symadrem.sirs.core.component.DigueRepository;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DbMapper {
    
    private DigueRepository digueRepository;
    
    private DigueDAO digueDAO = DigueDAOPostgres.getInstance();
    
    public DbMapper(DigueRepository digueRepository){
        this.digueRepository = digueRepository;
    }

    public DbMapper() {
    }
            
    
    public void mapDigues() throws SQLException{
        digueDAO.retrieveDigues().stream().forEach((digue) -> {
            System.out.println(digue);
            digueRepository.add(digue);
        });
    }
    
    public static void main(String[] args) throws SQLException {
        DbMapper dbMapper = new DbMapper();
        dbMapper.mapDigues();
    }
}
