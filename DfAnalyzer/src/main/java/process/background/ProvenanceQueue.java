package process.background;

import di.enumeration.process.TransactionType;
import java.text.DecimalFormat;
import java.util.ArrayList;
import di.object.process.Transaction;
import di.object.dataflow.Dataflow;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import rest.config.DbConnection;
import utils.Utils;

/**
 *
 * @author vitor
 */
public class ProvenanceQueue extends Thread {

    private ArrayList<Dataflow> dataflowsInMemory = new ArrayList<Dataflow>();
    private DbConnection db = null;
    private long startTime;
    private long endTime;
    private final int batchSize = 100;
    public double transactionProcessingTime;

    public double queueTotalTime;
    public int transactionsProcessed;

    public void removeDataflow(String dfTag) {
        for (Dataflow df : dataflowsInMemory) { //TODO: change to a Map with df_tag as keys (to insert and remove instances more efficiently)
            if (df.dataflowTag.equals(dfTag)) {
                dataflowsInMemory.remove(df);
                break;
            }
        }
    }

    public Dataflow getDataflow(String dfTag) {
        for (Dataflow df : dataflowsInMemory) { //TODO: change to a Map with df_tag as keys (to insert and remove instances more efficiently)
            if (df.dataflowTag.equals(dfTag)) {
                return df;
            }
        }
        return null;
    }

    public enum DBOperation {
        PENDENT_TRANSACTION, RUN_TRANSACTION
    }

    public ConcurrentLinkedQueue pendentTransactions = new ConcurrentLinkedQueue<>();

    public ProvenanceQueue(DbConnection db) {
        this.db = db;
        this.queueTotalTime = 0;
        this.transactionsProcessed = 0;
    }

    @Override
    public void run() {
        while (true) {
//            System.out.println("+++++++++++++++++++++++++++++");
//            System.out.println("start queue");
            runQueries();
//            System.out.println("end queue");
        }
    }

    public synchronized ArrayList<Transaction> handleTransaction(DBOperation oper, Transaction t) {
        ArrayList<Transaction> result = new ArrayList<>();
        if (oper == DBOperation.PENDENT_TRANSACTION) {
            t.queueStartTime = System.currentTimeMillis();
            pendentTransactions.add(t);
        } else if (oper == DBOperation.RUN_TRANSACTION) {
            while (!pendentTransactions.isEmpty() && result.size() < batchSize) {
                result.add((Transaction) pendentTransactions.remove());
            }
        }
        return result;
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void runQueries() {
        if (db == null) {
            return;
        }
        ArrayList<Transaction> transactions = handleTransaction(DBOperation.RUN_TRANSACTION, null);
        for (Transaction t : transactions) {
            startTime = System.currentTimeMillis();
            if ((t.getType() == TransactionType.TASK) && (t.getPath() == null)) {
                Provenance.performTransaction(db.getConnection(), t, getDataflow(t.getDataflowTag()));
            } else if (t.getType() == TransactionType.SHUTDOWN) {
                System.out.println("Shutting down...");
                try {
                    Utils.runCommand("touch finish.token", null);
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(ProvenanceQueue.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(900);
            } else {
                t.execute(db.getConnection());
            }
            endTime = System.currentTimeMillis();
            DecimalFormat df = new DecimalFormat("######.##");
            transactionProcessingTime = Double.valueOf(df.format(transactionProcessingTime + ((endTime - startTime) / 1000.00)));

            System.out.println("Current amount elapsed time: " + transactionProcessingTime);

            transactionsProcessed = transactionsProcessed + 1;
            t.queueEndTime = startTime;
            t.execStartTime = startTime;
            t.execEndTime = endTime;
            queueTotalTime = queueTotalTime + (t.queueEndTime - t.queueStartTime);

            Dataflow dataflow = t.getDataflowFromObjects();
            if (!dataflowsInMemory.contains(dataflow)) {
                dataflowsInMemory.add(dataflow);
            }
        }
    }

}
