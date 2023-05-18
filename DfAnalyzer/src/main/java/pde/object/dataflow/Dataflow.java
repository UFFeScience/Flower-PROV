package pde.object.dataflow;

import pde.configuration.Configuration;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import pde.ingestion.DataflowIngestion;
import pde.json.JSONWriter;
import pde.object.DataflowObject;
import pde.object.ObjectType;

/**
 *
 * @author vitor
 */
public class Dataflow extends DataflowObject {

    public String json = null;
    public ArrayList<Transformation> transformations = new ArrayList<>();
    public ArrayList<Dataset> datasets = new ArrayList<>();

//    JAPI
    public ArrayList<Program> programs = new ArrayList<>();

    protected Dataflow(ObjectType type) {
        super(type);
    }

    public Dataflow() {
        this(ObjectType.DATAFLOW);
    }

    public Dataflow(String dfTag) {
        this(ObjectType.DATAFLOW);
        this.setDataflowTag(dfTag.toLowerCase());
    }

    public ArrayList<Transformation> getTransformations() {
        return transformations;
    }

    public void setTransformations(ArrayList<Transformation> transformations) {
        this.transformations = transformations;
    }

    public ArrayList<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(ArrayList<Dataset> sets) {
        this.datasets = sets;
    }

    public void addTransformation(Transformation obj) {
        transformations.add(obj);
    }

    public void addDataset(Dataset obj) {
        datasets.add(obj);
    }

    public JSONObject writeJSON(Configuration config) {
        return JSONWriter.storeDataflow(this, config);
    }

    public void getJSON(Configuration config) {
        JSONWriter.getDataflow(this, config);
    }

    public void ingest(Configuration config) {
        DataflowIngestion dataflowIngestion = new DataflowIngestion(config, this.dataflowTag);
        dataflowIngestion.run();
    }

    void addProgram(Program program) {
        this.programs.add(program);
    }

    void updateDataDependencies(Transformation transformation) {
        for (Transformation dt : transformations) {
            for (Dataset inputDs : dt.getInputDatasets()) {
                if (transformation.getOutputDatasets().contains(inputDs)) {
                    new DataDependency(inputDs, transformation, dt);
                }
            }
            for (Dataset outputDs : dt.getOutputDatasets()) {
                if (transformation.getInputDatasets().contains(outputDs)) {
                    new DataDependency(outputDs, dt, transformation);
                }
            }
        }
    }

    public Dataset getDataset(String datasetTag) {
        for (Dataset dataset : datasets) {
            if (dataset.getTag().equals(datasetTag)) {
                return dataset;
            }
        }
        return null;
    }

    public Program getProgram(String programName) {
        for (Program program : programs) {
            if (program.name.equals(programName)) {
                return program;
            }
        }
        return null;
    }
}
