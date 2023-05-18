package qp.dataflow;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 *
 * @author tperrotta
 */
public class Transformation extends DataflowObject implements Comparable<Transformation> {

    private String tag;
    
    public Transformation() {
        
    }

    public Transformation(String tag) {
        this.tag = tag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tag);
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
        final Transformation other = (Transformation) obj;
        return Objects.equals(this.tag, other.tag);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("tag", tag)
                .toString();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public int compareTo(Transformation o) {
        return this.tag.compareTo(o.tag);
    }

}
