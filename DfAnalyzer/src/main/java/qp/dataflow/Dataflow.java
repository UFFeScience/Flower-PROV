package qp.dataflow;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import qp.query.AttrMapping;
import qp.tracing.DatasetTrack;
import qp.tracing.TransformationTrack;

/**
 *
 * @author tperrotta
 */
public class Dataflow extends DataflowObject {

    private Integer version;
    private String tag;

    private final Set<Transformation> transformations;
    private final Set<Dataset> datasets;
    private final Set<Dependency> dependencies;

    private final Multimap<Transformation, Dataset> dtPrevDatasets;
    private final Multimap<Transformation, Dataset> dtNextDatasets;
    private final Multimap<Transformation, Transformation> dtPrevTransformations;
    private final Multimap<Transformation, Transformation> dtNextTransformations;

    private final Multimap<Dataset, Transformation> dsPrevTransformations;
    private final Multimap<Dataset, Transformation> dsNextTransformations;
    // getDatasetPreviousDatasets() -> Multimap<Dataset, Dataset>
    // getDatasetNextDatasets() -> Multimap<Dataset, Dataset>

    private final List<Transformation> lastTransformations;

    /* Algorithm 2 memoization. */
    private boolean transformationTracksComputed = false;
    private Collection<TransformationTrack> transformationTracks;

    /* Algorithm 3 memoization. */
    private boolean datasetTracksComputed = false;
    private Collection<DatasetTrack> datasetTracks;

    public Dataflow(Integer version, String tag) {
        this.version = version;
        this.tag = tag;

        this.transformations = new LinkedHashSet<>();
        this.datasets = new LinkedHashSet<>();
        this.dependencies = new LinkedHashSet<>();

        // ArrayListMultimap: values *can* be duplicated; HashMultimap: *cannot*
        this.dtPrevDatasets = HashMultimap.create();
        this.dtNextDatasets = HashMultimap.create();
        this.dtPrevTransformations = HashMultimap.create();
        this.dtNextTransformations = HashMultimap.create();

        this.dsPrevTransformations = HashMultimap.create();
        this.dsNextTransformations = HashMultimap.create();

        this.lastTransformations = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(MoreObjects.toStringHelper(this.getClass())
                .add("version", version)
                .add("tag", tag)
                .toString());
        sb.append("\n\n");

        sb.append("Transformations:\n");
        Joiner.on("\n").appendTo(sb, transformations);
        sb.append("\n\n");

        sb.append("Datasets:\n");
        Joiner.on("\n").appendTo(sb, datasets);
        sb.append("\n\n");

        sb.append("Dependencies:\n");
        Joiner.on("\n").appendTo(sb, dependencies);
        sb.append("\n\n");

        sb.append("Dt Previous datasets:\n");
        Joiner.on("\n").appendTo(sb, dtPrevDatasets.entries());
        sb.append("\n\n");

        sb.append("Dt Next datasets:\n");
        Joiner.on("\n").appendTo(sb, dtNextDatasets.entries());
        sb.append("\n\n");

        sb.append("Dt Previous transformations:\n");
        Joiner.on("\n").appendTo(sb, dtPrevTransformations.entries());
        sb.append("\n\n");

        sb.append("Dt Next transformations:\n");
        Joiner.on("\n").appendTo(sb, dtNextTransformations.entries());
        sb.append("\n\n");

        sb.append("Ds Previous transformations:\n");
        Joiner.on("\n").appendTo(sb, dsPrevTransformations.entries());
        sb.append("\n\n");

        sb.append("Ds Next transformations:\n");
        Joiner.on("\n").appendTo(sb, dsNextTransformations.entries());
        sb.append("\n\n");

        sb.append("Ds Previous datasets:\n");
        Joiner.on("\n").appendTo(sb, getDatasetPreviousDatasets().entries());
        sb.append("\n\n");

        sb.append("Ds Next datasets:\n");
        Joiner.on("\n").appendTo(sb, getDatasetNextDatasets().entries());
        sb.append("\n\n");

        sb.append("Last transformations:\n");
        Joiner.on("\n").appendTo(sb, lastTransformations);
        sb.append("\n\n");

        sb.append("Transformation Tracks:\n");
        Joiner.on("\n").appendTo(sb, getTransformationTracks());
        sb.append("\n\n");

        sb.append("Dataset Tracks:\n");
        Joiner.on("\n").appendTo(sb, getDatasetTracks());
        sb.append("\n\n");

        return sb.toString();
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void addTransformation(Transformation transformation) {
        this.transformations.add(transformation);
    }

    public final void addTransformations(Collection<Transformation> transformations) {
        this.transformations.addAll(transformations);
    }

    public boolean containsTransformation(String transformationTag) {
        return this.containsTransformation(new Transformation(transformationTag));
    }

    public boolean containsTransformation(Transformation transformation) {
        return this.transformations.contains(transformation);
    }

    public Optional<Transformation> getTransformation(Integer id) {
        return transformations.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public Optional<Transformation> getTransformation(String transformationTag) {
        return transformations.stream().filter(t -> t.getTag().equals(transformationTag)).findFirst();
    }

    public Collection<Transformation> getTransformations() {
        return transformations;
    }

    public int getTransformationsSize() {
        return transformations.size();
    }

    public void addDataset(Dataset dataset) {
        this.datasets.add(dataset);
    }

    public final void addDatasets(Collection<Dataset> datasets) {
        this.datasets.addAll(datasets);
    }

    public boolean containsDataset(String datasetTag) {
        return this.containsDataset(new Dataset(datasetTag));
    }

    public boolean containsDataset(Dataset dataset) {
        return this.datasets.contains(dataset);
    }

    public Optional<Dataset> getDataset(Integer id) {
        return datasets.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public Optional<Dataset> getDataset(String datasetTag) {
        return datasets.stream().filter(s -> s.getTag().equals(datasetTag)).findFirst();
    }

    public Collection<Dataset> getDatasets() {
        return datasets;
    }

    public int getDatasetsSize() {
        return datasets.size();
    }

    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);

        if (dependency.getPreviousTransformation() != null && dependency.getNextTransformation() != null) {
            this.dtPrevTransformations.put(dependency.getNextTransformation(), dependency.getPreviousTransformation());
            this.dtNextTransformations.put(dependency.getPreviousTransformation(), dependency.getNextTransformation());
        }

        if (dependency.getPreviousTransformation() != null) {
            this.dtNextDatasets.put(dependency.getPreviousTransformation(), dependency.getDataset());
            this.dsPrevTransformations.put(dependency.getDataset(), dependency.getPreviousTransformation());
        }

        if (dependency.getNextTransformation() != null) {
            this.dtPrevDatasets.put(dependency.getNextTransformation(), dependency.getDataset());
            this.dsNextTransformations.put(dependency.getDataset(), dependency.getNextTransformation());
        }

        if (dependency.getNextTransformation() == null) {
            this.lastTransformations.add(dependency.getPreviousTransformation());
        }
    }

    public final void addDependencies(Collection<Dependency> dependencies) {
        dependencies.forEach(this::addDependency);
    }

    public boolean containsDependency(Dependency dependency) {
        return this.dependencies.contains(dependency);
    }

    public boolean containsDependency(Dataset dataset) {
        return this.dependencies.stream().anyMatch(dependency -> dataset.equals(dependency.getDataset()));
    }

    public boolean containsDependency(String datasetTag) {
        return this.dependencies.stream().anyMatch(dependency -> datasetTag.equals(dependency.getDataset().getTag()));
    }

    public boolean containsDependency(Transformation prevDt, Transformation nextDt) {
        return this.dependencies.stream().anyMatch(d -> prevDt.equals(d.getPreviousTransformation()) && nextDt.equals(d.getNextTransformation()));
    }

    public boolean containsDependency(String prevDtTag, String nextDtTag) {
        return this.dependencies.stream().anyMatch(d -> prevDtTag.equals(d.getPreviousTransformation().getTag()) && nextDtTag.equals(d.getNextTransformation().getTag()));
    }

    public Optional<Dependency> getDependency(String datasetTag) {
        return dependencies.stream().filter(dsDep -> datasetTag.equals(dsDep.getDataset().getTag())).findFirst();
    }

    public Optional<Dependency> getDependency(Dataset dataset) {
        return dependencies.stream().filter(dsDep -> dsDep.getDataset().equals(dataset)).findFirst();
    }

    public Optional<Dependency> getDependency(String previousDtTag, String nextDtTag) {
        return dependencies.stream().filter(dsDep
                -> previousDtTag.equals(dsDep.getPreviousTransformation().getTag())
                && nextDtTag.equals(dsDep.getNextTransformation().getTag())
        ).findFirst();
    }

    public Optional<Dependency> getDependency(Transformation previousTransformation, Transformation nextTransformation) {
        return dependencies.stream().filter(dsDep
                -> dsDep.getPreviousTransformation().equals(previousTransformation)
                && dsDep.getNextTransformation().equals(nextTransformation)
        ).findFirst();
    }

    public Collection<Dependency> getDependencies() {
        return this.dependencies;
    }

    public int getDependenciesSize() {
        return dependencies.size();
    }

    public boolean hasManyOutputDatasets(String transformationTag) {
        return this.hasManyOutputDatasets(new Transformation(transformationTag));
    }

    public boolean hasManyOutputDatasets(Transformation transformation) {
        return dtNextDatasets.get(transformation).size() > 1;
    }

    public boolean hasManyInputDatasets(String transformationTag) {
        return this.hasManyInputDatasets(new Transformation(transformationTag));
    }

    public boolean hasManyInputDatasets(Transformation transformation) {
        return dtPrevDatasets.get(transformation).size() > 1;
    }

    public Collection<Dataset> getTransformationPreviousDatasets(Transformation transformation) {
        return dtPrevDatasets.get(transformation);
    }

    public Collection<Dataset> getTransformationPreviousDatasets(String transformationTag) {
        return this.getTransformationPreviousDatasets(new Transformation(transformationTag));
    }

    public Collection<Dataset> getTransformationNextDatasets(Transformation transformation) {
        return dtNextDatasets.get(transformation);
    }

    public Collection<Dataset> getTransformationNextDatasets(String transformationTag) {
        return this.getTransformationNextDatasets(new Transformation(transformationTag));
    }

    public Collection<Transformation> getTransformationPreviousTransformations(String transformationTag) {
        return this.getTransformationPreviousTransformations(new Transformation(transformationTag));
    }

    public Collection<Transformation> getTransformationPreviousTransformations(Transformation transformation) {
        return dtPrevTransformations.get(transformation);
    }

    public Collection<Transformation> getTransformationNextTransformations(Transformation transformation) {
        return dtNextTransformations.get(transformation);
    }

    public Collection<Transformation> getTransformationNextTransformations(String transformationTag) {
        return this.getTransformationNextTransformations(new Transformation(transformationTag));
    }

    public Collection<Transformation> getDatasetPreviousTransformations(Dataset dataset) {
        return dsPrevTransformations.get(dataset);
    }

    public Collection<Transformation> getDatasetPreviousTransformations(String datasetTag) {
        return this.getDatasetPreviousTransformations(new Dataset(datasetTag));
    }

    public Collection<Transformation> getDatasetNextTransformations(Dataset dataset) {
        return dsNextTransformations.get(dataset);
    }

    public Collection<Transformation> getDatasetNextTransformations(String datasetTag) {
        return this.getDatasetNextTransformations(new Dataset(datasetTag));
    }

    public Collection<Dataset> getDatasetPreviousDatasets(Dataset dataset) {
        Collection<Dataset> datasetPreviousDatasets = new ArrayList<>();

        Collection<Transformation> prevDts = getDatasetPreviousTransformations(dataset);
        prevDts.forEach(dt
                -> datasetPreviousDatasets.addAll(this.getTransformationPreviousDatasets(dt))
        );

        return datasetPreviousDatasets;
    }

    public Collection<Dataset> getDatasetPreviousDatasets(String datasetTag) {
        return this.getDatasetPreviousDatasets(new Dataset(datasetTag));
    }

    public Collection<Dataset> getDatasetNextDatasets(Dataset dataset) {
        Collection<Dataset> datasetNextDatasets = new ArrayList<>();

        Collection<Transformation> nextDts = getDatasetNextTransformations(dataset);
        nextDts.forEach(dt
                -> datasetNextDatasets.addAll(this.getTransformationNextDatasets(dt))
        );

        return datasetNextDatasets;
    }

    public Collection<Dataset> getDatasetNextDatasets(String datasetTag) {
        return this.getDatasetNextDatasets(new Dataset(datasetTag));
    }

    public Multimap<Dataset, Dataset> getDatasetPreviousDatasets() {
        Multimap<Dataset, Dataset> dsPrevDatasets = HashMultimap.create();

        for (Dataset dataset : datasets) {
            Collection<Dataset> prevDatasets = this.getDatasetPreviousDatasets(dataset);
            for (Dataset prevDataset : prevDatasets) {
                dsPrevDatasets.put(dataset, prevDataset);
            }
        }

        return dsPrevDatasets;
    }

    public Multimap<Dataset, Dataset> getDatasetNextDatasets() {
        Multimap<Dataset, Dataset> dsNextDatasets = HashMultimap.create();

        for (Dataset dataset : datasets) {
            Collection<Dataset> nextDatasets = this.getDatasetNextDatasets(dataset);
            for (Dataset nextDataset : nextDatasets) {
                dsNextDatasets.put(dataset, nextDataset);
            }
        }

        return dsNextDatasets;
    }

    public Multimap<Transformation, Dataset> getTransformationPreviousDatasets() {
        return this.dtPrevDatasets;
    }

    public Multimap<Transformation, Dataset> getTransformationNextDatasets() {
        return this.dtNextDatasets;
    }

    public Multimap<Transformation, Transformation> getTransformationPreviousTransformations() {
        return this.dtPrevTransformations;
    }

    public Multimap<Transformation, Transformation> getTransformationNextTransformations() {
        return this.dtNextTransformations;
    }

    public Multimap<Dataset, Transformation> getDatasetPreviousTransformations() {
        return this.dsPrevTransformations;
    }

    public Multimap<Dataset, Transformation> getDatasetNextTransformations() {
        return this.dsNextTransformations;
    }

    public boolean hasManyNextTransformations(Transformation dt) {
        return this.dtNextTransformations.get(dt).size() > 1;
    }

    public Optional<Transformation> getTransformation(Dataset prevDataset, Dataset nextDataset) {
        {
            Collection<Transformation> c = getDatasetPreviousTransformations(prevDataset);
            Collection<Transformation> d = getDatasetNextTransformations(nextDataset);
            for (Transformation dt : c) {
                if (d.contains(dt)) {
                    return Optional.of(dt);
                }
            }
        }
        {
            Collection<Transformation> c = getDatasetPreviousTransformations(nextDataset);
            Collection<Transformation> d = getDatasetNextTransformations(prevDataset);
            for (Transformation dt : c) {
                if (d.contains(dt)) {
                    return Optional.of(dt);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Algorithm 1.
     *
     * @return collection of the last transformations of this dataflow
     */
    public Collection<Transformation> getLastTransformations() {
        return lastTransformations;
    }

    private boolean anyTransformationHasManyInputDatasets(Collection<Transformation> transformations) {
        return transformations.stream().anyMatch(t -> (hasManyInputDatasets(t)));
    }

    private Collection<Transformation> getNotVisitedTransformations(Collection<Transformation> visitedTransformations, Collection<Transformation> transformations) {
        Collection<Transformation> notVisitedDts = new ArrayList<>();

        transformations.stream().filter(t -> (!visitedTransformations.contains(t))).forEachOrdered(t -> {
            notVisitedDts.add(t);
        });

        return notVisitedDts;
    }

    private TransformationTrack getTrack(Collection<TransformationTrack> tracks, Collection<Transformation> nextTransformations) {
        Transformation firstDt = nextTransformations.stream().findFirst().get();

        for (TransformationTrack track : tracks) {
            if (track.contains(firstDt)) {
                return track;
            }
        }

        return null;
    }

    private Collection<TransformationTrack> getTransformationTracksInternal() {
        Queue<Transformation> queue = new LinkedList<>();
        queue.addAll(lastTransformations);

        Collection<Transformation> visitedTransformations = new ArrayList<>();
        visitedTransformations.addAll(lastTransformations);

        Collection<TransformationTrack> transformationTracksInternal = new ArrayList<>();

        while (!queue.isEmpty()) {
            Transformation dt = queue.remove();
            Collection<Transformation> nextTransformations = Dataflow.this.getTransformationNextTransformations(dt);

            // Create a new track.
            if (lastTransformations.contains(dt)
                    || hasManyOutputDatasets(dt)
                    || hasManyNextTransformations(dt)
                    || anyTransformationHasManyInputDatasets(nextTransformations)) {
                TransformationTrack tt = new TransformationTrack();
                tt.add(dt);
                transformationTracksInternal.add(tt);
            } // Add to an existing track.
            else {
                TransformationTrack tt = getTrack(transformationTracksInternal, nextTransformations);
                tt.add(dt);
            }

            Collection<Transformation> prevDts = getTransformationPreviousTransformations(dt);

            Collection<Transformation> notVisitedDts = getNotVisitedTransformations(visitedTransformations, prevDts);
            queue.addAll(notVisitedDts);
            visitedTransformations.addAll(notVisitedDts);
        }

        return transformationTracksInternal;
    }

    /**
     * Algorithm 2
     *
     * @return collection of the transformation tracks of this dataflow
     */
    public Collection<TransformationTrack> getTransformationTracks() {
        if (!transformationTracksComputed) {
            transformationTracks = getTransformationTracksInternal();
            transformationTracksComputed = true;
        }

        return transformationTracks;
    }

    private DatasetTrack getDatasetTrack(TransformationTrack transformationTrack) {
        DatasetTrack datasetTrack = new DatasetTrack(transformationTrack);

        for (Transformation transformation : transformationTrack) {
            if (datasetTrack.isEmpty()) {
                Collection<Dataset> nextDatasets = Dataflow.this.getTransformationNextDatasets(transformation);
                datasetTrack.addDatasets(nextDatasets);
            }
            Collection<Dataset> previousDatasets = Dataflow.this.getTransformationPreviousDatasets(transformation);
            datasetTrack.addDatasets(previousDatasets);
        }

        return datasetTrack;
    }

    private Collection<DatasetTrack> getDatasetTracksInternal() {
        Collection<DatasetTrack> datasetTracksInternal = new ArrayList<>();
        Collection<TransformationTrack> transformationTracks = getTransformationTracks();

        transformationTracks.forEach((transformationTrack) -> {
            datasetTracksInternal.add(this.getDatasetTrack(transformationTrack));
        });

        return datasetTracksInternal;
    }

    /**
     * Algorithm 3
     *
     * @return collection of the dataset tracks of this dataflow
     */
    public Collection<DatasetTrack> getDatasetTracks() {
        if (!datasetTracksComputed) {
            datasetTracks = getDatasetTracksInternal();
            datasetTracksComputed = true;
        }

        return datasetTracks;
    }

    /**
     * Returns the Dataset in the specified user input, if present in the
     * Dataflow.
     *
     * @param projection the user Input, such as "s1" or "s1.attribute1" or
     * "s1.attribute1 >= 10" or "s1.attribute1 = s2.attribute2"
     * @return the Dataset, if present
     */
    public Optional<Dataset> projectionQueryDataset(String projection) {
        return this.getDataset(projection.split("\\.", 2)[0].trim());
    }

    public boolean projectionIsValidDatasetAttribute(String projection) {
        return projectionIsValidDatasetAttribute(projection, AttrMapping.Type.HYBRID);
    }

    public boolean projectionIsValidDatasetAttribute(String projection, AttrMapping.Type type) {
        Optional<Dataset> optionalDataset = this.projectionQueryDataset(projection);

        if (!optionalDataset.isPresent()) {
            return false;
        }

        Dataset dataset = optionalDataset.get();
        String attributeName = projection.split("\\.", 2)[1].trim().split(" ", 2)[0].trim();

        if (type == AttrMapping.Type.LOGICAL || type == AttrMapping.Type.HYBRID) {
            Optional<Attribute> optionalAttribute = dataset.getAttribute(attributeName);

            if (optionalAttribute.isPresent()) {
                return true;
            }
        }

        if (type == AttrMapping.Type.PHYSICAL || type == AttrMapping.Type.HYBRID) {
            // Check out _task_id
            String taskIdSuffix = "_task_id";
            attributeName = attributeName.trim();

            for (Transformation transformation : this.getDatasetPreviousTransformations(dataset)) {
                if (attributeName.equals(transformation.getTag() + taskIdSuffix)) {
                    return true;
                }
            }

            for (Transformation transformation : this.getDatasetNextTransformations(dataset)) {
                if (attributeName.equals(transformation.getTag() + taskIdSuffix)) {
                    return true;
                }
            }
        }

        return false;
    }

}

// TODO: map de string para dataset;
// TODO: map de string para transformation;
// TODO: map de string + string (dataset + dataset) para dependency;
