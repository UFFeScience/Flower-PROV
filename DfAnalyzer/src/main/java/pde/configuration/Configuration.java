package pde.configuration;

import java.io.File;

/**
 *
 * @author vitor
 */
public class Configuration {
    
    public String directory = null;
    
    public Configuration(String directory){
        setDirectory(directory);
//        createDirectories();
    }
    
    private void setDirectory(String directory){
        this.directory = directory;
    }

    public String getDirectory() {
        return directory + File.separator + "provenance";
    }
    
    public String toString(){
        return "Directory is " + directory;
    }

    public void clear() {
        new File(getDirectory()).delete();
        createDirectories();
    }

    private void createDirectories() {
        new File(getDirectory()).mkdirs();
    }
    
}
