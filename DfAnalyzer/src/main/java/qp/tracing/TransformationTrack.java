package qp.tracing;

import com.google.common.base.MoreObjects;
import qp.dataflow.Transformation;
import java.util.ArrayList;

/**
 *
 * @author tperrotta
 */
public class TransformationTrack extends ArrayList<Transformation> {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .addValue(toArray())
                .toString();
    }
}
