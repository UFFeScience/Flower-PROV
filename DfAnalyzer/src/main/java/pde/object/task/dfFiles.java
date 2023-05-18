package pde.object.task;

import java.util.ArrayList;
import pde.object.ObjectType;

/**
 *
 * @author vinicius, debora
 */
public class dfFiles extends File {

    public ArrayList<File> files = new ArrayList<>();

    protected dfFiles(ObjectType type) {
        super(type);
    }

    public dfFiles() {
        this(ObjectType.FILES);
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    public File getFileByIndex(int index) {
        return this.files.get(index);
    }

}
