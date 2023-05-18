package qp.dataflow;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 *
 * @author tperrotta
 */
public class Extractor extends DataflowObject {

    public enum ExtractionCartridge {
        EXTRACTION,
        INDEXING,
    }

    public enum ExtractionExtension {
        PROGRAM,
        CSV,
        FITS,
        FASTBIT,
        OPTIMIZED_FASTBIT,
    }

    public String tag;
    public ExtractionCartridge cartridge;
    public ExtractionExtension extension;

    public Extractor(String tag) {
        this.tag = tag;
    }

    public Extractor(String tag, ExtractionCartridge cartridge, ExtractionExtension extension) {
        this.tag = tag;
        this.cartridge = cartridge;
        this.extension = extension;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tag, this.cartridge, this.extension);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Extractor other = (Extractor) obj;
        if (!Objects.equals(this.tag, other.tag)) {
            return false;
        }
        if (this.cartridge != other.cartridge) {
            return false;
        }
        if (this.extension != other.extension) {
            return false;
        }
        return true;
    }
    
    

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("tag", this.tag)
                .add("cartridge", this.cartridge)
                .add("extension", this.extension)
                .toString();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ExtractionCartridge getCartridge() {
        return cartridge;
    }

    public void setCartridge(ExtractionCartridge cartridge) {
        this.cartridge = cartridge;
    }

    public void setCartridge(String cartridge) {
        this.cartridge = ExtractionCartridge.valueOf(cartridge.toUpperCase());
    }

    public ExtractionExtension getExtension() {
        return extension;
    }

    public void setExtension(ExtractionExtension extension) {
        this.extension = extension;
    }

    public void setExtension(String extension) {
        this.extension = ExtractionExtension.valueOf(extension.toUpperCase());
    }
}
