package pde.object.task;

import java.util.Date;
import pde.object.DataflowObject;
import pde.object.ObjectType;

/**
 *
 * @author Débora, Vinícius
 */
public class Performance extends DataflowObject{

    public String taskID = null;
    public String subTaskID = null;
    public String transformationTag = "";    
    public String json = null;
    public String startTime = "";
    public String endTime = "";
    public String description = "";
    public String method = "";    
    public String invocation = null;
    
    protected Performance(ObjectType type) {
        super(type);
    }

    public Performance() {
        this(ObjectType.PERFORMANCE);
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getSubTaskID() {
        return subTaskID;
    }

    public void setSubTaskID(String subTaskID) {
        this.subTaskID = subTaskID;
    }

    public String getTransformationTag() {
        return transformationTag;
    }

    public void setTransformationTag(String transformationTag) {
        this.transformationTag = transformationTag;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getInvocation() {
        return invocation;
    }

    public void setInvocation(String invocation) {
        this.invocation = invocation;
    }
    
    public Performance(Task task, String startTime, String endTime, 
            String method, String description, String invocation){
        this(ObjectType.PERFORMANCE);
        setDataflowTag(task.getDataflowTag());
        setTransformationTag(task.getTransformationTag());
        setTaskID(task.getID());
        setSubTaskID(task.getSubID());
        setStartTime(startTime);
        setEndTime(endTime);
        setDescription(description);
        setMethod(method.toUpperCase());
        setInvocation(invocation);
        task.addPerformance(this);
    }
    
    public void setStartTime() {
        this.startTime = String.valueOf(new Date());
    }
    
    public void setEndTime() {
        this.endTime = String.valueOf(new Date());
    }
    
}
