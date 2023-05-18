package di.object.dataflow;

import di.enumeration.dataflow.AttributeType;
import di.object.extraction.ExtractorCombination;
import di.object.extraction.Extractor;
import di.enumeration.dataflow.DataflowType;
import di.enumeration.dataflow.SetType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import di.dataflow.object.DataflowObject;

/**
 *
 * @author vitor
 */
public class Set extends DataflowObject {

    public Transformation transformation = null;
    public String tag = "";
    public SetType type = null;
    public Transformation dependencyTransformation = null;
    public ArrayList<Attribute> attributes = new ArrayList<>();
    public HashMap<String, Extractor> extractors = new HashMap<>();
    public ArrayList<ExtractorCombination> extractorCombinations = new ArrayList<>();

//    table schema
    public ArrayList<String> previousTaskColumns = new ArrayList<>();
    public ArrayList<String> nextTaskColumns = new ArrayList<>();

//    management of data propagation
    public HashMap<String, Extractor> propagatedExtractors = new HashMap<>();

    protected Set(DataflowType type) {
        super(type);
    }

    public Set() {
        this(DataflowType.SET);
    }

    public void addAttribute(Attribute att) {
        attributes.add(att);
    }

    public Collection<Attribute> getAttributes() {
        return attributes;
    }

    public void addExtractor(Extractor ext) {
        extractors.put(ext.tag, ext);
    }

    public Collection<Extractor> getExtractors() {
        return extractors.values();
    }

    public Extractor getExtractor(String tag) {
        return extractors.get(tag);
    }

    public void addExtractorCombination(ExtractorCombination j) {
        extractorCombinations.add(j);
    }

    public ArrayList<ExtractorCombination> getExtractorCombinations() {
        return extractorCombinations;
    }

    public void defineTaskColumns() {
        if (type == SetType.OUTPUT && !previousTaskColumns.contains(transformation.tag.toLowerCase())) {
            previousTaskColumns.add(transformation.tag.toLowerCase());
        } else {
            if (dependencyTransformation != null && !previousTaskColumns.contains(dependencyTransformation.tag.toLowerCase())) {
                previousTaskColumns.add(dependencyTransformation.tag.toLowerCase());
            }

            if (!nextTaskColumns.contains(transformation.tag.toLowerCase())) {
                nextTaskColumns.add(transformation.tag.toLowerCase());
            }
        }
    }

    public void updateTaskColumns(Set s) {
        if (s.type == SetType.OUTPUT) {
            if (previousTaskColumns.indexOf(s.transformation.tag) == -1 
                    && !previousTaskColumns.contains(transformation.tag.toLowerCase())) {
                previousTaskColumns.add(s.transformation.tag.toLowerCase());
            }
        } else {
            if (s.dependencyTransformation != null
                    && !previousTaskColumns.contains(s.dependencyTransformation.tag.toLowerCase())) {
                previousTaskColumns.add(s.dependencyTransformation.tag.toLowerCase());
            }
            if (!nextTaskColumns.contains(transformation.tag.toLowerCase())) {
                nextTaskColumns.add(s.transformation.tag.toLowerCase());
            }
        }
    }

    public boolean hasAttribute(String name) {
        for(Attribute a : attributes){
            if(a.name.equals(name)){
                return true;
            }
        }
        return false;
    }

    public AttributeType getAttributeType(String attributeName) {
        for(Attribute att : attributes){
            if(att.name.toUpperCase().equals(attributeName.toUpperCase())){
                return att.type;
            }
        }
        return null;
    }

    public Attribute getAttribute(String attributeName) {
        for(Attribute att : attributes){
            if(att.name.toUpperCase().equals(attributeName.toUpperCase())){
                return att;
            }
        }
        return null;
    }

    public String getTransformationTag() {
        return transformation.tag;
    }

}
