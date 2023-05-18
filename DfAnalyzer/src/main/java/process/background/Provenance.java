package process.background;

import di.object.process.Transaction;
import java.sql.Connection;
import di.provenance.DataflowProvenance;
import di.provenance.TaskProvenance;
import di.object.dataflow.Dataflow;

/**
 *
 * @author vitor
 */
public class Provenance {

    public static Integer performTransaction(Connection db, Transaction t) {
        switch (t.getType()) {
            case DATAFLOW:
                return DataflowProvenance.handleDataflowTransaction(db, t);
            case TASK:
                return TaskProvenance.handleTaskTransaction(db, t);
        }
        
        return null;
    }
    public static Integer performTransaction(Connection db, Transaction t, Dataflow df) {
        switch (t.getType()) {
            case DATAFLOW:
                //TODO: transformar em  erro/log e identificar corretamente o objeto não inserido
                System.out.println("ERROR: performTransaction(Connection db, Transaction t, Dataflow df) Cant receive a DATAFLOW Transaction");
                // return DataflowProvenance.handleDataflowTransaction(db, t);
            case TASK:
                return TaskProvenance.handleTaskTransaction(db, t, df);
        }
        
        return null;
    }
}
