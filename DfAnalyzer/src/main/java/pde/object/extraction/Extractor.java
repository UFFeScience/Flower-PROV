package pde.object.extraction;

import java.util.ArrayList;
import pde.object.dataflow.Dataset;
import pde.enumeration.ExtractionMethod;
import pde.enumeration.ExtractionCartridge;
import pde.object.DataflowObject;
import pde.object.ObjectType;
import pde.object.dataflow.Attribute;

/**
 *
 * @author vitor
 */
public class Extractor extends DataflowObject {
    
    public String transformationTag = "";
    public String datasetTag = "";
    public String tag;
    public ExtractionMethod method;
    public ExtractionCartridge cartridge;
    public ArrayList<Attribute> attributes = new ArrayList<>();
    
    protected Extractor(ObjectType type) {
        super(type);
    }

    public Extractor() {
        this(ObjectType.EXTRACTOR);
    }

    public String getTransformationTag() {
        return transformationTag;
    }

    public void setTransformationTag(String transformationTag) {
        this.transformationTag = transformationTag;
    }

    public String getDatasetTag() {
        return datasetTag;
    }

    public void setDatasetTag(String setTag) {
        this.datasetTag = setTag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ExtractionMethod getMethod() {
        return method;
    }

    public void setMethod(ExtractionMethod method) {
        this.method = method;
    }

    public ExtractionCartridge getCartridge() {
        return cartridge;
    }

    public void setCartridge(ExtractionCartridge cartridge) {
        this.cartridge = cartridge;
    }
    
    public Extractor(Dataset dataset, String tag, ExtractionMethod method, ExtractionCartridge cartridge, String[] attNames, String[] attTypes) {
        this(ObjectType.EXTRACTOR);
        this.setDataflowTag(dataset.getDataflowTag());
        this.setDatasetTag(dataset.getTag());
        this.setTag(tag.toLowerCase());
        this.setMethod(method);
        this.setCartridge(cartridge);
        if(attNames != null && attTypes != null && attNames.length == attTypes.length){
            for(int index=0; index < attNames.length; index++){
                new Attribute(dataset, attNames[index], attTypes[index], this);
            }
        }
        dataset.addExtractor(this);
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }
    
}
