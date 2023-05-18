package qp.query;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tperrotta
 */
public class Expression {

    public enum Type {
        AND,
        OR,
        NOT,
        NONE,
    }

    private final Type type;
    
    // Only used if Type is NONE.
    private final String value;
    
    // Only used if Type is not NONE.
    private final List<Expression> operands;

    public Expression(String value) {
        this.type = Type.NONE;
        this.value = value;
        this.operands = new ArrayList<>();
    }

    public Expression(Type type) {
        this.type = type;
        this.value = null;
        this.operands = new ArrayList<>();
    }

    public Expression(Type type, List<Expression> operands) {
        this.type = type;
        this.value = null;
        this.operands = operands;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public List<Expression> getOperands() {
        return operands;
    }

    @Override
    public String toString() {
        switch (type) {
            case NONE:
                return "(" + value + ")";
            case NOT:
                return "(" + "NOT" + " " + value + ")";
            default:
                return "(" + Joiner.on(" " + String.valueOf(type).toUpperCase() + " ").join(operands) + ")";
        }
    }

}
