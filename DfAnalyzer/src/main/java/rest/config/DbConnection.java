package rest.config;

import com.google.common.base.MoreObjects;
import qp.dataflow.Attribute;
import qp.dataflow.Dataflow;
import qp.dataflow.Dataset;
import qp.dataflow.Dependency;
import qp.dataflow.Transformation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;
import pde.enumeration.ExtractionCartridge;
import pde.enumeration.ExtractionMethod;
import qp.query.Extractor;
import qp.query.ExtractorCombination;
import qp.query.FastBitQuery;
import qp.query.MonetDbSqlQuery;
import qp.query.QuerySpecification;
import qp.query.SqlClause;

/**
 *
 * @author tperrotta
 */
public class DbConnection {

    public Connection connection;
    public DbConfiguration config;
    public String path = "/Users/vitor/Desktop/";

    public DbConnection(DbConfiguration config) {
        try {
            this.config = config;
            Class.forName("org.monetdb.jdbc.MonetDriver");

            connection = DriverManager.getConnection(new StringBuilder()
                    .append("jdbc:monetdb://")
                    .append(config.getServer())
                    .append(":")
                    .append(config.getPort())
                    .append("/")
                    .append(config.getDatabaseName())
                    .toString(),
                    config.getUser(), config.getPassword());
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("db", connection)
                .toString();
    }

    public Dataflow loadDataflow(QuerySpecification querySpec) {
        Dataflow dataflow = getDataflow(querySpec.getDataflowTag(), querySpec.getDataflowVersion());
        dataflow.addTransformations(getTransformations(dataflow.getId(), dataflow.getVersion()));
        dataflow.addDatasets(getDatasets(dataflow.getId(), dataflow.getVersion()));
        // dataflow.addPrograms(getPrograms(dataflow.getId(), dataflow.getVersion()));

        for (Dataset ds : dataflow.getDatasets()) {
            ds.addExtractors(getExtractors(ds.getId()));
            ds.addExtractorCombinations(getExtractorCombinations(ds));
            ds.addAttributes(getAttributes(ds));
            updateDependencies(dataflow, ds);
        }
        return dataflow;
    }

    public Dataflow getDataflow(String dfTag, int dfVersion) {
        Dataflow dataflow = null;
        try {
            PreparedStatement st = connection.prepareStatement("SELECT df.id, df.tag, dfv.version "
                    + "FROM dataflow df, dataflow_version dfv "
                    + "WHERE tag=? AND df.id = dfv.df_id AND dfv.version=?;",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, dfTag);
            st.setInt(2, dfVersion);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                dataflow = new Dataflow(rs.getInt(3), rs.getString(2));
                dataflow.setId(rs.getInt(1));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dataflow;
    }

    public ArrayList<Transformation> getTransformations(Integer id, Integer version) {
        ArrayList<Transformation> dts = new ArrayList<>();
        try {
            PreparedStatement st = connection.prepareStatement("SELECT dt.id, dt.tag "
                    + "FROM data_transformation dt, dataflow df, dataflow_version dfv "
                    + "WHERE df.id = dfv.df_id AND df.id=? "
                    + "AND dfv.version=? AND df.id = dt.df_id;",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, id);
            st.setInt(2, version);
            ResultSet rs = st.executeQuery();

            Transformation dt;
            while (rs.next()) {
                dt = new Transformation();
                dt.setId(rs.getInt(1));
                dt.setTag(rs.getString(2));
                dts.add(dt);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return dts;
    }

    public ArrayList<Dataset> getDatasets(Integer id, Integer version) {
        ArrayList<Dataset> dss = new ArrayList<>();
        try {
            PreparedStatement st = connection.prepareStatement("SELECT ds.id, ds.tag "
                    + "FROM data_set ds, dataflow df, dataflow_version dfv "
                    + "WHERE df.id = dfv.df_id AND df.id=? "
                    + "AND dfv.version=? AND df.id = ds.df_id;",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, id);
            st.setInt(2, version);
            ResultSet rs = st.executeQuery();

            Dataset ds;
            while (rs.next()) {
                ds = new Dataset();
                ds.setId(rs.getInt(1));
                ds.setTag(rs.getString(2));
                dss.add(ds);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return dss;
    }

//    public ArrayList<Program> getPrograms(Integer id, Integer version) {
//        ArrayList<Program> programs = new ArrayList<>();
//        try {
//            PreparedStatement st = connection.prepareStatement("SELECT p.id, p.name, p.path "
//                    + "FROM program p, dataflow df, dataflow_version dfv "
//                    + "WHERE df.id = dfv.df_id AND df.id=? "
//                    + "AND dfv.version=? AND df.id = p.df_id;",
//                    Statement.RETURN_GENERATED_KEYS);
//            st.setInt(1, id);
//            st.setInt(2, version);
//            ResultSet rs = st.executeQuery();
//
//            Program p = null;
//            while (rs.next()) {
//                p = new Program();
//                p.setId(rs.getInt(1));
//                p.setName(rs.getString(2));
//                p.setPath(rs.getString(3));
//                programs.add(p);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return programs;
//    }
    public ArrayList<Extractor> getExtractors(Integer setID) {
        ArrayList<Extractor> extractors = new ArrayList<>();
        try {
            PreparedStatement st = connection.prepareStatement("SELECT e.id, e.tag, e.cartridge, e.extension "
                    + "FROM extractor e, data_set ds "
                    + "WHERE ds.id=? AND ds.id = e.ds_id;",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, setID);
            ResultSet rs = st.executeQuery();

            Extractor e = null;
            while (rs.next()) {
                e = new Extractor();
                e.setID(rs.getInt(1));
                e.setTag(rs.getString(2));
                e.setMethod(ExtractionMethod.valueOf(rs.getString(3)));
                e.setCartridge(ExtractionCartridge.valueOf(rs.getString(4)));
                extractors.add(e);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return extractors;
    }

    public ArrayList<ExtractorCombination> getExtractorCombinations(Dataset s) {
        ArrayList<ExtractorCombination> extractorCombinations = new ArrayList<>();
        try {
            PreparedStatement st = connection.prepareStatement("SELECT e.id, e.outer_ext_id, e.inner_ext_id, e.keys, e.key_types "
                    + "FROM extractor_combination e, data_set ds "
                    + "WHERE ds.id=? AND ds.id = e.ds_id;",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, s.getId());
            ResultSet rs = st.executeQuery();

            ExtractorCombination e = null;
            while (rs.next()) {
                e = new ExtractorCombination();
                e.setID(rs.getInt(1));
                e.setDataset(s);
                e.setOuterExtractor(s.getExtractor(rs.getInt(2)));
                e.setInnerExtractor(s.getExtractor(rs.getInt(3)));
                e.setKeys(rs.getString(4));
                e.setKeyTypes(rs.getString(5));
                extractorCombinations.add(e);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return extractorCombinations;
    }

    public ArrayList<Attribute> getAttributes(Dataset s) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        try {
            PreparedStatement st = connection.prepareStatement("SELECT a.id, a.extractor_id, a.name, a.type "
                    + "FROM attribute a, data_set ds "
                    + "WHERE ds.id=? AND ds.id = a.ds_id;",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, s.getId());
            ResultSet rs = st.executeQuery();

            Attribute att = null;
            while (rs.next()) {
                att = new Attribute();
                att.setId(rs.getInt(1));
                att.setExtractor(s.getExtractor(rs.getInt(2)));
                att.setName(rs.getString(3));
                att.setAttributeType(rs.getString(4));
                attributes.add(att);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return attributes;
    }

    public void updateDependencies(Dataflow dataflow, Dataset dataset) {
        try {
            PreparedStatement st = connection.prepareStatement("SELECT dep.previous_dt_id, dep.next_dt_id "
                    + "FROM data_dependency dep, data_set ds "
                    + "WHERE ds.id=? AND ds.id = dep.ds_id;",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, dataset.getId());
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                Optional<Transformation> optPreviousTransf = dataflow.getTransformation(rs.getInt(1));
                Optional<Transformation> optNextTransf = dataflow.getTransformation(rs.getInt(2));
                Transformation previousTransf = optPreviousTransf.orElse(null);
                Transformation nextTransf = optNextTransf.orElse(null);
                dataflow.addDependency(new Dependency(previousTransf, dataset, nextTransf));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void runQueries(Dataset dataset, MonetDbSqlQuery query) {
        try {
            //cleaning up
            String filePath = path + dataset.getTag() + ".csv";
            Files.deleteIfExists((new java.io.File(filePath)).toPath());
            //monetdb
            System.out.println(query.getCopyOperation("'" + filePath + "'"));
            PreparedStatement st = connection.prepareStatement(
                    query.getCopyOperation("'" + filePath + "'"));
            st.executeUpdate();
            //read line by line
            FileReader fileReader = new FileReader(new File(filePath));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int rowIndex = 1;
            int colIndex;
            while ((line = bufferedReader.readLine()) != null) {
                colIndex = 0;
                String[] columns = line.split(";");
                for (String indexerAttributeName : query.getSelectClause().getElements()) {
                    if (query.getIndexerAttributes().contains(indexerAttributeName)) {
                        //indexer
                        String idxFilePath = path + indexerAttributeName + ".csv";
                        FastBitQuery dbQuery = query.getIndexerQuery(indexerAttributeName);
                        dbQuery.setFromClause(new SqlClause(columns[colIndex]));
                        String idxQuery = dbQuery.getCopyOperation(idxFilePath);
                        System.out.println("-------------------------------------------");
                        System.out.println(idxQuery);
                        colIndex++;
                    }
                }
                rowIndex++;
            }
            fileReader.close();
            bufferedReader.close();
//            //indexers
//            for (Entry<Extractor, FastBitQuery> indexerQuery : query.getIndexerQueries().entrySet()) {
//                String idxQuery = indexerQuery.getValue().getCopyOperation(indexerQuery.getKey().getTag() + ".csv");
//                System.out.println("-------------------------------------------");
//                System.out.println(idxQuery);
//            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void runMonetDBQuery(MonetDbSqlQuery query) {
        try {
            String currentPath = System.getProperty("user.dir").replace('\\', '/');
            String filePath = currentPath + "/query_result.csv";
            Files.deleteIfExists((new java.io.File(filePath)).toPath());
            String querySQL = query.getCopyOperation(filePath);
            PreparedStatement st = connection.prepareStatement(querySQL);
            st.executeUpdate();
            System.out.println("================");
            System.out.println(querySQL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
