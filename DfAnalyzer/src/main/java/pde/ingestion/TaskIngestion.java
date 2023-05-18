package pde.ingestion;

import pde.configuration.Configuration;
import utils.Utils;

/**
 *
 * @author vitor
 */
public class TaskIngestion extends Ingestion {

    public Configuration configuration;
    public String transformationTag;
    public String dataflowTag;
    public String taskID;
    public String subTaskID;

    public TaskIngestion(Configuration config, String dfTag, String dtTag, String identifier, String subIdentifier) {
        this.configuration = config;
        this.dataflowTag = dfTag;
        this.transformationTag = dtTag;
        this.taskID = identifier;
        this.subTaskID = subIdentifier;
    }

    @Override
    public void run() {
        if (dataflowTag != null && transformationTag != null && taskID != null) {
            String taskCompleteIDs;
            if (subTaskID == null) {
                taskCompleteIDs = taskID;
            } else {
                taskCompleteIDs = taskID + Utils.SEPARATOR + subTaskID;
            }

            for (String s : Utils.status) {
//                Utils.moveFile(Utils.getDataflowDir(configuration.ProvenanceGathererDirectory, dataflowTag) 
//                        + transformationTag.toLowerCase() + Utils.SEPARATOR + taskCompleteIDs + Utils.SEPARATOR + s + ".json",
//                        Utils.getDataflowDir(configuration.DataIngestorDirectory, dataflowTag) + transformationTag.toLowerCase() + Utils.SEPARATOR + taskCompleteIDs + Utils.SEPARATOR + s + ".json");
                
                Utils.copyFile(Utils.getDataflowDir(configuration.getDirectory(), dataflowTag) 
                        + transformationTag.toLowerCase() + Utils.SEPARATOR + taskCompleteIDs + Utils.SEPARATOR + s + ".json",
                        Utils.getDataflowDir(configuration.getDirectory(), dataflowTag) + transformationTag.toLowerCase() + Utils.SEPARATOR + taskCompleteIDs + Utils.SEPARATOR + s + ".json");
            }
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getTransformationTag() {
        return transformationTag;
    }

    public void setTransformationTag(String transformationTag) {
        this.transformationTag = transformationTag;
    }

    public String getDataflowTag() {
        return dataflowTag;
    }

    public void setDataflowTag(String dataflowTag) {
        this.dataflowTag = dataflowTag;
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

}
