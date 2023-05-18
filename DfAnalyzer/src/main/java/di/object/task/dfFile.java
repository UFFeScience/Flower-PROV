package di.object.task;

import di.dataflow.object.DataflowObject;
import di.enumeration.dataflow.DataflowType;

/**
 *
 * @author vitor
 */
public class dfFile extends DataflowObject {
    
    public String transformationTag = "";
    public String taskID = "";
    public String taskSubID;
    public String name = "";
    public String path = "";
    
    protected dfFile(DataflowType type) {
        super(type);
    }

    public dfFile() {
        this(DataflowType.FILE);
    }

}
