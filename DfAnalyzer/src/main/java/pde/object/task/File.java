package pde.object.task;

import pde.object.DataflowObject;
import pde.object.ObjectType;

/**
 *
 * @author vitor
 */
public class File extends DataflowObject {
    
    public String transformationTag = "";
    public String taskID = "";
    public String taskSubID;
    public String name = "";
    public String path = "";
    
    protected File(ObjectType type) {
        super(type);
    }

    public File() {
        this(ObjectType.FILE);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public File(Task task, String path, String name){
        this(ObjectType.FILE);
        setTaskID(task.getID());
        setTaskSubID(task.getSubID());
        setPath(path);
        setName(name);
        task.addFile(this);
    }

}
