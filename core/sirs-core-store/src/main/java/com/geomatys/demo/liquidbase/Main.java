package com.geomatys.demo.liquidbase;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class Main {

    public static void main(String[] args) throws LiquibaseException, SQLException {

        java.sql.Connection c = DriverManager.getConnection("jdbc:h2:mem:db;INIT=RUNSCRIPT FROM 'classpath:/create.sql'");
        c.prepareCall("create schema admin").execute();
        Liquibase liquibase = null;
//        try {
//            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
//            liquibase = new Liquibase("db-changelog.xml", new ClassLoaderResourceAccessor(), database);
//            liquibase.update("");
//        } finally {
//            if (c != null) {
//                try {
//                    c.rollback();
//                } catch (SQLException e) {
//                    // nothing to do
//                }
//            }
//        }
        
        ResultSet tables = c.getMetaData().getTables(null, "PUBLIC", null, null);
        while(tables.next()) {
            System.out.println(tables.getString("TABLE_NAME"));
        }

//        File file = new File("/tmp/liquidtestdb");
//        if (file.exists())
//            org.apache.derby.iapi.services.io.FileUtil.removeDirectory(file);
//
//        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/applicationContext.xml");
//        applicationContext.close();
    }
}
