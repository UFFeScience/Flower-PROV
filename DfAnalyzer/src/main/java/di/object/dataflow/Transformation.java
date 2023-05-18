package di.object.dataflow;

import di.enumeration.dataflow.DataflowType;
import java.util.HashMap;
import di.dataflow.object.DataflowObject;

/**
 *
 * @author vitor
 */
public class Transformation extends DataflowObject {

    public String tag = "";
    public HashMap<String, Set> inputSets = new HashMap<>();
    public HashMap<String, Set> outputSets = new HashMap<>();

    protected Transformation(DataflowType type) {
        super(type);
    }

    public Transformation() {
        this(DataflowType.TRANSFORMATION);
    }

}
