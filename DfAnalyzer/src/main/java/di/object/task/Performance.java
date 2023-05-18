package di.object.task;

import di.dataflow.object.DataflowObject;
import di.enumeration.dataflow.DataflowType;

/**
 *
 * @author Débora, Vinícius
 */
public class Performance extends DataflowObject{

    public String taskID = null;
    public String taskSubID = null;
    public String transformationTag = "";    
    public String json = null;
    public String startTime = "";
    public String endTime = "";
    public String description = "";
    public String method = "";    
    public String invocation = null;
    public Integer ID = null;
    

    protected Performance(DataflowType type) {
        super(type);
    }

    public Performance() {
        this(DataflowType.PERFORMANCE);
    }
    
}
