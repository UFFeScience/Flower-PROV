package pde.json;

import pde.configuration.Configuration;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pde.enumeration.SetType;
import pde.enumeration.TaskStatus;
import pde.object.dataflow.Attribute;
import pde.object.dataflow.Dataflow;
import pde.object.DataflowObject;
import pde.object.dataflow.Dataset;
import pde.object.task.Element;
import pde.object.extraction.Extractor;
import pde.object.extraction.ExtractorCombination;
import static pde.object.ObjectType.ATTRIBUTE;
import static pde.object.ObjectType.DATAFLOW;
import static pde.object.ObjectType.ELEMENT;
import static pde.object.ObjectType.EXTRACTOR;
import static pde.object.ObjectType.EXTRACTOR_COMBINATION;
import static pde.object.ObjectType.FILE;
import static pde.object.ObjectType.FILES;
import static pde.object.ObjectType.PERFORMANCE;
import static pde.object.ObjectType.PROGRAM;
import static pde.object.ObjectType.TASK;
import static pde.object.ObjectType.TRANSFORMATION;
import pde.object.task.Performance;
import pde.object.dataflow.Program;
import pde.object.task.Task;
import pde.object.dataflow.Transformation;
import pde.object.task.dfFiles;
import utils.Utils;
import static utils.Utils.DATAFLOW_FILE_NAME;
import static utils.Utils.DIR_SEPARATOR;

/**
 *
 * @author vitor
 */
public class JSONWriter {

    public static void run(DataflowObject obj, String pgDir) throws FileNotFoundException {
        switch (obj.getObjectType()) {
            case DATAFLOW:
                storeDataflow((Dataflow) obj, pgDir);
                break;
            case TRANSFORMATION:
                storeDataTransformation((Transformation) obj, pgDir);
                break;
            case PROGRAM:
                storeProgram((Program) obj, pgDir);
                break;
            case DATASET:
                storeSet((Dataset) obj, pgDir);
                break;
            case ATTRIBUTE:
                storeAttribute((Attribute) obj, pgDir);
                break;
            case TASK:
                storeTask((Task) obj, pgDir);
                break;
            case PERFORMANCE:
                storePerformance((Performance) obj, pgDir);
                break;
            case FILES:
                storeFiles((dfFiles) obj, pgDir);
                break;
            case FILE:
                storeFile((pde.object.task.File) obj, pgDir);
                break;
            case ELEMENT:
                storeElement((Element) obj, pgDir);
                break;
            case EXTRACTOR:
                storeExtractor((Extractor) obj, pgDir);
                break;
            case EXTRACTOR_COMBINATION:
                storeExtractorCombination((ExtractorCombination) obj, pgDir);
                break;
        }
    }

    public static JSONObject storeDataflow(Dataflow df, Configuration config) {
//        dataflow directory
        String filepath = Utils.getDataflowDir(config.getDirectory(), df);
        java.io.File dir = new java.io.File(filepath);
        if (!dir.exists()) {
            dir.mkdir();
        }
//        dataflow
        JSONObject dfObj = new JSONObject();
        dfObj.put("tag", df.dataflowTag);
//        transformations
        for (Transformation dt : df.getTransformations()) {
            JSONObject dtObj = new JSONObject();
            dtObj.put("tag", dt.tag);

            JSONArray dts = (JSONArray) dfObj.get("transformations");
            if (dts == null) {
                dts = new JSONArray();
            }
            dts.add(dtObj);
            dfObj.put("transformations", dts);

            //        programs
            for (Program p : dt.getPrograms()) {
                JSONObject pObj = new JSONObject();
                pObj.put("name", p.getName());
                pObj.put("path", p.getFilePath());

                JSONArray pgs = (JSONArray) dtObj.get("programs");
                if (pgs == null) {
                    pgs = new JSONArray();
                }
                pgs.add(pObj);
                dtObj.put("programs", pgs);
            }

            //sets
            //input
            for (Dataset set : dt.getInputDatasets()) {
                JSONObject setObj = new JSONObject();
                setObj.put("tag", set.tag);
                setObj.put("type", SetType.INPUT.toString());
                if (!set.dependency.isEmpty()) {
                    setObj.put("dependency", set.dependency);
                }

                JSONArray sets = (JSONArray) dtObj.get("sets");
                if (sets == null) {
                    sets = new JSONArray();
                }
                sets.add(setObj);
                dtObj.put("sets", sets);

                if (set.dependency.isEmpty()) {
                    //attributes
                    for (Attribute att : set.getAttributes()) {
                        JSONObject attObj = new JSONObject();
                        attObj.put("name", att.name);
                        attObj.put("type", att.type.toString());
                        if (att.extractorTag != null) {
                            attObj.put("extractor", att.extractorTag);
                        }

                        JSONArray atts = (JSONArray) setObj.get("attributes");
                        if (atts == null) {
                            atts = new JSONArray();
                        }
                        atts.add(attObj);
                        setObj.put("attributes", atts);
                    }
                    //extractors
                    for (Extractor ext : set.getExtractors()) {
                        JSONObject ds = JSONReader.getSet(dfObj, dt.getTag(), set.getTag());

                        JSONObject extObj = new JSONObject();
                        extObj.put("tag", ext.tag);

                        if (ext.method != null && ext.cartridge != null) {
                            //JAPI -- todo: trocar notacao de cartridge e extension
                            extObj.put("cartridge", ext.method.toString());
                            extObj.put("extension", ext.cartridge.toString());
                        }

                        JSONArray exts = (JSONArray) ds.get("extractors");
                        if (exts == null) {
                            exts = new JSONArray();
                        }
                        exts.add(extObj);
                        ds.put("extractors", exts);
                    }
                    //extractor combinations
                    for (ExtractorCombination ec : set.getExtractorCombinations()) {
                        JSONObject ds = JSONReader.getSet(dfObj, dt.getTag(), set.getTag());

                        JSONObject ecObj = new JSONObject();
                        ecObj.put("outer", ec.outerExtractor);
                        ecObj.put("inner", ec.innerExtractor);
                        ecObj.put("keys", ec.keys);
                        ecObj.put("key_types", ec.keyTypes);

                        JSONArray combinations = (JSONArray) ds.get("extractor.combinations");
                        if (combinations == null) {
                            combinations = new JSONArray();
                        }
                        combinations.add(ecObj);
                        ds.put("extractor.combinations", combinations);
                    }
                }
            }
            //output
            for (Dataset set : dt.getOutputDatasets()) {
                JSONObject setObj = new JSONObject();
                setObj.put("tag", set.tag);
                setObj.put("type", SetType.OUTPUT.toString());

                JSONArray sets = (JSONArray) dtObj.get("sets");
                if (sets == null) {
                    sets = new JSONArray();
                }
                sets.add(setObj);
                dtObj.put("sets", sets);
                //attributes
                for (Attribute att : set.getAttributes()) {
                    JSONObject attObj = new JSONObject();
                    attObj.put("name", att.name);
                    attObj.put("type", att.type.toString());
                    if (att.extractorTag != null) {
                        attObj.put("extractor", att.extractorTag);
                    }

                    JSONArray atts = (JSONArray) setObj.get("attributes");
                    if (atts == null) {
                        atts = new JSONArray();
                    }
                    atts.add(attObj);
                    setObj.put("attributes", atts);
                }
                //extractors
                for (Extractor ext : set.getExtractors()) {
                    JSONObject ds = JSONReader.getSet(dfObj, dt.getTag(), set.getTag());

                    JSONObject extObj = new JSONObject();
                    extObj.put("tag", ext.tag);

                    if (ext.method != null && ext.cartridge != null) {
                        //JAPI -- todo: trocar notacao de cartridge e extension
                        extObj.put("cartridge", ext.method.toString());
                        extObj.put("extension", ext.cartridge.toString());
                    }

                    JSONArray exts = (JSONArray) ds.get("extractors");
                    if (exts == null) {
                        exts = new JSONArray();
                    }
                    exts.add(extObj);
                    ds.put("extractors", exts);
                }
                //extractor combinations
                for (ExtractorCombination ec : set.getExtractorCombinations()) {
                    JSONObject ds = JSONReader.getSet(dfObj, dt.getTag(), set.getTag());

                    JSONObject ecObj = new JSONObject();
                    ecObj.put("outer", ec.outerExtractor);
                    ecObj.put("inner", ec.innerExtractor);
                    ecObj.put("keys", ec.keys);
                    ecObj.put("key_types", ec.keyTypes);

                    JSONArray combinations = (JSONArray) ds.get("extractor.combinations");
                    if (combinations == null) {
                        combinations = new JSONArray();
                    }
                    combinations.add(ecObj);
                    ds.put("extractor.combinations", combinations);
                }
            }
        }

//        writeJSON
        filepath = Utils.getDataflowDir(config.getDirectory(), df) + DIR_SEPARATOR + "STORED_" + DATAFLOW_FILE_NAME;
        writeJSONFile(dfObj, filepath);
//        String message = "Dataflow JSON file was written in the following path:\n" + filepath;
//        System.out.println(message);
        return dfObj;
    }

    public static JSONObject storeTask(Task task, Configuration config) {
        JSONObject taskObj = new JSONObject();
        taskObj.put("dataflow", task.dataflowTag);
        taskObj.put("transformation", task.transformationTag);
        taskObj.put("id", task.ID);
        taskObj.put("subid", task.subID);

        JSONObject globalDeps = new JSONObject();

        JSONArray deps = new JSONArray();
        if (task.dependencyTransformationTags != null) {
            for (int i = 0; i < task.dependencyTransformationTags.length; i++) {
                JSONObject newObj = new JSONObject();
                newObj.put("tag", task.dependencyTransformationTags[i]);
                deps.add(newObj);
            }
            globalDeps.put("tags", deps);
        }
        taskObj.put("dependency", globalDeps);

        deps = new JSONArray();
        if (!task.dependencyDtIDs.isEmpty()) {
            for (String[] depDtID : task.dependencyDtIDs) {
                if (depDtID != null && depDtID.length > 0) {
                    String idStr = "";
                    for (String id : depDtID) {
                        if (!idStr.isEmpty()) {
                            idStr += ",";
                        }
                        idStr += id;
                    }
                    JSONObject nobj = new JSONObject();
                    nobj.put("id", idStr);
                    deps.add(nobj);
                }
            }
            globalDeps.put("ids", deps);
        }
        taskObj.put("dependency", globalDeps);

        taskObj.put("resource", task.resource);
        taskObj.put("workspace", task.workspace);
        taskObj.put("status", task.status.toString());
        if (!task.output.equals("")) {
            try {
                Scanner sc = new Scanner(new FileReader(task.output));
                StringBuilder sb = new StringBuilder();
                while (sc.hasNext()) {
                    sb.append(sc.next());
                }
                String content = sb.toString();
                taskObj.put("output", content);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!task.error.equals("")) {
            try {
                Scanner sc = new Scanner(new FileReader(task.error));
                StringBuilder sb = new StringBuilder();
                while (sc.hasNext()) {
                    sb.append(sc.next());
                }
                String content = sb.toString();
                taskObj.put("error", content);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!task.getFiles().isEmpty()) {
            JSONArray files = new JSONArray();
            for (pde.object.task.File file : task.getFiles()) {
                JSONObject fileObj = new JSONObject();
                fileObj.put("name", file.name);
                fileObj.put("path", file.path);
                files.add(fileObj);
            }
            taskObj.put("files", files);
        }

        JSONArray sets = new JSONArray();
        for (Element element : task.getElements()) {
            JSONObject setObj = new JSONObject();
            setObj.put("tag", element.setTag);

            JSONArray elements = (JSONArray) setObj.get("elements");
            if (elements == null) {
                elements = new JSONArray();
            }
            elements.addAll(element.values);
            setObj.put("elements", elements);
            sets.add(setObj);
        }
        taskObj.put("sets", sets);

        if (!task.getPerformances().isEmpty()) {
            JSONArray performances = new JSONArray();
            for (Performance perf : task.getPerformances()) {
                JSONObject perfObj = new JSONObject();

                if (perf.invocation != null) {
                    try {
                        Scanner sc = new Scanner(new FileReader(perf.invocation));
                        StringBuilder sb = new StringBuilder();
                        while (sc.hasNext()) {
                            sb.append(sc.next());
                        }
                        String content = sb.toString();
                        perfObj.put("invocation", content);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                perfObj.put("method", perf.method);
                perfObj.put("description", perf.description);
                if (!perf.startTime.isEmpty()) {
                    perfObj.put("startTime", perf.startTime);
                }
                if (!perf.endTime.isEmpty()) {
                    perfObj.put("endTime", perf.endTime);
                }
                performances.add(perfObj);
            }
            taskObj.put("performances", performances);
        }

        String filePath = "";
        switch (task.getStatus().toUpperCase()) {
            case "RUNNING":
                filePath = Utils.getRunningTaskFilePath(config.getDirectory(), task.dataflowTag, task.transformationTag, task.ID, task.subID);
                break;
            case "FINISHED":
                filePath = Utils.getFinishedTaskFilePath(config.getDirectory(), task.dataflowTag, task.transformationTag, task.ID, task.subID);
//                String fileRunningTaskPath = Utils.getRunningTaskFilePath(config.getDirectory(), task.dataflowTag, task.transformationTag, task.ID, task.subID);
//                if (fileRunningTaskPath != "") {
//                    new java.io.File(fileRunningTaskPath).delete();
//                }
                break;
        }

//        writeJSONFile(taskObj, filePath);
//        System.out.println("Task JSON file was written in the following path:");
//        System.out.println("    " + filePath);
        return taskObj;
    }

    public static JSONObject storeDataflow(Dataflow df, String pgDir) {
        createDataflowDirectory(df, pgDir);
        if (df.json == null) {
            JSONObject obj = new JSONObject();
            obj.put("tag", df.dataflowTag);
            writeJSONFile(obj, Utils.getDataflowDir(pgDir, df) + DIR_SEPARATOR + DATAFLOW_FILE_NAME);
            return obj;
        } else if (df.json != null && !df.dataflowTag.isEmpty()) {
            Utils.copyFile(df.json, Utils.getDataflowDir(pgDir, df) + DATAFLOW_FILE_NAME);
        } else {
            System.out.println("Problem to gather dataflow!");
        }
        return null;
    }

    public static JSONObject storeDataflow(String dataflowTag, Configuration configuration, String jsonStr) {
        //create directories
        java.io.File dir = new java.io.File(Utils.getDataflowDir(configuration.getDirectory(), dataflowTag));
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            System.err.println("Df-Gen: This dataflow already exists!");
            System.exit(1);
        }
        //write json
        if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(jsonStr);
                writeJSONFile(json, Utils.getDataflowDir(configuration.getDirectory(), dataflowTag) + DIR_SEPARATOR + "STORED_" + DATAFLOW_FILE_NAME);
                return json;
            } catch (ParseException ex) {
                Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static JSONObject storeTask(String dfTag, String dtTag, String taskID, String subTaskID, TaskStatus status, Configuration configuration, String jsonStr) {
        //filename
        String filePath = "";
        switch (status) {
            case RUNNING:
                filePath = Utils.getRunningTaskFilePath(configuration.getDirectory(), dfTag, dtTag, taskID, subTaskID);
                break;
            case FINISHED:
                filePath = Utils.getFinishedTaskFilePath(configuration.getDirectory(), dfTag, dtTag, taskID, subTaskID);
                break;
        }
        //write json
        if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(jsonStr);
                writeJSONFile(json, filePath);
                return json;
            } catch (ParseException ex) {
                Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private static void createDataflowDirectory(Dataflow df, String pgDir) {
        java.io.File dir = new java.io.File(Utils.getDataflowDir(pgDir, df));
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            System.err.println("Df-Gen: This dataflow already exists!");
            System.exit(1);
        }
    }

    private static void writeJSONFile(JSONObject json, String filePath) {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(json.toJSONString());
        } catch (IOException ex) {
            Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void storeDataTransformation(Transformation dt, String pgDir) {
        JSONObject df = JSONReader.readDataflow(Utils.getDataflowDir(pgDir, dt) + DATAFLOW_FILE_NAME);

        JSONObject obj = new JSONObject();
        obj.put("tag", dt.tag);

        JSONArray dts = (JSONArray) df.get("transformations");
        if (dts == null) {
            dts = new JSONArray();
        }
        dts.add(obj);
        df.put("transformations", dts);

        writeJSONFile(df, Utils.getDataflowDir(pgDir, dt) + DIR_SEPARATOR + Utils.DATAFLOW_FILE_NAME);
    }

    public static void storeProgram(Program program, String pgDir) {
        JSONObject df = JSONReader.readDataflow(Utils.getDataflowDir(pgDir, program) + DATAFLOW_FILE_NAME);
        JSONObject dt = JSONReader.getDataTransformation(df, program.transformationTag);

        JSONObject obj = new JSONObject();
        obj.put("name", program.name);
        obj.put("path", program.filePath);

        JSONArray pgs = (JSONArray) dt.get("programs");
        if (pgs == null) {
            pgs = new JSONArray();
        }
        pgs.add(obj);
        dt.put("programs", pgs);

        writeJSONFile(df, Utils.getDataflowDir(pgDir, program) + DIR_SEPARATOR + Utils.DATAFLOW_FILE_NAME);
    }

    private static void storeSet(Dataset set, String pgDir) {
        JSONObject df = JSONReader.readDataflow(Utils.getDataflowDir(pgDir, set) + DATAFLOW_FILE_NAME);
        JSONObject dt = JSONReader.getDataTransformation(df, set.transformationTag);

        JSONObject obj = new JSONObject();
        obj.put("tag", set.tag);
        obj.put("type", set.type.toString());
        if (!set.dependency.isEmpty()) {
            obj.put("dependency", set.dependency);
        }

        JSONArray sets = (JSONArray) dt.get("sets");
        if (sets == null) {
            sets = new JSONArray();
        }
        sets.add(obj);
        dt.put("sets", sets);

        writeJSONFile(df, Utils.getDataflowDir(pgDir, set) + DIR_SEPARATOR + Utils.DATAFLOW_FILE_NAME);
    }

    private static void storeAttribute(Attribute att, String pgDir) {
        JSONObject df = JSONReader.readDataflow(Utils.getDataflowDir(pgDir, att) + DATAFLOW_FILE_NAME);
        JSONObject ds = JSONReader.getSet(df, att.transformationTag, att.setTag);

        JSONObject obj = new JSONObject();
        obj.put("name", att.name);
        obj.put("type", att.type.toString());
        if (att.extractorTag != null) {
            obj.put("extractor", att.extractorTag);
        }

        JSONArray atts = (JSONArray) ds.get("attributes");
        if (atts == null) {
            atts = new JSONArray();
        }
        atts.add(obj);
        ds.put("attributes", atts);

        writeJSONFile(df, Utils.getDataflowDir(pgDir, att) + DIR_SEPARATOR + Utils.DATAFLOW_FILE_NAME);
    }

    private static void storeTask(Task t, String pgDir) throws FileNotFoundException {
        if (t.json == null) {
            JSONObject obj = JSONReader.getTask(pgDir, t.dataflowTag, t.transformationTag, t.ID, t.subID);
            if (obj == null) {
                obj = new JSONObject();
            }
            obj.put("dataflow", t.dataflowTag);
            obj.put("transformation", t.transformationTag);
            obj.put("id", t.ID);
            obj.put("subid", t.subID);

            JSONObject globalDeps = new JSONObject();

            JSONArray deps = new JSONArray();
            if (t.dependencyTransformationTags != null) {
                for (int i = 0; i < t.dependencyTransformationTags.length; i++) {
                    JSONObject newObj = new JSONObject();
                    newObj.put("tag", t.dependencyTransformationTags[i]);
                    deps.add(newObj);
                }
                globalDeps.put("tags", deps);
            }
            obj.put("dependency", globalDeps);

            deps = new JSONArray();
            if (!t.dependencyDtIDs.isEmpty()) {
                for (String[] depDtID : t.dependencyDtIDs) {
                    if (depDtID != null && depDtID.length > 0) {
                        for (String id : depDtID) {
                            JSONObject nobj = new JSONObject();
                            nobj.put("id", id);
                            deps.add(nobj);
                        }
                    }
                }
                globalDeps.put("ids", deps);
            }
            obj.put("dependency", globalDeps);

            obj.put("resource", t.resource);
            obj.put("workspace", t.workspace);
            obj.put("status", t.status);
            if (!t.output.equals("")) {
                Scanner sc = new Scanner(new FileReader(t.output));
                StringBuilder sb = new StringBuilder();
                while (sc.hasNext()) {
                    sb.append(sc.next());
                }
                String content = sb.toString();
                obj.put("output", content);
            }

            if (!t.error.equals("")) {
                Scanner sc = new Scanner(new FileReader(t.error));
                StringBuilder sb = new StringBuilder();
                while (sc.hasNext()) {
                    sb.append(sc.next());
                }
                String content = sb.toString();
                obj.put("error", content);
            }

            String filePath = "";
            switch (t.getStatus().toUpperCase()) {
                case "RUNNING":
                    filePath = Utils.getRunningTaskFilePath(pgDir, t.dataflowTag, t.transformationTag, t.ID, t.subID);
                    break;
                case "FINISHED":
                    filePath = Utils.getFinishedTaskFilePath(pgDir, t.dataflowTag, t.transformationTag, t.ID, t.subID);
                    String fileRunningTaskPath = Utils.getRunningTaskFilePath(pgDir, t.dataflowTag, t.transformationTag, t.ID, t.subID);
                    if (fileRunningTaskPath != "") {
                        new java.io.File(fileRunningTaskPath).delete();
                    }
            }

            writeJSONFile(obj, filePath);
        } else if (t.json != null && !t.dataflowTag.isEmpty()
                && !t.transformationTag.isEmpty() && !t.ID.isEmpty()) {
            String filePath = "";
            switch (t.getStatus().toUpperCase()) {
                case "RUNNING":
                    filePath = Utils.getRunningTaskFilePath(pgDir, t.dataflowTag, t.transformationTag, t.ID, t.subID);
                    break;
                case "FINISHED":
                    filePath = Utils.getFinishedTaskFilePath(pgDir, t.dataflowTag, t.transformationTag, t.ID, t.subID);
                    break;
            }
            Utils.copyFile(t.json, filePath);
        } else {
            System.out.println("Problem to gather task!");
        }
    }

    private static void storePerformance(Performance p, String pgDir) throws FileNotFoundException {
        JSONObject task = JSONReader.getTask(pgDir, p.dataflowTag, p.transformationTag, p.taskID, p.subTaskID);

        if (task != null) {
            if (task.get("performances") == null) {
                //se não tiver nenhum performance prévio em task
                JSONArray performances = new JSONArray();
                JSONObject performance = new JSONObject();

                if (p.invocation != null) {
                    Scanner sc = new Scanner(new FileReader(p.invocation));
                    StringBuilder sb = new StringBuilder();
                    while (sc.hasNext()) {
                        sb.append(sc.next());
                    }
                    String content = sb.toString();
                    performance.put("invocation", content);
                }
                performance.put("method", p.method);
                performance.put("description", p.description);
                if (!p.startTime.isEmpty()) {
                    performance.put("startTime", p.startTime);
                }
                if (!p.endTime.isEmpty()) {
                    performance.put("endTime", p.endTime);
                }
                performances.add(performance);
                task.put("performances", performances);
            } else if (task.get("performances") != null) {
                //deverá atualizar os dados de uma performance previamente registrada
                JSONArray performances = (JSONArray) task.get("performances");

                boolean newPerf = true;
                for (Object performance : performances) {
                    JSONObject perf = (JSONObject) performance;
                    if (perf.get("method").equals(p.method) && perf.get("endTime") == null) {
                        perf.put("endTime", p.endTime);
                        newPerf = false;
                        break;
                    }
                }

                if (newPerf) {
                    //caso seja uma nova performance
                    JSONObject performance = new JSONObject();

                    if (p.invocation != null) {
                        Scanner sc = new Scanner(new FileReader(p.invocation));
                        StringBuilder sb = new StringBuilder();
                        while (sc.hasNext()) {
                            sb.append(sc.next());
                        }
                        String content = sb.toString();
                        performance.put("invocation", content);
                    }
                    performance.put("method", p.method);
                    performance.put("description", p.description);
                    if (!p.startTime.isEmpty()) {
                        performance.put("startTime", p.startTime);
                    }
                    if (!p.endTime.isEmpty()) {
                        performance.put("endTime", p.endTime);
                    }
                    performances.add(performance);
                    task.put("performances", performances);
                }
            }
            String filePath = "";
            Object o = task.get("subid");
            String subID = null;
            if (o != null) {
                subID = o.toString();
            }
            switch (task.get("status").toString().toUpperCase()) {
                case "RUNNING":
                    filePath = Utils.getRunningTaskFilePath(pgDir,
                            task.get("dataflow").toString().toLowerCase(),
                            task.get("transformation").toString().toLowerCase(),
                            task.get("id").toString(),
                            subID);
                    break;
                case "FINISHED":
                    filePath = Utils.getFinishedTaskFilePath(pgDir,
                            task.get("dataflow").toString().toLowerCase(),
                            task.get("transformation").toString().toLowerCase(),
                            task.get("id").toString(),
                            subID);
                    break;
            }
            writeJSONFile(task, filePath);
        }
    }

    private static void storeFiles(dfFiles files, String pgDir) {
        JSONObject task = JSONReader.getTask(pgDir, files.getFileByIndex(0).dataflowTag, files.getFileByIndex(0).transformationTag, files.getFileByIndex(0).taskID, files.getFileByIndex(0).taskSubID);
        JSONArray taskFiles = new JSONArray();

        if (task.get("files") != null) {
            taskFiles = (JSONArray) task.get("files");
        }

        for (pde.object.task.File df : files.files) {
            JSONObject obj = new JSONObject();
            obj.put("name", df.name);
            obj.put("path", df.path);

            taskFiles.add(obj);
        }
        task.put("files", taskFiles);

        String filePath = "";
        Object o = task.get("subid");
        String subID = null;
        if (o != null) {
            subID = o.toString();
        }
        switch (task.get("status").toString().toUpperCase()) {
            case "RUNNING":
                filePath = Utils.getRunningTaskFilePath(pgDir,
                        task.get("dataflow").toString().toLowerCase(),
                        task.get("transformation").toString().toLowerCase(),
                        task.get("id").toString(),
                        subID);
                break;
            case "FINISHED":
                filePath = Utils.getFinishedTaskFilePath(pgDir,
                        task.get("dataflow").toString().toLowerCase(),
                        task.get("transformation").toString().toLowerCase(),
                        task.get("id").toString(),
                        subID);
                break;
        }
        writeJSONFile(task, filePath);
    }

    private static void storeFile(pde.object.task.File file, String pgDir) {
        JSONObject task = JSONReader.getTask(pgDir, file.dataflowTag, file.transformationTag, file.taskID, file.taskSubID);
        JSONObject obj = JSONReader.getFile(task, file);

        boolean newObj = false;
        if (obj == null) {
            obj = new JSONObject();
            newObj = true;
        }
        obj.put("name", file.name);
        obj.put("path", file.path);

        if (newObj) {
            JSONArray files = (JSONArray) task.get("files");
            if (files == null) {
                files = new JSONArray();
            }
            files.add(obj);
            task.put("files", files);
        }

        String filePath = "";
        Object o = task.get("subid");
        String subID = null;
        if (o != null) {
            subID = o.toString();
        }
        switch (task.get("status").toString().toUpperCase()) {
            case "RUNNING":
                filePath = Utils.getRunningTaskFilePath(pgDir,
                        task.get("dataflow").toString().toLowerCase(),
                        task.get("transformation").toString().toLowerCase(),
                        task.get("id").toString(),
                        subID);
                break;
            case "FINISHED":
                filePath = Utils.getFinishedTaskFilePath(pgDir,
                        task.get("dataflow").toString().toLowerCase(),
                        task.get("transformation").toString().toLowerCase(),
                        task.get("id").toString(),
                        subID);
                break;
        }
        writeJSONFile(task, filePath);
    }

    private static void storeElement(Element element, String pgDir) {
        JSONObject task = JSONReader.getTask(pgDir, element.dataflowTag, element.transformationTag, element.taskID, element.taskSubID);

        JSONObject set = JSONReader.getSet(task, element.setTag);
        boolean newSet = false;
        if (set == null) {
            set = new JSONObject();
            set.put("tag", element.setTag);
            newSet = true;
        }

        JSONArray elements = (JSONArray) set.get("elements");
        if (elements == null) {
            elements = new JSONArray();
        }
        elements.addAll(element.values);
        set.put("elements", elements);

        JSONArray sets = (JSONArray) task.get("sets");
        if (sets == null || sets.isEmpty()) {
            sets = new JSONArray();
            sets.add(set);
        } else if (newSet) {
            sets.add(set);
        }
        task.put("sets", sets);

        String filePath = "";
        Object o = task.get("subid");
        String subID = null;
        if (o != null) {
            subID = o.toString();
        }
        switch (task.get("status").toString().toUpperCase()) {
            case "RUNNING":
                filePath = Utils.getRunningTaskFilePath(pgDir,
                        task.get("dataflow").toString().toLowerCase(),
                        task.get("transformation").toString().toLowerCase(),
                        task.get("id").toString(),
                        subID);
                break;
            case "FINISHED":
                filePath = Utils.getFinishedTaskFilePath(pgDir,
                        task.get("dataflow").toString().toLowerCase(),
                        task.get("transformation").toString().toLowerCase(),
                        task.get("id").toString(),
                        subID);
                break;
        }
        writeJSONFile(task, filePath);
    }

    private static void storeExtractor(Extractor ext, String pgDir) {
        JSONObject df = JSONReader.readDataflow(Utils.getDataflowDir(pgDir, ext) + DATAFLOW_FILE_NAME);
        JSONObject ds = JSONReader.getSet(df, ext.transformationTag, ext.datasetTag);

        JSONObject obj = new JSONObject();
        obj.put("tag", ext.tag);

        if (ext.method != null && ext.cartridge != null) {
            obj.put("cartridge", ext.method.toString());
            obj.put("extension", ext.cartridge.toString());
        }

        JSONArray atts = (JSONArray) ds.get("extractors");
        if (atts == null) {
            atts = new JSONArray();
        }
        atts.add(obj);
        ds.put("extractors", atts);

        writeJSONFile(df, Utils.getDataflowDir(pgDir, ext) + DIR_SEPARATOR + Utils.DATAFLOW_FILE_NAME);
    }

    private static void storeExtractorCombination(ExtractorCombination j, String pgDir) {
        JSONObject df = JSONReader.readDataflow(Utils.getDataflowDir(pgDir, j) + DATAFLOW_FILE_NAME);
        JSONObject ds = JSONReader.getSet(df, j.transformationTag, j.setTag);

        JSONObject obj = new JSONObject();
        obj.put("outer", j.outerExtractor);
        obj.put("inner", j.innerExtractor);
        obj.put("keys", j.keys);
        obj.put("key_types", j.keyTypes);

        JSONArray combinations = (JSONArray) ds.get("extractor.combinations");
        if (combinations == null) {
            combinations = new JSONArray();
        }
        combinations.add(obj);
        ds.put("extractor.combinations", combinations);

        writeJSONFile(df, Utils.getDataflowDir(pgDir, j) + DIR_SEPARATOR + Utils.DATAFLOW_FILE_NAME);
    }

    public static JSONObject getDataflow(Dataflow df, Configuration config) {
//        dataflow
        JSONObject dfObj = new JSONObject();
        dfObj.put("tag", df.dataflowTag);
//        transformations
        for (Transformation dt : df.getTransformations()) {
            JSONObject dtObj = new JSONObject();
            dtObj.put("tag", dt.tag);

            JSONArray dts = (JSONArray) dfObj.get("transformations");
            if (dts == null) {
                dts = new JSONArray();
            }
            dts.add(dtObj);
            dfObj.put("transformations", dts);

            //        programs
            for (Program p : dt.getPrograms()) {
                JSONObject pObj = new JSONObject();
                pObj.put("name", p.getName());
                pObj.put("path", p.getFilePath());

                JSONArray pgs = (JSONArray) dtObj.get("programs");
                if (pgs == null) {
                    pgs = new JSONArray();
                }
                pgs.add(pObj);
                dtObj.put("programs", pgs);
            }

            //sets
            //input
            for (Dataset set : dt.getInputDatasets()) {
                JSONObject setObj = new JSONObject();
                setObj.put("tag", set.tag);
                setObj.put("type", SetType.INPUT.toString());
                if (!set.dependency.isEmpty()) {
                    setObj.put("dependency", set.dependency);
                }

                JSONArray sets = (JSONArray) dtObj.get("sets");
                if (sets == null) {
                    sets = new JSONArray();
                }
                sets.add(setObj);
                dtObj.put("sets", sets);

                if (set.dependency.isEmpty()) {
                    //attributes
                    for (Attribute att : set.getAttributes()) {
                        JSONObject attObj = new JSONObject();
                        attObj.put("name", att.name);
                        attObj.put("type", att.type.toString());
                        if (att.extractorTag != null) {
                            attObj.put("extractor", att.extractorTag);
                        }

                        JSONArray atts = (JSONArray) setObj.get("attributes");
                        if (atts == null) {
                            atts = new JSONArray();
                        }
                        atts.add(attObj);
                        setObj.put("attributes", atts);
                    }
                    //extractors
                    for (Extractor ext : set.getExtractors()) {
                        JSONObject ds = JSONReader.getSet(dfObj, dt.getTag(), set.getTag());

                        JSONObject extObj = new JSONObject();
                        extObj.put("tag", ext.tag);

                        if (ext.method != null && ext.cartridge != null) {
                            //JAPI -- todo: trocar notacao de cartridge e extension
                            extObj.put("cartridge", ext.method.toString());
                            extObj.put("extension", ext.cartridge.toString());
                        }

                        JSONArray exts = (JSONArray) ds.get("extractors");
                        if (exts == null) {
                            exts = new JSONArray();
                        }
                        exts.add(extObj);
                        ds.put("extractors", exts);
                    }
                    //extractor combinations
                    for (ExtractorCombination ec : set.getExtractorCombinations()) {
                        JSONObject ds = JSONReader.getSet(dfObj, dt.getTag(), set.getTag());

                        JSONObject ecObj = new JSONObject();
                        ecObj.put("outer", ec.outerExtractor);
                        ecObj.put("inner", ec.innerExtractor);
                        ecObj.put("keys", ec.keys);
                        ecObj.put("key_types", ec.keyTypes);

                        JSONArray combinations = (JSONArray) ds.get("extractor.combinations");
                        if (combinations == null) {
                            combinations = new JSONArray();
                        }
                        combinations.add(ecObj);
                        ds.put("extractor.combinations", combinations);
                    }
                }
            }
            //output
            for (Dataset set : dt.getOutputDatasets()) {
                JSONObject setObj = new JSONObject();
                setObj.put("tag", set.tag);
                setObj.put("type", SetType.OUTPUT.toString());

                JSONArray sets = (JSONArray) dtObj.get("sets");
                if (sets == null) {
                    sets = new JSONArray();
                }
                sets.add(setObj);
                dtObj.put("sets", sets);
                //attributes
                for (Attribute att : set.getAttributes()) {
                    JSONObject attObj = new JSONObject();
                    attObj.put("name", att.name);
                    attObj.put("type", att.type.toString());
                    if (att.extractorTag != null) {
                        attObj.put("extractor", att.extractorTag);
                    }

                    JSONArray atts = (JSONArray) setObj.get("attributes");
                    if (atts == null) {
                        atts = new JSONArray();
                    }
                    atts.add(attObj);
                    setObj.put("attributes", atts);
                }
                //extractors
                for (Extractor ext : set.getExtractors()) {
                    JSONObject ds = JSONReader.getSet(dfObj, dt.getTag(), set.getTag());

                    JSONObject extObj = new JSONObject();
                    extObj.put("tag", ext.tag);

                    if (ext.method != null && ext.cartridge != null) {
                        //JAPI -- todo: trocar notacao de cartridge e extension
                        extObj.put("cartridge", ext.method.toString());
                        extObj.put("extension", ext.cartridge.toString());
                    }

                    JSONArray exts = (JSONArray) ds.get("extractors");
                    if (exts == null) {
                        exts = new JSONArray();
                    }
                    exts.add(extObj);
                    ds.put("extractors", exts);
                }
                //extractor combinations
                for (ExtractorCombination ec : set.getExtractorCombinations()) {
                    JSONObject ds = JSONReader.getSet(dfObj, dt.getTag(), set.getTag());

                    JSONObject ecObj = new JSONObject();
                    ecObj.put("outer", ec.outerExtractor);
                    ecObj.put("inner", ec.innerExtractor);
                    ecObj.put("keys", ec.keys);
                    ecObj.put("key_types", ec.keyTypes);

                    JSONArray combinations = (JSONArray) ds.get("extractor.combinations");
                    if (combinations == null) {
                        combinations = new JSONArray();
                    }
                    combinations.add(ecObj);
                    ds.put("extractor.combinations", combinations);
                }
            }
        }

        return dfObj;
    }
}
