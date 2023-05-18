package qp.samples;

import qp.dataflow.Attribute;
import java.util.ArrayList;
import java.util.List;
import qp.dataflow.Dataflow;
import qp.dataflow.Dataset;
import qp.dataflow.Dependency;
import qp.dataflow.Transformation;
import java.util.Arrays;
import pde.enumeration.AttributeType;

/**
 *
 * @author tperrotta
 */
public class DataflowSamples {

    public static Dataflow sampleDataflowMultiplePaths() {
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(new Dataset("s0"));
        datasets.add(new Dataset("s1"));
        datasets.add(new Dataset("s2"));
        datasets.add(new Dataset("s3"));
        datasets.add(new Dataset("s4"));
        datasets.add(new Dataset("s5"));
        datasets.add(new Dataset("s6"));
        datasets.add(new Dataset("s7", Arrays.asList(new Attribute("nemesis", AttributeType.NUMERIC))));
        datasets.add(new Dataset("s8"));
        datasets.add(new Dataset("s9", Arrays.asList(new Attribute("nemesis", AttributeType.NUMERIC))));

        List<Transformation> transformations = new ArrayList<>();
        transformations.add(new Transformation("t0"));
        transformations.add(new Transformation("t1"));
        transformations.add(new Transformation("t2"));
        transformations.add(new Transformation("t3"));
        transformations.add(new Transformation("t4"));
        transformations.add(new Transformation("t5"));
        transformations.add(new Transformation("t6"));
        transformations.add(new Transformation("t7"));
        transformations.add(new Transformation("t8"));
        transformations.add(new Transformation("t9"));
        transformations.add(new Transformation("t10"));
        transformations.add(new Transformation("t11"));

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(null, datasets.get(9), transformations.get(10)));
        dependencies.add(new Dependency(null, datasets.get(9), transformations.get(9)));
        dependencies.add(new Dependency(transformations.get(10), datasets.get(7), transformations.get(7)));
        dependencies.add(new Dependency(transformations.get(9), datasets.get(8), transformations.get(8)));

        dependencies.add(new Dependency(transformations.get(7), datasets.get(0), transformations.get(0)));
        dependencies.add(new Dependency(transformations.get(8), datasets.get(0), transformations.get(0)));
        dependencies.add(new Dependency(transformations.get(0), datasets.get(1), transformations.get(1)));
        dependencies.add(new Dependency(transformations.get(1), datasets.get(2), transformations.get(2)));
        dependencies.add(new Dependency(transformations.get(2), datasets.get(3), null));

        dependencies.add(new Dependency(transformations.get(8), datasets.get(0), transformations.get(11)));
        dependencies.add(new Dependency(transformations.get(7), datasets.get(0), transformations.get(11)));
        dependencies.add(new Dependency(transformations.get(11), datasets.get(5), transformations.get(5)));
        dependencies.add(new Dependency(transformations.get(5), datasets.get(6), transformations.get(6)));
        dependencies.add(new Dependency(transformations.get(6), datasets.get(3), null));

        dependencies.add(new Dependency(transformations.get(7), datasets.get(0), transformations.get(3)));
        dependencies.add(new Dependency(transformations.get(8), datasets.get(0), transformations.get(3)));
        dependencies.add(new Dependency(transformations.get(3), datasets.get(4), transformations.get(4)));
        dependencies.add(new Dependency(transformations.get(4), datasets.get(3), null));

        Dataflow df = new Dataflow(1, "dataflow");
        df.addDatasets(datasets);
        df.addTransformations(transformations);
        df.addDependencies(dependencies);

        return df;
    }

    public static Dataflow sampleDataflowMultiplePathsPatched() {
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(new Dataset("s0"));
        datasets.add(new Dataset("s1"));
        datasets.add(new Dataset("s2"));
        datasets.add(new Dataset("s3"));
        datasets.add(new Dataset("s4"));
        datasets.add(new Dataset("s5"));
        datasets.add(new Dataset("s6"));
        datasets.add(new Dataset("s7"));
        datasets.add(new Dataset("s8"));
        datasets.add(new Dataset("s9"));

        List<Transformation> transformations = new ArrayList<>();
        transformations.add(new Transformation("t0"));
        transformations.add(new Transformation("t1"));
        transformations.add(new Transformation("t2"));
        transformations.add(new Transformation("t3"));
        transformations.add(new Transformation("t4"));
        transformations.add(new Transformation("t5"));
        transformations.add(new Transformation("t6"));
        transformations.add(new Transformation("t7"));
        transformations.add(new Transformation("t8"));
        transformations.add(new Transformation("t9"));
        transformations.add(new Transformation("t10"));
        transformations.add(new Transformation("t11"));

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(null, datasets.get(9), transformations.get(10)));
        dependencies.add(new Dependency(null, datasets.get(9), transformations.get(9)));
        dependencies.add(new Dependency(transformations.get(10), datasets.get(7), null));
        dependencies.add(new Dependency(transformations.get(9), datasets.get(8), transformations.get(8)));

        dependencies.add(new Dependency(transformations.get(8), datasets.get(0), transformations.get(0)));
        dependencies.add(new Dependency(transformations.get(0), datasets.get(1), transformations.get(1)));
        dependencies.add(new Dependency(transformations.get(1), datasets.get(2), null));

        dependencies.add(new Dependency(transformations.get(8), datasets.get(0), transformations.get(11)));
        dependencies.add(new Dependency(transformations.get(11), datasets.get(5), transformations.get(5)));
        dependencies.add(new Dependency(transformations.get(5), datasets.get(6), transformations.get(6)));
        dependencies.add(new Dependency(transformations.get(6), datasets.get(3), null));

        dependencies.add(new Dependency(transformations.get(8), datasets.get(0), transformations.get(3)));
        dependencies.add(new Dependency(transformations.get(3), datasets.get(4), null));

        Dataflow df = new Dataflow(1, "dataflow");
        df.addDatasets(datasets);
        df.addTransformations(transformations);
        df.addDependencies(dependencies);

        return df;
    }

    public static Dataflow sampleDataflowExperiment() {
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(new Dataset("iinputmesh"));
        datasets.add(new Dataset("oinputmesh"));
        datasets.add(new Dataset("oamrconfig"));
        datasets.add(new Dataset("ocreateequationsystems"));
        datasets.add(new Dataset("otimestepcontrolconfig"));
        datasets.add(new Dataset("oioconfig"));
        datasets.add(new Dataset("ogetmaximumiterationstoflow"));
        datasets.add(new Dataset("ogetmaximumiterationstotransport"));
        datasets.add(new Dataset("oline0iextraction"));
        datasets.add(new Dataset("oline1iextraction"));
        datasets.add(new Dataset("oline2iextraction"));
        datasets.add(new Dataset("oline3iextraction"));
        datasets.add(new Dataset("oivisualization"));
        datasets.add(new Dataset("osolversimulationflow"));
        datasets.add(new Dataset("osolversimulationtransport"));
        datasets.add(new Dataset("ocomputesolutionchange"));
        datasets.add(new Dataset("ocomputetimestep"));
        datasets.add(new Dataset("omeshrefinement"));
        datasets.add(new Dataset("omeshwriter"));
        datasets.add(new Dataset("oline0extraction"));
        datasets.add(new Dataset("oline1extraction"));
        datasets.add(new Dataset("oline2extraction"));
        datasets.add(new Dataset("oline3extraction"));
        datasets.add(new Dataset("ovisualization"));
        datasets.add(new Dataset("omeshaggregator"));

        List<Transformation> transformations = new ArrayList<>();
        transformations.add(new Transformation("inputmesh"));
        transformations.add(new Transformation("amrconfig"));
        transformations.add(new Transformation("createequationsystems"));
        transformations.add(new Transformation("timestepcontrolconfig"));
        transformations.add(new Transformation("ioconfig"));
        transformations.add(new Transformation("getmaximumiterationstoflow"));
        transformations.add(new Transformation("getmaximumiterationstotransport"));
        transformations.add(new Transformation("iline0extraction"));
        transformations.add(new Transformation("iline1extraction"));
        transformations.add(new Transformation("iline2extraction"));
        transformations.add(new Transformation("iline3extraction"));
        transformations.add(new Transformation("ivisualization"));
        transformations.add(new Transformation("solversimulationflow"));
        transformations.add(new Transformation("solversimulationtransport"));
        transformations.add(new Transformation("computesolutionchange"));
        transformations.add(new Transformation("computetimestep"));
        transformations.add(new Transformation("meshrefinement"));
        transformations.add(new Transformation("meshwriter"));
        transformations.add(new Transformation("line0extraction"));
        transformations.add(new Transformation("line1extraction"));
        transformations.add(new Transformation("line2extraction"));
        transformations.add(new Transformation("line3extraction"));
        transformations.add(new Transformation("visualization"));
        transformations.add(new Transformation("meshaggregator"));

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(null, datasets.get(0), transformations.get(0)));
        dependencies.add(new Dependency(transformations.get(0), datasets.get(1), transformations.get(1)));
        dependencies.add(new Dependency(transformations.get(1), datasets.get(2), null));
        dependencies.add(new Dependency(transformations.get(0), datasets.get(1), transformations.get(2)));
        dependencies.add(new Dependency(transformations.get(2), datasets.get(3), transformations.get(5)));
        dependencies.add(new Dependency(transformations.get(0), datasets.get(1), transformations.get(3)));
        dependencies.add(new Dependency(transformations.get(3), datasets.get(4), null));
        dependencies.add(new Dependency(transformations.get(0), datasets.get(1), transformations.get(4)));
        dependencies.add(new Dependency(transformations.get(4), datasets.get(5), null));
        dependencies.add(new Dependency(transformations.get(5), datasets.get(6), transformations.get(6)));
        dependencies.add(new Dependency(transformations.get(6), datasets.get(7), transformations.get(7)));
        dependencies.add(new Dependency(transformations.get(7), datasets.get(8), null));
        dependencies.add(new Dependency(transformations.get(6), datasets.get(7), transformations.get(8)));
        dependencies.add(new Dependency(transformations.get(8), datasets.get(9), null));
        dependencies.add(new Dependency(transformations.get(6), datasets.get(7), transformations.get(9)));
        dependencies.add(new Dependency(transformations.get(9), datasets.get(10), null));
        dependencies.add(new Dependency(transformations.get(6), datasets.get(7), transformations.get(10)));
        dependencies.add(new Dependency(transformations.get(10), datasets.get(11), null));
        dependencies.add(new Dependency(transformations.get(6), datasets.get(7), transformations.get(11)));
        dependencies.add(new Dependency(transformations.get(11), datasets.get(12), null));
        dependencies.add(new Dependency(transformations.get(6), datasets.get(7), transformations.get(12)));
        dependencies.add(new Dependency(transformations.get(12), datasets.get(13), transformations.get(13)));
        dependencies.add(new Dependency(transformations.get(13), datasets.get(14), transformations.get(14)));
        dependencies.add(new Dependency(transformations.get(14), datasets.get(15), transformations.get(15)));
        dependencies.add(new Dependency(transformations.get(15), datasets.get(16), null));
        dependencies.add(new Dependency(transformations.get(13), datasets.get(14), transformations.get(16)));
        dependencies.add(new Dependency(transformations.get(16), datasets.get(17), null));
        dependencies.add(new Dependency(transformations.get(13), datasets.get(14), transformations.get(17)));
        dependencies.add(new Dependency(transformations.get(17), datasets.get(18), transformations.get(23)));
        dependencies.add(new Dependency(transformations.get(13), datasets.get(14), transformations.get(18)));
        dependencies.add(new Dependency(transformations.get(18), datasets.get(19), null));
        dependencies.add(new Dependency(transformations.get(13), datasets.get(14), transformations.get(19)));
        dependencies.add(new Dependency(transformations.get(19), datasets.get(20), null));
        dependencies.add(new Dependency(transformations.get(13), datasets.get(14), transformations.get(20)));
        dependencies.add(new Dependency(transformations.get(20), datasets.get(21), null));
        dependencies.add(new Dependency(transformations.get(13), datasets.get(14), transformations.get(22)));
        dependencies.add(new Dependency(transformations.get(21), datasets.get(22), null));
        dependencies.add(new Dependency(transformations.get(13), datasets.get(14), transformations.get(22)));
        dependencies.add(new Dependency(transformations.get(22), datasets.get(23), null));
        dependencies.add(new Dependency(transformations.get(23), datasets.get(24), null));

        Dataflow df = new Dataflow(1, "dataflow");
        df.addDatasets(datasets);
        df.addTransformations(transformations);
        df.addDependencies(dependencies);

        return df;
    }

    public static Dataflow sampleDataflowSimple() {
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(new Dataset("s0"));
        datasets.add(new Dataset("s1"));
        datasets.add(new Dataset("s2"));
        datasets.add(new Dataset("s3"));
        datasets.add(new Dataset("s4"));
        datasets.add(new Dataset("s5"));
        datasets.add(new Dataset("s6"));
        datasets.add(new Dataset("s7"));
        datasets.add(new Dataset("s8"));
        datasets.add(new Dataset("s9"));

        List<Transformation> transformations = new ArrayList<>();
        transformations.add(new Transformation("t0"));
        transformations.add(new Transformation("t1"));
        transformations.add(new Transformation("t2"));
        transformations.add(new Transformation("t3"));
        transformations.add(new Transformation("t4"));
        transformations.add(new Transformation("t5"));

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(null, datasets.get(0), transformations.get(0)));
        dependencies.add(new Dependency(transformations.get(0), datasets.get(1), transformations.get(1)));
        dependencies.add(new Dependency(transformations.get(1), datasets.get(2), transformations.get(4)));
        dependencies.add(new Dependency(transformations.get(4), datasets.get(5), transformations.get(5)));
        dependencies.add(new Dependency(transformations.get(5), datasets.get(6), null));
        dependencies.add(new Dependency(null, datasets.get(7), transformations.get(2)));
        dependencies.add(new Dependency(null, datasets.get(8), transformations.get(2)));
        dependencies.add(new Dependency(transformations.get(2), datasets.get(3), transformations.get(3)));
        dependencies.add(new Dependency(transformations.get(3), datasets.get(4), transformations.get(4)));
        dependencies.add(new Dependency(transformations.get(3), datasets.get(9), null));

        Dataflow df = new Dataflow(1, "dataflow");
        df.addDatasets(datasets);
        df.addTransformations(transformations);
        df.addDependencies(dependencies);

        return df;
    }
}
