package qp.dataflow;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 *
 * @author tperrotta
 */
public class Dependency extends DataflowObject implements Comparable<Dependency> {

    private Transformation previousTransformation;
    private Dataset dataset;
    private Transformation nextTransformation;

    public Dependency(Transformation previousTransformation, Dataset dataset, Transformation nextTransformation) {
        this.previousTransformation = previousTransformation;
        this.dataset = dataset;
        this.nextTransformation = nextTransformation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.previousTransformation, this.dataset, this.nextTransformation);
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
        final Dependency other = (Dependency) obj;
        if (!Objects.equals(this.previousTransformation, other.previousTransformation)) {
            return false;
        }
        if (!Objects.equals(this.dataset, other.dataset)) {
            return false;
        }
        if (!Objects.equals(this.nextTransformation, other.nextTransformation)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("dtPrev", previousTransformation)
                .add("ds", dataset)
                .add("dtNext", nextTransformation)
                .toString();
    }

    public Transformation getPreviousTransformation() {
        return previousTransformation;
    }

    public void setPreviousTransformation(Transformation previousTransformation) {
        this.previousTransformation = previousTransformation;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Transformation getNextTransformation() {
        return nextTransformation;
    }

    public void setNextTransformation(Transformation nextTransformation) {
        this.nextTransformation = nextTransformation;
    }

    @Override
    public int compareTo(Dependency o) {
        int datasetResult = this.dataset.compareTo(o.dataset);
        if (datasetResult == 0) {
            int previousTransformationResult = this.previousTransformation.compareTo(o.previousTransformation);
            if (previousTransformationResult == 0) {
                return this.nextTransformation.compareTo(o.nextTransformation);
            }
            return previousTransformationResult;
        }
        return datasetResult;
    }

}
