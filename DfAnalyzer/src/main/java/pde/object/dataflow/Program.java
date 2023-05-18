package pde.object.dataflow;

import pde.object.DataflowObject;
import pde.object.ObjectType;

/**
 *
 * @author vitor
 */
public class Program extends DataflowObject {
    
    public String transformationTag = "";
    public String name = "";
    public String filePath = "";
    
    protected Program(ObjectType type) {
        super(type);
    }

    public Program() {
        this(ObjectType.PROGRAM);
    }

    public String getTransformationTag() {
        return transformationTag;
    }

    public void setTransformationTag(String transformationTag) {
        this.transformationTag = transformationTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String path) {
        this.filePath = path;
    }
    
    public Program(Dataflow dataflow, String name, String path) {
        this(ObjectType.PROGRAM);
        this.dataflowTag = dataflow.getDataflowTag();
        this.name = name;
        this.filePath = path;
        dataflow.addProgram(this);
    }
}
