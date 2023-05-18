package pde;

import pde.configuration.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.simple.JSONObject;
import pde.enumeration.ExtractionCartridge;
import pde.enumeration.ExtractionMethod;
import pde.enumeration.TaskStatus;
import pde.json.JSONWriter;
import pde.object.dataflow.Dataflow;
import pde.object.dataflow.Dataset;
import pde.object.dataflow.Program;
import pde.object.dataflow.Transformation;
import pde.object.extraction.Extractor;
import pde.object.extraction.ExtractorCombination;
import pde.object.task.Element;
import pde.object.task.File;
import pde.object.task.Performance;
import pde.object.task.Task;

/**
 *
 * @author vitor
 */
public class PDE {

    private Configuration configuration;

//    main
    public PDE(String directory) {
        configuration(directory);
    }

    private Configuration configuration(String directory) {
        this.configuration = new Configuration(directory);
        return this.configuration;
    }

    public void clear() {
        configuration.clear();
    }

//    prospective provenance
    public Dataflow dataflow(String tag) {
        return new Dataflow(tag);
    }

    public Program program(Dataflow dataflow, String name, String path) {
        return new Program(dataflow, name, path);
    }

    public Dataset dataset(Dataflow dataflow, String tag, String[] attributes, String[] types) {
        return new Dataset(dataflow, tag, attributes, types);
    }

    public Extractor extractor(Dataset dataset, String tag,
            ExtractionMethod method, ExtractionCartridge cartridge,
            String[] attNames, String[] attTypes) {
        return new Extractor(dataset, tag, method, cartridge, attNames, attTypes);
    }

    public ExtractorCombination extractor_combination(Dataset dataset,
            Extractor innerExtractor, Extractor outerExtractor,
            String[] keys, String[] keyTypes) {
        return new ExtractorCombination(dataset, outerExtractor, innerExtractor, keys, keyTypes);
    }

    public Transformation transformation(Dataflow dataflow, String tag, Dataset[] inputDatasets,
            Dataset[] outputDatasets, Program[] programs) {
        return new Transformation(dataflow, tag, inputDatasets, outputDatasets,
                programs);
    }

    public JSONObject ingest(Dataflow dataflow) {
        return dataflow.writeJSON(configuration);
    }

//    restful
    public Extractor extractor(Dataflow dataflow, String datasetTag, String tag,
            ExtractionMethod method, ExtractionCartridge cartridge,
            String[] attNames, String[] attTypes) {
        Dataset dataset = dataflow.getDataset(datasetTag);
        return new Extractor(dataset, tag, method, cartridge, attNames, attTypes);
    }

    public ExtractorCombination extractor_combination(Dataflow dataflow, String datasetTag,
            String innerExtractorTag, String outerExtractorTag,
            String[] keys, String[] keyTypes) {
        Dataset dataset = dataflow.getDataset(datasetTag);
        Extractor innerExtractor = dataset.getExtractor(innerExtractorTag);
        Extractor outerExtractor = dataset.getExtractor(outerExtractorTag);
        return new ExtractorCombination(dataset, outerExtractor, innerExtractor, keys, keyTypes);
    }

    public Transformation transformation(Dataflow dataflow, String tag, String[] inputDatasetTags,
            String[] outputDatasetTags, String[] programNames) {
        Dataset[] inputDatasets = new Dataset[inputDatasetTags.length];
        Dataset[] outputDatasets = new Dataset[outputDatasetTags.length];
        Program[] programs = null;

        for (int index = 0; index < inputDatasetTags.length; index++) {
            inputDatasets[index] = dataflow.getDataset(inputDatasetTags[index]);
        }

        for (int index = 0; index < outputDatasetTags.length; index++) {
            outputDatasets[index] = dataflow.getDataset(outputDatasetTags[index]);
        }

        if (programNames != null) {
            programs = new Program[programNames.length];
            for (int index = 0; index < programNames.length; index++) {
                programs[index] = dataflow.getProgram(programNames[index]);
            }
        }

        return new Transformation(dataflow, tag, inputDatasets, outputDatasets,
                programs);
    }

//    retrospective provenance
    public Task task(String dataflowTag, String transformationTag,
            int ID, Integer subID, TaskStatus status) {
        return new Task(dataflowTag, transformationTag, ID, subID, status);
    }

    public Task task(String dataflowTag, String transformationTag,
            int ID, int subID, TaskStatus status, String workspace, String resource) {
        Task task = new Task(dataflowTag, transformationTag, ID, subID, status);
        task.setWorkspace(workspace);
        task.setResource(resource);
        return task;
    }

    public Task task(String dataflowTag, String transformationTag,
            int ID, TaskStatus status) {
        return new Task(dataflowTag, transformationTag, ID, status);
    }

    public Task task(String dataflowTag, String transformationTag,
            int ID, TaskStatus status, String workspace, String resource) {
        Task task = new Task(dataflowTag, transformationTag, ID, status);
        task.setWorkspace(workspace);
        task.setResource(resource);
        return task;
    }

    public Task message(Task task, String outputMessage, String errorMessage) {
        task.setOutput(outputMessage);
        task.setError(errorMessage);
        return task;
    }

    public File file(Task task, String name, String path) {
        return new File(task, path, name);
    }

    public Performance performance(Task task, String startTime, String endTime,
            String method, String description, String invocation) {
        return new Performance(task, startTime, endTime,
                method, description, invocation);
    }

    public ArrayList<Element> collection(Task task, String datasetTag,
            ArrayList<String[]> elementValues) {
        task.addElements(datasetTag, elementValues);
        return task.getElements();
    }

    public Element element(Task task, String datasetTag, String[] values) {
        return new Element(task, datasetTag, Arrays.asList(values));
    }

    public String dependency(Task task,
            String[] transformationTags, String[] dependencyTaskIDs) {
        task.addDependency(transformationTags, dependencyTaskIDs);
        return task.getTaskDependencies();
    }

    public void workspace(Task task, String path) {
        task.setWorkspace(path);
    }

    public void resource(Task task, String resource) {
        task.setResource(resource);
    }

    public void updateDirectory(String directory) {
        configuration.directory = directory;
    }

    public Dataflow getDataflow(Dataflow dataflow) {
        return dataflow;
    }

    public JSONObject ingest(Task task) {
        return task.writeJSON(configuration);
    }

    public JSONObject ingestDataflowFromJSON(String dataflowTag, String jsonObject) {
        return JSONWriter.storeDataflow(dataflowTag, configuration, jsonObject);
    }

    public JSONObject ingestTask(String dfTag, String dtTag, String taskID, String subTaskID, TaskStatus status, String jsonObject) {
        return JSONWriter.storeTask(dfTag, dtTag, taskID, subTaskID, status, configuration, jsonObject);
    }

    public String getDirectory() {
        return configuration.getDirectory();
    }
}
