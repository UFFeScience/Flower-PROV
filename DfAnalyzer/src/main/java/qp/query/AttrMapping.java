package qp.query;

import com.google.common.base.MoreObjects;
import qp.dataflow.Attribute;
import qp.dataflow.Dataset;
import qp.dataflow.Transformation;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import pde.enumeration.AttributeType;

/**
 *
 * @author tperrotta
 */
public class AttrMapping {

    public enum Type {
        PHYSICAL,
        LOGICAL,
        HYBRID,
    }

    private final Dataset prevDataset;
    private final Dataset nextDataset;
    private final Transformation transformation;
    private final Set<Attribute> attributes;
    private final Type type;

    AttrMapping(Dataset prevDataset, Dataset nextDataset) {
        this.prevDataset = prevDataset;
        this.nextDataset = nextDataset;
        this.type = Type.LOGICAL;
        this.transformation = null;

        this.attributes = new LinkedHashSet<>();
        findCommonAttributes();
    }

    AttrMapping(Dataset prevDataset, Dataset nextDataset, Type type, Transformation transformation) {
        this.prevDataset = prevDataset;
        this.nextDataset = nextDataset;
        this.type = type;
        this.transformation = transformation;

        this.attributes = new LinkedHashSet<>();
        findCommonAttributes();
    }

    private void findCommonAttributes() {
        if (type == Type.PHYSICAL || type == Type.HYBRID) {
            this.attributes.add(new Attribute(transformation.getTag() + "_task_id", AttributeType.NUMERIC));
        }

        if (type == Type.LOGICAL || type == Type.HYBRID) {
            prevDataset.getAttributes().forEach(prevDsAttr -> {
                nextDataset.getAttributes().forEach(nextDsAttr -> {
                    if (prevDsAttr.equals(nextDsAttr)) {
                        this.attributes.add(prevDsAttr);
                    }
                });
            });
        }
    }

    Collection<String> toSqlClauseElements() {
        Set<String> elements = new LinkedHashSet<>();

        if (attributes != null) {
            attributes.forEach(attr -> {
                elements.add((prevDataset.getTag() + "." + attr.getName()));
                elements.add((nextDataset.getTag() + "." + attr.getName()));
            });
        }

        return elements;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("prevDataset", prevDataset)
                .add("nextDataset", nextDataset)
                .add("transformation", transformation)
                .add("attributes", attributes)
                .add("type", type)
                .toString();
    }

}
