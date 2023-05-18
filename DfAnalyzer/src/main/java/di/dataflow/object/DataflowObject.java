package di.dataflow.object;

import di.object.task.Element;
import di.object.task.Task;
import di.object.dataflow.Dataflow;
import di.object.dataflow.Attribute;
import di.object.dataflow.Transformation;
import di.object.dataflow.Program;
import di.object.dataflow.Set;
import di.object.extraction.Extractor;
import di.enumeration.dataflow.DataflowType;
import di.object.task.dfFiles;


/**
 *
 * @author vitor
 */
public class DataflowObject {
    
    private final DataflowType type;
    public String dataflowTag = "";
    public Integer ID;
    
    public static DataflowObject newInstance(DataflowType objectType) {
        switch (objectType) {
            case DATAFLOW:
                return new Dataflow();
            case TRANSFORMATION:
                return new Transformation();
            case PROGRAM:
                return new Program();
            case SET:
                return new Set();
            case ATTRIBUTE:
                return new Attribute();
            case EXTRACTOR:
                return new Extractor();
            case TASK:
                return new Task();
            case FILES:
                return new dfFiles();
            case ELEMENT:
                return new Element();
        }

        return null;
    }
    
    public DataflowObject(DataflowType objectType) {
        this.type = objectType;
    }
    
    public String getStrType(){
        return type.toString();
    }
    
    public DataflowType getType(){
        return type;
    }
    
}
