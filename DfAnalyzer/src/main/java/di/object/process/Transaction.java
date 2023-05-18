package di.object.process;

import di.enumeration.process.TransactionType;
import java.sql.Connection;
import java.util.ArrayList;
import di.json.JSONReader;
import di.object.dataflow.Dataflow;
import di.dataflow.object.DataflowObject;
import di.enumeration.dataflow.DataflowType;
import di.enumeration.dbms.DBMS;
import process.background.Provenance;
import di.main.Utils;

/**
 *
 * @author vitor
 */
public class Transaction {

    private final TransactionType type;
    private String fileName;
    private String path;
    private String dataflowTag;
    private final ArrayList<DataflowObject> objects = new ArrayList<>();
    protected DBMS dbms;
    public long queueEndTime;
    public long queueStartTime;
    public long execStartTime;
    public long execEndTime;
    public long parsingStart;
    public long parsingEnd;
    public long generationStart;
    public long generationEnd;
    public long queueingStart;
    public long queueingEnd;
    
    public Transaction(TransactionType type){
        this.type = type;
    }

    public Transaction(TransactionType type, String fileName, String path, DBMS dbms) {
        this.type = type;
        this.fileName = fileName;
        this.path = path;
        this.dbms = dbms;
        this.dataflowTag = null;
    }

//    public void addObject(DataflowObject obj) {
//        objects.add(obj);
//    }
    public void addObject(DataflowObject obj) {

        if (this.type == TransactionType.TASK) {

            if (this.dataflowTag == null) {
                this.dataflowTag = obj.dataflowTag;
            } else {
                if (obj.dataflowTag != this.dataflowTag) {
                    //TODO: transformar em  erro/log e identificar corretamente o objeto não inserido
                    System.out.println("ERROR: This Transaction object supports only objects from dataflow: " + this.dataflowTag + "Object rejected!!");
                    return;
                }
            }
        }
        objects.add(obj);
    }

    public ArrayList<DataflowObject> getObjects() {
        return objects;
    }

    public void execute(Connection db) {
        Provenance.performTransaction(db, this);
        if (path == null) {
            return;
        }
        if (fileName == null) {
            return;
        }
    }

    public Dataflow getDataflowFromObjects() {
        for (DataflowObject o : objects) {
            if (o.getType() == DataflowType.DATAFLOW) {
                return (Dataflow) o;
            }
        }

        return null;
    }

    public TransactionType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public DBMS getDBMS() {
        return dbms;
    }

    public String getDataflowTag() {
        return this.dataflowTag;
    }

}
