package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import pde.object.DataflowObject;
import rest.config.DbConfiguration;

/**
 *
 * @author vitor
 */
public class Utils {

    public static boolean verbose = false;
    public static String indentationContent = "  ";
    public final static String[] status = {"R", "F"};

    public final static String DIR_SEPARATOR = File.separator;
    public final static String DATAFLOW_FILE_NAME = "dataflow.json";
    public final static String DATAFLOW_PREFIX = "df";
    public final static String SEPARATOR = "-";
    public final static String ELEMENT_SEPARATOR = ";";

    public static void print(int indentationLevel, String content) {
        for (int i = 0; i < indentationLevel; i++) {
            System.out.print(indentationContent);
        }
        System.out.println(content);
    }

    public static String getDataflowDir(String pgDir, String dataflowTag) {
        return pgDir + DIR_SEPARATOR + dataflowTag + DIR_SEPARATOR;
    }

    public static String getDataflowDir(String pgDir, DataflowObject obj) {
        return pgDir + DIR_SEPARATOR + obj.dataflowTag + DIR_SEPARATOR;
    }

    public static String getRunningTaskFilePath(String pgDir, String dataflowTag, String transformationTag, String ID, String subID) {
        String completeTaskIDs = ID;
        if (subID != null) {
            completeTaskIDs += Utils.SEPARATOR + subID;
        }
        return pgDir + DIR_SEPARATOR + dataflowTag + DIR_SEPARATOR
                + transformationTag + SEPARATOR
                + completeTaskIDs + SEPARATOR + "R.json";
    }

    public static String getFinishedTaskFilePath(String pgDir, String dataflowTag, String transformationTag, String ID, String subID) {
        String completeTaskIDs = ID;
        if (subID != null) {
            completeTaskIDs += Utils.SEPARATOR + subID;
        }
        return pgDir + DIR_SEPARATOR + dataflowTag + DIR_SEPARATOR
                + transformationTag + SEPARATOR
                + completeTaskIDs + SEPARATOR + "F.json";
    }

    public static String copyFile(String origin, String destination) {
        try {
            String cmd;
            Utils.createDirectory(destination);
            if (Utils.isWindows()) {
                if (destination.substring(destination.length() - 2, destination.length()).equals("\\\\")) {
                    destination = destination.substring(0, destination.length() - 1);
                }
                cmd = "xcopy " + origin + " " + destination;
                cmd += " /q /c /y";
            } else {
                cmd = "cp " + origin + " " + destination;
            }
            runCommand(cmd, null);

            return destination;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void moveFile(String origin, String destination) {
        try {
            String cmd = "";
            Utils.createDirectory(destination);
            if (Utils.isWindows()) {
                cmd = "move " + origin + " " + destination;
            } else {
                cmd = "mv " + origin + " " + destination;
            }
            runCommand(cmd, null);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String createDirectory(String filePath) {
        String current = "";
        current += filePath.substring(0, filePath.lastIndexOf(Utils.DIR_SEPARATOR));
        File f = new File(current);
        f.mkdir();
        return current;
    }

    public static void runCommand(String cmd, String dir) throws IOException, InterruptedException {
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

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process pr = pb.start();

    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("win"));
    }

    public static boolean isMacOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("mac"));
    }

    public static DbConfiguration readConfigurationFile() {
        DbConfiguration config = new DbConfiguration();
        try {
            FileReader fr = new FileReader("DfA.properties");
            BufferedReader br = new BufferedReader(fr);
            String line;
            while (br.ready()) {
                line = br.readLine();
                String[] slices = line.split("=");

                switch (slices[0]) {
                    case "db_port":
                        config.setPort(Integer.parseInt(slices[1]));
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
                }
            }
            br.close();
            fr.close();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return config;
    }
    
//    public static String[] breakTaskIDs(String arguments) {
//        int startBrace = arguments.indexOf("{");
//        int endBrace = arguments.lastIndexOf("}");
//        String[] elements = arguments.substring(startBrace + 1, endBrace).split("\\};\\{");
//        for(int index=0; index<elements.length; index++){
//            if(elements[index].startsWith("{")){
//                elements[index] = elements[index].substring(1);
//            }else if(elements[index].endsWith("}")){
//                elements[index] = elements[index].substring(0, elements[index].length());
//            }
//        }
//        return elements;
//    }

    public static ArrayList<String[]> breakDataCollection(String arguments) {
        int startBrace = arguments.indexOf("{");
        int endBrace = arguments.lastIndexOf("}");
        String[] elements = arguments.substring(startBrace + 1, endBrace-1).split("\\};\\{");
        ArrayList<String[]> collection = new ArrayList<>();
        for(String element : elements){
            if(element.startsWith("{")){
                element = element.substring(1);
            }else if(element.endsWith("}")){
                element = element.substring(0, element.length()-1);
            }
            collection.add(element.split(";"));
        }
        return collection;
    }

    public static String[] breakSets(String arguments) {
        return arguments.replaceAll("\\{", "").replaceAll("\\}", "").split(";");
    }
}
