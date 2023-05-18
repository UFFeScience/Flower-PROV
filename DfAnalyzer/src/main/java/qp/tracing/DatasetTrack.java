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
public class DatasetTrack extends ArrayList<DatasetLevel> {
    
    private final TransformationTrack transformationTrack;
    
    public DatasetTrack(TransformationTrack transformationTrack) {
        this.transformationTrack = transformationTrack;
    }
    
    public DatasetTrack(TransformationTrack transformationTrack, Collection<DatasetLevel> datasetLevels) {
        this.transformationTrack = transformationTrack;
        this.addAll(datasetLevels);
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .addValue(toArray())
                .add("transformationTrack", transformationTrack)
                .toString();
    }
    
    public TransformationTrack getTransformationTrack() {
        return transformationTrack;
    }
    
    public boolean containsDataset(String datasetTag) {
        return this.stream().anyMatch((datasetLevel) -> (datasetLevel.contains(new Dataset(datasetTag))));
    }
    
    public boolean containsDataset(Dataset dataset) {
        return this.stream().anyMatch((datasetLevel) -> (datasetLevel.contains(dataset)));
    }
    
    public Optional<Dataset> getDataset(String datasetTag) {
        for (DatasetLevel datasetLevel : this) {
            for (Dataset dataset : datasetLevel) {
                if (dataset.equals(new Dataset(datasetTag))) {
                    return Optional.of(dataset);
                }
            }
        }
        
        return Optional.empty();
    }
    
    public void addDatasets(Collection<Dataset> datasets) {
        this.add(new DatasetLevel(datasets));
    }
    
    public Collection<Dataset> getDatasets() {
        Collection<Dataset> datasets = new ArrayList<>();
        this.stream().forEach(dsl -> datasets.addAll(dsl));
        return datasets;
    }
}
