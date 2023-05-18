package qp.query;

import com.google.common.base.MoreObjects;
import pde.enumeration.ExtractionCartridge;
import pde.enumeration.ExtractionMethod;

/**
 *
 * @author vitor
 */
public class Extractor {
    
    Integer ID;
    String tag;
    ExtractionMethod method;
    ExtractionCartridge cartridge;
    
    public void Extractor(){
        
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(MoreObjects.toStringHelper(this.getClass())
                .add("ID", ID)
                .add("tag", tag)
                .add("method", method)
                .add("cartridge", cartridge)
                .toString());
        return sb.toString();
    }
}
