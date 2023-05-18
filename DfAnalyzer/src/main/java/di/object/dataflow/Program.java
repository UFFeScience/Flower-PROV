package di.object.dataflow;

import di.enumeration.dataflow.DataflowType;
import di.dataflow.object.DataflowObject;

/**
 *
 * @author vitor
 */
public class Program extends DataflowObject {
    
    public String transformationTag = "";
    public String name = "";
    public String path = "";
    
    protected Program(DataflowType type) {
        super(type);
    }

    public Program() {
        this(DataflowType.PROGRAM);
    }

}
