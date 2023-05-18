package di.object.extraction;

import di.enumeration.dataflow.DataflowType;
import di.enumeration.dataflow.AttributeType;
import di.dataflow.object.DataflowObject;
import utils.Utils;

/**
 *
 * @author vitor
 */
public class ExtractorCombination extends DataflowObject {
    
    public String transformationTag = "";
    public String setTag = "";
    public String outerExtractorTag = "";
    public String innerExtractorTag = "";
    public String[] keys;
    public AttributeType[] keyTypes;
    
    protected ExtractorCombination(DataflowType type) {
        super(type);
    }

    public ExtractorCombination() {
        this(DataflowType.EXTRACTOR);
    }

    public String getKeysAsString() {
        String result = "";
        for(String k : keys){
            if(!result.isEmpty()){
                result += Utils.ELEMENT_SEPARATOR;
            }
            result += k;
        }
        return result;
    }

    public String getKeyTypesAsString() {
        String result = "";
        for(AttributeType k : keyTypes){
            if(!result.isEmpty()){
                result += Utils.ELEMENT_SEPARATOR;
            }
            result += k;
        }
        return result;
    }

}
