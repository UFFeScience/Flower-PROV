package qp.query;

import com.google.common.base.Joiner;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author tperrotta
 */
public class SqlClause {

    private final String separator;
    private final Set<String> elements;

    public SqlClause(String rawString) {
        this.separator = "";
        this.elements = new LinkedHashSet<>(Arrays.asList(rawString));
    }

    public SqlClause(Collection<String> elements, String separator) {
        this.elements = new LinkedHashSet<>(elements != null ? elements : Arrays.asList());
        this.separator = separator;
    }

    public Set<String> getElements() {
        return elements;
    }

    public void addElement(String element) {
        this.elements.add(element);
    }

    public void addElements(Collection<String> elements) {
        this.elements.addAll(elements);
    }

    @Override
    public String toString() {
        return Joiner.on(separator).join(elements);
    }
    
    public String getAttributesAsSql(){
        boolean first = true;
        String sql = "";
        for(String de : elements){
            if(first){
                first = false;
            }else{
                sql += separator;
            }
            sql += "'" + de.split("\\.")[1] + "'";
        }
        return sql;
    }

}
