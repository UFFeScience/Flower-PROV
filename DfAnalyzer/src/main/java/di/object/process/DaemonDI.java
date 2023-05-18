package di.object.process;

import di.enumeration.dbms.DBMS;
import di.dataflow.object.ProcessObject;
import di.enumeration.process.ProcessType;
import java.io.File;
import java.io.FilenameFilter;
import di.json.JSONReader;
import di.object.dataflow.Dataflow;
import di.object.dataflow.Transformation;
import org.json.simple.JSONObject;
import process.background.ProvenanceQueue;
import di.main.Utils;
import java.sql.Timestamp;
import java.text.DecimalFormat;

/**
 *
 * @author vitor
 */
public class DaemonDI extends ProcessObject {

    public ProvenanceQueue queue = null;
    public DBMS dbms;

//    performance metrics
    public int transactionsGenerated = 0;
    public double parsingTime = 0.00;
    public double generationTime = 0.00;
    public double queueingTime = 0.00;

    protected DaemonDI(String dataflowAnalyzerDirectory, ProcessType type) {
        super(dataflowAnalyzerDirectory, type);
    }

    public DaemonDI(String dataflowAnalyzerDirectory) {
        this(dataflowAnalyzerDirectory, ProcessType.DAEMON);
    }

    @Override
    public void run() {
        System.out.println("Starting daemon process...");
        while (true) {
            Utils.sleep(500);
        }
//        File jsonDir = new File(Utils.getJSONDirectory(dataflowAnalyzerDirectory));
//        if (jsonDir.isDirectory() && jsonDir.exists()) {
//            File[] dataflows = jsonDir.listFiles();
//            for (File df : dataflows) {
//                if (df.isDirectory()) {
//                    undoFiles(df);
//                }
//            }
//
//            while (true) {
////                dataflows = jsonDir.listFiles();
////                if (dataflows != null) {
////                    for (File df : dataflows) {
////                        if (df.isDirectory()) {
////                            handleJSONFiles(df);
////                        }
////                    }
////                }
//            }
//        }
    }

    private void handleJSONFiles(File dfDir) {
        Dataflow df = handleDataflowFile(dfDir);
        if (df != null) {
            handleTaskFiles(dfDir, df);
        }
        handleFinishToken(dfDir);
    }

    private void undoFiles(File dfDir) {
        FilenameFilter dfFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(".json")
                        && name.startsWith(Utils.RUNNING_FILE);
            }
        };

        File[] fs = dfDir.listFiles(dfFilter);
        for (File f : fs) {
            Utils.renameFromRunningToOriginalFile(Utils.getPathFromFile(f),
                    f.getName().replaceAll(Utils.RUNNING_FILE, ""));
        }
    }

    private Dataflow handleDataflowFile(File df) {
        FilenameFilter readyDataflowFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals(Utils.DATAFLOW_FILE_NAME);
            }
        };

        FilenameFilter storedDataflowFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals(Utils.STORED_FILE + Utils.DATAFLOW_FILE_NAME);
            }
        };

        File[] dfs = df.listFiles(readyDataflowFilter);
        Dataflow dataflow = getDataflowFromFiles(dfs, true);
        if (dataflow == null) {
            dfs = df.listFiles(storedDataflowFilter);
            dataflow = getDataflowFromFiles(dfs, false);
        }

        return dataflow;
    }

    private void handleFinishToken(File dfDir) {
        FilenameFilter finishFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals(Utils.FINISH_TOKEN);
            }
        };

        FilenameFilter jsonFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(".json")
                        && !name.startsWith(Utils.STORED_FILE);
            }
        ;
        };

        File[] token = dfDir.listFiles(finishFilter);
        File[] jsonFiles = dfDir.listFiles(jsonFilter);

        if (token != null && jsonFiles != null
                && token.length > 0 && jsonFiles.length == 0) {
            Utils.deleteDirectory(dfDir);
        }
    }

    private void handleTaskFiles(File dfDir, Dataflow df) {
        FilenameFilter dfFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(".json")
                        && !name.equals(Utils.DATAFLOW_FILE_NAME)
                        && !name.startsWith(Utils.RUNNING_FILE)
                        && !name.startsWith(Utils.STORED_FILE);
            }
        };

        String[] status = {"r", "f"};
        File[] taskFiles = dfDir.listFiles(dfFilter);
        if (taskFiles != null) {
            for (Transformation dt : df.transformations) {
                for (String s : status) {
                    for (File f : taskFiles) {
                        if (f.getName().toLowerCase().startsWith(dt.tag)
                                && f.getName().toLowerCase().endsWith("-" + s + ".json")) {
                            JSONObject taskFile = JSONReader.readDataflow(f.getAbsolutePath());
                            if (taskFile != null) {
                                String fileName = f.getName();
                                String path = Utils.getPathFromFile(f);
                                Transaction t = JSONReader.generationTaskTransaction(fileName, path, taskFile, dbms);
                                if (t != null) {
                                    queue.handleTransaction(ProvenanceQueue.DBOperation.PENDENT_TRANSACTION, t);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Dataflow getDataflowFromFiles(File[] dfs, boolean newDataflow) {
        if (dfs != null && dfs.length > 0) {
            File dfFile = dfs[0];
            JSONObject dataflow = JSONReader.readDataflow(dfFile.getAbsolutePath());
            if (dataflow != null) {
                String fileName = dfFile.getName();
                String path = Utils.getPathFromFile(dfFile);
                Transaction dfTr = JSONReader.generationDataflowTransaction(fileName, path, dataflow, dbms);
                if (newDataflow && dfTr != null) {
                    queue.handleTransaction(ProvenanceQueue.DBOperation.PENDENT_TRANSACTION, dfTr);
                }

                return dfTr.getDataflowFromObjects();
            }
        }

        return null;
    }

    public String getPerformanceData(boolean header) {
        StringBuilder sb = new StringBuilder();
        if (header) {
            sb.append("timestamp").append(Utils.ELEMENT_SEPARATOR)
                    .append("transactions_generated").append(Utils.ELEMENT_SEPARATOR)
                    .append("transactions_processed").append(Utils.ELEMENT_SEPARATOR)
                    .append("transactions_generation_time").append(Utils.ELEMENT_SEPARATOR)
                    .append("transactions_parsing_time").append(Utils.ELEMENT_SEPARATOR)
                    .append("queueing_time").append(Utils.ELEMENT_SEPARATOR)
                    .append("queue_average_time").append(Utils.ELEMENT_SEPARATOR)
                    .append("queue_pendent_transactions").append(Utils.ELEMENT_SEPARATOR)
                    .append("processing_time").append(Utils.NEW_LINE);
        }
        DecimalFormat df = new DecimalFormat("######.##");
        double averageQueueTime = this.queue.queueTotalTime / 1000.00;
        if (this.queue.transactionsProcessed > 0) {
            averageQueueTime = averageQueueTime / this.queue.transactionsProcessed;
        }
//        milliseconds
        sb.append(new Timestamp(System.currentTimeMillis())).append(Utils.ELEMENT_SEPARATOR)
                .append(this.transactionsGenerated).append(Utils.ELEMENT_SEPARATOR)
                .append(this.queue.transactionsProcessed).append(Utils.ELEMENT_SEPARATOR)
                .append(df.format(this.generationTime / 1000.00)).append(Utils.ELEMENT_SEPARATOR)
                .append(df.format(this.parsingTime / 1000.00)).append(Utils.ELEMENT_SEPARATOR)
                .append(df.format(this.queueingTime / 1000.00)).append(Utils.ELEMENT_SEPARATOR)
                .append(df.format(averageQueueTime)).append(Utils.ELEMENT_SEPARATOR)
                .append(this.queue.pendentTransactions.size()).append(Utils.ELEMENT_SEPARATOR)
                .append(this.queue.transactionProcessingTime);
        return sb.toString();
    }
}
