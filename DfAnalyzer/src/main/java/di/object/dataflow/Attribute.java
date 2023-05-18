package di.object.dataflow;

import di.enumeration.dataflow.DataflowType;
import di.enumeration.dataflow.AttributeType;
import di.dataflow.object.DataflowObject;

/**
 *
 * @author vitor
 */
public class Attribute extends DataflowObject {
    
    public String transformationTag = "";
    public String setTag = "";
    public String name = "";
    public AttributeType type;
    public String extractorTag = null;
    
    protected Attribute(DataflowType type) {
        super(type);
    }

    public Attribute() {
        this(DataflowType.ATTRIBUTE);
    }

}
