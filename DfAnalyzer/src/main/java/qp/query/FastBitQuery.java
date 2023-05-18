package qp.query;

/**
 *
 * @author tperrotta
 */
public class FastBitQuery {

    private SqlClause selectClause;
    private SqlClause fromClause;
    private SqlClause whereClause;

    public FastBitQuery() {

    }

    public FastBitQuery(SqlClause selectClause, SqlClause fromClause, SqlClause whereClause) {
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
        
//        ibis -d OptimizedFastBit_deduplication_b.precision\=2_e.equality/deduplication -q 
//                "select CUSTOMERID, COUNTRY, CONTINENT  where CONTINENT = 'Europe'" 
//        -output-with-header result.csv

        sb.append("ibis -d ").append(fromClause);
        sb.append(" -q \"SELECT ").append(selectClause.toString().isEmpty() ? "*" : selectClause).append(" ");
        if (!whereClause.toString().isEmpty()) {
            sb.append("WHERE ").append(whereClause);
        }

        sb.append("\" -output-with-header ").append(filename).append(";");

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

}
