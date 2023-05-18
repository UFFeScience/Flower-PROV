package pde.object.dataflow;

import java.util.ArrayList;
import java.util.List;

import pde.enumeration.SetType;
import pde.object.DataflowObject;
import pde.object.ObjectType;
import pde.object.extraction.Extractor;
import pde.object.extraction.ExtractorCombination;

/**
 *
 * @author vitor
 */
public class Dataset extends DataflowObject {

    public Long id;
    public String transformationTag = "";
    public String tag = "";
    public SetType type = null;
    public String dependency = "";
    public List<Attribute> attributes = new ArrayList<>();
    public List<Extractor> extractors = new ArrayList<>();
    public List<ExtractorCombination> extractorCombinations = new ArrayList<>();

    protected Dataset(ObjectType type) {
        super(type);
    }

    public Dataset() {
        this(ObjectType.DATASET);
    }

    public String getTransformationTag() {
        return transformationTag;
    }

    public void setTransformationTag(String transformationTag) {
        this.transformationTag = transformationTag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public SetType getType() {
        return type;
    }

    public void setType(SetType type) {
        this.type = type;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }

    public Dataset(Dataflow dataflow, String tag, String[] attNames, String[] attTypes) {
        this(ObjectType.DATASET);
        this.setDataflowTag(dataflow.getDataflowTag());
        this.setTag(tag.toLowerCase());
        // adding attributes to dataset
        if (attNames != null && attTypes != null && attNames.length == attTypes.length) {
            for (int index = 0; index < attNames.length; index++) {
                new Attribute(this, attNames[index], attTypes[index]);
            }
        }
        dataflow.addDataset(this);
    }

    public List<Extractor> getExtractors() {
        return extractors;
    }

    public void setExtractors(List<Extractor> extractors) {
        this.extractors = extractors;
    }

    public List<ExtractorCombination> getExtractorCombinations() {
        return extractorCombinations;
    }

    public void setExtractorCombinations(List<ExtractorCombination> extractorCombinations) {
        this.extractorCombinations = extractorCombinations;
    }

    public void addExtractor(Extractor obj) {
        extractors.add(obj);
    }

    public void addExtractorCombination(ExtractorCombination obj) {
        extractorCombinations.add(obj);
    }

    public Extractor getExtractor(String extractorTag) {
        for(Extractor extractor : extractors){
            if(extractor.getTag().equals(extractorTag)){
                return extractor;
            }
        }
        return null;
    }
}
