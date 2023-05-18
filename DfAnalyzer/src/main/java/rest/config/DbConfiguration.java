package rest.config;

import com.google.common.base.MoreObjects;
import di.enumeration.dbms.DBMS;
import java.util.Objects;

/**
 *
 * @author tperrotta
 */
public class DbConfiguration {

    private Integer port;
    private String server;
    private String databaseName;
    private String user;
    private String password;
    private DBMS dbms;
    private String dataIngestorDirectory;
    
    public DbConfiguration() {
        
    }

    public DbConfiguration(Integer port, String server, String databaseName, 
            String user, String password, DBMS dbms, String diDirectory) {
        this.port = port;
        this.server = server;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        this.dbms = dbms;
        this.dataIngestorDirectory = diDirectory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.port, this.server, this.databaseName, this.user, this.password, this.dbms);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbConfiguration other = (DbConfiguration) obj;
        return Objects.equals(this.port, other.port)
                && Objects.equals(this.server, other.server)
                && Objects.equals(this.databaseName, other.databaseName)
                && Objects.equals(this.user, other.user)
                && Objects.equals(this.password, other.password)
                && Objects.equals(this.dbms, other.dbms);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("port", port)
                .add("server", server)
                .add("databaseName", databaseName)
                .add("user", user)
                .add("password", password)
                .add("dbms", dbms)
                .toString();
    }

    public int getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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

    public DBMS getDbms() {
        return dbms;
    }

    public void setDbms(DBMS dbms) {
        this.dbms = dbms;
    }

    public boolean isMemSQL() {
        return dbms.equals(DBMS.MEMSQL);
    }

    public boolean isMonetDB() {
        return dbms.equals(DBMS.MONETDB);
    }

    public String getDataIngestorDirectory() {
        return dataIngestorDirectory;
    }

    public void setDataIngestorDirectory(String dataIngestorDirectory) {
        this.dataIngestorDirectory = dataIngestorDirectory;
    }
}
