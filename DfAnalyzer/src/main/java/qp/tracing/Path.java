package qp.tracing;

import com.google.common.base.MoreObjects;
import qp.dataflow.Dataset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 *
 * @author tperrotta
 */
public class Path extends ArrayList<Dataset> {

    public Path() {

    }

    public Path(Collection<Dataset> datasets) {
        this.addAll(datasets);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .addValue(toArray())
                .toString();
    }

    public boolean contains(String datasetTag) {
        return this.contains(new Dataset(datasetTag));
    }

    public Optional<Dataset> get(String datasetTag) {
        return this.stream().filter(dataset -> datasetTag.equals(dataset.getTag())).findFirst();
    }
}
