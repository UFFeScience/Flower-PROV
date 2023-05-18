package pde.object.dataflow;

import java.util.ArrayList;
import java.util.Arrays;
import pde.object.DataflowObject;
import pde.object.ObjectType;

/**
 *
 * @author vitor
 */
public class Transformation extends DataflowObject {

    public String tag = "";
    public ArrayList<Program> programs = new ArrayList<>();
    public ArrayList<Dataset> inputDatasets = new ArrayList<>();
    public ArrayList<Dataset> outputDatasets = new ArrayList<>();

    protected Transformation(ObjectType type) {
        super(type);
    }

    public Transformation() {
        this(ObjectType.TRANSFORMATION);
    }

    public String getTag() {
        return tag;
    }

    private void setTag(String tag) {
        this.tag = tag;
    }

    public ArrayList<Program> getPrograms() {
        return programs;
    }

    protected void addProgram(Program program) {
        this.programs.add(program);
    }

    private void addPrograms(Program[] programs) {
        this.programs.addAll(Arrays.asList(programs));
    }

    private void addInputDatasets(Dataset[] datasets) {
        this.inputDatasets.addAll(Arrays.asList(datasets));
    }

    private void addOutputDatasets(Dataset[] datasets) {
        this.outputDatasets.addAll(Arrays.asList(datasets));
    }

    public Transformation(Dataflow dataflow, String tag,
            Dataset[] inputDatasets, Dataset[] outputDatasets, Program[] programs) {
        this(ObjectType.TRANSFORMATION);
        setDataflowTag(dataflow.getDataflowTag());
        setTag(tag.toLowerCase());
        addInputDatasets(inputDatasets);
        addOutputDatasets(outputDatasets);
        if (programs != null) {
            addPrograms(programs);
        }
        dataflow.addTransformation(this);
        /**
         * update data dependencies according to previous data transformations
         * and their consumed/produced datasets
         */
        dataflow.updateDataDependencies(this);
    }

    public ArrayList<Dataset> getInputDatasets() {
        return inputDatasets;
    }

    public ArrayList<Dataset> getOutputDatasets() {
        return outputDatasets;
    }

}
