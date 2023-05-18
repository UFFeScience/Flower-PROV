package rest.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pde.PDE;
import pde.enumeration.ExtractionCartridge;
import pde.enumeration.ExtractionMethod;
import pde.enumeration.TaskStatus;
import pde.object.DataflowObject;
import pde.object.ObjectType;
import static pde.object.ObjectType.DATAFLOW;
import static pde.object.ObjectType.PROGRAM;
import pde.object.dataflow.Dataflow;
import pde.object.task.Task;
import utils.Utils;

/**
 *
 * @author vitor
 */
@Service
public class PDEHandler {

    @Autowired
    private PDE pde;
    
    public String getDataflowFileName(){
        return "dataflow.json";
    }
    
    public String getPath(){
        return pde.getDirectory(); 
    }

    public JSONObject runDataflowFunctions(String message) {
        Dataflow dataflow = null;
        for (String functionStr : message.split("\n")) {
            if (!functionStr.isEmpty()) {
                int startFunction = functionStr.indexOf("(");
                int endFunction = functionStr.lastIndexOf(")");

                ObjectType function = ObjectType.valueOf(
                        functionStr.substring(0, startFunction).toUpperCase().trim());
                String[] arguments = functionStr.substring(
                        startFunction + 1, endFunction).split(",");

                for (int index = 0; index < arguments.length; index++) {
                    arguments[index] = arguments[index].trim();
                }

                dataflow = (Dataflow) runDataflowFunction(dataflow, function, arguments);
            }
        }
        return pde.ingest(dataflow);
//        di.runDataflowRequest(pdg.getDataflow());
    }

    public JSONObject runTaskFunctions(String message) {
        Task task = null;
        for (String functionStr : message.split("\n")) {
            if (!functionStr.isEmpty()) {
                int startFunction = functionStr.indexOf("(");
                int endFunction = functionStr.lastIndexOf(")");

                if (startFunction > 0 && endFunction != -1) {
                    ObjectType function = ObjectType.valueOf(
                            functionStr.substring(0, startFunction).toUpperCase().trim());
                    String[] arguments = functionStr.substring(
                            startFunction + 1, endFunction).split(",");

                    for (int index = 0; index < arguments.length; index++) {
                        arguments[index] = arguments[index].trim();
                    }

                    task = runTaskFunction(task, function, arguments);
                }
            }
        }

        return pde.ingest(task);
    }

    private DataflowObject runDataflowFunction(Dataflow dataflow, ObjectType function, String[] args) {
        switch (function) {
            case DATAFLOW:
                dataflow = dataflow(args);
                break;
            case PROGRAM:
                program(dataflow, args);
                break;
            case DATASET:
                dataset(dataflow, args);
                break;
            case EXTRACTOR:
                extractor(dataflow, args);
                break;
            case EXTRACTOR_COMBINATION:
                extractorCombination(dataflow, args);
                break;
            case TRANSFORMATION:
                transformation(dataflow, args);
                break;
        }
        return dataflow;
    }

    private Task runTaskFunction(Task task, ObjectType function, String[] args) {
        switch (function) {
            case TASK:
                return task(args);
            case COLLECTION:
                return collection(task, args);
            case DEPENDENCY:
                return dependency(task, args);
            case FILE:
                return file(task, args);
        }

        return null;
    }

    private Dataflow dataflow(String[] arguments) {
        if (arguments.length == 1) {
            return pde.dataflow(arguments[0]);
        }
        return null;
    }

    void setDirectory(String directory) {
        pde.updateDirectory(directory);
    }

    private DataflowObject program(Dataflow dataflow, String[] args) {
        if (args.length == 2) {
            return pde.program(dataflow, args[0], args[1]);
        }
        return null;
    }

    private DataflowObject dataset(Dataflow dataflow, String[] args) {
        if (args.length == 3) {
            String[] attributes = null;
            String[] attributeTypes = null;
            if (!args[1].isEmpty() && !args[2].isEmpty()) {
                attributes = Utils.breakSets(args[1]);
                attributeTypes = Utils.breakSets(args[2]);
            }
            return pde.dataset(dataflow, args[0], attributes, attributeTypes);
        }
        return null;
    }

    private DataflowObject extractor(Dataflow dataflow, String[] args) {
        if (args.length == 6) {
            String[] attributes = null;
            String[] attributeTypes = null;
            if (!args[4].isEmpty() && !args[5].isEmpty()) {
                attributes = Utils.breakSets(args[4]);
                attributeTypes = Utils.breakSets(args[5]);
            }
            return pde.extractor(dataflow,
                    args[0],
                    args[1],
                    ExtractionMethod.valueOf(args[2].toUpperCase()),
                    ExtractionCartridge.valueOf(args[3].toUpperCase()),
                    attributes,
                    attributeTypes);
        }
        return null;
    }

    private DataflowObject extractorCombination(Dataflow dataflow, String[] args) {
        if (args.length == 5) {
            String[] attributes = null;
            String[] attributeTypes = null;
            if (!args[3].isEmpty() && !args[4].isEmpty()) {
                attributes = Utils.breakSets(args[3]);
                attributeTypes = Utils.breakSets(args[4]);
            }
            return pde.extractor_combination(dataflow,
                    args[0],
                    args[1],
                    args[2],
                    attributes,
                    attributeTypes);
        }
        return null;
    }

    private DataflowObject transformation(Dataflow dataflow, String[] args) {
        if (args.length == 4) {
            String[] inputDatasets = null;
            String[] outputDatasets = null;
            String[] programs = null;
            if (!args[1].isEmpty()) {
                inputDatasets = Utils.breakSets(args[1]);
            }
            if (!args[2].isEmpty()) {
                outputDatasets = Utils.breakSets(args[2]);
            }
            if (!args[3].isEmpty()) {
                programs = Utils.breakSets(args[3]);
            }
            return pde.transformation(dataflow,
                    args[0],
                    inputDatasets,
                    outputDatasets,
                    programs);
        }
        return null;
    }

    private Task task(String[] args) {
        if (args.length >= 5 && args.length <= 7) {
            //subID
            Integer subID = null;
            if (!args[3].isEmpty()) {
                subID = Integer.parseInt(args[3]);
            }

            Task task = pde.task(args[0],
                    args[1],
                    Integer.parseInt(args[2]),
                    subID,
                    TaskStatus.valueOf(args[4].toUpperCase()));
            if (args.length >= 6) {
                pde.workspace(task, args[5]);
            }
            if (args.length >= 7) { 
               pde.resource(task, args[6]);
            }
            return task;
        }
        return null;
    }

    private Task collection(Task task, String[] args) {
        if (args.length == 2) {
            pde.collection(task,
                    args[0],
                    Utils.breakDataCollection(args[1]));
            return task;
        }
        return null;
    }

    private Task dependency(Task task, String[] args) {
        if (args.length == 2) {
            String[] transformationTags = null;
            String[] dependencyTaskIDs = null;
            if (!args[0].isEmpty() && !args[1].isEmpty()) {
                transformationTags = Utils.breakSets(args[0]);
                dependencyTaskIDs = Utils.breakSets(args[1]);
            }
            pde.dependency(task, transformationTags, dependencyTaskIDs);
            return task;
        }
        return null;
    }

    private Task file(Task task, String[] args) {
        if (args.length == 2) {
            pde.file(task, args[0], args[1]);
            return task;
        }
        return null;
    }

    public void writeDataflow(String dataflowTag, String jsonObject) {
        pde.ingestDataflowFromJSON(dataflowTag, jsonObject);
    }

    public void writeTask(String dfTag, String dtTag, String taskID, String subTaskID, String statusStr, String jsonObject) {
        TaskStatus status = TaskStatus.valueOf(statusStr.toUpperCase());
        pde.ingestTask(dfTag, dtTag, taskID, subTaskID, status, jsonObject);
    }

    String getDataflowVisualization(String dataflowTag) {
        try {
            String filePath = new File(".").getCanonicalPath() + "/src/main/resources/static/visualization/visualization.html";
            if (dataflowTag.equals("clothing")) {
                String content = readFile(filePath, StandardCharsets.UTF_8);
                return content;
            }
        } catch (IOException ex) {
            Logger.getLogger(PDEHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
