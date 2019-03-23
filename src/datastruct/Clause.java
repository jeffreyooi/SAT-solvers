package datastruct;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A data structure that contains literals joined by OR.
 */
public class Clause implements Comparable<Clause> {
    private Set<Literal> literals;

    public Clause() {
        literals = new HashSet<>();
    }

    public Set<Literal> getLiterals() {
        return literals;
    }

    public void addLiteral(Literal literal) {
        literals.add(literal);
    }

    public void addLiterals(Set<Literal> literals) {
        this.literals.addAll(literals);
    }

    public void removeLiteral(Literal literal) {
        literals.remove(literal);
    }

    public boolean isSatisfied(Map<String, Boolean> assignments) {
        // Since it is in CNF form, as long as any one of the literal is true, the clause is true
        for (Literal l : literals) {
            Boolean assignment = assignments.get(l.getName());
            if (assignment != null && l.isSatisfied(assignment)) {
                return true;
            }
        }
        return false;
    }

    public Literal getLiteral(String literalName) {
        for (Literal l : literals) {
            if (l.getName().equals(literalName)) {
                return l;
            }
        }
        return null;
    }

    public int getNumberOfLiterals() {
        return literals.size();
    }

    /**
     * Used to sort clauses by number of literals
     * @param other
     * @return
     */
    public int compareTo(Clause other) {
        return literals.size() - other.literals.size();
    }
}
