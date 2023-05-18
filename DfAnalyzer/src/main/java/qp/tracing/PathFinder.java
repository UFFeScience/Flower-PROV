package qp.tracing;

import qp.dataflow.Dataflow;
import qp.dataflow.Dataset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 *
 * @author tperrotta
 */
public class PathFinder {

    private class PathFinderState {

        // State
        public final LinkedList<Dataset> visited;
        public final Collection<Path> paths;

        // Arguments
        public Dataset dsOrigin;
        public Dataset dsDestination;
        public Collection<Dataset> dsIncludes;
        public Collection<Dataset> dsExcludes;

        public PathFinderState() {
            this.visited = new LinkedList<>();
            this.paths = new ArrayList<>();

            this.dsOrigin = null;
            this.dsDestination = null;
            this.dsIncludes = new ArrayList<>();
            this.dsExcludes = new ArrayList<>();
        }

    }

    private final Dataflow dataflow;

    public PathFinder(Dataflow dataflow) {
        this.dataflow = dataflow;
    }

    public Dataflow getDataflow() {
        return this.dataflow;
    }

    public Collection<Path> findPaths(
            String dsOriginStr,
            String dsDestinationStr,
            Collection<String> dsIncludesStrs,
            Collection<String> dsExcludesStrs) {

        Dataset dsOrigin = dataflow.getDataset(dsOriginStr).get();

        Optional<Dataset> optionalDsDestination = dataflow.getDataset(dsDestinationStr);
        if (optionalDsDestination.isPresent()) {
            Dataset dsDestination = optionalDsDestination.get();
            Collection<Dataset> dsIncludes = new ArrayList<>();
            if (dsIncludesStrs != null) {
                dsIncludesStrs.forEach(dsStr -> dsIncludes.add(dataflow.getDataset(dsStr).get()));
            }

            Collection<Dataset> dsExcludes = new ArrayList<>();
            if (dsExcludesStrs != null) {
                dsExcludesStrs.forEach(dsStr -> dsExcludes.add(dataflow.getDataset(dsStr).get()));
            }

            return findPaths(dsOrigin, dsDestination, dsIncludes, dsExcludes);
        }
        return null;
    }

    public Collection<Path> findPaths(
            Dataset dsOrigin,
            Dataset dsDestination,
            Collection<Dataset> dsIncludes,
            Collection<Dataset> dsExcludes) {

        PathFinderState state = new PathFinderState();

        state.visited.add(dsOrigin);
        state.dsOrigin = dsOrigin;
        state.dsDestination = dsDestination;

        if (dsIncludes != null) {
            state.dsIncludes = dsIncludes;
        }

        if (dsExcludes != null) {
            state.dsExcludes = dsExcludes;
        }

        depthFirstSearch(state);

        return state.paths;
    }

    // Based on: https://stackoverflow.com/questions/58306/graph-algorithm-to-find-all-connections-between-two-arbitrary-vertices
    // Additional reference: https://stackoverflow.com/questions/9535819/find-all-paths-between-two-graph-nodes
    private void depthFirstSearch(PathFinderState state) {
        Collection<Dataset> nodes = dataflow.getDatasetNextDatasets(state.visited.getLast());

        // Examine adjacent nodes
        for (Dataset node : nodes) {
            if (state.visited.contains(node)) {
                continue;
            }
            if (node.equals(state.dsDestination)) {
                state.visited.add(node);

                Path path = new Path(state.visited);

                if (path.containsAll(state.dsIncludes) && !path.stream().anyMatch(ds -> state.dsExcludes.contains(ds))) {
                    state.paths.add(path);
                }

                state.visited.removeLast();
                break;
            }
        }

        for (Dataset node : nodes) {
            if (state.visited.contains(node) || node.equals(state.dsDestination)) {
                continue;
            }
            state.visited.addLast(node);
            depthFirstSearch(state);
            state.visited.removeLast();
        }
    }
}
