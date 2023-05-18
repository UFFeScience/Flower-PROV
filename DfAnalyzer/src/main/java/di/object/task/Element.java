package di.object.task;

import di.enumeration.dataflow.DataflowType;
import java.util.ArrayList;
import java.util.Collection;
import di.dataflow.object.DataflowObject;
import utils.Utils;

/**
 *
 * @author vitor
 */
public class Element extends DataflowObject {

    public String transformationTag = "";
    public Integer taskID;
    public String setTag = "";
    public ArrayList<String> values = new ArrayList<>();

    protected Element(DataflowType type) {
        super(type);
    }

    public Element() {
        this(DataflowType.ELEMENT);
    }

    protected Collection<? extends dfFile> getFilesFromExtractors(int extractorSize) {
        ArrayList<dfFile> result = new ArrayList<>();
        ArrayList<String> fileNames = new ArrayList<>();

        for (String v : values) {
            String[] slices = v.split(";");

            if (slices.length - extractorSize >= 0) {
                for (int i = slices.length - extractorSize; i < slices.length; i++) {
                    String currentValue = slices[i];
                    if (!fileNames.contains(currentValue)) {
                        fileNames.add(currentValue);
                        dfFile f = new dfFile();
                        f.dataflowTag = dataflowTag;
                        f.taskID = String.valueOf(taskID);
                        f.transformationTag = transformationTag;
                        int splitter = currentValue.lastIndexOf(Utils.DIR_SEPARATOR);
                        f.name = currentValue.substring(splitter + 1);
                        f.path = currentValue.substring(0, splitter);
                        result.add(f);
                    }
                }
            }
        }

        return result;
    }

}
