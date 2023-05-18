package pde.object.extraction;

import pde.object.dataflow.Dataset;
import pde.object.DataflowObject;
import pde.object.ObjectType;

/**
 *
 * @author vitor
 */
public class ExtractorCombination extends DataflowObject {
    
    public String transformationTag = "";
    public String setTag = "";
    public Extractor outerExtractor;
    public Extractor innerExtractor;
    public String[] keys;
    public String[] keyTypes;
    
    protected ExtractorCombination(ObjectType type) {
        super(type);
    }

    public ExtractorCombination() {
        this(ObjectType.EXTRACTOR_COMBINATION);
    }

    public String getTransformationTag() {
        return transformationTag;
    }

    public void setTransformationTag(String transformationTag) {
        this.transformationTag = transformationTag;
    }

    public String getSetTag() {
        return setTag;
    }

    public void setSetTag(String setTag) {
        this.setTag = setTag;
    }

    public Extractor getOuterExtractor() {
        return outerExtractor;
    }

    public void setOuterExtractor(Extractor outerExtractor) {
        this.outerExtractor = outerExtractor;
    }

    public Extractor getInnerExtractor() {
        return innerExtractor;
    }

    public void setInnerExtractor(Extractor innerExtractor) {
        this.innerExtractor = innerExtractor;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public String[] getKeyTypes() {
        return keyTypes;
    }

    public void setKeyTypes(String[] keyTypes) {
        this.keyTypes = keyTypes;
    }
    
    public ExtractorCombination(Dataset dataset, Extractor outer, Extractor inner, String[] keys, String[] keyTypes) {
        this(ObjectType.EXTRACTOR_COMBINATION);
        this.setDataflowTag(dataset.getDataflowTag());
        this.setSetTag(dataset.getTag());
        this.setOuterExtractor(outer);
        this.setInnerExtractor(inner);
        this.setKeys(keys);
        this.setKeyTypes(keyTypes);
        dataset.addExtractorCombination(this);
    }
    
}
