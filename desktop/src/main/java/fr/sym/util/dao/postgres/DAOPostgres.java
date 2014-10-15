/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.dao.postgres;

import fr.sym.util.dao.DAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class DAOPostgres implements DAO {

    private static final String LOGIN = "geouser";
    private static final String PASSWORD = "geopw";
    private static final String DATABASE = "geodb";
    protected static final String REGEXP = "~";
    private Connection connection = null;
    protected PreparedStatement preparedStatement = null;
    protected Statement statement = null;
    protected String sqlStatement = null;
    protected ResultSet resultSet = null;

    @Override
    public void initConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Pas trouvé de driver !");
        }
        String url = "jdbc:postgresql://localhost:5432/" + DAOPostgres.DATABASE;
        // String url = "jdbc:postgresql://localhost:5433/dardi";//NE MARCHE PAS
        // CHEZ SAMUEL
        connection = DriverManager.getConnection(url, DAOPostgres.LOGIN,
                DAOPostgres.PASSWORD);
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (preparedStatement != null) {
            preparedStatement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void executePreparedQuery() throws SQLException {
        this.resultSet = this.preparedStatement.executeQuery();
    }

    @Override
    public void prepareCall(String sqlStatement) throws SQLException {
        this.sqlStatement = sqlStatement;
        System.out.println(sqlStatement);
        if (preparedStatement != null) {
            preparedStatement.close();
        }
        this.preparedStatement = getConnection().prepareCall(sqlStatement,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    }

    @Override
    public void executeQuery(String sqlStatement) throws SQLException {
        this.sqlStatement = sqlStatement;
        System.out.println(sqlStatement);
        if (statement != null) {
            statement.close();
        }
        statement = getConnection().createStatement();
        this.resultSet = statement.executeQuery(sqlStatement);
    }

    @Override
    public int foundRows() throws SQLException {
        //String pattern = "/select .* from .* order by .* limit \\d* offset \\d*/";
        //String replacement = "select count(*) as total from $2";

        Pattern pattern1 = Pattern.compile("select .* from");
        //	Pattern pattern2 = Pattern.compile("from .* order by");
        Pattern pattern3 = Pattern.compile("order by .* ");
        Pattern pattern4 = Pattern.compile("limit \\d* offset \\d*");

        Matcher matcher1 = pattern1.matcher(this.sqlStatement);
        //Matcher matcher2 = pattern2.matcher(this.sqlStatement);

        StringBuffer sbf = new StringBuffer();
        if (matcher1.find()) {
            matcher1.appendReplacement(sbf, "select count(*) as total from");
        }
        matcher1.appendTail(sbf);

        Matcher matcher4 = pattern4.matcher(sbf.toString());
        sbf = new StringBuffer();
        if (matcher4.find()) {
            matcher4.appendReplacement(sbf, "");
        }
        matcher4.appendTail(sbf);

        Matcher matcher3 = pattern3.matcher(sbf.toString());
        sbf = new StringBuffer();
        if (matcher3.find()) {
            matcher3.appendReplacement(sbf, "");
        }
        matcher3.appendTail(sbf);

        System.out.println(sbf.toString());
        if (statement != null) {
            statement.close();
        }
        statement = getConnection().createStatement();
        this.resultSet = statement.executeQuery(sbf.toString());
        resultSet.next();

        return resultSet.getInt("total");
    }
}
