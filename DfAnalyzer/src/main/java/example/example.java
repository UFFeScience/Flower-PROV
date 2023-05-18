package example;

import pde.PDE;
import java.util.ArrayList;
import pde.enumeration.ExtractionCartridge;
import pde.enumeration.ExtractionMethod;
import pde.enumeration.TaskStatus;
import pde.object.dataflow.Dataflow;
import pde.object.dataflow.Dataset;
import pde.object.dataflow.Program;
import pde.object.dataflow.Transformation;
import pde.object.extraction.Extractor;
import pde.object.task.Task;

/**
 *
 * @author vitor
 */
public class example {
    
    public static void main(String[] args) {
        //Spark dataflow example
        //Prospective provenance
        //PDG configuration
        PDE pde = new PDE("/home/vitor/Desktop");     
        pde.clear();
        //dataflow
        Dataflow df = pde.dataflow("clothing");
        //deduplication
        Program dedupProgram = pde.program(df, "ClothingApp::DEDUPLICATION()", "/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark");
        
        Dataset idedup = pde.dataset(df, "ideduplication", null, null);
        Extractor eidedup = pde.extractor(idedup, "ideduplication", 
                ExtractionMethod.INDEXING, ExtractionCartridge.OPTIMIZED_FASTBIT, 
                new String[]{"customerid","country","continent","age","gender","children","status"}, 
                new String[]{"numeric","text","text","numeric","text","numeric","text"});
        
        Dataset odedup = pde.dataset(df, "odeduplication", null, null);
        Extractor eodedup = pde.extractor(odedup, "odeduplication", 
                ExtractionMethod.INDEXING, ExtractionCartridge.OPTIMIZED_FASTBIT, 
                new String[]{"customerid","country","continent","age","gender","children","status"}, 
                new String[]{"numeric","text","text","numeric","text","numeric","text"});
        
        Transformation deduplication = pde.transformation(df, "deduplication", 
                new Dataset[]{idedup}, new Dataset[]{odedup}, 
                new Program[]{dedupProgram});
        
        //europe
        Program europeProgram = pde.program(df, "ClothingApp::EUROPE()", "/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark");
        
        Dataset oeurope = pde.dataset(df, "oeurope", null, null);
        Extractor eoeurope = pde.extractor(oeurope, "oeurope", 
                ExtractionMethod.INDEXING, ExtractionCartridge.OPTIMIZED_FASTBIT, 
                new String[]{"customerid","country","continent","age","gender","children","status"}, 
                new String[]{"numeric","text","text","numeric","text","numeric","text"});
        
        Transformation europe = pde.transformation(df, "europe", 
                new Dataset[]{odedup}, new Dataset[]{oeurope}, new Program[]{europeProgram});
        
        pde.ingest(df);
        
        //Retrospective provenance
        //deduplication
        Integer taskID = 1;
        Integer subTaskID = 1;
        
        Task task = pde.task(df.getDataflowTag(), deduplication.getTag(), 
                taskID, subTaskID, TaskStatus.RUNNING);
        pde.workspace(task, "/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/deduplication");
        pde.resource(task, "spark://mercedes:7077");
        
        ArrayList<String[]> elements = new ArrayList<>();
        elements.add(new String[]{"/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/customer_list1/customer_list1.index"});
        elements.add(new String[]{"/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/customer_list2/customer_list2.index"});
        elements.add(new String[]{"/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/customer_list3/customer_list3.index"});
        elements.add(new String[]{"/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/customer_list4/customer_list4.index"});
        pde.collection(task, "ideduplication", elements);
        
        pde.ingest(task);
        
        task = pde.task(df.getDataflowTag(), deduplication.getTag(), 
                taskID, subTaskID, TaskStatus.FINISHED);
        pde.workspace(task, "/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/deduplication");
        pde.resource(task, "spark://mercedes:7077");
        
        elements = new ArrayList<>();
        elements.add(new String[]{"/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/deduplication/odeduplication.index"});
        pde.collection(task, "odeduplication", elements);
        
        pde.ingest(task);
        
        //europe
        task = pde.task(df.getDataflowTag(), europe.getTag(), 
                taskID, subTaskID, TaskStatus.RUNNING);
        pde.workspace(task, "/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/europe");
        pde.resource(task, "spark://mercedes:7077");
        
        pde.dependency(task, new String[]{"deduplication"}, new String[]{"1"});
        
        pde.ingest(task);
        
        task = pde.task(df.getDataflowTag(), europe.getTag(), 
                taskID, subTaskID, TaskStatus.FINISHED);
        pde.workspace(task, "/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/europe");
        pde.resource(task, "spark://mercedes:7077");
        
        elements = new ArrayList<>();
        elements.add(new String[]{"/Users/vitor/Documents/repository/dfanalyzer-spark/Clothing-Spark/output/europe/oeurope.index"});
        pde.collection(task, "oeurope", elements);
        
        pde.dependency(task, new String[]{"deduplication"}, new String[]{"1"});
        
        pde.ingest(task);
    }
    
}
