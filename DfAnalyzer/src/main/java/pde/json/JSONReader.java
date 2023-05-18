package pde.json;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import pde.object.task.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Utils;

/**
 *
 * @author vitor
 */
public class JSONReader {

    public static JSONObject readDataflow(String filePath) {
        try {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(new FileReader(filePath));
        } catch (IOException | ParseException ex) {
            Logger.getLogger(JSONReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static JSONObject getDataTransformation(JSONObject df, String dataTransformationTag) {
        JSONArray dts = (JSONArray) df.get("transformations");
        if (dts != null) {
            ListIterator iDts = dts.listIterator();
            while (iDts.hasNext()) {
                JSONObject dt = (JSONObject) iDts.next();
                if (dt.get("tag").equals(dataTransformationTag)) {
                    return dt;
                }
            }
        }

        return null;
    }

    public static JSONObject getSet(JSONObject df, String transformationTag, String setTag) {
        JSONObject dt = getDataTransformation(df, transformationTag);
        JSONArray dss = (JSONArray) dt.get("sets");
        if (dss != null) {
            ListIterator iDss = dss.listIterator();
            while (iDss.hasNext()) {
                JSONObject ds = (JSONObject) iDss.next();
                if ((ds.get("tag") != null && ds.get("tag").equals(setTag))
                        || (ds.get("dependency") != null && ds.get("dependency").equals(setTag))) {
                    return ds;
                }
            }
        }

        return null;
    }

    static JSONObject getSet(JSONObject task, String setTag) {
        JSONArray dss = (JSONArray) task.get("sets");
        if (dss != null) {
            ListIterator iDss = dss.listIterator();
            while (iDss.hasNext()) {
                JSONObject ds = (JSONObject) iDss.next();
                if ((ds.get("tag") != null && ds.get("tag").equals(setTag))) {
                    return ds;
                }
            }
        }

        return null;
    }

    static JSONObject getTask(String pgDir, String dataflowTag, String transformationTag, String identifier, String subIdentifier) {
        try {
            try {
                JSONParser parser = new JSONParser();
                String filePath = Utils.getRunningTaskFilePath(pgDir, dataflowTag, transformationTag, identifier, subIdentifier);
                return (JSONObject) parser.parse(new FileReader(filePath));
            } catch (ParseException ex) {
                Logger.getLogger(JSONReader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                JSONParser parser = new JSONParser();
                String filePath = Utils.getFinishedTaskFilePath(pgDir, dataflowTag, transformationTag, identifier, subIdentifier);
                try {
                    FileReader fr = new FileReader(filePath);
                    return (JSONObject) parser.parse(fr);
                } catch (FileNotFoundException exp) {
                }
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(JSONReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    static JSONObject getFile(JSONObject task, File file) {
        if (task != null) {
            JSONArray files = (JSONArray) task.get("files");
            if (files != null) {
                ListIterator iFiles = files.listIterator();
                while (iFiles.hasNext()) {
                    JSONObject f = (JSONObject) iFiles.next();
                    if (f.get("name") != null && f.get("path") != null
                            && f.get("name").equals(file.name) && f.get("path").equals(file.path)) {
                        return f;
                    }
                }
            }
        }

        return null;
    }

}
