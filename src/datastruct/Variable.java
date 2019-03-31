package datastruct;

/**
 * A data structure used for nodes in graph. It stores the assignment of a literal.
 */
public class Variable {
    private String variableName;
    private boolean assignment;

    public Variable(String variableName) {
        this.variableName = variableName;
    }

    public Variable(String variableName, boolean assignment) {
        this.variableName = variableName;
        this.assignment = assignment;
    }

    public String getName() {
        return variableName;
    }

    public boolean getAssignment() {
        return assignment;
    }

    public void setAssignment(boolean assignment) {
        this.assignment = assignment;
    }

    @Override
    public String toString() {
        return variableName + " " + assignment;
    }
}
