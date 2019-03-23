package datastruct;

/**
 * A data structure that represents a literal in CNF. It can represent a positive or negative literal denoted by a
 * boolean.
 */
public class Literal {
    private boolean isPositive;
    private String name;

    public Literal(String name, boolean isPositive) {
        this.name = name;
        this.isPositive = isPositive;
    }

    public String getName() {
        return name;
    }

    public boolean isPositive() {
        return isPositive;
    }

    /**
     * Check if the assignment is satisfied. To check, verify that the assignment is the same as the sign.
     * @param assignment assignment of the literal
     * @return true if assignment makes literal true, false otherwise
     */
    public boolean isSatisfied(boolean assignment) {
        return isPositive == assignment;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Literal)) {
            return false;
        }
        Literal other = (Literal) obj;
        return this.name.equals(other.name) && this.isPositive == other.isPositive;
    }
}
