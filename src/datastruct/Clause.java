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

    public Variable getImpliedVariable(Map<String, Boolean> currentAssignment) {
        // Can only imply if there's only 1 literal without assignments
        if ((literals.size() - currentAssignment.size()) != 1) {
            return null;
        }
        boolean isSatisfied = false;
        Literal unassignedLiteral = null;
        for (Literal l : literals) {
            if (currentAssignment.containsKey(l.getName())) {
                boolean assignment = currentAssignment.get(l.getName());
                if (l.isSatisfied(assignment)) {
                    isSatisfied = true;
                }
            } else {
                unassignedLiteral = l;
            }
        }

        if (unassignedLiteral == null) {
            return null;
        }
        if (!unassignedLiteral.isPositive()) {
            isSatisfied = !isSatisfied;
        }
        return new Variable(unassignedLiteral.getName(), !isSatisfied);
    }

    public Literal getLiteral(String literalName) {
        return literals.stream()
                .filter(l -> l.getName().equals(literalName))
                .findFirst()
                .orElse(null);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        literals.forEach(l -> sb.append(l.getName()).append(", "));
        sb.replace(sb.length() - 2, sb.length(), "");

        return sb.toString();
    }
}
