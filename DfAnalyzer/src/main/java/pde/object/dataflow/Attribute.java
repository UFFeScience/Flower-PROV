package pde.object.dataflow;

import pde.enumeration.AttributeType;
import pde.object.DataflowObject;
import pde.object.extraction.Extractor;
import pde.object.ObjectType;

/**
 *
 * @author vitor
 */
public class Attribute extends DataflowObject {

    public String transformationTag = "";
    public String setTag = "";
    public String extractorTag;
    public String name = "";
    public AttributeType type;

    protected Attribute(ObjectType type) {
        super(type);
    }

    public Attribute() {
        this(ObjectType.ATTRIBUTE);
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

    public String getExtractorTag() {
        return extractorTag;
    }

    public void setExtractorTag(String extractorTag) {
        this.extractorTag = extractorTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }
    
    public Attribute(Dataset ds, String name, String type, Extractor ext) {
        this(ObjectType.ATTRIBUTE);
        this.setDataflowTag(ds.getDataflowTag());
        this.setTransformationTag(ds.getTransformationTag());
        this.setSetTag(ds.getTag());
        this.setName(name.toLowerCase());
        this.setType(AttributeType.valueOf(type.toUpperCase()));
        if (ext != null) {
            this.setExtractorTag(ext.getTag());
        }
        ds.addAttribute(this);
    }
    
    public Attribute(Dataset ds, String attName, String attType) {
        this(ObjectType.ATTRIBUTE);
        this.setDataflowTag(ds.getDataflowTag());
        this.setSetTag(ds.getTag());
        this.setName(attName.toLowerCase());
        this.setType(AttributeType.valueOf(attType.toUpperCase()));
        ds.addAttribute(this);
    }

}
