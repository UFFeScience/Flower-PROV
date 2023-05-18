package pde.object.task;

import java.util.List;
import pde.object.DataflowObject;
import pde.object.ObjectType;

/**
 *
 * @author vitor
 */
public class Element extends DataflowObject {
    
    public String transformationTag = "";
    public String taskID = "";
    public String taskSubID;
    public String setTag = "";
    public List<String> values;
    
    protected Element(ObjectType type) {
        super(type);
    }

    public Element() {
        this(ObjectType.ELEMENT);
    }

    public String getTransformationTag() {
        return transformationTag;
    }

    public void setTransformationTag(String transformationTag) {
        this.transformationTag = transformationTag;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getTaskSubID() {
        return taskSubID;
    }

    public void setTaskSubID(String taskSubID) {
        this.taskSubID = taskSubID;
    }

    public String getSetTag() {
        return setTag;
    }

    public void setSetTag(String setTag) {
        this.setTag = setTag;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
    
    public Element(Task task, String datasetTag, List<String> values){
        this(ObjectType.ELEMENT);
        setDataflowTag(task.getDataflowTag());
        setTransformationTag(task.getTransformationTag());
        setTaskID(task.getID());
        setTaskSubID(task.getSubID());
        setSetTag(datasetTag);
        setValues(values);
        task.addElement(this);
    }

}
