package pde.ingestion;

import pde.configuration.Configuration;
import utils.Utils;

/**
 *
 * @author vitor
 */
public class DataflowIngestion extends Ingestion {

    public Configuration configuration;
    public String dataflowTag;

    public DataflowIngestion(Configuration config, String dataflowTag) {
        this.dataflowTag = dataflowTag;
        this.configuration = config;
    }

    @Override
    public void run() {
        if (dataflowTag != null) {
            Utils.copyFile(Utils.getDataflowDir(configuration.getDirectory(), dataflowTag) + Utils.DATAFLOW_FILE_NAME,
                    Utils.getDataflowDir(configuration.getDirectory(), dataflowTag));
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getDataflowTag() {
        return dataflowTag;
    }

    public void setDataflowTag(String dataflowTag) {
        this.dataflowTag = dataflowTag;
    }
}
