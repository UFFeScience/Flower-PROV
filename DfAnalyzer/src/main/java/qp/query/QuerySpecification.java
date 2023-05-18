package qp.query;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author vitor
 */
public class QuerySpecification {
    
    String dataflowTag = null;
    Integer dataflowVersion = null;
    //query specification
    AttrMapping.Type mapping = null;
    Collection<String> sources = null;
    Collection<String> targets = null;
    Collection<String> includes = null;
    Collection<String> excludes = null;
    Collection<String> selections = null;
    Collection<String> projections = null;

    public enum OperationType {
        MAPPING,
        SOURCE,
        TARGET,
        INCLUDE,
        EXCLUDE,
        SELECTION,
        PROJECTION
    }
    
    public QuerySpecification(String dataflowTag, Integer dataflowVersion,
            String message){
        this.dataflowTag = dataflowTag;
        this.dataflowVersion = dataflowVersion;
        this.sources = Arrays.asList();
        this.targets = Arrays.asList();
        this.includes = Arrays.asList();
        this.excludes = Arrays.asList();
        this.selections = Arrays.asList();
        this.projections = Arrays.asList();
        this.handleMessage(message);
    }

    public String getDataflowTag() {
        return dataflowTag;
    }

    public Integer getDataflowVersion() {
        return dataflowVersion;
    }

    private void handleMessage(String message) {
        for(String functionStr : message.split("\n")){
            if (!functionStr.isEmpty()) {
                int startFunction = functionStr.indexOf("(");
                int endFunction = functionStr.lastIndexOf(")");

                if (startFunction > 0 && endFunction != -1) {
                    OperationType function = OperationType.valueOf(
                            functionStr.substring(0, startFunction).toUpperCase());
                    String[] arguments = 
                            functionStr.substring(startFunction + 1, endFunction)
                                    .split(";");

                    for (int index = 0; index < arguments.length; index++) {
                        arguments[index] = arguments[index].trim();
                    }

                    switch(function){
                        case MAPPING:
                            this.mapping = AttrMapping.Type.valueOf(arguments[0].toUpperCase());
                            break;
                        case SOURCE:
                            this.sources = Arrays.asList(arguments);
                            break;
                        case TARGET:
                            this.targets = Arrays.asList(arguments);
                            break;
                        case INCLUDE:
                            this.includes = Arrays.asList(arguments);
                            break;
                        case EXCLUDE:
                            this.excludes = Arrays.asList(arguments);
                            break;
                        case PROJECTION:
                            this.projections = Arrays.asList(arguments);
                            break;
                        case SELECTION:
                            this.selections = Arrays.asList(arguments);
                            break;                        
                    }
                }
            }
        }
    }

    public AttrMapping.Type getMapping() {
        return mapping;
    }

    public Collection<String> getSources() {
        return sources;
    }

    public Collection<String> getTargets() {
        return targets;
    }

    public Collection<String> getIncludes() {
        return includes;
    }

    public Collection<String> getExcludes() {
        return excludes;
    }

    public Collection<String> getSelections() {
        return selections;
    }

    public Collection<String> getProjections() {
        return projections;
    }
    
}
