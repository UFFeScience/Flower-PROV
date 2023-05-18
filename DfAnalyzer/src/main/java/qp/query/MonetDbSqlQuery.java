package qp.query;

import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author tperrotta
 */
public class MonetDbSqlQuery {

    private SqlClause selectClause;
    private SqlClause fromClause;
    private SqlClause whereClause;
    private HashMap<Extractor,FastBitQuery> indexerQueries;
    private HashMap<String,Extractor> indexerAttributes;

    public MonetDbSqlQuery() {
        this.indexerQueries = new HashMap<>();
        this.indexerAttributes = new HashMap<>();
    }

    public MonetDbSqlQuery(SqlClause selectClause, SqlClause fromClause, SqlClause whereClause) {
        this.indexerQueries = new HashMap<>();
        this.indexerAttributes = new HashMap<>();
        this.selectClause = selectClause;
        this.fromClause = fromClause;
        this.whereClause = whereClause;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        sb.append(selectClause.toString().isEmpty() ? "*" : selectClause);
        sb.append("\n");

        sb.append("FROM ").append(fromClause).append("\n");

        if (!whereClause.toString().isEmpty()) {
            sb.append("WHERE ").append(whereClause).append("\n");
        }

        sb.append(";");

        return sb.toString().trim();
    }
    
    public String getCopyOperation(String filename){
        StringBuilder sb = new StringBuilder();
        sb.append("COPY SELECT ");
        sb.append(selectClause.toString().isEmpty() ? "*" : selectClause.getAttributesAsSql()).append("\n");
        sb.append(" UNION SELECT ").append(selectClause.toString().isEmpty() ? "*" : selectClause);
        sb.append("\n");
        sb.append("FROM ").append(fromClause).append("\n");
        if (!whereClause.toString().isEmpty()) {
            sb.append("WHERE ").append(whereClause);
        }
        sb.append("\n").append(" into '").append(filename).append("' delimiters ';';");
        return sb.toString().trim();
    }

    public SqlClause getSelectClause() {
        return selectClause;
    }

    public void setSelectClause(SqlClause selectClause) {
        this.selectClause = selectClause;
    }

    public SqlClause getFromClause() {
        return fromClause;
    }

    public void setFromClause(SqlClause fromClause) {
        this.fromClause = fromClause;
    }

    public SqlClause getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(SqlClause whereClause) {
        this.whereClause = whereClause;
    }

    public HashMap<Extractor, FastBitQuery> getIndexerQueries() {
        return indexerQueries;
    }

    public void setIndexerQueries(HashMap<Extractor, FastBitQuery> indexerQueries) {
        this.indexerQueries = indexerQueries;
    }
    
    public void addIndexerQuery(Extractor ext, FastBitQuery query){
        this.indexerQueries.put(ext, query);
    }

    void addIndexerAttribute(String attributeName, Extractor ext) {
        this.indexerAttributes.put(attributeName, ext);
    }
    
    public Set<String> getIndexerAttributes(){
        return this.indexerAttributes.keySet();
    }

    public FastBitQuery getIndexerQuery(String indexerAttribute) {
        return this.indexerQueries.get(this.indexerAttributes.get(indexerAttribute));
    }

}
