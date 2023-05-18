package qp.dataflow;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import pde.enumeration.ExtractionMethod;
import qp.query.Extractor;
import qp.query.ExtractorCombination;

/**
 *
 * @author tperrotta
 */
public class Dataset extends DataflowObject implements Comparable<Dataset> {

    private String tag;
    private List<Attribute> attributes;
    private List<Extractor> extractors;
    private List<ExtractorCombination> extractorCombinations;

    public Dataset() {
        this.attributes = new ArrayList<>();
        this.extractors = new ArrayList<>();
        this.extractorCombinations = new ArrayList<>();
    }

    public Dataset(String tag) {
        this.tag = tag;
        this.attributes = new ArrayList<>();
    }

    public Dataset(String tag, List<Attribute> attributes) {
        this.tag = tag;
        this.attributes = attributes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tag);
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
        final Dataset other = (Dataset) obj;
        return Objects.equals(this.tag, other.tag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

//        if (!extractors.isEmpty()) {
//            sb.append("\n   Extractors:\n");
//            Joiner.on("\n").appendTo(sb, extractors);
//            sb.append("\n\n");
//        }
//
//        if (!extractorCombinations.isEmpty()) {
//            sb.append("   ExtractorCombinations:\n");
//            Joiner.on("\n").appendTo(sb, extractorCombinations);
//            sb.append("\n\n");
//        }

        sb.append(MoreObjects.toStringHelper(this.getClass())
                .add("tag", tag)
                .toString());
        return sb.toString();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Optional<Attribute> getAttribute(String attributeName) {
        return this.attributes.stream().filter(attr -> attributeName.equals(attr.getName())).findFirst();
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void addAttributes(List<Attribute> attributes) {
        this.attributes.addAll(attributes);
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public int compareTo(Dataset o) {
        return this.tag.compareTo(o.tag);
    }

    public Extractor getExtractor(int extractorID) {
        for (Extractor ext : extractors) {
            if (ext.getID() == extractorID) {
                return ext;
            }
        }
        return null;
    }

    public void addExtractors(ArrayList<Extractor> extractors) {
        this.extractors.addAll(extractors);
    }

    public void addExtractorCombinations(ArrayList<ExtractorCombination> extractorCombinations) {
        this.extractorCombinations.addAll(extractorCombinations);
    }

    public List<Extractor> getIndexingExtractors(){
        return extractors;
    }
    
    public List<ExtractorCombination> getExtractorCombinations(){
        return extractorCombinations;
    }
    
    public boolean hasIndexingExtractor(){
        return extractors.stream().anyMatch((ext) -> (ext.getMethod() == ExtractionMethod.INDEXING));
    }
}
