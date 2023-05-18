package pde.object;

import pde.object.task.Task;
import pde.object.extraction.ExtractorCombination;
import pde.object.extraction.Extractor;
import pde.object.task.dfFiles;
import pde.object.task.Element;
import pde.object.task.Performance;
import pde.object.dataflow.Dataset;
import pde.object.dataflow.Program;
import pde.object.dataflow.Dataflow;
import pde.object.dataflow.Attribute;
import pde.object.dataflow.DataDependency;
import pde.object.dataflow.Transformation;
import pde.object.task.File;

/**
 *
 * @author vitor
 */
public class DataflowObject {

    private final ObjectType type;
    public String dataflowTag = "";

    public static DataflowObject newInstance(ObjectType objectType) {
        switch (objectType) {
            case DATAFLOW:
                return new Dataflow();
            case TRANSFORMATION:
                return new Transformation();
            case PROGRAM:
                return new Program();
            case DATASET:
                return new Dataset();
            case ATTRIBUTE:
                return new Attribute();
            case DEPENDENCY:
                return new DataDependency();
            case EXTRACTION:
                return new Extractor();
            case EXTRACTOR_COMBINATION:
                return new ExtractorCombination();
            case TASK:
                return new Task();
            case FILE:
                return new File();
            case FILES:
                return new dfFiles();
            case ELEMENT:
                return new Element();
            case PERFORMANCE:
                return new Performance();
        }

        return null;
    }

    protected DataflowObject(ObjectType objectType) {
        this.type = objectType;
    }

    protected String getStrObjectType() {
        return type.toString();
    }

    public ObjectType getObjectType() {
        return type;
    }

    public String getDataflowTag() {
        return dataflowTag;
    }

    public void setDataflowTag(String dataflowTag) {
        this.dataflowTag = dataflowTag;
    }

}
