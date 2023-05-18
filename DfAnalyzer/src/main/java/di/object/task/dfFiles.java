package di.object.task;

import java.util.ArrayList;
import di.enumeration.dataflow.DataflowType;

/**
 *
 * @author vinicius, debora
 */

public class dfFiles extends dfFile{
    public ArrayList<dfFile> files = new ArrayList<>();
    
        protected dfFiles(DataflowType type) {
        super(type);
    }

    public dfFiles() {
        this(DataflowType.FILES);
    }
    
    public void add (dfFile file){
        this.files.add(file);
    }
    
    public dfFile get (int index){
        return this.files.get(index);
    }

}
