package di.dataflow.object;

import di.enumeration.process.ProcessType;
import di.object.process.DaemonDI;


/**
 *
 * @author vitor
 */
public abstract class ProcessObject extends Thread {
    
    protected final String dataflowAnalyzerDirectory;
    private final ProcessType type;
    
    public static ProcessObject newInstance(String dataflowAnalyzerDirectory, ProcessType objectType) {
        switch (objectType) {
            case DAEMON:
                return new DaemonDI(dataflowAnalyzerDirectory);
        }

        return null;
    }
    
    public ProcessObject(String dataflowAnalyzerDirectory, ProcessType objectType) {
        this.dataflowAnalyzerDirectory = dataflowAnalyzerDirectory;
        this.type = objectType;
    }
    
    public String getStrType(){
        return type.toString();
    }
    
    public ProcessType getType(){
        return type;
    }
    
    @Override
    public abstract void run();
}
