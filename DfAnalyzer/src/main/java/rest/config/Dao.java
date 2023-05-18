package rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import pde.enumeration.AttributeType;
import pde.object.DataflowObject;
import pde.object.ObjectType;
import pde.object.dataflow.Attribute;
import pde.object.dataflow.Dataset;
import rest.config.entities.DataTransformationDependency;
import rest.config.entities.Dataflow;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class Dao {

    @Autowired
    private Sql2o sql2o;

    public List<Dataflow> getAllDataflows() {
        try (Connection con = sql2o.open()) {
            String sql = "SELECT * FROM dataflow;";
            return con.createQuery(sql).executeAndFetch(Dataflow.class);
        }
    }

    public List<Attribute> getAllAttributesOfDataSet(Long datasetID) {
        try (Connection con = sql2o.open()) {
            String sql = "SELECT  extractor_id, name, type FROM attribute where ds_id=:id;";
            return con.createQuery(sql).addParameter("id", datasetID).executeAndFetchTable()
                    .rows().stream().map(row -> {
                        Attribute attribute = (Attribute) DataflowObject.newInstance(ObjectType.ATTRIBUTE);
                        attribute.setName(row.getString("name"));
                        attribute.setType(AttributeType.valueOf(row.getString("type")));
                        return attribute;
                    }).collect(Collectors.toList());
        }
    }

    public List<Dataset> getAllDataSetofDataflow(Long dataflowID) {
        try (Connection con = sql2o.open()) {
            String sql = "SELECT * FROM data_set where df_id=:id;";
            return con.createQuery(sql).addParameter("id", dataflowID).executeAndFetchTable()
                    .rows().stream().map(row -> {//criando dataset
                        Dataset dataset = (Dataset) DataflowObject.newInstance(ObjectType.DATASET);
                        dataset.tag = row.getString("tag");
                        dataset.id = row.getLong("id");
                        dataset.setAttributes(getAllAttributesOfDataSet(row.getLong("id")));
                        return dataset;
                    }).collect(Collectors.toList());
        }
    }

    public Map<Integer, String> getAllDataTransformationOfDataflow(Long dfId) {
        String sql = "SELECT dt.id, dt.tag from data_transformation dt WHERE  dt.df_id=:id";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).addParameter("id", dfId).executeAndFetchTable()
                    .rows().stream().collect(
                            Collectors.toMap(
                                    row -> row.getInteger("id"),

                                    row -> row.getString("tag")
                            )
                    );
        }
    }


    public Collection<DataTransformationDependency> getAllDataDependencyOfDataflow(Long dfId) {
        String sql = "SELECT dp.* from data_dependency dp INNER JOIN data_set ds on dp.ds_id = ds.id WHERE ds.df_id =:id";

        Map<Integer, DataTransformationDependency> result = new HashMap<>();
        Map<Integer, String> dataTransformation = getAllDataTransformationOfDataflow(dfId);

//        dataTransformation.forEach(((a, s) -> System.out.println(a + " -> " + s)));

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).addParameter("id", dfId).executeAndFetchTable()
                    .rows().stream().forEach(row -> {

                Integer prevId = (Integer) row.getObject("previous_dt_id");
                if (prevId != null && prevId != Integer.MIN_VALUE) {
                    DataTransformationDependency current = result.get(prevId);
                    if (current == null) {
                        current = new DataTransformationDependency(prevId, dataTransformation.get(prevId));
                        result.put(prevId, current);
                    }
                    current.addNext(row.getInteger("ds_id"));
                }

                Integer next = (Integer) row.getObject("next_dt_id");
                if (next != null && next != Integer.MIN_VALUE) {
                    DataTransformationDependency current = result.get(next);
                    if (current == null) {
                        current = new DataTransformationDependency(next, dataTransformation.get(next));
                        result.put(next, current);
                    }
                    current.addPrevious(row.getInteger("ds_id"));
                }
            });
        }
        return result.values();
    }

    public Dataflow findDataflowById(Long dfId) {
       try (Connection con = sql2o.open()) {
            String sql = "SELECT * FROM dataflow where id=:id;";
            return con.createQuery(sql).addParameter("id", dfId)
                    .executeAndFetchFirst(Dataflow.class);
        }
    }
}
