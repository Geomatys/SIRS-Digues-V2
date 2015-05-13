/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util.temp.dao.postgres;

import fr.sirs.util.temp.dao.DigueDAO;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.ElementCreator;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DigueDAOPostgres extends DAOPostgres implements DigueDAO {

    private static DigueDAO instance;
    
    private DigueDAOPostgres() {
    }

    public static DigueDAO getInstance() {
        if (DigueDAOPostgres.instance == null) {
            DigueDAOPostgres.instance = new DigueDAOPostgres();
        }
        return DigueDAOPostgres.instance;
    }

    @Override
    public List<Digue> retrieveDigues() throws SQLException {

        List<Digue> digues = new ArrayList<>();

        try {
            this.initConnection();
            StringBuilder query = new StringBuilder(
                    "select * from digue");
            /*query.append(TABLE_COLLECTION);
             query.append(" where issn = ?");*/
            this.prepareCall(query.toString());
            //this.preparedStatement.setString(1, issn);
            this.executePreparedQuery();

            while (resultSet.next()) {
                Digue digue = ElementCreator.createAnonymValidElement(Digue.class);
                digue.setLibelle(this.resultSet.getString("libelle_digue"));
                digue.setCommentaire(this.resultSet.getString("commentaire_digue"));
                if (this.resultSet.getString("date_derniere_maj")!=null)
                    digue.setDateMaj(LocalDateTime.parse(this.resultSet.getString("date_derniere_maj")));
                digues.add(digue);
            }
        } finally {
            this.close();
        }
        return digues;
    }

    public static void main(String[] args) {
        DigueDAO dbc = new DigueDAOPostgres();

        try {
            dbc.retrieveDigues().stream().forEach((digue) -> {
                System.out.println(digue);
            });
            
        } catch (SQLException ex) {
            Logger.getLogger(DigueDAOPostgres.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
