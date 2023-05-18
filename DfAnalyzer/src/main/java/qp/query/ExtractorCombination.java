package qp.query;

import qp.dataflow.Dataset;

/**
 *
 * @author vitor
 */
public class ExtractorCombination {
    
    Integer ID;
    Dataset dataset;
    Extractor innerExtractor;
    Extractor outerExtractor;
    String[] keys;
    String[] keyTypes;
    
    public ExtractorCombination(){
        
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Extractor getInnerExtractor() {
        return innerExtractor;
    }

    public void setInnerExtractor(Extractor innerExtractor) {
        this.innerExtractor = innerExtractor;
    }

    public Extractor getOuterExtractor() {
        return outerExtractor;
    }

    public void setOuterExtractor(Extractor outerExtractor) {
        this.outerExtractor = outerExtractor;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys.replaceAll("\\{", "").replaceAll("\\}", "").split(";");
    }

    public String[] getKeyTypes() {
        return keyTypes;
    }

    public void setKeyTypes(String keyTypes) {
        this.keyTypes = keyTypes.replaceAll("\\{", "").replaceAll("\\}", "").split(";");
    }
    
    
    
}
