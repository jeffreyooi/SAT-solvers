package datastruct;

public class Node {
    private Variable variable;
    private int decisionLevel;

    Node(Variable variable, int decisionLevel) {
        this.variable = variable;
        this.decisionLevel = decisionLevel;
    }

    Variable getVariable() {
        return variable;
    }

    public int getDecisionLevel() {
        return decisionLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) {
            return false;
        }

        Node other = (Node) obj;
        return this.variable.equals(other.variable) && this.decisionLevel == other.decisionLevel;
    }

    @Override
    public String toString() {
        return String.format("Decision level: %d Variable: %s", decisionLevel, variable.toString());
    }
}
