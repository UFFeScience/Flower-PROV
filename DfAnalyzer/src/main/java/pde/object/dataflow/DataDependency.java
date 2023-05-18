package pde.object.dataflow;

import pde.object.DataflowObject;
import pde.object.ObjectType;

/**
 *
 * @author vitor
 */
public class DataDependency extends DataflowObject {

    private Dataset dataset;
    private Transformation previousTransformation;
    private Transformation nextTransformation;
    
    protected DataDependency(ObjectType type) {
        super(type);        
    }

    public DataDependency() {
        this(ObjectType.DEPENDENCY);
    }

    public DataDependency(Dataset ds, Transformation previousDt, Transformation nextDt) {
        this(ObjectType.DEPENDENCY);
        setDataflowTag(ds.getDataflowTag());
        this.dataset = ds;
        this.previousTransformation = previousDt;
        this.nextTransformation = nextDt;
        this.dataset.dependency = previousDt.getTag();
    }
    
    public Dataset getDataset(){
        return dataset;
    }
    
    public Transformation getPreviousTransformation(){
        return previousTransformation;
    }
    
    public Transformation getNextTransformation(){
        return nextTransformation;
    }

}
