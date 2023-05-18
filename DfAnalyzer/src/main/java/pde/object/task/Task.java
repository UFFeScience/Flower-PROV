package pde.object.task;

import pde.configuration.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.simple.JSONObject;
import pde.enumeration.TaskStatus;
import pde.ingestion.TaskIngestion;
import pde.json.JSONWriter;
import pde.object.DataflowObject;
import pde.object.ObjectType;

/**
 *
 * @author vitor
 */
public class Task extends DataflowObject {

    public String json = null;
    public String transformationTag = "";
    public String ID = "";
    public String subID;
    public String[] dependencyTransformationTags;
    public ArrayList<String[]> dependencyDtIDs = new ArrayList<>();
    public String resource = "";
    public String workspace = "";
    public TaskStatus status;
    public String output = "";
    public String error = "";
    public ArrayList<Performance> performances = new ArrayList<>();
    public ArrayList<File> files = new ArrayList<>();
    public ArrayList<Element> elements = new ArrayList<>();

    protected Task(ObjectType type) {
        super(type);
    }

    public Task() {
        this(ObjectType.TASK);
    }

    public String getTransformationTag() {
        return transformationTag;
    }

    public void setTransformationTag(String transformationTag) {
        this.transformationTag = transformationTag;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getSubID() {
        return subID;
    }

    public void setSubID(String subID) {
        this.subID = subID;
    }

    public String[] getDependencyTransformations() {
        return dependencyTransformationTags;
    }

    public void setDependencyTransformations(String[] dependencyTransformations) {
        this.dependencyTransformationTags = dependencyTransformations;
    }

    public ArrayList<String[]> getDependencyDtIDs() {
        return dependencyDtIDs;
    }

    public void setDependencyTaskIDs(ArrayList<String[]> dependencyDtIDs) {
        this.dependencyDtIDs = dependencyDtIDs;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getStatus() {
        return status.toString();
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ArrayList<Performance> getPerformances() {
        return performances;
    }

    public void setPerformances(ArrayList<Performance> performances) {
        this.performances = performances;
    }

    public void addPerformance(Performance perf) {
        this.performances.add(perf);
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }

    public ArrayList<Element> getElements() {
        return elements;
    }

    public void setElements(ArrayList<Element> elements) {
        this.elements = elements;
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    public void addElement(Element element) {
        this.elements.add(element);
    }

    public JSONObject writeJSON(Configuration config) {
        return JSONWriter.storeTask(this, config);
    }

    public void ingest(Configuration config) {
        TaskIngestion taskIngestion = new TaskIngestion(config, dataflowTag, transformationTag, ID, subID);
        taskIngestion.run();
    }
    
    public Task(String dataflowTag, String transformationTag,
            int ID, Integer subID, TaskStatus status) {
        this(ObjectType.TASK);
        setDataflowTag(dataflowTag);
        setTransformationTag(transformationTag);
        setID(String.valueOf(ID));
        setSubID(String.valueOf(subID));
        setStatus(status);
    }
    
    public Task(String dataflowTag, String transformationTag,
            int ID, TaskStatus status) {
        this(ObjectType.TASK);
        setDataflowTag(dataflowTag);
        setTransformationTag(transformationTag);
        setID(String.valueOf(ID));
        setStatus(status);
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void addDependency(String[] transformationTags, String[] dependencyTaskIDs) {
        this.dependencyTransformationTags = transformationTags;
        this.dependencyDtIDs.add(dependencyTaskIDs);
    }

    public String getTaskDependencies() {
        String dependencies = Arrays.toString(this.dependencyTransformationTags) + "\n";
        dependencies = dependencyDtIDs.stream().
                map((depID) -> Arrays.toString(depID) + "\n").
                reduce(dependencies, String::concat);
        return dependencies;
    }

    public void addElements(String datasetTag, ArrayList<String[]> newDataElements) {
        newDataElements.forEach((String[] values) -> {
            new Element(this, datasetTag, Arrays.asList(values));
        });
    }
}
