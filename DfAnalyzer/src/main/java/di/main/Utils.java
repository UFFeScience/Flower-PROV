package di.main;

import di.enumeration.dbms.DBMS;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import di.json.JSONReader;
import di.object.dataflow.Dataflow;
import di.dataflow.object.DataflowObject;
import org.json.simple.JSONObject;
import di.object.process.Transaction;
import di.object.task.dfFile;

/**
 *
 * @author vitor
 */
public class Utils {

    public static String indentationContent = "  ";

    public final static boolean verbose = true;
    public final static boolean debug = false;
    public final static String DIR_SEPARATOR = File.separator;
    public final static String DATAFLOW_FILE_NAME = "dataflow.json";
    public final static String FINISH_TOKEN = "finish.token";
    public final static String DATAFLOW_PREFIX = "df";
    public final static String SEPARATOR = "-";
    public final static String ELEMENT_SEPARATOR = ";";
    public final static int SLEEP_INTERVAL = 100;
    public final static String STORED_FILE = "STORED_";
    public final static String RUNNING_FILE = "RUNNING_";
    private final static String DATA_SET_PREFIX = "ds_";
    public final static String NEW_LINE = "\n";

    public static void print(int indentationLevel, String content) {
        for (int i = 0; i < indentationLevel; i++) {
            System.out.print(indentationContent);
        }
        System.out.println(content);
    }

    public static String getJSONDirectory(String dfaDirectory) {
        return dfaDirectory;
    }

    public static String getDataflowDir(String dfaDirectory, DataflowObject obj) {
        return Utils.getJSONDirectory(dfaDirectory) + DIR_SEPARATOR + obj.dataflowTag + DIR_SEPARATOR;
    }

    public static String getTaskFilePath(String dfaDirectory, String dataflowTag, String transformationTag, Integer taskID) {
        return dfaDirectory + DIR_SEPARATOR
                + dataflowTag + DIR_SEPARATOR
                + transformationTag + SEPARATOR
                + taskID + ".json";
    }

    public static void sleep() {
        try {
            Thread.sleep(SLEEP_INTERVAL);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void renameFromReadyToRunningFile(String fileName, String path) {
        File f = new File(path + fileName);
        File newF = new File(path + Utils.RUNNING_FILE + fileName);
        f.renameTo(newF);
    }

    public static void renameFromRunningToStoredFile(String fileName, String path) {
        File f = new File(path + Utils.RUNNING_FILE + fileName);
        File newF = new File(path + Utils.STORED_FILE + fileName);
        f.renameTo(newF);
    }

    public static void renameFromRunningToOriginalFile(String path, String fileName) {
        File f = new File(path + Utils.RUNNING_FILE + fileName);
        File newF = new File(path + fileName);
        f.renameTo(newF);
    }

    public static void removeStoredFile(String fileName, String path) {
        File f = new File(path + Utils.RUNNING_FILE + fileName);
        f.delete();
    }

    public static String getPathFromFile(File f) {
        return f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(File.separator) + 1);
    }

    public static void deleteDirectory(File dfDir) {
        if (dfDir.isDirectory()) {
            try {
                Utils.runCommand("rm -rf " + dfDir.getAbsolutePath(), ".");
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static int runCommand(String cmd, String dir) throws IOException, InterruptedException {
        Runtime run = Runtime.getRuntime();
        int result;
        String command[];
        if (Utils.isWindows()) {
            String cmdWin[] = {"cmd.exe", "/c", cmd};
            command = cmdWin;
        } else {
            String cmdLinux = cmd;
            if (cmd.contains(">")) {
                cmdLinux = cmd.replace(">", ">>");
            }
            String cmdLin[] = {"/bin/bash", "-c", cmdLinux};
            command = cmdLin;
        }
        if (verbose) {
            System.out.println(command[command.length - 1]);
        }
        Process pr;
        if (dir == null) {
            pr = run.exec(command);
        } else {
            pr = run.exec(command, null, new File(dir));
        }

        pr.waitFor();
        result = pr.exitValue();
        pr.destroy();

        return result;
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("win"));
    }

    public static EnvironmentConfiguration readConfigurationFile() {
        EnvironmentConfiguration config = new EnvironmentConfiguration();

        try {
            FileReader fr = new FileReader("DfA.properties");
            BufferedReader br = new BufferedReader(fr);
            String line;
            while (br.ready()) {
                line = br.readLine();
                String[] slices = line.split("=");

                switch (slices[0]) {
                    case "di_dir":
                        config.setDataIngestorDirectory(slices[1] + Utils.DIR_SEPARATOR);
                        break;
                    case "db_port":
                        config.setPort(slices[1]);
                        break;
                    case "db_server":
                        config.setServer(slices[1]);
                        break;
                    case "db_name":
                        config.setDatabaseName(slices[1]);
                        break;
                    case "db_user":
                        config.setUser(slices[1]);
                        break;
                    case "db_password":
                        if (slices.length >= 2) {
                            config.setPassword(slices[1]);
                        } else {
                            config.setPassword("");
                        }
                        break;
                    case "dbms":
                        config.setDBMS(slices[1]);
                        break;
                }
            }
            br.close();
            fr.close();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return config;
    }

    public static String getDataSetTableName(String tag) {
        return (DATA_SET_PREFIX + tag.toLowerCase());
    }

    public static Dataflow handleDataflowFile(String dataflowDirectory, DBMS dbms) {
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

        File df = new File(dataflowDirectory);
        File[] dfs = df.listFiles(readyDataflowFilter);
        Dataflow dataflow = getDataflowFromFiles(dfs, true, dbms);
        if (dataflow == null) {
            dfs = df.listFiles(storedDataflowFilter);
            dataflow = getDataflowFromFiles(dfs, false, dbms);
        }

        return dataflow;
    }

    private static Dataflow getDataflowFromFiles(File[] dfs, boolean newDataflow, DBMS dbms) {
        if (dfs != null && dfs.length > 0) {
            File dfFile = dfs[0];
            JSONObject dataflow = JSONReader.readDataflow(dfFile.getAbsolutePath());
            if (dataflow != null) {
                String fileName = dfFile.getName();
                String path = Utils.getPathFromFile(dfFile);
                Transaction dfTr = JSONReader.generationDataflowTransaction(fileName, path, dataflow, dbms);
                return dfTr.getDataflowFromObjects();
            }
        }

        return null;
    }

    public static String getExtractorTableName(String extractorTag) {
        return "ext_" + extractorTag.toLowerCase();
    }

    public static String getSequenceNameFromExtractor(String extractorTag) {
        return "ext_" + extractorTag.toLowerCase() + "_id_seq";
    }

    public static String getExtractorFileColumn(String extractorTag) {
        return extractorTag.toLowerCase() + "_file_id";
    }

    public static String getExtractorIDColumn(String extractorTag) {
        return extractorTag.toLowerCase() + "_id";
    }

    public static Integer getFileID(ArrayList<di.object.task.dfFile> files, String filePath) {
        int splitter = filePath.lastIndexOf(Utils.DIR_SEPARATOR);
        String name = filePath.substring(splitter + 1);
        String path = filePath.substring(0, splitter);
        for (di.object.task.dfFile f : files) {
            if (f.name.equals(name) && f.path.equals(path)) {
                return f.ID;
            }
        }

        return null;
    }

    public static String getExtractorFileNameColumn() {
        return "filename";
    }

    public static boolean isNumericArray(String str) {
        if (str == null) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static String getFilePath(ArrayList<dfFile> files, int fileID) {
        for (dfFile file : files) {
            if (file.ID == fileID) {
                return file.path + "/" + file.name;
            }
        }
        return null;
    }
}
