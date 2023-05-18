package rest.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pde.object.dataflow.Attribute;
import pde.object.dataflow.Dataset;
import rest.config.Dao;
import rest.config.entities.DataTransformationDependency;
import rest.config.entities.Dataflow;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thaylon Guedes
 * @email thaylongs@gmail.com
 */
@Controller
@RequestMapping("/api")
public class VisualizationAPI {

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private Dao dao;

    @ResponseBody
    @RequestMapping(value = "/dataflows/list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Dataflow> getAllDataflows() {
        return dao.getAllDataflows();
    }

    @ResponseBody
    @RequestMapping("/dataflows/{dfId}")
    public ObjectNode getInfoDataflow(@PathVariable Long dfId) {
        ObjectNode result = mapper.createObjectNode();

        ObjectNode graphResult = result.putObject("graph");
        ArrayNode nodesList = graphResult.putArray("nodes");
        ArrayNode edgesList = graphResult.putArray("edges");

        ObjectNode allAttrsMap = result.putObject("allAttrsMap");
        List<Dataset> allDataSet = dao.getAllDataSetofDataflow(dfId);
        for (Dataset dataset : allDataSet) {
            ObjectNode node = nodesList.addObject();
            node.put("id", dataset.id)
                    .put("label", dataset.tag)
                    .put("shape", "box")
                    .put("title", "("
                            + dataset.attributes.stream().map(Attribute::getName)
                            .collect(Collectors.joining(",")) + ")");
            if (dataset.tag.startsWith("i")) {
                node.put("color", "#7BE141");
            }

            ArrayNode attsOfDataSet = allAttrsMap
                    .putObject(dataset.id.toString())
                    .put("name", dataset.tag)
                    .putArray("atts");
            for (Attribute attribute : dataset.attributes) {
                attsOfDataSet.addObject()
                        .put("name", attribute.name)
                        .put("type", attribute.type.name());
            }

        }

        Collection<DataTransformationDependency> dataTransformationaDependencies = dao.getAllDataDependencyOfDataflow(dfId);
        for (DataTransformationDependency dtDependency : dataTransformationaDependencies) {
            for (Integer previousDsId : dtDependency.previousDsIds) {
                for (Integer nextDsId : dtDependency.nextDsIds) {
                    edgesList.addObject()
                            .put("from", previousDsId)
                            .put("to", nextDsId)
                            .put("arrows", "to")
                            .put("label", dtDependency.name)
                            .putObject("color")
                            .put("color", "black");
                }
            }
        }
        return result;
    }

}
