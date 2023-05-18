package rest.config.entities;

import java.util.HashSet;
import java.util.Set;

public class DataTransformationDependency {
    public Integer id;
    public String name;

    public DataTransformationDependency(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Set<Integer> previousDsIds = new HashSet<>();
    public Set<Integer> nextDsIds = new HashSet<>();

    public void addPrevious(Integer previousDsId) {
        if (previousDsId != null)
            previousDsIds.add(previousDsId);
    }

    public void addNext(Integer nextDtId) {
        if (nextDtId != null)
            nextDsIds.add(nextDtId);
    }
}
