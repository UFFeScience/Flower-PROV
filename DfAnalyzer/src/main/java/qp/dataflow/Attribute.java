package qp.dataflow;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import pde.enumeration.AttributeType;
import qp.query.Extractor;

/**
 *
 * @author tperrotta
 */
public class Attribute extends DataflowObject implements Comparable<Attribute> {

    private String name;
    private AttributeType attributeType;
    private Extractor extractor;

    public Attribute() {

    }

    public Attribute(String name, AttributeType attributeType) {
        this.name = name;
        this.attributeType = attributeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.attributeType);
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
        final Attribute other = (Attribute) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.attributeType, other.attributeType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("name", name)
                .add("type", attributeType)
                .toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = AttributeType.valueOf(attributeType.toUpperCase());
    }

    @Override
    public int compareTo(Attribute o) {
        int nameCompareTo = this.name.compareTo(o.name);

        if (nameCompareTo == 0) {
            return this.attributeType.compareTo(o.attributeType);
        }

        return nameCompareTo;
    }

    public Extractor getExtractor() {
        return extractor;
    }

    public void setExtractor(Extractor extractor) {
        this.extractor = extractor;
    }
    
}
