package di.main;

import di.enumeration.dbms.DBMS;

/**
 *
 * @author vitor
 */
public class EnvironmentConfiguration {
    
    private String DataIngestorDirectory = null;
    private int port = 50000;
    private String server = "localhost";
    private String databaseName = "dataflow_analyzer";
    private String user = "monetdb";
    private String password = "monetdb";
    private DBMS dbms = DBMS.MONETDB;

    public EnvironmentConfiguration() {
    }
    
    public String getDataIngestorDirectory() {
        return DataIngestorDirectory;
    }

    public void setDataIngestorDirectory(String DataIngestorDirectory) {
        this.DataIngestorDirectory = DataIngestorDirectory;
    }

    public int getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isMemSQL() {
        return dbms.equals(DBMS.MEMSQL);
    }

    public void setDBMS(String dbmsName) {
        this.dbms = DBMS.valueOf(dbmsName.toUpperCase());
    }
    
    public DBMS getDBMS(){
        return dbms;
    }

    public boolean isMonetDB() {
        return (dbms.equals(DBMS.MONETDB));
    }
    
}