package qp.dataflow;

import com.google.common.base.MoreObjects;

/**
 *
 * @author tperrotta
 */
public abstract class DataflowObject {

    private Integer id;
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("id", id)
                .toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
