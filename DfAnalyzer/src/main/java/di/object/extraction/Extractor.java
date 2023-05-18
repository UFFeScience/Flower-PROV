package di.object.extraction;

import di.enumeration.extraction.ExtractionExtension;
import di.enumeration.extraction.ExtractionCartridge;
import di.enumeration.dataflow.DataflowType;
import di.enumeration.dataflow.AttributeType;
import di.dataflow.object.DataflowObject;

/**
 *
 * @author vitor
 */
public class Extractor extends DataflowObject {
    
    public String transformationTag = "";
    public String setTag = "";
    public String tag = "";
    public AttributeType type;
    public ExtractionCartridge cartridge = null;
    public ExtractionExtension extension = null;
    
    protected Extractor(DataflowType type) {
        super(type);
    }

    public Extractor() {
        this(DataflowType.EXTRACTOR);
    }

}
